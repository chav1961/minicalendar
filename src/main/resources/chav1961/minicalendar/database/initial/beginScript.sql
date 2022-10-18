create or replace function minical.to_number_selected(testVal in text, aliases in text, aliasesLength in integer) returns integer as
$$
	declare
		firstChar text;
		loopVar integer;
		returned integer := -1;
	begin
		firstChar := substring(testVal, 1, 1);
		if firstChar >= '0' and firstChar <= '9' then
			returned := to_number(testVal, '999999999');
		else 
			for loopVar in 1..aliasesLength loop
				if split_part(aliases, ',' , loopVar) = testVal then
					returned := loopVar;
					exit;
				end if;
			end loop;
		end if;
		return returned;
	end;
$$ language plpgsql;


create or replace function minical.in_cron_clause(testVal in integer, cron in text, aliases in text, aliasesLength in integer) returns integer as
$$
	declare
		returned integer := 0;
		part text;
		pos integer := 0;
		slash integer;
		hyphen integer;
		fromRange integer;
		toRange integer;
		step integer;
		loopVar integer;
	begin
		loop
			pos := position(',' in cron);
			if pos > 0 then
				part := substring(cron, 1, pos-1);
				cron := substring(cron, pos+1);
			else 
				part := cron;
				cron := '';
			end if;
			
			if part = '*' then
				returned := 1;
			else
				slash := position('/' in part);
				if slash > 0 then
					step := to_number(substring(part, slash+1), '99999999999999');
					part := substring(part, 1, slash-1);
				else 
					step := 1;
				end if;
				hyphen := position('-' in part);
				if hyphen > 0 then
					fromRange := to_number_selected(substring(part, 1, hyphen-1), aliases, aliasesLength);
					toRange := to_number_selected(substring(part, hyphen+1), aliases, aliasesLength);
				else
					fromRange := to_number_selected(part, aliases, aliasesLength);
					toRange := fromRange;
				end if;
				for loopVar in fromRange..toRange by step loop
					if testVal = loopVar then
						returned := 1;
						exit;
					end if;
				end loop;
			end if;
			
			exit when pos <= 0 or returned = 1;
		end loop;
		return returned;
	end;
$$ language plpgsql;

create or replace function minical.in_cron_clause(testVal in integer, cron in text) returns integer as
$$
	begin
		return in_cron_clause(testVal, cron, '', 1);
	end;
$$ language plpgsql;

create or replace function minical.in_cron_string(currentTime in timestamp, cron in text) returns integer as
$$
	declare	
		currentPart text;
		currentVal integer;
		val integer := 0;
	begin
		currentPart = split_part(cron, ' ', 1);
		currentVal := extract(minute from currentTime);
		val := val + in_cron_clause(currentVal, currentPart);
		currentPart = split_part(cron, ' ', 2);
		currentVal := extract(hour from currentTime);
		val := val + in_cron_clause(currentVal, currentPart);
		currentPart = split_part(cron, ' ', 3);
		currentVal := extract(day from currentTime);
		val := val + in_cron_clause(currentVal, currentPart);
		currentPart = split_part(cron, ' ', 4);
		currentVal := extract(month from currentTime);
		val := val + in_cron_clause(currentVal, currentPart);
		currentPart = split_part(cron, ' ', 5);
		currentVal := extract(dow from currentTime);
		val := val + in_cron_clause(currentVal, currentPart);
		if val = 5 then
			return 1;
		else 
			return 0;
		end if;
	end;
$$ language plpgsql;


create or replace view minical.currentEvents(eventType, eventId, userId) as 
select 1 as eventType, "ev_Id" as eventId, "us_Id" as userId from minical.events where now() between "ev_StartFrom" and coalesce("ev_ExpectedTo",now()) and public.in_cron_string(now()::timestamp, "ev_CronMask") = 1;

create or replace view minical.awaitedEvents(eventType, eventId, userId) as 
select 2 as eventType, "ev_Id" as eventId, "us_Id" as userId from minical.events where now()+"ev_NotifyBefore"::interval between "ev_StartFrom" and coalesce("ev_ExpectedTo",now()+"ev_NotifyBefore"::interval) and public.in_cron_string((now()+"ev_NotifyBefore"::interval)::timestamp, "ev_CronMask") = 1;

create or replace view minical.overdueEvents(eventType, eventId, userId) as 
select 3 as eventType, "ev_Id" as eventId, "us_Id" as userId from minical.events where now()-"ev_NotifyAfter"::interval between "ev_StartFrom" and coalesce("ev_ExpectedTo",now()-"ev_NotifyAfter"::interval) and public.in_cron_string((now()-"ev_NotifyAfter"::interval)::timestamp, "ev_CronMask") = 1;

create or replace view minical.totalEvents(eventType, eventId, userId) as 
select eventtype, eventid, userId from minical.awaitedevents
union all
select eventtype, eventid, userId from minical.currentevents
union all
select eventtype, eventid, userId from minical.overdueevents;



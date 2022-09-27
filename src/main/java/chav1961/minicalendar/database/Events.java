package chav1961.minicalendar.database;

import java.sql.Timestamp;

public class Events {
	public long					ev_Id;
	public Users				us_Id;
	public Timestamp			ev_Created;
	public String				ev_CronMask;
	public NotificationTypes	nt_Id;
	public Timestamp			ev_NotifyBefore;
	public Timestamp			ev_NotifyAfter;
	public Timestamp			ev_StartFrom;
	public Timestamp			ev_ExpectedTo;
	public String				ev_EventType;
	public String				ev_Comment;
	
	
	@Override
	public String toString() {
		return "Events [ev_Id=" + ev_Id + ", us_Id=" + us_Id + ", ev_Created=" + ev_Created + ", ev_CronMask="
				+ ev_CronMask + ", nt_Id=" + nt_Id + ", ev_NotifyBefore=" + ev_NotifyBefore + ", ev_NotifyAfter="
				+ ev_NotifyAfter + ", ev_StartFrom=" + ev_StartFrom + ", ev_ExpectedTo=" + ev_ExpectedTo
				+ ", ev_EventType=" + ev_EventType + ", ev_Comment=" + ev_Comment + "]";
	}
}

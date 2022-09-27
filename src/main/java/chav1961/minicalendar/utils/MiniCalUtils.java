package chav1961.minicalendar.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.minicalendar.interfaces.TestCronInterface;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.ReflectedMapWrapper;

public class MiniCalUtils {
	// % % % % %  (*, N,N N-N) 
	public static TestCronInterface buildTestCronInterface(final String cronLine) throws SyntaxException {
		if (cronLine == null || cronLine.isEmpty()) {
			throw new IllegalArgumentException("Cron line can't be null or empty"); 
		}
		else {
			final char[]	content = CharUtils.terminateAndConvert2CharArray(cronLine, '\n');
			final long[]	result = new long[5]; 
			int				from = 0;
			
			from	= parse(content, from, 0, 59, result, 0);
			from	= parse(content, from, 0, 23, result, 1);
			from	= parse(content, from, 1, 31, result, 2);
			from	= parse(content, from, 1, 12, result, 3);
			from	= parse(content, from, 0, 6, result, 4);
			return new CronBitmask(result[0], (int)result[1], (int)result[2], (int)result[3], (int)result[4]);
		}
	}
	
	public static void createInstance(final ResultSet rs, final ReflectedMapWrapper wrapper) throws SQLException{
		final Set<String> fields = getFieldsSupported(rs);
		
		rs.moveToInsertRow();
		for (Entry<String, Object> item : wrapper.entrySet()) {
			if (fields.contains(item.getKey())) {
				rs.updateObject(item.getKey(), item.getValue());
			}
		}
		rs.insertRow();
		rs.moveToCurrentRow();
	}

	public static void readInstance(final ResultSet rs, final ReflectedMapWrapper wrapper) throws SQLException{
		final Set<String> fields = getFieldsSupported(rs);
		
		rs.refreshRow();
		for (Entry<String, Object> item : wrapper.entrySet()) {
			if (fields.contains(item.getKey())) {
				item.setValue(rs.getObject(item.getKey()));
			}
		}
	}
	
	public static void updateInstance(final ResultSet rs, final ReflectedMapWrapper wrapper) throws SQLException{
		final Set<String> fields = getFieldsSupported(rs);
		
		for (Entry<String, Object> item : wrapper.entrySet()) {
			if (fields.contains(item.getKey())) {
				rs.updateObject(item.getKey(), item.getValue());
			}
		}
		rs.updateRow();
	}

	private static Set<String> getFieldsSupported(final ResultSet rs) throws SQLException {
		final ResultSetMetaData	rsmd = rs.getMetaData();
		final Set<String>		result = new HashSet<>();
		
		for(int index = 1; index <= rsmd.getColumnCount(); index++) {
			result.add(rsmd.getColumnName(index));
		}
		return result;
	}
	
	private static int parse(final char[] content, int from, final int min, final int max, final long[] result, final int where) throws SyntaxException {
		from = CharUtils.skipBlank(content, from, true);

		result[where] = 0;
		if (content[from] == '*') {
			result[where] |= buildMask(min, max);
			return from;
		}
		else if (Character.isDigit(content[from])) {
			final int[]		temp = new int[1];
			int				start, end;
			
			from--;
			do {
				from = CharUtils.parseInt(content, from + 1, temp, false);
				if (temp[0] < min || temp[0] > max) {
					throw new SyntaxException(0, from, "Integer ["+temp[0]+"] out of range "+min+".."+max);
				}
				else {
					start = temp[0];
					if (content[from] == '-') {
						from = CharUtils.parseInt(content, from + 1, temp, false);
						if (temp[0] < min || temp[0] > max) {
							throw new SyntaxException(0, from, "Integer ["+temp[0]+"] out of range "+min+".."+max);
						}
						else {
							end = temp[0];
						}
					}
					else {
						end = start;
					}
					if (end < start) {
						throw new SyntaxException(0, from, "Start range ["+start+"] is greater than end range ["+end+"]");
					}
					else {
						result[where] |= buildMask(start, end);
					}
				}
			} while (content[from] == ',');
			
			return from;
		}
		else {
			throw new SyntaxException(0, from, "Neither asterisk nor digit was detected");
		}
	}

	private static long buildMask(final int min, final int max) {
		long	result = 0;
		
		for(int index = 0; index < 63; index++, result <<= 1) {
			result |= index >= min && index <= max ? 1 : 0;
		}
		return result;
	}

	private static class CronBitmask implements TestCronInterface {
		private final long	minutes;
		private final int	hours;
		private final int	days;
		private final int	months;
		private final int	dows;
		
		public CronBitmask(final long minutes, final int hours, final int days, final int months, final int dows) {
			this.minutes = minutes;
			this.hours = hours;
			this.days = days;
			this.months = months;
			this.dows = dows;
		}

		@Override
		public boolean test(final Calendar date) {
			if (date == null) {
				throw new NullPointerException("Date can't be null");
			}
			else {
				return (((1L << date.get(Calendar.MINUTE)) & minutes) != 0 && ((1L << date.get(Calendar.HOUR_OF_DAY)) & hours) != 0
						&& ((1L << date.get(Calendar.DAY_OF_MONTH)) & days) != 0  && ((1L << date.get(Calendar.MONTH)) & months) != 0
						 && ((1L << date.get(Calendar.DAY_OF_WEEK)) & dows) != 0);
			}
		}
	}
}

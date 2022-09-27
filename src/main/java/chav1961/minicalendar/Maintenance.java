package chav1961.minicalendar;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import chav1961.minicalendar.interfaces.TestCronInterface;
import chav1961.minicalendar.utils.MiniCalUtils;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.JDBCUtils;

public class Maintenance extends TimerTask {
	private final SubstitutableProperties	props;
	private final ContentNodeMetadata		root;
	private final Map<String, UserCache>	cache = new HashMap<>(); 
	
	public Maintenance(final SubstitutableProperties props, final ContentNodeMetadata root) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root model can't be null");
		}
		else {
			this.props = props;
			this.root = root;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try(final SimpleURLClassLoader	loader = new SimpleURLClassLoader(new URL[0])) {
			final Driver				driver = JDBCUtils.loadJdbcDriver(loader,  props.getProperty(Application.PROP_JDBC_DRIVER, File.class));

			try(final Connection		conn = JDBCUtils.getConnection(driver, props.getProperty(Application.PROP_JDBC_CONN_STRING, URI.class)
																			, props.getProperty(Application.PROP_JDBC_USER, String.class)
																			, props.getProperty(Application.PROP_JDBC_PASSWORD, char[].class))) {
				
				try(final Statement		stmt = conn.createStatement();
					final ResultSet		rs = stmt.executeQuery("select us_Id, us_Name, us_LastModified from users")) {
					
					while (rs.next()) {
						refreshUserCache(conn, rs.getLong(1), rs.getString(2), rs.getTimestamp(3));
					}
				}
				
				final List<EventTrigger>	triggers = new ArrayList<>();
				final long					millis = System.currentTimeMillis(); 
				final Calendar				cal = Calendar.getInstance();
				
				cal.setTimeInMillis(millis);
				for (Entry<String, UserCache> item : cache.entrySet()) {
					for (EventListener event : item.getValue().listeners) {
						if (event.testCron.test(cal)) {
							triggers.add(new EventTrigger(EventTrigger.EventTriggerType.EXACT, event.ev_Id));
						}
						else if (event.testCron.test(subtract(cal, event.ev_NotifyBefore))) {
							triggers.add(new EventTrigger(EventTrigger.EventTriggerType.BEFORE, event.ev_Id));
						}
						else if (event.testCron.test(add(cal, event.ev_NotifyAfter))) {
							triggers.add(new EventTrigger(EventTrigger.EventTriggerType.AFTER, event.ev_Id));
						}
					}
				}
				
			} catch (SQLException exc) {
				// TODO Auto-generated catch block
			}
		} catch (IOException | ContentException exc) {
			// TODO Auto-generated catch block
		}
	}

	private Calendar subtract(Calendar cal, Timestamp ev_NotifyBefore) {
		final Calendar	result = Calendar.getInstance();
		
		result.setTimeInMillis(cal.getTimeInMillis() - ev_NotifyBefore.getTime());
		return result;
	}

	private Calendar add(final Calendar cal, final Timestamp ev_NotifyAfter) {
		final Calendar	result = Calendar.getInstance();
		
		result.setTimeInMillis(cal.getTimeInMillis() + ev_NotifyAfter.getTime());
		return result;
	}

	
	private void refreshUserCache(final Connection conn, final long userId, final String userName, final Timestamp lastModified) throws SQLException {
		try(final PreparedStatement	ps = conn.prepareStatement("select * from events where us_Id = ?")) {
			final List<EventListener>	result = new ArrayList<>();
			
			ps.setLong(1, userId);
			
			try(final ResultSet		rs = ps.executeQuery()) {
				while (rs.next()) {
					try{result.add(new EventListener(rs.getLong("ev_Id"), rs.getTimestamp("ev_NotifyBefore"), rs.getTimestamp("ev_NotifyAfter"), MiniCalUtils.buildTestCronInterface(rs.getString("ev_CronString"))));
					} catch (SyntaxException e) {
						throw new SQLException(e);
					}
				}
			}
			cache.put(userName, new UserCache(userName, lastModified, result.toArray(new EventListener[result.size()])));
		}
	}

	private static class EventListener {
		final long					ev_Id;
		final Timestamp				ev_NotifyBefore;
		final Timestamp				ev_NotifyAfter;
		final TestCronInterface		testCron;
		
		public EventListener(final long ev_Id, final Timestamp ev_NotifyBefore, final Timestamp ev_NotifyAfter, final TestCronInterface testCron) {
			this.ev_Id = ev_Id;
			this.ev_NotifyBefore = ev_NotifyBefore;
			this.ev_NotifyAfter = ev_NotifyAfter;
			this.testCron = testCron;
		}
	}
	
	private static class UserCache {
		private final String			user;
		private final Timestamp			lastModified;
		private final EventListener[]	listeners;
		
		public UserCache(final String user, final Timestamp lastModified, final EventListener... listeners) {
			this.user = user;
			this.lastModified = lastModified;
			this.listeners = listeners;
		}
	}
	
	private static class EventTrigger {
		private static enum EventTriggerType {
			BEFORE, EXACT, AFTER
		}
		
		private final EventTriggerType	triggerType;
		private final long				ev_Id;
		
		public EventTrigger(final EventTriggerType triggerType, final long ev_Id) {
			this.triggerType = triggerType;
			this.ev_Id = ev_Id;
		}
	}
}

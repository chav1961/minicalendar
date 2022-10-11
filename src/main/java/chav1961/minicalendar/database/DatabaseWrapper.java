package chav1961.minicalendar.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import chav1961.purelib.sql.interfaces.UniqueIdGenerator;

public class DatabaseWrapper implements AutoCloseable, UniqueIdGenerator {
	private static final String		SQL_UNIQUE_ID = "select nextval('systemseq')";
	private static final String		SQL_NOTIFICATION_TYPES = "select * from notificationtypes";
	private static final String		SQL_NOTIFICATION_TYPE = "select * from notificationtypes where nt_Id = ?";
	private static final String		SQL_USERS = "select us_Id, us_Name from users";
	private static final String		SQL_USER = "select * from Users where us_Id = ?";
	private static final String		SQL_EVENTS = "select eventId, eventType from totalevents where userId = ?";
	private static final String		SQL_EVENT = "select * from events where us_Id = ? and ev_Id = ?";
	private static final String		SQL_ATTACHMENTS = "select at_Id, at_Type from attachments where ev_Id = ?";
	private static final String		SQL_ATTACHMENT = "select * from attachments where ev_Id = ? and at_Id = ?";
	private static final String		SQL_ALERTS = "select al_Id, al_State from alerts where us_Id = ?";
	private static final String		SQL_ALERT = "select * from alerts where us_Id = ? and al_Id = ?";
	
	private final PreparedStatement	psUniqueId; 
	private final PreparedStatement	psNotificationTypes; 
	private final PreparedStatement	psNotificationType; 
	private final PreparedStatement	psUsers; 
	private final PreparedStatement	psUser; 
	private final PreparedStatement	psEvents; 
	private final PreparedStatement	psEvent; 
	private final PreparedStatement	psAttachments; 
	private final PreparedStatement	psAttachment; 
	private final PreparedStatement	psAlerts; 
	private final PreparedStatement	psAlert; 
	
	public DatabaseWrapper(final Connection conn) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else {
			this.psUniqueId = conn.prepareStatement(SQL_UNIQUE_ID);
			this.psNotificationTypes = conn.prepareStatement(SQL_NOTIFICATION_TYPES, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psNotificationType = conn.prepareStatement(SQL_NOTIFICATION_TYPE, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psUsers = conn.prepareStatement(SQL_USERS, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psUser = conn.prepareStatement(SQL_USER, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psEvents = conn.prepareStatement(SQL_EVENTS, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psEvent = conn.prepareStatement(SQL_EVENT, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psAttachments = conn.prepareStatement(SQL_ATTACHMENTS, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psAttachment = conn.prepareStatement(SQL_ATTACHMENT, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psAlerts = conn.prepareStatement(SQL_ALERTS, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			this.psAlert = conn.prepareStatement(SQL_ALERT, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
	}

	@Override
	public long getId() throws SQLException {
		synchronized (psUniqueId) {
			try(final ResultSet	rs = psUniqueId.executeQuery()) {
				if (rs.next()) {
					return rs.getLong(1);
				}
				else {
					throw new SQLException("Unique id error");
				}
			}
		}
	}
	
	@Override
	public void close() throws SQLException {
		final boolean	result = close(psNotificationTypes) & close(psNotificationType) & close(psUsers) & close(psUser)
									& close(psEvents)& close(psEvent) & close(psAttachments) & close(psAttachment)
									& close(psAlerts) & close(psAlert) & close(psUniqueId);
	}
	
	public ResultSet getNotificationTypes() throws SQLException {
		synchronized (psNotificationTypes) {
			return psNotificationTypes.executeQuery();
		}
	}

	public ResultSet getNotificationType(final long id) throws SQLException {
		synchronized (psNotificationType) {
			psNotificationType.setLong(1, id);
			
			return psNotificationType.executeQuery();
		}
	}

	public ResultSet getUsers() throws SQLException {
		synchronized (psUsers) {
			return psUsers.executeQuery();
		}
	}

	public ResultSet getUser(final long id) throws SQLException {
		synchronized (psUser) {
			psUser.setLong(1, id);
			
			return psUser.executeQuery();
		}
	}

	public ResultSet getEvents(final long userId) throws SQLException {
		synchronized (psEvents) {
			psEvents.setLong(1, userId);
			
			return psEvents.executeQuery();
		}
	}

	public ResultSet getEvent(final long userId, final long id) throws SQLException {
		synchronized (psEvent) {
			psEvent.setLong(1, userId);
			psEvent.setLong(2, id);
			
			return psEvent.executeQuery();
		}
	}

	public ResultSet getAttachments(final long eventId) throws SQLException {
		synchronized (psAttachments) {
			psAttachments.setLong(1, eventId);
			
			return psAttachments.executeQuery();
		}
	}

	public ResultSet getAttachment(final long eventId, final long id) throws SQLException {
		synchronized (psAttachment) {
			psAttachment.setLong(1, eventId);
			psAttachment.setLong(2, id);
			
			return psAttachment.executeQuery();
		}
	}

	public ResultSet getAlerts(final long userId) throws SQLException {
		synchronized (psAlerts) {
			psAlerts.setLong(1, userId);
			
			return psAlerts.executeQuery();
		}
	}

	public ResultSet getAlert(final long userId, final long id) throws SQLException {
		synchronized (psAlert) {
			psAlert.setLong(1, userId);
			psAlert.setLong(2, id);
			
			return psAlert.executeQuery();
		}
	}
	
	private boolean close(final PreparedStatement ps) {
		try{
			ps.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

}

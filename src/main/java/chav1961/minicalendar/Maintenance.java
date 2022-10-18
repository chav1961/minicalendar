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

import chav1961.minicalendar.database.DatabaseWrapper;
import chav1961.minicalendar.interfaces.TestCronInterface;
import chav1961.minicalendar.utils.MiniCalUtils;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.ui.swing.useful.JSystemTray;

public class Maintenance extends TimerTask {
	private final SubstitutableProperties	props;
	private final ContentNodeMetadata		root;
	private final JSystemTray				tray;
	private final boolean					dontCreateUsers;
	
	public Maintenance(final SubstitutableProperties props, final ContentNodeMetadata root, final JSystemTray tray, final boolean dontCreateUsers) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root model can't be null");
		}
		else if (tray == null) {
			throw new NullPointerException("System tray can't be null");
		}
		else {
			this.props = props;
			this.root = root;
			this.tray = tray;
			this.dontCreateUsers = dontCreateUsers;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try(final SimpleURLClassLoader	loader = new SimpleURLClassLoader(new URL[0])) {
			final Driver				driver = JDBCUtils.loadJdbcDriver(loader,  props.getProperty(Application.PROP_JDBC_DRIVER, File.class));

			try(final Connection		conn = JDBCUtils.getConnection(driver, props.getProperty(Application.PROP_JDBC_CONN_STRING, URI.class)
																			, props.getProperty(Application.PROP_JDBC_USER, String.class)
																			, props.getProperty(Application.PROP_JDBC_PASSWORD, char[].class));
				final DatabaseWrapper	wrapper = new DatabaseWrapper(conn)) {
				int		eventCount = 0;
				
				conn.setSchema("minical");
				conn.setAutoCommit(false);
				
				try(final Statement		stmt = conn.createStatement();
					final ResultSet		rs = stmt.executeQuery("select eventType, eventId, userId from totalevents, users where users.\"us_Id\" = totalevents.userId");
					final PreparedStatement	ps = conn.prepareStatement("insert into alerts(\"al_Id\",\"ev_Id\",\"al_Created\",\"al_State\") values(?,?,now(),?)")) {
					
					while (rs.next()) {
						ps.setLong(1, wrapper.getId());
						ps.setLong(2, rs.getLong("eventId"));
						ps.setLong(3, rs.getLong("eventType"));
						ps.executeUpdate();
						eventCount++;
					}
				}
				conn.commit();
				if (eventCount > 0) {
					tray.message(Severity.info, "count="+eventCount);
				}
			} catch (SQLException exc) {
				exc.printStackTrace();
				// TODO Auto-generated catch block
			}
		} catch (IOException | ContentException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}
}

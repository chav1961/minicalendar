package chav1961.minicalendar;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.TimerTask;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.sql.JDBCUtils;

public class Maintenance extends TimerTask {
	private final SubstitutableProperties	props;
	
	public Maintenance(final SubstitutableProperties props) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			this.props = props;
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
			} catch (SQLException exc) {
				// TODO Auto-generated catch block
			}
		} catch (IOException | ContentException exc) {
			// TODO Auto-generated catch block
		}
	}

}

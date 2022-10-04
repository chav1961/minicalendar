package chav1961.minicalendar.install.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class PrepareDatabaseActionTest {
	private static final File	driver = new File("./src/main/resources/chav1961/minicalendar/database/postgresql-42.4.1.jar");
	private static final URI	conn = URI.create("jdbc:postgresql://localhost:5432/postgres");
	private static final String	admin = "test"; 
	private static final char[]	adminPassword = "test".toCharArray(); 
	private static final String	user = "test2"; 
	private static final char[]	userPassword = "test2".toCharArray(); 

	@Test
	public void createDatabaseTest() throws EnvironmentException, ContentException, IOException, SQLException {
		final PrepareDatabaseAction	pda = new PrepareDatabaseAction(PureLibSettings.PURELIB_LOCALIZER, driver.toURI().toURL());
		
		pda.execute(PureLibSettings.CURRENT_LOGGER, driver, conn, admin, adminPassword, user, userPassword, "");
	}
}

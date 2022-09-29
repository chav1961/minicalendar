package chav1961.minicalendar.install.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import chav1961.minicalendar.install.InstallUtils;
import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.InstallationError;
import chav1961.minicalendar.install.actions.ActionInterface.State;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;
import chav1961.purelib.sql.model.SQLModelUtils;
import chav1961.purelib.sql.model.SimpleDatabaseManager;
import chav1961.purelib.sql.model.SimpleDatabaseModelManagement;
import chav1961.purelib.sql.model.SimpleDottedVersion;
import chav1961.purelib.sql.model.interfaces.DatabaseManagement;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class PrepareDatabaseAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_ACTION_NAME = "PrepareDatabaseAction.actionname";
	
	private final Localizer				localizer;
	private final URL					jdbcDriverUrl;
	private final ProgressIndicatorImpl	pii;
	
	private State		state = State.UNPREPARED;
	
	public PrepareDatabaseAction(final Localizer localizer, final URL jdbcDriverURL) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (jdbcDriverURL == null) {
			throw new NullPointerException("Error processing can't be null");
		}
		else {
			this.localizer = localizer;
			this.jdbcDriverUrl = jdbcDriverURL;
			this.pii = new ProgressIndicatorImpl(localizer);
		}
	}
	
	@Override
	public String getActionName() {
		return KEY_ACTION_NAME;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public ProgressIndicator getProgressIndicator() {
		return pii;
	}

	@Override
	public Class<?>[] getAncestors() {
		return new Class<?>[0];
	}

	@Override
	public void prepare(final LoggerFacade logger) throws Exception {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			state = State.AWAITING;
		}
	}

	@Override
	public boolean execute(final LoggerFacade logger, InstallationDescriptor content, Object... parameters) throws Exception {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Installation descriptor can't be null");
		}
		else {
			final SimpleDatabaseModelManagement	dbmm = new SimpleDatabaseModelManagement(SimpleDatabaseModelManagement.collectModels(URI.create("root://"+getClass().getCanonicalName()+"/chav1961/minicalendar/database/models.txt")));
			
			try(final SimpleURLClassLoader		scl = new SimpleURLClassLoader(new URL[] {jdbcDriverUrl})) {
				final File						driverFile = content.jdbcSelected ? content.jdbcDriver : InstallUtils.extractDriverFile(jdbcDriverUrl);
				final Driver					driver = JDBCUtils.loadJdbcDriver(scl, driverFile);
				final ConnectionGetter			connGetter = ()-> JDBCUtils.getConnection(driver, 
														URI.create(content.connString), 
														content.admin, 
														content.adminPassword);
				final DatabaseManagement<SimpleDottedVersion>			dbMgmt = new InstallDatabaseManagement();
	
				try(final SimpleDatabaseManager<SimpleDottedVersion>	mgr = new SimpleDatabaseManager<>(logger, dbmm, connGetter, (c)->dbMgmt);
					final Connection			conn = mgr.getConnection()) {
					
					SQLModelUtils.createSchemaOwnerByModel(conn, mgr.getCurrentDatabaseModel(), content.user, content.user, content.userPassword);
					SQLModelUtils.createDatabaseByModel(conn, mgr.getCurrentDatabaseModel(), content.user);
				}
				return true;
			} catch (SQLException exc) {
				logger.message(Severity.error, exc, exc.getLocalizedMessage());
				return false;
			} catch (IOException | ContentException exc) {
				logger.message(Severity.error, exc, exc.getLocalizedMessage());
				return false;
			}
		}
	}

	@Override
	public void markAsFailed() {
		state = State.FAILED;
	}

	@Override
	public void unprepare(final LoggerFacade logger) throws Exception {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			state = State.UNPREPARED;
		}
	}
	
	private static class InstallDatabaseManagement implements DatabaseManagement<SimpleDottedVersion> {
		@Override public void onOpen(final Connection conn, final ContentNodeMetadata model) throws SQLException {}
		@Override public void onClose(final Connection conn, final ContentNodeMetadata model) throws SQLException {}
		@Override public void onDowngrade(final Connection conn, final SimpleDottedVersion version, final ContentNodeMetadata model, final SimpleDottedVersion oldVersion, final ContentNodeMetadata oldModel) throws SQLException {}

		@Override
		public SimpleDottedVersion getInitialVersion() throws SQLException {
			return SimpleDottedVersion.INITIAL_VERSION;
		}

		@Override
		public SimpleDottedVersion getVersion(final ContentNodeMetadata model) throws SQLException {
			return SQLModelUtils.extractVersionFromModel(model, getInitialVersion());
		}

		@Override
		public SimpleDottedVersion getDatabaseVersion(final Connection conn) throws SQLException {
			return SQLModelUtils.extractVersionFromModel(getDatabaseModel(conn), getInitialVersion());
		}

		@Override
		public ContentNodeMetadata getDatabaseModel(final Connection conn) throws SQLException {
			return null;
		}

		@Override
		public void onCreate(final Connection conn, final ContentNodeMetadata model) throws SQLException {
			SQLModelUtils.createDatabaseByModel(conn, model);
		}

		@Override
		public void onUpgrade(final Connection conn, final SimpleDottedVersion version, final ContentNodeMetadata model, final SimpleDottedVersion oldVersion, ContentNodeMetadata oldModel) throws SQLException {
		}
	}
}

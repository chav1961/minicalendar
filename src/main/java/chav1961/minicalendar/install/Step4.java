package chav1961.minicalendar.install;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import chav1961.minicalendar.install.components.TablespaceSelector;
import chav1961.minicalendar.install.components.UserAndPasswordSelector;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.swing.SwingUtils;

/*
 * Install branch: Ask tablespace, schema and user/password
 */
public class Step4 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step4.caption";
	public static final String	KEY_DESCRIPTION = "installation.step4.description";
	public static final String	KEY_HELP = "installation.step4.help";

	private final DatabasePanel	panel;
	private final URL			jdbcDriverURL;
	
	public Step4(final Localizer localizer, final URL jdbcDriverURL) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (jdbcDriverURL == null) {
			throw new NullPointerException("JDBC driver URL can't be null"); 
		}
		else {
			this.panel = new DatabasePanel(localizer, jdbcDriverURL);
			this.jdbcDriverURL = jdbcDriverURL;
		}
	}
	
	@Override
	public String getStepId() {
		return getClass().getSimpleName();
	}

	@Override
	public StepType getStepType() {
		return StepType.ORDINAL;
	}

	@Override
	public String getCaption() {
		return KEY_CAPTION;
	}

	@Override
	public String getDescription() {
		return KEY_DESCRIPTION;
	}

	@Override
	public String getHelpId() {
		return KEY_HELP;
	}

	@Override
	public JComponent getContent() {
		return panel;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		final List<String>		tableSpaces = new ArrayList<>();
		
		try(final SimpleURLClassLoader	scl = new SimpleURLClassLoader(new URL[] {jdbcDriverURL})) {
			final File					driverFile = content.jdbcSelected ? content.jdbcDriver : InstallUtils.extractDriverFile(jdbcDriverURL);
			final Driver				driver = JDBCUtils.loadJdbcDriver(scl, driverFile);
			
			try(final Connection		conn = JDBCUtils.getConnection(driver, URI.create(content.connString), content.admin, content.adminPassword);
				final Statement			stmt = conn.createStatement();
				final ResultSet			rs = stmt.executeQuery("select spcname from pg_tablespace")) {
				
				while (rs.next()) {
					tableSpaces.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
		} catch (IOException | ContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		panel.getValuesFrom(content, tableSpaces);
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return panel.testSettings(content);
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		panel.fillValuesTo(content);
	}
	
	private static class DatabasePanel extends JPanel implements LocaleChangeListener {
		private static final long 		serialVersionUID = 1L;
		private static final String		KEY_TABLESPACE = "installation.step4.DatabasePanel.tablespace.border";
		private static final String		KEY_USER_PASSWORD = "installation.step4.JdbcPanel.userPassword.border";
		
		private final Localizer					localizer;
		private final URL						jdbcDriverURL;
		private final TitledBorder				tssBorder = new TitledBorder("");
		private final TablespaceSelector		tss;
		private final TitledBorder				upsBorder = new TitledBorder("");
		private final UserAndPasswordSelector	ups;

		private DatabasePanel(final Localizer localizer, final URL jdbcDriverURL) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null"); 
			}
			else {
				this.localizer = localizer;
				this.jdbcDriverURL = jdbcDriverURL;
				this.tss = new TablespaceSelector(localizer);
				this.ups = new UserAndPasswordSelector(localizer);
				
				final JPanel	centerPanel = new JPanel(new BorderLayout(5, 5));
				final BoxLayout	layout = new BoxLayout(this, BoxLayout.Y_AXIS);

				setLayout(layout);
				
				tss.setBorder(tssBorder);
				centerPanel.setBorder(upsBorder);
				centerPanel.add(ups, BorderLayout.CENTER);
				
				add(tss);
				add(Box.createRigidArea(new Dimension(5,5)));
				add(centerPanel);
				add(Box.createVerticalStrut(1000));
				
				fillLocalizedStrings();
			}
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			SwingUtils.refreshLocale(tss, oldLocale, newLocale);
			SwingUtils.refreshLocale(ups, oldLocale, newLocale);
			fillLocalizedStrings();
		}

		public void getValuesFrom(final InstallationDescriptor desc, final List<String> tableSpaces) {
			tss.fillTableSpaces(tableSpaces);
			tss.setRequestSelected(desc.tableSpaceSelected);
			tss.setCurrentTablespace(desc.tableSpace);
			ups.setUser(desc.user);
			ups.setPassword(desc.userPassword);
			ups.setCurrentSchema(desc.schemaName);
		}
		
		public void fillValuesTo(final InstallationDescriptor desc) {
			desc.tableSpaceSelected = tss.isRequestSelected();
			desc.tableSpace = tss.getCurrentTablespace();
			desc.user = ups.getUser();
			desc.userPassword = ups.getPassword();
			desc.schemaName = ups.getCurrentSchema();
		}
		
		public boolean testSettings(final InstallationDescriptor desc) {
			if (!desc.jdbcSelected) {
				try{return testSettings(desc, InstallUtils.extractDriverFile(jdbcDriverURL));
				} catch (IOException e) {
					SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
					return false;
				}
			}
			else {
				return testSettings(desc, desc.jdbcDriver);
			}
		}
		
		private boolean testSettings(final InstallationDescriptor desc, final File jdbcDriver) {
			try(final SimpleURLClassLoader	scl = new SimpleURLClassLoader(new URL[] {jdbcDriver.toURI().toURL()})) {
				final Driver				driver = JDBCUtils.loadJdbcDriver(scl, jdbcDriver);
				
				try(final Connection		conn = JDBCUtils.getConnection(driver, URI.create(desc.connString), desc.admin, desc.adminPassword)) {
					try(final PreparedStatement	stmt = conn.prepareStatement("select count(*) from pg_catalog.pg_user where usename = ?")) {
						stmt.setString(1, ups.getUser());
						try(final ResultSet		rs = stmt.executeQuery()) {
							if (rs.next() && rs.getInt(1) > 0) {
								SwingUtils.getNearestLogger(this).message(Severity.warning, "User ["+ups.getUser()+"] already exists");
							}
							else {
								return true;
							}
						}
					}
					
					try(final PreparedStatement	stmt = conn.prepareStatement("select count(*) from pg_catalog.pg_namespace where nspname = ?")) {
						stmt.setString(1, ups.getCurrentSchema());
						try(final ResultSet		rs = stmt.executeQuery()) {
							if (rs.next() && rs.getInt(1) > 0) {
								SwingUtils.getNearestLogger(this).message(Severity.warning, "Schema ["+ups.getUser()+"] already exists");
							}
							else {
								return true;
							}
						}
					}
				}
			} catch (SQLException | IOException | ContentException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
			}
			return false;
		}

		private void fillLocalizedStrings() {
			tssBorder.setTitle(localizer.getValue(KEY_TABLESPACE));
			upsBorder.setTitle(localizer.getValue(KEY_USER_PASSWORD));
		}
	}
}

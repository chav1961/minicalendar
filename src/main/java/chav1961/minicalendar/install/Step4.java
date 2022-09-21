package chav1961.minicalendar.install;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import chav1961.minicalendar.install.components.TablespaceSelector;
import chav1961.minicalendar.install.components.UserAndPasswordSelector;
import chav1961.purelib.basic.Utils;
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
	
	public Step4(final Localizer localizer, final URL jdbcDriverURL) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (jdbcDriverURL == null) {
			throw new NullPointerException("JDBC driver URL can't be null"); 
		}
		else {
			this.panel = new DatabasePanel(localizer, jdbcDriverURL);
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
		panel.getValuesFrom(content);
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		panel.fillValuesTo(content);
	}
	
		
	
	private static class DatabasePanel extends JPanel implements LocaleChangeListener {
		private static final long 		serialVersionUID = 1L;
		private static final String		KEY_TABLESPACE = "";
		private static final String		KEY_USER_PASSWORD = "";
		
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
				
				setLayout(new BorderLayout(5, 5));
				
				this.tss.setBorder(tssBorder);
				this.ups.setBorder(upsBorder);
				add(tss, BorderLayout.NORTH);
				add(ups, BorderLayout.SOUTH);
				
				fillLocalizedStrings();
			}
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			SwingUtils.refreshLocale(tss, oldLocale, newLocale);
			SwingUtils.refreshLocale(ups, oldLocale, newLocale);
			fillLocalizedStrings();
		}

		public void getValuesFrom(final InstallationDescriptor desc) {
			tss.setRequestSelected(desc.tableSpaceSelected);
			tss.setCurrentTablespace(desc.tableSpace);
			ups.setUser(desc.user);
			ups.setPassword(desc.userPassword);
		}
		
		public void fillValuesTo(final InstallationDescriptor desc) {
			desc.tableSpaceSelected = tss.isRequestSelected();
			desc.tableSpace = tss.getCurrentTablespace();
			desc.user = ups.getUser();
			desc.userPassword = ups.getPassword();
		}
		
		public boolean testSettings(final InstallationDescriptor desc) {
			if (!desc.jdbcSelected) {
				try{final File	temp = File.createTempFile("jdbc", ".jar");
					
					try{
						try(final InputStream	is = jdbcDriverURL.openStream();
							final OutputStream	os = new FileOutputStream(temp)) {
							
							Utils.copyStream(is, os);
						}
						return testSettings(desc, temp);
					} finally {
						temp.delete();
					}
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
			// TODO Auto-generated method stub
			return false;
		}

		private void fillLocalizedStrings() {
			tssBorder.setTitle(KEY_TABLESPACE);
			upsBorder.setTitle(KEY_USER_PASSWORD);
		}
	}
}

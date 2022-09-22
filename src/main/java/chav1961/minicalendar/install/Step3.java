package chav1961.minicalendar.install;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import chav1961.minicalendar.install.components.ConnectionStringSelector;
import chav1961.minicalendar.install.components.JdbcDriverSelector;
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
 * Install branch: Ask JDBC driver, PostgreSQL database, admin user and password. Test connection
 */
public class Step3 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step3.caption";
	public static final String	KEY_DESCRIPTION = "installation.step3.description";
	public static final String	KEY_HELP = "installation.step3.help";

	private final JdbcPanel	panel;
	
	public Step3(final Localizer localizer, final URL jdbcDriverURL) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (jdbcDriverURL == null) {
			throw new NullPointerException("JDBC driver URL can't be null"); 
		}
		else {
			this.panel = new JdbcPanel(localizer, jdbcDriverURL);
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
		return panel.testConnection();
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		panel.fillValuesTo(content);
	}
	
	private static class JdbcPanel extends JPanel implements LocaleChangeListener {
		private static final long 		serialVersionUID = 1L;
		private static final String		KEY_DRIVER_BORDER = "installation.step3.JdbcPanel.driver.border";
		private static final String		KEY_CONN_STRING_BORDER = "installation.step3.JdbcPanel.connstring.border";
		private static final String		KEY_BUTTON_TEXT = "installation.step3.JdbcPanel.button.text";
		private static final Icon		OK_ICON = new ImageIcon(JdbcPanel.class.getResource("testOK.png"));
		private static final Icon		FAILED_ICON = new ImageIcon(JdbcPanel.class.getResource("testFailed.png"));
		
		private final Localizer					localizer;
		private final URL						jdbcDriverURL;
		private final JdbcDriverSelector		jds;
		private final TitledBorder				jdsBorder = new TitledBorder("");
		private final ConnectionStringSelector	css;
		private final TitledBorder				cssBorder = new TitledBorder("");
		private final JButton					test = new JButton();
		
		public JdbcPanel(final Localizer localizer, final URL jdbcDriverURL) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null"); 
			}
			else if (jdbcDriverURL == null) {
				throw new NullPointerException("JDBC driver URL can't be null"); 
			}
			else {
				this.localizer = localizer;
				this.jdbcDriverURL = jdbcDriverURL;
				this.jds = new JdbcDriverSelector(localizer);
				this.css = new ConnectionStringSelector(localizer);
				
				final JPanel	southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				final JPanel	centerPanel = new JPanel(new BorderLayout(5, 5));
				final BoxLayout	layout = new BoxLayout(this, BoxLayout.Y_AXIS); 
				
				setLayout(layout);
				
				jds.setBorder(jdsBorder);
				centerPanel.setBorder(cssBorder);
				
				centerPanel.add(css, BorderLayout.CENTER);
				southPanel.add(test);
				add(jds);
				add(Box.createRigidArea(new Dimension(5,5)));
				add(centerPanel);
				add(Box.createRigidArea(new Dimension(5,5)));
				add(southPanel);
				add(Box.createVerticalStrut(1000));
				
				test.addActionListener((e)->{
					if (testConnection()) {
						SwingUtils.getNearestLogger(JdbcPanel.this).message(Severity.info, "Success");
					}
				});
				
				fillLocalizedStrings();
			}
		}

		public boolean testConnection() {
			if (!jds.isRequestSelected()) {
				try{return testConnection(InstallUtils.extractDriverFile(jdbcDriverURL));
				} catch (IOException e) {
					SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
					return false;
				}
//				try{final File	temp = File.createTempFile("jdbc", ".jar");
//				
//					try{
//						try(final InputStream	is = jdbcDriverURL.openStream();
//							final OutputStream	os = new FileOutputStream(temp)) {
//							
//							Utils.copyStream(is, os);
//						}
//						return testConnection(temp);
//					} finally {
//						temp.delete();
//					}
//				} catch (IOException e) {
//					SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
//					return false;
//				}
			}
			else {
				return testConnection(jds.getCurrentFile());
			}
		}

		public void getValuesFrom(final InstallationDescriptor desc) {
			jds.setRequestSelected(desc.jdbcSelected);
			jds.setCurrentFile(desc.jdbcDriver);
			css.setConnString(desc.connString);
			css.setUser(desc.admin);
			css.setPassword(desc.adminPassword);
		}
		
		public void fillValuesTo(final InstallationDescriptor desc) {
			desc.jdbcSelected = jds.isRequestSelected();
			desc.jdbcDriver = jds.getCurrentFile();
			desc.connString = css.getConnString();
			desc.admin = css.getUser();
			desc.adminPassword = css.getPassword();
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			SwingUtils.refreshLocale(jds, oldLocale, newLocale);
			SwingUtils.refreshLocale(css, oldLocale, newLocale);
			fillLocalizedStrings();
		}

		private boolean testConnection(final File driver) {
			final boolean	result = JDBCUtils.testConnection(driver, URI.create(css.getConnString()), css.getUser(), css.getPassword(), SwingUtils.getNearestLogger(this));
			
			test.setIcon(result ? OK_ICON : FAILED_ICON);
			return result;
		}		
		
		private void fillLocalizedStrings() {
			jdsBorder.setTitle(localizer.getValue(KEY_DRIVER_BORDER));
			cssBorder.setTitle(localizer.getValue(KEY_CONN_STRING_BORDER));
			test.setText(localizer.getValue(KEY_BUTTON_TEXT));
		}
	}
}

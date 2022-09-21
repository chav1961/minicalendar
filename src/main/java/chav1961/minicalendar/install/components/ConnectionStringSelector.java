package chav1961.minicalendar.install.components;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.ui.swing.JFileFieldWithMeta;
import chav1961.purelib.ui.swing.JPasswordFieldWithMeta;
import chav1961.purelib.ui.swing.JTextFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

public class ConnectionStringSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long 		serialVersionUID = 1L;
	private static final String		KEY_CONN_STRING_LABEL = "ConnectionStringSelector.connstring.label";
	private static final String		KEY_CONN_STRING_TOOLTIP = "ConnectionStringSelector.connstring.tooltip";
	private static final String		KEY_CONN_STRING_HELP = "ConnectionStringSelector.connstring.help";
	private static final String		KEY_USER_LABEL = "ConnectionStringSelector.user.label";
	private static final String		KEY_USER_TOOLTIP = "ConnectionStringSelector.user.tooltip";
	private static final String		KEY_USER_HELP = "ConnectionStringSelector.user.help";
	private static final String		KEY_PASSWORD_LABEL = "ConnectionStringSelector.password.label";
	private static final String		KEY_PASSWORD_TOOLTIP = "ConnectionStringSelector.password.tooltip";
	private static final String		KEY_PASSWORD_HELP = "ConnectionStringSelector.password.help";

	private final Localizer					localizer;
	private final JLabel					connStringLabel = new JLabel();
	private final ContentNodeMetadata		connStringMeta;
	private final JTextFieldWithMeta		connStringField;
	private final JLabel					userLabel = new JLabel();
	private final ContentNodeMetadata		userMeta;
	private final JTextFieldWithMeta		userField;
	private final JLabel					passwordLabel = new JLabel();
	private final ContentNodeMetadata		passwordMeta;
	private final JPasswordFieldWithMeta	passwordField;
	
	private String		connString = "jdbc:postgres:/";
	private String		user = "admin";
	private char[]		password = new char[] {'?'};
	
	public ConnectionStringSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.connStringMeta = new MutableContentNodeMetadata("connString", String.class, "connString", localizerUri, KEY_CONN_STRING_LABEL, KEY_CONN_STRING_TOOLTIP, KEY_CONN_STRING_HELP, new FieldFormat(String.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			this.userMeta = new MutableContentNodeMetadata("user", String.class, "user", localizerUri, KEY_USER_LABEL, KEY_USER_TOOLTIP, KEY_USER_HELP, new FieldFormat(String.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			this.passwordMeta = new MutableContentNodeMetadata("password", char[].class, "password", localizerUri, KEY_PASSWORD_LABEL, KEY_PASSWORD_TOOLTIP, KEY_PASSWORD_HELP, new FieldFormat(char[].class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);

			try{
				this.connStringField = (JTextFieldWithMeta)SwingUtils.prepareRenderer(connStringMeta, localizer, FieldFormat.ContentType.StringContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(connString);
															break;
														case Validation	:
															final String	temp = ((String)component.getChangedValueFromComponent()).trim();
															
															if (temp.isEmpty()) {
																SwingUtils.getNearestLogger(ConnectionStringSelector.this).message(Severity.warning, "Conn string can be empty");
																return false;
															}
															else if (!JDBCUtils.isConnectionStringValid(URI.create(temp),SwingUtils.getNearestLogger(ConnectionStringSelector.this))) {
																return false;
															}
															else {
																connString = temp;
																return true;
															}
														default:
															break;
													
													}
													return true;
												}
											});
				this.userField = (JTextFieldWithMeta)SwingUtils.prepareRenderer(userMeta, localizer, FieldFormat.ContentType.StringContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(user);
															break;
														case Validation	:
															final String	temp = ((String)component.getChangedValueFromComponent()).trim();
															
															if (temp.isEmpty()) {
																SwingUtils.getNearestLogger(ConnectionStringSelector.this).message(Severity.warning, "Admin user can't be empty");
																return false;
															}
															else {
																user = temp;
																return true;
															}
														default:
															break;
													
													}
													return true;
												}
											});
				this.passwordField = (JPasswordFieldWithMeta)SwingUtils.prepareRenderer(passwordMeta, localizer, FieldFormat.ContentType.PasswordContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(password);
															break;
														case Validation	:
															final char[]	temp = (char[])component.getChangedValueFromComponent();
															
															if (temp.length == 0) {
																SwingUtils.getNearestLogger(ConnectionStringSelector.this).message(Severity.warning, "Password can't be empty");
																return false;
															}
															else {
																password = temp;
																return true;
															}
														default:
															break;
													
													}
													return true;
												}
											});
			} catch (SyntaxException e) {
				throw new IllegalArgumentException(e); 
			}
			buildScreen();
			fillLocalizedStrings();
		}
	}


	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	public String getConnString() {
		return connString;
	}
	
	public void setConnString(final String connString) {
		this.connString = connString;
	}

	public String getUser() {
		return user;
	}
	
	public void setUser(final String user) {
		this.user = user;
	}

	public char[] getPassword() {
		return password;
	}
	
	public void setPassword(final char[] password) {
		this.password = password;
	}
	
	private void buildScreen() {
		setLayout(new LabelledLayout(5, 5));
		
		add(connStringLabel, LabelledLayout.LABEL_AREA);
		add(connStringField, LabelledLayout.CONTENT_AREA);
		add(userLabel, LabelledLayout.LABEL_AREA);
		add(userField, LabelledLayout.CONTENT_AREA);
		add(passwordLabel, LabelledLayout.LABEL_AREA);
		add(passwordField, LabelledLayout.CONTENT_AREA);
	}
	
	private void fillLocalizedStrings() {
		connStringLabel.setText(localizer.getValue(KEY_CONN_STRING_LABEL));
		connStringField.setToolTipText(localizer.getValue(KEY_CONN_STRING_TOOLTIP));
		userLabel.setText(localizer.getValue(KEY_USER_LABEL));
		userField.setToolTipText(localizer.getValue(KEY_USER_TOOLTIP));
		passwordLabel.setText(localizer.getValue(KEY_PASSWORD_LABEL));
		passwordField.setToolTipText(localizer.getValue(KEY_PASSWORD_TOOLTIP));
	}
}

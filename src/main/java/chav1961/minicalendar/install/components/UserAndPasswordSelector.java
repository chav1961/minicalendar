package chav1961.minicalendar.install.components;

import java.net.URI;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.JPasswordFieldWithMeta;
import chav1961.purelib.ui.swing.JTextFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

public class UserAndPasswordSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long 		serialVersionUID = 1L;
	private static final String		KEY_USER_LABEL = "UserAndPasswordSelector.user.label";
	private static final String		KEY_USER_TOOLTIP = "UserAndPasswordSelector.user.tooltip";
	private static final String		KEY_USER_HELP = "UserAndPasswordSelector.user.help";
	private static final String		KEY_PASSWORD_LABEL = "UserAndPasswordSelector.password.label";
	private static final String		KEY_PASSWORD_TOOLTIP = "UserAndPasswordSelector.password.tooltip";
	private static final String		KEY_PASSWORD_HELP = "UserAndPasswordSelector.password.help";
	private static final String		KEY_SCHEMA_LABEL = "UserAndPasswordSelector.schema";
	private static final String		KEY_SCHEMA_TOOLTIP = "UserAndPasswordSelector.schema.tooltip";
	private static final String		KEY_SCHEMA_HELP = "UserAndPasswordSelector.schema.help";

	private final Localizer					localizer;
	private final JLabel					userLabel = new JLabel();
	private final ContentNodeMetadata		userMeta;
	private final JTextFieldWithMeta		userField;
	private final JLabel					passwordLabel = new JLabel();
	private final ContentNodeMetadata		passwordMeta;
	private final JPasswordFieldWithMeta	passwordField;
	private final JLabel					schemaLabel = new JLabel();
	private final ContentNodeMetadata		schemaMeta;
	private final JTextFieldWithMeta		schemaField;
	
	private String		user = "user";
	private char[]		password = new char[] {'?'};
	private String		schema = "minical";
	
	public UserAndPasswordSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.userMeta = new MutableContentNodeMetadata("user", String.class, "user", localizerUri, KEY_USER_LABEL, KEY_USER_TOOLTIP, KEY_USER_HELP, new FieldFormat(String.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			this.passwordMeta = new MutableContentNodeMetadata("password", char[].class, "password", localizerUri, KEY_PASSWORD_LABEL, KEY_PASSWORD_TOOLTIP, KEY_PASSWORD_HELP, new FieldFormat(char[].class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			this.schemaMeta = new MutableContentNodeMetadata("schema", String.class, "schema", localizerUri, KEY_SCHEMA_LABEL, KEY_SCHEMA_TOOLTIP, KEY_SCHEMA_HELP, new FieldFormat(char[].class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);

			try{
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
																SwingUtils.getNearestLogger(UserAndPasswordSelector.this).message(Severity.warning, "Admin user can't be empty");
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
																SwingUtils.getNearestLogger(UserAndPasswordSelector.this).message(Severity.warning, "Password can't be empty");
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
				this.schemaField = (JTextFieldWithMeta)SwingUtils.prepareRenderer(schemaMeta, localizer, FieldFormat.ContentType.StringContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(schema);
															break;
														case Validation	:
															final String	temp = ((String)component.getChangedValueFromComponent()).trim();
															
															if (temp.isEmpty()) {
																SwingUtils.getNearestLogger(UserAndPasswordSelector.this).message(Severity.warning, "Schema can't be empty");
																return false;
															}
															else {
																schema = temp;
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

	public String getUser() {
		return user;
	}
	
	public void setUser(final String user) {
		if (user == null) {
			throw new NullPointerException("User to set can't be null");
		}
		else {
			this.user = user;
		}
	}

	public char[] getPassword() {
		return password;
	}
	
	public void setPassword(final char[] password) {
		if (password == null) {
			throw new NullPointerException("Password to set can't be null");
		}
		else {
			this.password = password;
		}
	}
	
	public String getCurrentSchema() {
		return schema;
	}
	
	public void setCurrentSchema(final String newSchema) {
		if (newSchema == null) {
			throw new NullPointerException("Schema to set can't be null");
		}
		else {
			schemaField.setText(newSchema);
			this.schema = newSchema;
		}
	}
	
	
	private void buildScreen() {
		setLayout(new LabelledLayout(5, 5));
		
		add(userLabel, LabelledLayout.LABEL_AREA);
		add(userField, LabelledLayout.CONTENT_AREA);
		add(passwordLabel, LabelledLayout.LABEL_AREA);
		add(passwordField, LabelledLayout.CONTENT_AREA);
		add(schemaLabel, LabelledLayout.LABEL_AREA);
		add(schemaField, LabelledLayout.CONTENT_AREA);
	}
	
	private void fillLocalizedStrings() {
		userLabel.setText(localizer.getValue(KEY_USER_LABEL));
		userField.setToolTipText(localizer.getValue(KEY_USER_TOOLTIP));
		passwordLabel.setText(localizer.getValue(KEY_PASSWORD_LABEL));
		passwordField.setToolTipText(localizer.getValue(KEY_PASSWORD_TOOLTIP));
		schemaLabel.setText(localizer.getValue(KEY_SCHEMA_LABEL));
		schemaField.setToolTipText(localizer.getValue(KEY_SCHEMA_TOOLTIP));
	}
}

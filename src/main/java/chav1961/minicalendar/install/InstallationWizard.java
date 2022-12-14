package chav1961.minicalendar.install;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import chav1961.minicalendar.interfaces.InstallMode;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.PureLibSettings.CurrentOS;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.ReflectedMapWrapper;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.swing.useful.JDialogContainer;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;

public class InstallationWizard {
	public static final String	ARG_MODE = "mode";
	public static final String	ARG_MODE_DEFAULT = "install";
	public static final String	ARG_UPDATE_URI = "uri";
	public static final String	ARG_UPDATE_URI_DEFAULT = "http://localhost:8080";
	public static final String	ARG_CONFIRM = "y";
	public static final String	ARG_PROPFILE_LOCATION = "prop";
	public static final String	ARG_PROPFILE_LOCATION_DEFAULT = "./.minicalendar.properties";
	
	public static final File	LAST_STATE = new File("./.install.last");
	public static final String	LAST_STATE_CONTENT_TYPE = "contentType";
	public static final String	LAST_STATE_CONTENT_TYPE_AWAITED = "minicalendar";

	public static final String	KEY_CONFIRM_RESTORE_TITLE = "installer.confirm.restore.title";
	public static final String	KEY_CONFIRM_RESTORE_MESSAGE = "installer.confirm.restore.message";
	public static final String	KEY_CONFIRM_CANCEL_TITLE = "installer.confirm.cancel.title";
	public static final String	KEY_CONFIRM_CANCEL_MESSAGE = "installer.confirm.cancel.title";
	public static final String	KEY_SAVE_SETTINGS_LABEL = "installer.confirm.savesettings.label";
	public static final String	KEY_SAVE_SETTINGS_TOOLTIP = "installer.confirm.savesettings.tooltip";

	public static final String	ICON_LOCATION = "/images/favicon.png";
	public static final String	JDBC_DRIVER_LOCATION = "/chav1961/minicalendar/database/postgresql-42.4.1.jar";
	
	
	public static void main(final String[] args) {
		try(final Reader					rdr = new InputStreamReader(System.in);
			final BufferedReader			brdr = new BufferedReader(rdr)) {
			final ArgParser					parser = new ApplicationArgParser().parse(args);
			final ContentMetadataInterface	appModel = ContentModelFactory.forXmlDescription(InstallationWizard.class.getResourceAsStream("/chav1961/minicalendar/model/installer.xml"));
			final SubstitutableProperties	props = parser.isTyped(ARG_PROPFILE_LOCATION) ? SubstitutableProperties.of(parser.getValue(ARG_PROPFILE_LOCATION, File.class)) : new SubstitutableProperties();

			if (PureLibSettings.CURRENT_OS != CurrentOS.WINDOWS) {
				throw new ConsoleCommandException("Installer doesn't support current OS ["+System.getProperty("os.name")+"]"); 
			}
			else if (!checkAdminRights()) {
				throw new ConsoleCommandException("You don't have admin rights to start this application"); 
			}
			else {
				if (parser.isTyped(ARG_MODE)) {
					switch (parser.getValue(ARG_MODE, InstallMode.class)) {
						case remove	:
							if (parser.getValue(ARG_CONFIRM, boolean.class) || askConfirmation(brdr, "To confirm removing type 'Y'")) {
								remove(new InstallationDescriptor());
							}
							break;
						case update	:
							if (parser.getValue(ARG_CONFIRM, boolean.class) || askConfirmation(brdr, "To confirm update type 'Y'")) {
								updateSoft(new InstallationDescriptor());
							}
							break;
						default:
							if (parser.getValue(ARG_CONFIRM, boolean.class) || askConfirmation(brdr, "To confirm installation type 'Y'")) {
								install(new InstallationDescriptor());
							}
							break;
					}
				}
				else {
					final Localizer					localizer = LocalizerFactory.getLocalizer(appModel.getRoot().getLocalizerAssociated());
					
					PureLibSettings.PURELIB_LOCALIZER.push(localizer);
					
					final SubstitutableProperties	lastStateProps = LAST_STATE.exists() && LAST_STATE.isFile() && LAST_STATE.canRead() ? SubstitutableProperties.of(LAST_STATE) : new SubstitutableProperties();
					final ErrorProcessing<InstallationDescriptor, InstallationError>	ep = new ErrorProcessing<InstallationDescriptor, InstallationError>() {
														@Override
														public void processWarning(final InstallationDescriptor content, final InstallationError err, final Object... parameters) {
															new JLocalizedOptionPane(localizer).message(null, err.name(), ARG_CONFIRM, JOptionPane.WARNING_MESSAGE);
														}
													};
					final InstallationDescriptor	desc = new InstallationDescriptor();
					
					if (lastStateProps.containsKey(LAST_STATE_CONTENT_TYPE) && lastStateProps.getProperty(LAST_STATE_CONTENT_TYPE).equals(LAST_STATE_CONTENT_TYPE_AWAITED)) {
						if (new JLocalizedOptionPane(localizer).confirm(null, KEY_CONFIRM_RESTORE_MESSAGE, KEY_CONFIRM_RESTORE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							final ReflectedMapWrapper	wrapper = new ReflectedMapWrapper(desc);	// Quick deserialization
							
							for (Entry<Object, Object> item : lastStateProps.entrySet()) {
								if (!LAST_STATE_CONTENT_TYPE.equals(item.getKey())) {
									wrapper.put((String)item.getKey(), lastStateProps.getProperty((String)item.getKey(), wrapper.getValueClass((String)item.getKey())));
								}
							}
						}
					}
					
					final JDialogContainer<InstallationDescriptor, InstallationError, JComponent>	container = 
													new JDialogContainer<InstallationDescriptor, InstallationError, JComponent>(localizer
																, (JFrame)null
																, desc
																, ep
																, new Step1(localizer), new Step2(localizer), new Step3(localizer, InstallationWizard.class.getResource(JDBC_DRIVER_LOCATION))
																, new Step4(localizer, InstallationWizard.class.getResource(JDBC_DRIVER_LOCATION)), new Step5(localizer)
																, new Step6(localizer), new Step7(localizer, InstallationWizard.class.getResource(JDBC_DRIVER_LOCATION))
																, new Step8(localizer), new Step9(), new Step10(), new Step11(), new Step12(), new Step13(), new Step14()
																, new Step15(), new Step16(localizer)) {
						
														@Override
														protected void cancel() {
															if (processCancel(localizer, desc)) {
																super.cancel();
															}
														}
													};

					container.setIconImage(ImageIO.read(URI.create("root://"+InstallationWizard.class.getCanonicalName()+ICON_LOCATION).toURL()));
					if (container.showDialog()) {
						LAST_STATE.delete();
					}
				}
			}
		} catch (CommandLineParametersException | ConsoleCommandException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		} catch (IOException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		} catch (EnvironmentException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}

	private static void install(final InstallationDescriptor desc) {
		// TODO Auto-generated method stub
		
	}

	private static void updateSoft(final InstallationDescriptor desc) {
		// TODO Auto-generated method stub
		
	}
	
	private static void remove(final InstallationDescriptor desc) {
		// TODO Auto-generated method stub
		
	}

	private static boolean checkAdminRights() {
		return true;
//		switch (PureLibSettings.CURRENT_OS) {
//			case LINUX		:
//				return false;
//			case MACOS		:
//				return false;
//			case UNKNOWN	:
//				return false;
//			case WINDOWS	:
//				return checkWindowsAdminRights();
//			default:
//				throw new UnsupportedOperationException("OS ["+PureLibSettings.CURRENT_OS+"] is not supported yet"); 
//		}
	}

	private static boolean checkWindowsAdminRights() {
		try {
			final ProcessBuilder	pb = new ProcessBuilder("net","session").redirectError(Redirect.DISCARD).redirectOutput(Redirect.DISCARD);
			final Process			p = pb.start();
			
			p.waitFor();
			return p.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	private static boolean askConfirmation(BufferedReader brdr, String string) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean processCancel(final Localizer localizer, final InstallationDescriptor desc) {
		final JPanel	panel = new JPanel(new BorderLayout(5, 5));
		final JLabel	label = new JLabel(localizer.getValue(KEY_CONFIRM_CANCEL_MESSAGE));
		final JCheckBox	check = new JCheckBox(localizer.getValue(KEY_SAVE_SETTINGS_LABEL), true);
		
		panel.add(label, BorderLayout.CENTER);
		panel.add(check, BorderLayout.SOUTH);
		check.setToolTipText(localizer.getValue(KEY_SAVE_SETTINGS_TOOLTIP));
		
		final boolean	result = new JLocalizedOptionPane(localizer).confirm(null, panel, KEY_CONFIRM_CANCEL_TITLE, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
		
		if (result && check.isSelected()) {
			final SubstitutableProperties	props = new SubstitutableProperties();
			
			storeInstallationState(desc, props);
			props.remove("adminPassword");
			props.remove("userPassword");
			props.put("mode", "install");
			props.put(LAST_STATE_CONTENT_TYPE, LAST_STATE_CONTENT_TYPE_AWAITED);
			try{props.store(LAST_STATE);
			} catch (IOException e) {
			}
		}
		return result;
	}
	
	
	private static void storeInstallationState(final InstallationDescriptor desc, final SubstitutableProperties props) {
		final ReflectedMapWrapper	wrapper = new ReflectedMapWrapper(desc);	// Quick deserialization
		
		for ( Entry<String, Object> item : wrapper.entrySet()) {
			props.put(item.getKey(), wrapper.getValue(item.getKey()));
		}
		props.put(LAST_STATE_CONTENT_TYPE, LAST_STATE_CONTENT_TYPE_AWAITED);
	}

	
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new EnumArg<InstallMode>(ARG_MODE, InstallMode.class, false, true, "Installation mode (optional)"),
			new URIArg(ARG_UPDATE_URI, false, "URI to update software from", ARG_UPDATE_URI_DEFAULT),
			new BooleanArg(ARG_CONFIRM, false, "Answer 'yes' for all requests", false),
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", ARG_PROPFILE_LOCATION_DEFAULT)
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
}

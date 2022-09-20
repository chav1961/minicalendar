package chav1961.minicalendar.install;


import java.io.File;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import chav1961.minicalendar.install.components.WorkingDirectorySelector;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

/*
 * Install branch: Ask location for the calendar software (select directory)
 */
public class Step2 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step2.caption";
	public static final String	KEY_DESCRIPTION = "installation.step2.description";
	public static final String	KEY_HELP = "installation.step2.help";
	public static final String	KEY_VALIDATION_NOT_EXISTS = "installation.step2.validation.notexists";
	public static final String	KEY_VALIDATION_NOT_ACCESSIBLE = "installation.step2.validation.notaccessible";
	public static final String	KEY_VALIDATION_NOT_A_DIRECTORY = "installation.step2.validation.notadirectory";
	public static final String	KEY_CONFIRM_TITLE = "installation.step2.confirm.title";
	public static final String	KEY_CONFIRM_MESSAGE = "installation.step2.confirm.message";
	
	
	private final Localizer					localizer;
	private final WorkingDirectorySelector	wds;
	
	public Step2(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.wds = new WorkingDirectorySelector(localizer, 200);
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
		return wds;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		wds.setCurrentFile(content.workDir);
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		final File	f = wds.getCurrentFile();
		
		if (!f.exists()) {
			SwingUtils.getNearestLogger(wds).message(Severity.error, KEY_VALIDATION_NOT_EXISTS, f.getPath());
			return false;
		}
		else if (f.isFile()) {
			SwingUtils.getNearestLogger(wds).message(Severity.error, Step2.KEY_VALIDATION_NOT_A_DIRECTORY, f.getPath());
			return false;
		}
		else if (!f.canRead() || !f.canWrite()) {
			SwingUtils.getNearestLogger(wds).message(Severity.error, KEY_VALIDATION_NOT_ACCESSIBLE, f.getPath());
			return false;
		}
		else {
			final File[]	dirContent = f.listFiles();
			
			if (dirContent != null && dirContent.length > 0) {
				return new JLocalizedOptionPane(localizer).confirm(wds, new LocalizedFormatter(KEY_CONFIRM_MESSAGE, f.getPath()), KEY_CONFIRM_TITLE, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			}
			else {
				return true;
			}
		}
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		content.workDir = wds.getCurrentFile(); 
	}
}

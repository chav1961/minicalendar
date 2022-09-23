package chav1961.minicalendar.install;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.minicalendar.install.components.LogFileViewer;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Error window
 */
public class Step16 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step16.caption";
	public static final String	KEY_DESCRIPTION = "installation.step16.description";
	public static final String	KEY_HELP = "installation.step16.help";

	private final LogFileViewer	lfv;
	
	public Step16(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.lfv = new LogFileViewer(localizer);
		}
	}
	
	@Override
	public String getStepId() {
		return getClass().getSimpleName();
	}

	@Override
	public StepType getStepType() {
		return StepType.TERM_FAILURE;
	}

	@Override
	public String getPrevStep() {
		return Step1.class.getSimpleName();
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
		return lfv;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		try{lfv.setLogFile((File)temporary.get("logfile"));
		} catch (IOException e) {
			throw new FlowException(e); 
		}
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return false;
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
	}
}

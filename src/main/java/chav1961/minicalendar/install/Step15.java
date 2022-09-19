package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Error window
 */
public class Step15 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step15.caption";
	public static final String	KEY_DESCRIPTION = "installation.step15.description";
	public static final String	KEY_HELP = "installation.step15.help";

	public Step15() {
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		
	}
}

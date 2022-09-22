package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Update software: show updates, ask to continue and make backup
 */
public class Step13 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step13.caption";
	public static final String	KEY_DESCRIPTION = "installation.step13.description";
	public static final String	KEY_HELP = "installation.step13.help";

	public Step13() {
		
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
	public String getNextStep() {
		return WizardStep.super.getNextStep();
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
		return new JLabel(getStepId());
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

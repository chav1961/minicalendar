package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.WizardStep.StepType;

/*
 * Install branch: Ask JDBC driver, PostgreSQL database, admin user and password. Test connection
 */
public class Step3 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step3.caption";
	public static final String	KEY_DESCRIPTION = "installation.step3.description";
	public static final String	KEY_HELP = "installation.step3.help";

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		// TODO Auto-generated method stub
		
	}
}

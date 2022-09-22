package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.minicalendar.install.components.ServiceNameSelector;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Install branch: Ask service name and startup parameters
 */
public class Step5 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step5.caption";
	public static final String	KEY_DESCRIPTION = "installation.step5.description";
	public static final String	KEY_HELP = "installation.step5.help";

	private final Localizer				localizer;
	private final ServiceNameSelector	sns;
	
	public Step5(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.sns = new ServiceNameSelector(localizer);
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
		return sns;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		sns.setService(content.serviceName);
	}

	@Override
	public boolean validate(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return true;
	}

	@Override
	public void afterShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		content.serviceName = sns.getService();
	}
}

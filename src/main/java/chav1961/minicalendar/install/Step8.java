package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.minicalendar.install.components.StartReviewSelector;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*  
 * Install branch: Complete and start browser
 */
public class Step8 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step8.caption";
	public static final String	KEY_DESCRIPTION = "installation.step8.description";
	public static final String	KEY_HELP = "installation.step8.help";

	private final Localizer				localizer;
	private final StartReviewSelector	srs;
	
	
	public Step8(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localzier can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.srs = new StartReviewSelector(localizer);
		}
	}
	
	@Override
	public String getStepId() {
		return getClass().getSimpleName();
	}

	@Override
	public StepType getStepType() {
		return StepType.TERM_SUCCESS;
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
		return srs;
	}

	@Override
	public void beforeShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
	}

	@Override
	public boolean validate(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return true;
	}

	@Override
	public void afterShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		if (srs.isStartReviewRequired()) {
			// TODO:
		}
	}
}

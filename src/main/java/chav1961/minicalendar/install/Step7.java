package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.minicalendar.install.actions.ActionExecutor;
import chav1961.minicalendar.install.actions.FirstAction;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Create software and start service
 */
public class Step7 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step7.caption";
	public static final String	KEY_DESCRIPTION = "installation.step7.description";
	public static final String	KEY_HELP = "installation.step7.help";

	private final Localizer			localizer;
	private final ActionExecutor	ax;
	private boolean					result = false;
	
	public Step7(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localzier can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.ax = new ActionExecutor(localizer, new FirstAction(localizer));
		}
	}
	
	@Override
	public String getStepId() {
		return getClass().getSimpleName();
	}

	@Override
	public StepType getStepType() {
		return StepType.PROCESSING;
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
		return ax;
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		ax.setInstallationDescriptor(content);
		try{ax.start();
			result = ax.waitCompletion();
		} catch (Exception e) {
			result = false;
			throw new FlowException(e);
		} finally {
			try{ax.stop();
			} catch (Exception e) {
				throw new FlowException(e);
			}
		}
	}

	@Override
	public boolean validate(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return true;
	}

	@Override
	public void afterShow(InstallationDescriptor content, Map<String, Object> temporary, ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
	}
}

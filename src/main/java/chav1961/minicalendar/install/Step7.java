package chav1961.minicalendar.install;

import java.net.URL;
import java.util.Map;

import javax.swing.JComponent;

import chav1961.minicalendar.install.actions.ActionExecutor;
import chav1961.minicalendar.install.actions.CleanDirectoryAction;
import chav1961.minicalendar.install.actions.CopyApplicationContentAction;
import chav1961.minicalendar.install.actions.PrepareDatabaseAction;
import chav1961.minicalendar.install.actions.PrepareRegistryAction;
import chav1961.minicalendar.install.actions.PrepareServiceAction;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.swing.SwingUtils;

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
	
	public Step7(final Localizer localizer, final URL jdbcDriverURL) {
		if (localizer == null) {
			throw new NullPointerException("Localzier can't be null"); 
		}
		else if (jdbcDriverURL == null) {
			throw new NullPointerException("JDBC driver URL can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.ax = new ActionExecutor(localizer, new CleanDirectoryAction(localizer), new CopyApplicationContentAction(localizer)
								, new PrepareDatabaseAction(localizer, jdbcDriverURL), new PrepareRegistryAction(localizer)
								, new PrepareServiceAction(localizer));
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
	public String getNextStep() {
		if (!result) {
			return Step16.class.getSimpleName();
		}
		else {
			return null;
		}
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
		temporary.put("logfile",ax.getErrorLog());
		ax.setInstallationDescriptor(content);
		
		try{ax.start();
			result = ax.waitCompletion();
		} catch (Exception e) {
			result = false;
			throw new FlowException(e);
		} finally {
			try{if (ax.isStarted()) {
					ax.stop();
				}
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
	
	@Override
	public boolean onCancel() {
		try{ax.stop();
		} catch (Exception e) {
			SwingUtils.getNearestLogger(ax).message(Severity.error, "INstallation cancelled");
		}
		return false;
	}
}

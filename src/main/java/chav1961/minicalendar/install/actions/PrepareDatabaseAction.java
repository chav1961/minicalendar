package chav1961.minicalendar.install.actions;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.InstallationError;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class PrepareDatabaseAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_ACTION_NAME = "PrepareDatabaseAction.actionname";
	
	private final Localizer				localizer;
	private final ProgressIndicatorImpl	pii;
	private final ErrorProcessing<InstallationDescriptor, InstallationError>	err;
	
	private State		state = State.UNPREPARED;
	
	public PrepareDatabaseAction(final Localizer localizer, final ErrorProcessing<InstallationDescriptor, InstallationError> err) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (err == null) {
			throw new NullPointerException("Error processing can't be null");
		}
		else {
			this.localizer = localizer;
			this.pii = new ProgressIndicatorImpl(localizer);
			this.err = err;
		}
	}
	
	@Override
	public String getActionName() {
		return KEY_ACTION_NAME;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public ProgressIndicator getProgressIndicator() {
		return pii;
	}

	@Override
	public Class<?>[] getAncestors() {
		return new Class<?>[0];
	}

	@Override
	public void prepare(final LoggerFacade logger) throws Exception {
	}

	@Override
	public boolean execute(final LoggerFacade logger, InstallationDescriptor content, Object... parameters) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void markAsFailed() {
		state = State.FAILED;
	}

	@Override
	public void unprepare(final LoggerFacade logger) throws Exception {
	}
}

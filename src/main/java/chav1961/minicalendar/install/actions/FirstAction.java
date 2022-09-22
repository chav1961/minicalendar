package chav1961.minicalendar.install.actions;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;

public class FirstAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_FIRSTACTION_STEP = "testaction";
	
	private final Localizer				localizer;
	private final ProgressIndicatorImpl	pi;
	private State	currentState = State.UNPREPARED;
	
	public FirstAction(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.pi = new ProgressIndicatorImpl(localizer);
		}
	}
	
	@Override
	public String getActionName() {
		return getClass().getSimpleName();
	}

	@Override
	public State getState() {
		return currentState;
	}

	@Override
	public ProgressIndicator getProgressIndicator() {
		return pi;
	}

	@Override
	public Class<?>[] getAncestors() {
		return new Class<?>[0];
	}

	@Override
	public void prepare() throws Exception {
		currentState = State.AWAITING;
	}

	@Override
	public boolean execute(final InstallationDescriptor content, final Object... parameters) throws Exception {
		pi.start(KEY_FIRSTACTION_STEP,10);
		for (int index = 0; index < 10; index++) {
			Thread.sleep(1000);
			pi.processed(index);
		}
		pi.end();
		currentState = State.COMPLETED;
		return true;
	}

	@Override
	public void markAsFailed() {
		currentState = State.FAILED;
	}

	@Override
	public void unprepare() throws Exception {
		currentState = State.UNPREPARED;
	}

}

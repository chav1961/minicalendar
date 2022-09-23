package chav1961.minicalendar.install.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.InstallationError;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class CleanDirectoryAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_ACTION_NAME = "CleanDirectoryAction.actionname";
	private static final String		KEY_CALCULATE_DIR_CONTENT = "CleanDirectoryAction.actionname";
	private static final String		KEY_REMOVE_DIR_CONTENT = "CleanDirectoryAction.actionname";
	
	private final Localizer				localizer;
	private final ProgressIndicatorImpl	pii;
	private final ErrorProcessing<InstallationDescriptor, InstallationError>	err;
	
	private State		state = State.UNPREPARED;
	
	public CleanDirectoryAction(final Localizer localizer, final ErrorProcessing<InstallationDescriptor, InstallationError> err) {
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
		state = State.AWAITING;
	}

	@Override
	public boolean execute(final LoggerFacade logger, final InstallationDescriptor content, final Object... parameters) throws Exception {
		final File		workDir = content.workDir;
		final File[]	wdContent = workDir.listFiles();
		
		if (wdContent == null) {
			state = State.COMPLETED;
			return true;
		}
		else {
			final List<File>	collection = new ArrayList<>();
			boolean	success = true;

			try{state = State.PROCESSING;
			
				pii.start(KEY_CALCULATE_DIR_CONTENT, wdContent.length);
				for(int index = 0; index < wdContent.length; index++) {
					calculateContent(wdContent[index], collection);
					pii.processed(index);
				}
				pii.end();
				
				
				pii.start(KEY_REMOVE_DIR_CONTENT, collection.size());
				for (int index = collection.size() - 1, count = 0; index >= 0; index--, count++) {
					if (!collection.get(index).delete()) {
						success = false;
					}
					pii.processed(count);
				}
				pii.end();
	
				return success;
			} finally {
				state = success ? State.COMPLETED : State.FAILED;
			}
		}
	}

	private void calculateContent(final File root, final List<File> collection) {
		collection.add(root);
		if (root.isDirectory()) {
			final File[] list = root.listFiles();
			
			if (list != null) {
				for (File item : list) {
					calculateContent(item, collection);
				}
			}
		}
	}

	@Override
	public void markAsFailed() {
		state = State.FAILED;
	}

	@Override
	public void unprepare(final LoggerFacade logger) throws Exception {
		state = State.UNPREPARED;
	}
}

package chav1961.minicalendar.install.actions;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.actions.ActionInterface.State;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

public class ActionExecutor extends JPanel implements ExecutionControl, LocaleChangeListener, AutoCloseable {
	private static final long 			serialVersionUID = 1L;
	private static final String			KEY_NAME_LABEL = "ActionExecutor.table.name";
	private static final String			KEY_STATE_LABEL = "ActionExecutor.table.state";
	
	private final Localizer				localizer;
	private final AtomicBoolean			result = new AtomicBoolean();
	private final AtomicReference<CountDownLatch>	ar = new AtomicReference<>(null);
	private final ActionInterface<InstallationDescriptor>[]	steps;
	
	private final JLabel				nameLabel = new JLabel("", JLabel.CENTER);
	private final JLabel				stateLabel = new JLabel("", JLabel.CENTER);
	private final JLabel[]				names;
	private final StateAndProgress[]	states;
	
	private InstallationDescriptor	desc;
	private boolean					isStarted = false;
	private Thread					t = null;
	
	public ActionExecutor(final Localizer localizer, final ActionInterface<InstallationDescriptor>... steps) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (steps == null || steps.length == 0 || Utils.checkArrayContent4Nulls(steps) >= 0) {
			throw new IllegalArgumentException("Action steps are null, empty or contains nulls inside");
		}
		else {
			this.localizer = localizer;
			this.steps = steps;
			
			final Set<Class<?>>	cl = new HashSet<>();
			
			for (ActionInterface<InstallationDescriptor> item : steps) {
				cl.add(item.getClass());
			}
			for (ActionInterface<InstallationDescriptor> item : steps) {
				for (Class<?> ancestor : item.getAncestors()) {
					if (!cl.contains(ancestor)) {
						throw new IllegalArgumentException("Action step ["+item.getActionName()+"] contains ancestor ref ["+ancestor.getCanonicalName()+"] to action class that is missing in the step parameters list"); 
					}
				}
			}
			for (ActionInterface<InstallationDescriptor> item : steps) {
				final List<ActionInterface<InstallationDescriptor>> passed = new ArrayList<>();
				
				if (item.getAncestors().length > 0) {
					testLoops(item, steps, passed);
				}
			}
			
			this.names = new JLabel[steps.length];
			this.states = new StateAndProgress[steps.length];
			for (int index = 0; index < steps.length; index++) {
				names[index] = new JLabel();
				states[index] = new StateAndProgress(steps[index].getProgressIndicator());
			}
			
			buildScreen();
			fillLocalizedStrings();
		}
	}

	public void setInstallationDescriptor(final InstallationDescriptor desc) {
		if (desc == null) {
			throw new NullPointerException("Installation descriptor can't be null");
		}
		else {
			this.desc = desc;
		}
	}
	
	
	@Override
	public void start() throws Exception {
		if (isStarted()) {
			throw new IllegalStateException("Attempt to start already started process");
		}
		else {
			ar.set(new CountDownLatch(steps.length));
			t = new Thread(()->process());
			t.setDaemon(true);
			result.set(false);
			isStarted = true;
			t.start();
		}
	}

	@Override
	public void suspend() throws Exception {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void resume() throws Exception {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void stop() throws Exception {
		if (!isStarted()) {
			throw new IllegalStateException("Attempt to stop non-started process");
		}
		else {
			isStarted = false;
			t.interrupt();
			t.join();
		}
	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

	@Override
	public boolean isSuspended() {
		return false;
	}

	public boolean waitCompletion() {
		if (ar.get() == null) {
			throw new IllegalStateException("Process is not started yet");
		}
		else {
			try{ar.get().await();
				ar.set(null);
				return result.get();
			} catch (InterruptedException e) {
				return false;
			}
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		for (StateAndProgress item : states) {
			SwingUtils.refreshLocale(item, oldLocale, newLocale);
		}
		fillLocalizedStrings();
	}

	@Override
	public void close() throws RuntimeException {
		if (isStarted()) {
			try{stop();
			} catch (Exception e) {
			}
		}
		if (ar.get() != null) {
			for (int index = 0; index < steps.length; index++) {
				ar.get().countDown();
			}
		}
	}

	private void process() {
		// TODO Auto-generated method stub
		try{
			SwingUtils.getNearestLogger(this).message(Severity.info, "Preparation...");
			fillState();
			
			for (ActionInterface<InstallationDescriptor> item : steps) {
				item.prepare();
			}
			
			boolean	totalResult = true;
			
			SwingUtils.getNearestLogger(this).message(Severity.info, "Start process...");
			while (ar.get().getCount() > 0 && !Thread.interrupted()) {
				fillState();
				for (ActionInterface<InstallationDescriptor> item : steps) {	// Select and call
					if (item.getState() == State.AWAITING) {
						if (allAncestorsCompleted(item.getAncestors())) {
							try{
								if (!item.execute(desc)) {
									totalResult = false;
									item.markAsFailed();
								}
							} catch (Exception exc) {
								totalResult = false;
								item.markAsFailed();
							} finally {
								ar.get().countDown();
							}
						}
						else if (anyAncestorFailed(item.getAncestors())) {
							item.markAsFailed();
							ar.get().countDown();
						}
						
					}
				}
			}
			
			if (Thread.interrupted()) {
				for (int index = 0; index < steps.length; index++) {
					ar.get().countDown();
				}
			}
			
			SwingUtils.getNearestLogger(this).message(Severity.info, "Unprepare...");
			for (ActionInterface<InstallationDescriptor> item : steps) {
				item.unprepare();
			}
			result.set(totalResult);
		} catch (Exception e) {
			SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
		}
	}

	private void testLoops(final ActionInterface<InstallationDescriptor> item, final ActionInterface<InstallationDescriptor>[] steps, final List<ActionInterface<InstallationDescriptor>> passed) {
		if (passed.contains(item)) {
			final StringBuilder	sb = new StringBuilder();
			
			for (ActionInterface<InstallationDescriptor> p : passed) {
				sb.append(',').append(p.getActionName());
			}
			throw new IllegalArgumentException("Ancestor loop chain detected: "+sb.substring(1));
		}
		else {
			passed.add(item);
			for (Class<?> ancestorClass : item.getAncestors()) {
				for (ActionInterface<InstallationDescriptor> ancestor : steps) {
					if (ancestorClass.isInstance(ancestor)) {
						testLoops(ancestor, steps, passed);
					}
				}
			}
		}
	}

	private boolean allAncestorsCompleted(final Class<?>[] ancestors) {
		if (ancestors.length == 0) {
			return true;
		}
		else {
			for (Class<?> item : ancestors) {
				if (intanceByClass(item).getState() != State.COMPLETED) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean anyAncestorFailed(Class<?>[] ancestors) {
		if (ancestors.length == 0) {
			return false;
		}
		else {
			for (Class<?> item : ancestors) {
				if (intanceByClass(item).getState() == State.FAILED) {
					return true;
				}
			}
			return false;
		}
	}


	private ActionInterface<InstallationDescriptor> intanceByClass(final Class<?> cl) {
		for (ActionInterface<InstallationDescriptor> item : steps) {
			if (cl.isInstance(item)) {
				return item;
			}
		}
		throw new IllegalArgumentException("Item with the class ["+cl.getCanonicalName()+"] not found");
	}

	
	private void buildScreen() {
		setLayout(new LabelledLayout(10, 10));
		add(nameLabel, LabelledLayout.LABEL_AREA);
		add(stateLabel, LabelledLayout.CONTENT_AREA);
		
		for (int index = 0; index < steps.length; index++) {
			add(names[index], LabelledLayout.LABEL_AREA);
			add(states[index], LabelledLayout.CONTENT_AREA);
		}
	}

	private void fillState() {
		for (int index = 0; index < steps.length; index++) {
			states[index].state.setIcon(steps[index].getState().getStateIcon());
		}
	}
	
	private void fillLocalizedStrings() {
		nameLabel.setText(localizer.getValue(KEY_NAME_LABEL));
		stateLabel.setText(localizer.getValue(KEY_STATE_LABEL));
		for (int index = 0; index < steps.length; index++) {
			names[index].setText(steps[index].getActionName());
		}
	}

	
	private final class StateAndProgress extends JPanel {
		private static final long serialVersionUID = 1L;
		
		private JLabel			state = new JLabel();
		private JProgressBar	progress;
		
		private StateAndProgress(final ProgressIndicator progressIndicator) {
			super(new BorderLayout(5, 5));
			
			this.progress = (JProgressBar)progressIndicator;
			add(state, BorderLayout.WEST);
			add(progress, BorderLayout.CENTER);
		}
	}
}

package chav1961.minicalendar.install.actions;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.interfaces.ProgressIndicator;

public interface ActionInterface<T> {
	public static enum State {
		UNPREPARED(new ImageIcon(State.class.getResource("unprepared.png"))), 
		AWAITING(new ImageIcon(State.class.getResource("awaiting.png"))), 
		PROCESSING(new ImageIcon(State.class.getResource("processing.png"))), 
		COMPLETED(new ImageIcon(State.class.getResource("completed.png"))),
		FAILED(new ImageIcon(State.class.getResource("failed.png")));
		
		private final Icon	stateIcon;
		
		private State(final Icon stateIcon) {
			this.stateIcon = stateIcon;
		}
		
		public Icon getStateIcon() {
			return stateIcon;
		}
	}
	
	String getActionName();
	State getState();
	ProgressIndicator getProgressIndicator();
	Class<?>[] getAncestors();
	
	void prepare() throws Exception;
	boolean execute(T content, Object... parameters) throws Exception;
	void markAsFailed();
	void unprepare() throws Exception;
}

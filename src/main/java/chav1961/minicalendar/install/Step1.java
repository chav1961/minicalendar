package chav1961.minicalendar.install;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.minicalendar.install.components.RadioButtonSelector;
import chav1961.minicalendar.interfaces.InstallMode;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

/*
 * Ask "Install", "Remove", or "Update software"
 */
public class Step1 implements WizardStep<InstallationDescriptor, InstallationError, JComponent> {
	public static final String	KEY_CAPTION = "installation.step1.caption";
	public static final String	KEY_DESCRIPTION = "installation.step1.description";
	public static final String	KEY_HELP = "installation.step1.help";

	public static final String	KEY_SELECT_INSTALL = "installation.step1.select.install";
	public static final String	KEY_SELECT_REINSTALL = "installation.step1.select.reinstall";
	public static final String	KEY_SELECT_UPDATE = "installation.step1.select.update";
	public static final String	KEY_SELECT_REMOVE = "installation.step1.select.remove";
	
	private final RadioButtonSelector	rbs;
	
	public Step1(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.rbs = new RadioButtonSelector(localizer, KEY_SELECT_INSTALL, KEY_SELECT_REINSTALL, KEY_SELECT_UPDATE, KEY_SELECT_REMOVE);
		}
	}
	
	@Override
	public String getNextStep() {
		final int	selected = rbs.getSelectionIndex();

		switch (selected) {
			case 0 : case 1 :
				return Step2.class.getSimpleName();
			case 2 :
				return Step12.class.getSimpleName();
			case 3 :
				return Step8.class.getSimpleName();
			default :
				throw new UnsupportedOperationException("Selection ["+selected+"] is not supported yet"); 
		}
	}
	
	@Override
	public String getStepId() {
		return getClass().getSimpleName();
	}

	@Override
	public StepType getStepType() {
		return StepType.INITIAL;
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
		return rbs; 
	}

	@Override
	public void beforeShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		if (content.mode == null) {
			rbs.setSelectionIndex(0);
		}
		else {
			rbs.setSelectionIndex(content.mode.ordinal());
		}
	}

	@Override
	public boolean validate(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		return true;
	}

	@Override
	public void afterShow(final InstallationDescriptor content, final Map<String, Object> temporary, final ErrorProcessing<InstallationDescriptor, InstallationError> err) throws FlowException {
		final int	selected = rbs.getSelectionIndex();
		
		if (selected == 0) {
			content.mode = null;
		}
		else {
			content.mode = InstallMode.values()[selected - 1];
		}
	}
}

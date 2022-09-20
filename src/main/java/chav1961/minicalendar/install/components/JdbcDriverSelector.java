package chav1961.minicalendar.install.components;

import java.io.File;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.JFileFieldWithMeta;

public class JdbcDriverSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long serialVersionUID = 1L;

	private final Localizer				localizer;
	private final ButtonGroup			group = new ButtonGroup();
	private final JRadioButton			internal = new JRadioButton();
	private final JRadioButton			selected = new JRadioButton();
	private final JFileFieldWithMeta	fileField = null;
	private boolean 					requestSelected = false;
	
	public JdbcDriverSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			
			group.add(internal);
			group.add(selected);
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
	
	private void fillLocalizedStrings() {
		
	}
}

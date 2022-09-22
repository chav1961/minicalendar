package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class StartReviewSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long 	serialVersionUID = 1L;
	private static final String	KEY_START_REVIEW = "StartReviewSelector.startreview.check";
	
	private final Localizer		localizer;
	private final JCheckBox		check = new JCheckBox("", true);
	
	public StartReviewSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			buildScreen();
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void allowUnnamedModuleAccess(Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	public boolean isStartReviewRequired() {
		return check.isSelected();
	}

	private void buildScreen() {
		setLayout(new BorderLayout(10, 10));
		add(check, BorderLayout.CENTER);
	}
	
	private void fillLocalizedStrings() {
		check.setText(localizer.getValue(KEY_START_REVIEW));
	}
}

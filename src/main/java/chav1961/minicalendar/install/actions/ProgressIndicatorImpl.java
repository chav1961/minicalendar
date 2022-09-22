package chav1961.minicalendar.install.actions;

import java.awt.Font;
import java.util.Locale;

import javax.swing.JProgressBar;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class ProgressIndicatorImpl extends JProgressBar implements ProgressIndicator, LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final Localizer	localizer;
	private final Font		font = getFont();
	private String			caption = "";
	
	public ProgressIndicatorImpl(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			setStringPainted(true);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public void start(final String caption, final long total) {
		if (caption == null) {
			throw new NullPointerException("Caption can't be null");
		}
		else if (total <= 0) {
			throw new IllegalArgumentException("Total value ["+total+"] must be positive");
		}
		else {
			this.caption = caption;
			
			setMinimum(0);
			setMaximum((int)total);
			setValue(0);
			setFont(font.deriveFont(Font.PLAIN));
			fillLocalizedStrings();
		}
	}

	@Override
	public void start(final String caption) {
		start(caption, 100);
	}

	@Override
	public boolean processed(final long processed) {
		if (processed < 0 || processed > getMaximum()) {
			throw new IllegalArgumentException("Total value ["+processed+"] must be in range 0.."+getMaximum());
		}
		else {
			setValue((int)processed);
			return true;
		}
	}

	@Override
	public void end() {
		setFont(font.deriveFont(Font.BOLD));
	}

	private void fillLocalizedStrings() {
		try{setString(localizer.getValue(caption));
		} catch (LocalizationException exc) {
			setString(caption);
		}
	}
}

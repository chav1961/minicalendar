package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class LogFileViewer extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = 1L;
	private static final String	KEY_STORE_FILE = "LogFileViewer.button.store";

	private final Localizer	localizer;
	private final JTextArea	content = new JTextArea();
	private final JButton	storeLog = new JButton();
	private File			logFile = null;
	
	public LogFileViewer(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;

			storeLog.addActionListener((e)->storeLog());
			buildScreen();
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	public void setLogFile(final File logFile) throws IOException {
		if (logFile == null) {
			throw new NullPointerException("Log file can't be null");
		}
		else {
			this.logFile = logFile;
			content.setText(Utils.fromResource(logFile.toURI().toURL(), PureLibSettings.DEFAULT_CONTENT_ENCODING));
		}
	}
	
	private void storeLog() {
		
	}
	
	private void buildScreen() {
		final JPanel		southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		final JScrollPane	pane = new JScrollPane(content);
		
		setLayout(new BorderLayout(5, 5));
		
		southPanel.add(storeLog);
		add(pane, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
	}
	
	private void fillLocalizedStrings() {
		storeLog.setText(localizer.getValue(KEY_STORE_FILE));
	}
}

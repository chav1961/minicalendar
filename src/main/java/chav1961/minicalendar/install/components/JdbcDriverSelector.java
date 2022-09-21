package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.ui.swing.JFileFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class JdbcDriverSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long 			serialVersionUID = 1L;
	private static final String			KEY_USE_INTERNAL = "JdbcDriverSelector.button.useinternal";
	private static final String			KEY_SELECT = "JdbcDriverSelector.button.selected";
	private static final String			KEY_TOOLTIP = "JdbcDriverSelector.button.selected.tooptip";
	private static final String			KEY_HELP = "JdbcDriverSelector.button.selected.help";

	private final Localizer				localizer;
	private final ContentNodeMetadata	fileMeta;
	private final ButtonGroup			group = new ButtonGroup();
	private final JRadioButton			internal = new JRadioButton();
	private final JRadioButton			selected = new JRadioButton();
	private final JLabel				internalLabel = new JLabel();
	private final JLabel				selectedLabel = new JLabel();
	private final JFileFieldWithMeta	fileField;
	
	private File						currentFile = new File("./"); 
	private boolean 					requestSelected = false;
	
	public JdbcDriverSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.fileMeta = new MutableContentNodeMetadata("file", File.class, "file", localizerUri, KEY_SELECT, KEY_TOOLTIP, KEY_HELP, new FieldFormat(File.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			try{
				this.fileField = (JFileFieldWithMeta)SwingUtils.prepareRenderer(fileMeta, localizer, FieldFormat.ContentType.FileContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(currentFile);
															break;
														case Validation	:
															final File	temp = (File)component.getChangedValueFromComponent();
															
															if (!temp.exists()) {
																SwingUtils.getNearestLogger(JdbcDriverSelector.this).message(Severity.warning, "File is not exists");
																return false;
															}
															else if (temp.isDirectory()) {
																SwingUtils.getNearestLogger(JdbcDriverSelector.this).message(Severity.warning, "Entity must be a file, not a directory");
																return false;
															}
															else if (!temp.canRead()) {
																SwingUtils.getNearestLogger(JdbcDriverSelector.this).message(Severity.warning, "File is not accessible for you due to security restrictions");
																return false;
															}
															else if (!JDBCUtils.isJDBCDriverValid(temp, SwingUtils.getNearestLogger(JdbcDriverSelector.this))) {
																return false;
															}
															else {
																currentFile = temp; 
																return true;
															}
														default:
															break;
													
													}
													return true;
												}
											});
			} catch (SyntaxException e) {
				throw new IllegalArgumentException(e); 
			}
			
			buildScreen();
			internal.addActionListener((e)->setRequestSelected(false));
			selected.addActionListener((e)->setRequestSelected(true));
			setRequestSelected(false);
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	public boolean isRequestSelected() {
		return requestSelected;
	}
	
	public void setRequestSelected(final boolean request) {
		if (requestSelected = request) {
			group.setSelected(selected.getModel(), true);
			fileField.setEnabled(true);
		}
		else {
			group.setSelected(internal.getModel(), true);
			fileField.setEnabled(false);
		}
	}

	public File getCurrentFile() {
		return currentFile;
	}
	
	public void setCurrentFile(final File newFile) {
		if (newFile == null) {
			throw new NullPointerException("File to set can't be null");
		}
		else {
			this.currentFile = newFile;
		}
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	private void buildScreen() {
		final JPanel	topPanel = new JPanel(new BorderLayout(5, 5));
		final JPanel	bottomPanel = new JPanel(new BorderLayout(5, 5));
		final JPanel	selectPanel = new JPanel(new BorderLayout(5, 5));

		group.add(internal);
		group.add(selected);
		
		selectPanel.add(selectedLabel, BorderLayout.WEST);
		selectPanel.add(fileField, BorderLayout.CENTER);
		bottomPanel.add(selected, BorderLayout.WEST);
		bottomPanel.add(selectPanel, BorderLayout.CENTER);
		topPanel.add(internal, BorderLayout.WEST);
		topPanel.add(internalLabel, BorderLayout.CENTER);
		setLayout(new GridLayout(2, 1, 5, 5));
		add(topPanel);
		add(bottomPanel);
	}

	
	private void fillLocalizedStrings() {
		internalLabel.setText(localizer.getValue(KEY_USE_INTERNAL));
		selectedLabel.setText(localizer.getValue(KEY_SELECT));
	}
}

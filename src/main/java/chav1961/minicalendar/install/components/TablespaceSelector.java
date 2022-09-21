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
import chav1961.purelib.ui.swing.JTextFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

public class TablespaceSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long 			serialVersionUID = 1L;
	private static final String			KEY_USE_COMMON = "TablespaceSelector.button.usecommon";
	private static final String			KEY_SELECT = "TablespaceSelector.button.dedicated";
	private static final String			KEY_TOOLTIP = "TablespaceSelector.button.dedicated.tooltip";
	private static final String			KEY_HELP = "TablespaceSelector.button.dedicated.help";

	private final Localizer				localizer;
	private final ContentNodeMetadata	tablespaceMeta;
	private final ButtonGroup			group = new ButtonGroup();
	private final JRadioButton			common = new JRadioButton();
	private final JRadioButton			dedicated = new JRadioButton();
	private final JLabel				commonLabel = new JLabel();
	private final JLabel				dedicatedLabel = new JLabel();
	private final JTextFieldWithMeta	tablespaceField;
	
	private String						currentTablespace = "public"; 
	private boolean 					requestSelected = false;
	
	public TablespaceSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.tablespaceMeta = new MutableContentNodeMetadata("tablespace", String.class, "tablespace", localizerUri, KEY_SELECT, KEY_TOOLTIP, KEY_HELP, new FieldFormat(String.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			try{
				this.tablespaceField = (JTextFieldWithMeta)SwingUtils.prepareRenderer(tablespaceMeta, localizer, FieldFormat.ContentType.StringContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(currentTablespace);
															break;
														case Validation	:
															currentTablespace = ((String)component.getChangedValueFromComponent()).trim();
															return true;
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
			common.addActionListener((e)->setRequestSelected(false));
			dedicated.addActionListener((e)->setRequestSelected(true));
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
			group.setSelected(dedicated.getModel(), true);
			tablespaceField.setEnabled(true);
		}
		else {
			group.setSelected(common.getModel(), true);
			tablespaceField.setEnabled(false);
		}
	}

	public String getCurrentTablespace() {
		return currentTablespace;
	}
	
	public void setCurrentTablespace(final String newTablespace) {
		if (newTablespace == null) {
			throw new NullPointerException("Tablespace to set can't be null");
		}
		else {
			this.currentTablespace = newTablespace;
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

		group.add(common);
		group.add(dedicated);
		
		selectPanel.add(dedicatedLabel, BorderLayout.WEST);
		selectPanel.add(tablespaceField, BorderLayout.CENTER);
		bottomPanel.add(dedicated, BorderLayout.WEST);
		bottomPanel.add(selectPanel, BorderLayout.CENTER);
		topPanel.add(common, BorderLayout.WEST);
		topPanel.add(commonLabel, BorderLayout.CENTER);
		setLayout(new GridLayout(2, 1, 5, 5));
		add(topPanel);
		add(bottomPanel);
	}

	private void fillLocalizedStrings() {
		commonLabel.setText(localizer.getValue(KEY_USE_COMMON));
		dedicatedLabel.setText(localizer.getValue(KEY_SELECT));
	}
}

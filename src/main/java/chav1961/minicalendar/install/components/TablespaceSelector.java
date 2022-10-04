package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

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
	private static final String			KEY_COMMON = "TablespaceSelector.button.usecommon";
	private static final String			KEY_COMMON_TOOLTIP = "TablespaceSelector.button.usecommon.tooltip";
	private static final String			KEY_DEDICATED = "TablespaceSelector.button.dedicated";
	private static final String			KEY_DEDICATED_TOOLTIP = "TablespaceSelector.button.dedicated.tooltip";

	private final Localizer				localizer;
	private final ButtonGroup			group = new ButtonGroup();
	private final JRadioButton			common = new JRadioButton();
	private final JRadioButton			dedicated = new JRadioButton();
	private final JLabel				commonLabel = new JLabel();
	private final JLabel				dedicatedLabel = new JLabel();
	private final JComboBox<String>		tablespaceField = new JComboBox<>();
	
	private String						currentTablespace = "public"; 
	private boolean 					requestSelected = false;
	
	public TablespaceSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			
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

	public void fillTableSpaces(final List<String> tableSpaces) {
		if (tableSpaces == null) {
			throw new NullPointerException("Tablespaces can't be null");
		}
		else {
			tablespaceField.removeAllItems();
			for (String item : tableSpaces) {
				tablespaceField.addItem(item);
			}
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
			tablespaceField.setSelectedItem(newTablespace);
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
		final JPanel	selectPanel = new JPanel(new BorderLayout(5, 5));
		final JPanel	topPanel = new JPanel(new BorderLayout(5, 5));
		final JPanel	bottomPanel = new JPanel(new BorderLayout(5, 5));

		group.add(common);
		group.add(dedicated);
		
		selectPanel.add(dedicatedLabel, BorderLayout.WEST);
		selectPanel.add(tablespaceField, BorderLayout.CENTER);
		topPanel.add(common, BorderLayout.WEST);
		topPanel.add(commonLabel, BorderLayout.CENTER);
		bottomPanel.add(dedicated, BorderLayout.WEST);
		bottomPanel.add(selectPanel, BorderLayout.CENTER);
		setLayout(new GridLayout(2, 1, 5, 5));
		add(topPanel);
		add(bottomPanel);
	}

	private void fillLocalizedStrings() {
		commonLabel.setText(localizer.getValue(KEY_COMMON));
		common.setToolTipText(localizer.getValue(KEY_COMMON_TOOLTIP));
		dedicatedLabel.setText(localizer.getValue(KEY_DEDICATED));
		dedicated.setToolTipText(localizer.getValue(KEY_DEDICATED_TOOLTIP));
	}
}

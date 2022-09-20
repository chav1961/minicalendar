package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URI;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.JFileFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class WorkingDirectorySelector extends JPanel implements LocaleChangeListener {
	private static final long 			serialVersionUID = 1L;
	public static final String			KEY_LABEL = "WorkingDirectorySelector.file.label";
	public static final String			KEY_TOOLTIP = "WorkingDirectorySelector.file.tooltip";
	public static final String			KEY_HELP = "WorkingDirectorySelector.file.help";
	public static final String			KEY_BORDER_CAPTION = "WorkingDirectorySelector.file.available.border.caption";
	public static final String			KEY_APPROX_SIZE = "WorkingDirectorySelector.file.sizeapprox";
	public static final String			KEY_SPACE_ROOT = "WorkingDirectorySelector.file.space.root";
	public static final String			KEY_SPACE_TOTAL = "WorkingDirectorySelector.file.space.total";
	public static final String			KEY_SPACE_AVAILABLE = "WorkingDirectorySelector.file.space.available";
	

	private final Localizer				localizer;
	private final int					sizeApprox;
	private final ContentNodeMetadata	fileMeta;
	private final JLabel				fileLabel = new JLabel();
	private final JFileFieldWithMeta	fileField;
	private final TitledBorder			spaceBorder = new TitledBorder("");
	private final JTable				space;
	private final JLabel				requestLabel = new JLabel();
	private File						currentFile = new File("./");
	
	public WorkingDirectorySelector(final Localizer localizer, final int sizeApprox) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (sizeApprox <= 0) {
			throw new IllegalArgumentException("Approx size must be positive");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.sizeApprox = sizeApprox;
			this.fileMeta = new MutableContentNodeMetadata("file", File.class, "file", localizerUri, KEY_LABEL, KEY_TOOLTIP, KEY_HELP, new FieldFormat(File.class, "ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			
			try{
				this.fileField = (JFileFieldWithMeta)SwingUtils.prepareRenderer(fileMeta, localizer, FieldFormat.ContentType.FileContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(currentFile);
															break;
														case Validation	:
															currentFile = (File)component.getChangedValueFromComponent();
															
															if (currentFile.isFile()) {
																SwingUtils.getNearestLogger(WorkingDirectorySelector.this).message(Severity.warning, "Entity must be a directory, not a file");
																return false;
															}
															else if (!currentFile.canRead() || !currentFile.canWrite()) {
																SwingUtils.getNearestLogger(WorkingDirectorySelector.this).message(Severity.warning, "Entity is not accessible for you due to security restrictions");
																return false;
															}
															else {
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
			setLayout(new BorderLayout(5,5));
			
			final JPanel 	panel = new JPanel(new BorderLayout(5,5));
			
			panel.add(fileLabel, BorderLayout.WEST);
			panel.add(fileField, BorderLayout.CENTER);
			
			this.space = new JTable(new SpaceTableModel(localizer));
			final JScrollPane	pane = new JScrollPane(this.space); 
			
			pane.setFocusable(false);
			pane.setBorder(spaceBorder);
			
			add(panel, BorderLayout.NORTH);
			add(pane, BorderLayout.CENTER);
			add(requestLabel, BorderLayout.SOUTH);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
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
	
	private void fillLocalizedStrings() {
		fileLabel.setText(localizer.getValue(fileMeta.getLabelId()));
		spaceBorder.setTitle(localizer.getValue(KEY_BORDER_CAPTION));
		requestLabel.setText(String.format(localizer.getValue(KEY_APPROX_SIZE), sizeApprox));
	}
	
	private static class SpaceTableModel extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		private static final long	MBYTE = 1024 * 1024; 
		
		private final Localizer	localizer;
		
		private SpaceTableModel(final Localizer localizer) {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else {
				this.localizer = localizer;
			}
		}
		
		
		@Override
		public int getRowCount() {
			return File.listRoots().length;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0	: return localizer.getValue(KEY_SPACE_ROOT);
				case 1	: return localizer.getValue(KEY_SPACE_TOTAL);
				case 2	: return localizer.getValue(KEY_SPACE_AVAILABLE);
				default : throw new UnsupportedOperationException("Column index["+columnIndex+"] is not supported yet");
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0	: return String.class;
				case 1	: return Number.class;
				case 2	: return Number.class;
				default : throw new UnsupportedOperationException("Column index["+columnIndex+"] is not supported yet");
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0	: return File.listRoots()[rowIndex].getAbsolutePath();
				case 1	: return File.listRoots()[rowIndex].getTotalSpace() / MBYTE;
				case 2	: return File.listRoots()[rowIndex].getFreeSpace() / MBYTE;
				default : throw new UnsupportedOperationException("Column index["+columnIndex+"] is not supported yet");
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		}
	}
}

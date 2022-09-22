package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class ParametersView extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long serialVersionUID = 1L;
	
	private final Localizer	localizer; 
	private final JTable	content = new JTable();

	public ParametersView(final Localizer localizer) {
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
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		((DefaultTableModel)content.getModel()).fireTableStructureChanged();
	}

	public void setParameters(final InstallationDescriptor desc) {
		content.setModel(new ParametersTableModel(localizer, desc));
	}
	
	@Override
	public void allowUnnamedModuleAccess(Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	private void buildScreen() {
		final JScrollPane	pane = new JScrollPane(content); 
		
		pane.setPreferredSize(new Dimension(450,200));
		add(pane);
	}
	
	private void fillLocalizedStrings() {
	}
}

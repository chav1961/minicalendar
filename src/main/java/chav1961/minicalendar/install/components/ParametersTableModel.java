package chav1961.minicalendar.install.components;

import javax.swing.table.DefaultTableModel;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.purelib.i18n.interfaces.Localizer;

public class ParametersTableModel extends DefaultTableModel {
	private static final long 		serialVersionUID = 1L;
	private static final String		KEY_COL_NAME = "ParametersTableModel.column.name";
	private static final String		KEY_COL_VALUE = "ParametersTableModel.column.value";
	private static final String		KEY_ROW_WORKDIR = "ParametersTableModel.row.workdir";
	private static final String		KEY_ROW_JDBC_DRIVER = "ParametersTableModel.row.jdbcdriver";
	private static final String		KEY_ROW_CONN_STRING = "ParametersTableModel.row.connstring";
	private static final String		KEY_ROW_ADMIN = "ParametersTableModel.row.admin";
	private static final String		KEY_ROW_USER = "ParametersTableModel.row.user";
	private static final String		KEY_ROW_TABLESPACE = "ParametersTableModel.row.tablespace";
	private static final String		KEY_ROW_SERVICE = "ParametersTableModel.row.connstring";
	private static final String		KEY_VALUE_DEFAULT = "ParametersTableModel.row.value.default";
	private static final String		KEY_VALUE_NOT_TYPED = "ParametersTableModel.row.value.nottyped";
	
	private final Localizer					localizer;
	private final InstallationDescriptor	desc;
	
	public ParametersTableModel(final Localizer localizer, final InstallationDescriptor desc) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (desc == null) {
			throw new NullPointerException("Descriptor can't be null");
		}
		else {
			this.localizer = localizer;
			this.desc = desc;
		}
	}

	@Override
	public int getRowCount() {
		return 7;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(final int columnIndex) {
		switch (columnIndex) {
			case 0	: return localizer.getValue(KEY_COL_NAME);
			case 1	: return localizer.getValue(KEY_COL_VALUE);
			default : return null;
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		switch (columnIndex) {
			case 0 :
				switch (rowIndex) {
					case 0	: return localizer.getValue(KEY_ROW_WORKDIR);
					case 1	: return localizer.getValue(KEY_ROW_JDBC_DRIVER);
					case 2	: return localizer.getValue(KEY_ROW_CONN_STRING);
					case 3	: return localizer.getValue(KEY_ROW_ADMIN);
					case 4	: return localizer.getValue(KEY_ROW_USER);
					case 5	: return localizer.getValue(KEY_ROW_TABLESPACE);
					case 6	: return localizer.getValue(KEY_ROW_SERVICE);
					default : return null;
				}
			case 1 :
				switch (rowIndex) {
					case 0	: return desc.workDir.getAbsolutePath();
					case 1	: return desc.jdbcSelected ? desc.jdbcDriver.getAbsolutePath() : localizer.getValue(KEY_VALUE_DEFAULT);
					case 2	: return desc.connString;
					case 3	: return desc.admin;
					case 4	: return desc.user;
					case 5	: return desc.tableSpaceSelected ? desc.tableSpace : localizer.getValue(KEY_VALUE_NOT_TYPED);
					case 6	: return desc.serviceName;
					default : return null;
				}
			default : 
				return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}
}

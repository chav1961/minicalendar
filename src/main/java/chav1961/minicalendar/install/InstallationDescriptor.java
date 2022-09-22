package chav1961.minicalendar.install;

import java.io.File;

import chav1961.minicalendar.interfaces.InstallMode;
import chav1961.purelib.basic.interfaces.ModuleAccessor;

public class InstallationDescriptor implements ModuleAccessor {
	public InstallMode	mode = null;
	public File			workDir = new File("./");
	public boolean		jdbcSelected = false;
	public File			jdbcDriver = new File("./");
	public String		connString = "jdbc:postgresql://localhost:5432/postgres";
	public String		admin = "test";
	public char[]		adminPassword = "test".toCharArray();
	public boolean		tableSpaceSelected = false;
	public String		tableSpace = "";
	public String		user = "user";
	public char[]		userPassword = new char[] {'?'};
	public String		serviceName = "minicalendar";

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}

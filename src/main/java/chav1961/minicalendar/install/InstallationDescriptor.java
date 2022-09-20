package chav1961.minicalendar.install;

import java.io.File;

import chav1961.minicalendar.interfaces.InstallMode;
import chav1961.purelib.basic.interfaces.ModuleAccessor;

public class InstallationDescriptor implements ModuleAccessor {
	public InstallMode	mode = null;
	public File			workDir = new File("./");



	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}

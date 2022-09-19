package chav1961.minicalendar.service;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.nanoservice.interfaces.RootPath;

@RootPath("/content")
public class RequestEngine implements ModuleAccessor, AutoCloseable, LoggerFacadeOwner, LocalizerOwner, NodeMetadataOwner {

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer getLocalizer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoggerFacade getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void allowUnnamedModuleAccess(Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
}

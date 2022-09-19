package chav1961.minicalendar.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.SQLException;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.nanoservice.interfaces.Path;
import chav1961.purelib.nanoservice.interfaces.RootPath;
import chav1961.purelib.nanoservice.interfaces.ToBody;

@RootPath("/content")
public class RequestEngine implements ModuleAccessor, AutoCloseable, LoggerFacadeOwner, LocalizerOwner, NodeMetadataOwner {
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentNodeMetadata		model;
	private final SubstitutableProperties	props;
	
	
	public RequestEngine(final ContentNodeMetadata model, final Localizer localizer, final LoggerFacade logger, final SubstitutableProperties properties) throws IOException, ContentException, SQLException {
		if (model == null) {
			throw new NullPointerException("Model can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (properties == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			this.model = model;
			this.localizer = localizer;
			this.logger = logger;
			this.props = properties;
			
		}
	}
	
	
	@Path("/alerts")
	public int alerts(@ToBody(mimeType="text/html") final Writer wr) throws IOException {
		printStartPage(wr);
//		wr.write("<form action=\"./search\" method=\"GET\" class=\"" + ResponseFormatter.SNIPPET_SEARCH_FORM_CLASS + "\" accept-charset=\"UTF-8\">\n");
//		wr.write("<p>" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_LABEL) + ": ");
//		wr.write("<input type=\"search\" id=\"query\" name=\"query\" placeholder=\"" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_PLACEHOLDER) + "\" size=\"40\">\n");
//		wr.write("<input type=\"submit\" value=\"" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_SEARCH) + "\">\n");
//		wr.write("</p>\n</form>\n");
//		wr.write("<hr/>\n");
		printEndPage(wr);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	

	@Path("/manage")
	public int manage(@ToBody(mimeType="text/html") final Writer wr) throws IOException {
		printStartPage(wr);
//		wr.write("<form action=\"./search\" method=\"GET\" class=\"" + ResponseFormatter.SNIPPET_SEARCH_FORM_CLASS + "\" accept-charset=\"UTF-8\">\n");
//		wr.write("<p>" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_LABEL) + ": ");
//		wr.write("<input type=\"search\" id=\"query\" name=\"query\" placeholder=\"" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_PLACEHOLDER) + "\" size=\"40\">\n");
//		wr.write("<input type=\"submit\" value=\"" + getLocalizer().getValue(ResponseFormatter.SNIPPET_QUERY_SEARCH) + "\">\n");
//		wr.write("</p>\n</form>\n");
//		wr.write("<hr/>\n");
		printEndPage(wr);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	

	
	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return model;
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
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

	private void printStartPage(final Writer wr) {
		// TODO Auto-generated method stub
		
	}

	private void printEndPage(final Writer wr) {
		// TODO Auto-generated method stub
		
	}
}

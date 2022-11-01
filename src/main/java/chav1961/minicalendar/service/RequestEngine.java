package chav1961.minicalendar.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import chav1961.minicalendar.Application;
import chav1961.minicalendar.database.DatabaseWrapper;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.HttpUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.nanoservice.interfaces.FromBody;
import chav1961.purelib.nanoservice.interfaces.FromHeader;
import chav1961.purelib.nanoservice.interfaces.FromQuery;
import chav1961.purelib.nanoservice.interfaces.MultipartContent;
import chav1961.purelib.nanoservice.interfaces.Path;
import chav1961.purelib.nanoservice.interfaces.QueryType;
import chav1961.purelib.nanoservice.interfaces.RootPath;
import chav1961.purelib.nanoservice.interfaces.ToBody;
import chav1961.purelib.sql.JDBCUtils;
import chav1961.purelib.sql.ResultSetSubstitutionSource;
import chav1961.purelib.streams.MultipartEntry;
import chav1961.purelib.streams.byte2byte.MultipartInputStream;
import chav1961.purelib.streams.char2char.SubstitutableWriter;

@RootPath("/content")
public class RequestEngine implements ModuleAccessor, AutoCloseable, LoggerFacadeOwner, LocalizerOwner, NodeMetadataOwner {
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentNodeMetadata		model;
	private final SubstitutableProperties	props;
	private final SimpleURLClassLoader		loader;
	private final Driver					driver;
	private final Connection				conn;
	private final DatabaseWrapper			dbw;
	private final boolean					dontCreateUsers;
	
	public RequestEngine(final ContentNodeMetadata model, final Localizer localizer, final LoggerFacade logger, final SubstitutableProperties properties, final boolean dontCreateUsers) throws IOException, ContentException, SQLException {
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
			this.dontCreateUsers = dontCreateUsers;
			
			this.loader = new SimpleURLClassLoader(new URL[] {});
			this.driver = JDBCUtils.loadJdbcDriver(this.loader, props.getProperty(Application.PROP_JDBC_DRIVER, File.class));
			this.conn = JDBCUtils.getConnection(driver, props.getProperty(Application.PROP_JDBC_CONN_STRING, URI.class), props.getProperty(Application.PROP_JDBC_USER), props.getProperty(Application.PROP_JDBC_PASSWORD, char[].class));
			this.dbw = new DatabaseWrapper(conn);
			this.conn.setSchema("minical");
			
//			if (!dontCreateUsers) {
//				try(final PreparedStatement	ps = conn.prepareStatement("", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
//					ps.setString(1, System.getProperty("user.name"));
//					
//					try(final ResultSet		rs = ps.executeQuery()) {
//						if () {
//							
//						}
//					}
//				}
//			}
			
		}
	}
	
	@Path("/login")
	public int login(@FromHeader("Accept-Language") final String lang, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages[]	langs = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru);

		printStartPage(wr, langs[0]);
		printContent("userlogon.html", langs[0], (s)->s, wr);
		printEndPage(wr, langs[0]);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	
	
	@Path("/notificationtypes")
	public int notificationTypes(@FromHeader("Accept-Language") final String lang, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];

		printStartPage(wr, sLang);
		printContent("notificationtypes_header.html", sLang, (s)->s, wr);
		try(final ResultSet	rs = dbw.getNotificationTypes()) {
			printResultSetContent(rs, "notificationtypes_line.html", sLang, wr);
		} catch (SQLException e) {
			throw new IOException(e); 
		}
		printContent("notificationtypes_footer.html", sLang, (s)->s, wr);
		printEndPage(wr, sLang);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	

	@Path("/events")
	public int events(@FromQuery("month") String month, @FromHeader("Accept-Language") final String lang, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];
		final Calendar				currentDate = Calendar.getInstance();

		currentDate.setTimeInMillis(System.currentTimeMillis());
		
		printStartPage(wr, sLang);
		
		printContent("events_header.html", sLang, (s)->s, wr);
		try(final ResultSet	rs = dbw.getEventList(100)) {
			printResultSetContent(rs, "events_line.html", sLang, wr);
		} catch (SQLException e) {
			throw new IOException(e); 
		}
		printContent("events_footer.html", sLang, (s)->s, wr);
		
		printEndPage(wr, sLang);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	
	
	@Path(value="/events/insert",type={QueryType.POST})
	public int insertEvent(@FromQuery("month") String month, @FromHeader("Accept-Language") final String lang, @FromBody(mimeType="multipart/form-data") final InputStream is, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];
		final Calendar				currentDate = Calendar.getInstance();
		final MultipartInputStream	mis = new MultipartInputStream(is);
		String						insertText = "";
		byte[]						insertFile = new byte[0];
		
		MultipartEntry				me;

		while((me = mis.getNextEntry()) != null) {
			switch (me.getName()) {
				case "insert"		:
					break;
				case "insertText"	:
					insertText = extractTextContent(mis);
					break;
				case "insertFile"	:
					insertFile = extractFileContent(mis);
					break;
				default :
					break;
			}
		}
		System.err.println("text="+insertText);
		System.err.println("content="+insertFile.length);
		
//		final String ss = Utils.fromResource(new InputStreamReader(rdr));
		
		currentDate.setTimeInMillis(System.currentTimeMillis());
		
		printStartPage(wr, sLang);
		
		printContent("events_header.html", sLang, (s)->s, wr);
		try(final ResultSet	rs = dbw.getEventList(100)) {
			printResultSetContent(rs, "events_line.html", sLang, wr);
		} catch (SQLException e) {
			throw new IOException(e); 
		}
		printContent("events_footer.html", sLang, (s)->s, wr);
		
		printEndPage(wr, sLang);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	

	
	@Path("/alerts")
	public int alerts(@FromHeader("Accept-Language") final String lang, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];

		printStartPage(wr, sLang);
		printContent("alerts_header.html", sLang, (s)->s, wr);
		try(final ResultSet	rs = dbw.getEvents(100)) {
			printResultSetContent(rs, "alerts_line.html", sLang, wr);
		} catch (SQLException e) {
			throw new IOException(e); 
		}
		printContent("alerts_footer.html", sLang, (s)->s, wr);
		printEndPage(wr, sLang);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	

	@Path("/manage")
	public int manage(@FromHeader("Accept-Language") final String lang, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];

		printStartPage(wr, sLang);
		printEndPage(wr, sLang);
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
	public void close() throws IOException, SQLException {
		dbw.close();
		conn.close();
		loader.close();
	}

	private void printStartPage(final Writer wr, final SupportedLanguages lang) throws IOException {
		printContent("startpage.html", lang, (s)->s, wr);
	}

	
	private void printResultSetContent(final ResultSet rs, final String content, final SupportedLanguages langs, final Writer wr) throws IOException, SQLException {
		final SubstitutionSource	ss = new ResultSetSubstitutionSource(rs);
	
		while (rs.next()) {
			printContent(content, langs, ss, wr);
		}
	}

	private void printResultSetContent(final ResultSet rs, final String[] content, final SupportedLanguages langs, final Writer wr) throws IOException, SQLException {
		final SubstitutionSource	ss = new ResultSetSubstitutionSource(rs);
		int		count = 0;
	
		while (rs.next()) {
			printContent(content[count], langs, ss, wr);
			count = (count + 1) % content.length;
		}
	}
	
	private void printContent(final String content, final SupportedLanguages lang, final SubstitutionSource source, final Writer wr) throws IOException {
		try(final InputStream	is = URI.create("root://"+getClass().getCanonicalName()+"/templates/"+lang.name()+"/"+content).toURL().openStream();
			final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {

			final SubstitutableWriter	swr = new SubstitutableWriter(wr, source);
			Utils.copyStream(rdr, swr);
		}
	}
	
	private void printEndPage(final Writer wr, final SupportedLanguages lang) throws IOException {
		printContent("endpage.html", lang, (s)->s, wr);
	}
	
	private String extractTextContent(final InputStream is) throws IOException {
		final StringWriter	wr = new StringWriter();
		
		Utils.copyStream(new InputStreamReader(is), wr);
		return wr.toString();
	}

	private byte[] extractFileContent(final InputStream is) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			return baos.toByteArray();
		}
	}
}

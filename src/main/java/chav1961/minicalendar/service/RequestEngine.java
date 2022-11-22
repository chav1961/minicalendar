package chav1961.minicalendar.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import chav1961.minicalendar.Application;
import chav1961.minicalendar.database.Attachments;
import chav1961.minicalendar.database.DatabaseWrapper;
import chav1961.minicalendar.database.Events;
import chav1961.minicalendar.database.NotificationTypes;
import chav1961.minicalendar.database.Users;
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
	private static final String[]			DAYS_OF_WEEK = {"week.sunday", "week.monday", "week.tuesday", "week.wednesday", "week.thursday", "week.friday", "week.saturday"};
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentNodeMetadata		model;
	private final SubstitutableProperties	props;
	private final SimpleURLClassLoader		loader;
	private final Driver					driver;
	private final Connection				conn;
	private final DatabaseWrapper			dbw;
	private final boolean					dontCreateUsers;
	private final Proxy						httpsProxy;
	
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
			this.conn.setAutoCommit(false);
			
			final String 	proxy = System.getenv("HTTPS_PROXY");
			if (proxy != null && !proxy.isEmpty()) {
				final URI	proxyURI = URI.create(proxy);
				
				this.httpsProxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyURI.getHost(), proxyURI.getPort()));
			}
			else {
				this.httpsProxy = null;
			}
			
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
		final Calendar				currentDate = Calendar.getInstance(Locale.getDefault());
		final int					startDay = currentDate.getActualMinimum(Calendar.DAY_OF_MONTH), endDay = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);   
		int 						celebrates = getDayOffState(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH)+1);

		currentDate.setTimeInMillis(System.currentTimeMillis());
		
		currentDate.set(Calendar.DAY_OF_MONTH, 1);
		int		startMonthDay = currentDate.get(Calendar.DAY_OF_WEEK), startFrom = 1;
		
		while (startMonthDay > 1) {
			startFrom--;
			startMonthDay--;
		}
		
		printStartPage(wr, sLang);
		printContent("events_cal_start.html", sLang, (s)->s, wr);
		for (int row = 0; row < 5; row++) {
			printContent("events_cal_start_line.html", sLang, (s)->s, wr);
			for (int col = 0; col < 7; col++) {
				final int currentCol = col, currentDay = startFrom;
				
				if (startFrom >= startDay && startFrom <= endDay) {
					final boolean	isRed = (celebrates & 0x01) != 0; 	
					printContent("events_cal_cell.html", sLang, (s)->{
						switch (s) {
							case "dayOfMonth" :
								return ""+currentDay;
							case "dayOfWeek" :
								return getLocalizer().getValue(DAYS_OF_WEEK[currentCol]);
							case "celebratedClass" :
								return isRed ? "celebrated" : "ordinal";
							case "eventList" :
								try(final Writer	nested = new StringWriter()) {
									printContent("events_cal_cell_header.html", sLang, (sn)->sn, nested);
									
									printContent("events_cal_cell_footer.html", sLang, (sn)->sn, nested);
									nested.flush();
									return nested.toString();
								} catch (IOException e) {
									return "Content error";
								}
							default :
								return "?";
						}
					}, wr);
					celebrates >>= 1;
				}
				else {
					printContent("events_cal_empty_cell.html", sLang, (s)->s, wr);
				}
				startFrom++;
			}
			printContent("events_cal_end_line.html", sLang, (s)->s, wr);
		}
		printContent("events_cal_end.html", sLang, (s)->s, wr);
		printEndPage(wr, sLang);
		wr.flush();
		return HttpURLConnection.HTTP_OK;
	}	
	
	@Path(value="/events/insert",type={QueryType.POST})
	public int insertEvent(@FromQuery("month") String month, @FromHeader("Accept-Language") final String lang, @FromBody(mimeType="multipart/form-data") final InputStream is, @ToBody(mimeType="text/html") final Writer wr) throws IOException {
		final SupportedLanguages	sLang = HttpUtils.extractSupportedLanguages(lang, SupportedLanguages.ru)[0];
		final long					currentTime = System.currentTimeMillis();
		final Calendar				currentDate = Calendar.getInstance();
		final MultipartInputStream	mis = new MultipartInputStream(is);
		String						insertText = "", insertFileName = "";
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
					insertFileName = me.getProperty("filename");
					insertFile = extractFileContent(mis);
					break;
				default :
					break;
			}
		}
//		System.err.println("text="+insertText);
//		System.err.println("content="+insertFile.length);
		
		try{final Events			ev = new Events();
			final Users				us = new Users();
			final NotificationTypes	nt = new NotificationTypes(); 
			final Timestamp			ts = new Timestamp(currentTime); 
		
			us.us_Id = DatabaseWrapper.userId;
			nt.nt_Id = dbw.getId();
			
			ev.ev_Id = dbw.getId();
			ev.us_Id = us;
			ev.ev_Created = ts;
			ev.ev_CronMask = "* * * * *";
			ev.nt_Id = nt;
			ev.ev_NotifyBefore = ts;
			ev.ev_NotifyAfter = ts;
			ev.ev_StartFrom = ts;
			ev.ev_ExpectedTo = ts;
			ev.ev_EventType = "event";
			ev.ev_Comment = insertText;
		
			dbw.insertEvent(ev);
			
			if (insertFile.length > 0) {
				final Attachments	ats = new Attachments();
				
				ats.at_Id = dbw.getId();
				ats.ev_Id = ev;
				ats.at_Type = "???";
				ats.at_Reference = insertFileName;
				ats.at_Content = insertFile;
			
				dbw.insertAttachment(ats);
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try{conn.rollback();
			} catch (SQLException e1) {
			}
			return HttpURLConnection.HTTP_INTERNAL_ERROR;
		}
		
		currentDate.setTimeInMillis(currentTime);
		
//		printEventsPage(wr, sLang);
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
	
//	private void printEventsPage(final Writer wr, final SupportedLanguages sLang) throws IOException {
//		printStartPage(wr, sLang);
//		
//		printContent("events_header.html", sLang, (s)->s, wr);
//		try(final ResultSet	rs = dbw.getEventList(DatabaseWrapper.userId)) {
//			printResultSetContent(rs, "events_line.html", sLang, wr);
//		} catch (SQLException e) {
//			throw new IOException(e); 
//		}
//		printContent("events_footer.html", sLang, (s)->s, wr);
//		
//		printEndPage(wr, sLang);
//		wr.flush();
//	}
	
	private int getDayOffState(final int year, final int month) throws IOException {
		final URL					dayOffURL = URI.create(String.format("https://isdayoff.ru/api/getdata?year=%1$4d&month=%2$2d&delimeter=%%0D", year, month)).toURL();
		final HttpsURLConnection	conn = httpsProxy != null ? (HttpsURLConnection)dayOffURL.openConnection(httpsProxy) : (HttpsURLConnection)dayOffURL.openConnection();
		int							result = 0, marker = 1;
		
		try(final InputStream		is = conn.getInputStream();
			final Reader			rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				final int	value = Integer.valueOf(line.trim());
				
				if (value == 1) {
					result |= marker;
				}
				marker <<= 1;
			}
		}
		return result;
	}
}

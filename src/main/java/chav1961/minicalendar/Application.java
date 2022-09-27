package chav1961.minicalendar;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Timer;

import javax.swing.JPopupMenu;

import chav1961.minicalendar.service.RequestEngine;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JSystemTray;

public class Application {
	public static final String	APP_NAME = "application.name";
	public static final String	APP_TOOLTIP = "application.tooltip";

	public static final String	ARG_PORT = "port";
	public static final int		ARG_PORT_DEFAULT = 23109;
	public static final String	ARG_PROPFILE_LOCATION = "prop";
	public static final String	ARG_PROPFILE_LOCATION_DEFAULT = "./.minicalendar.properties";
	
	public static final String	PATH_CONTENT = "/content";

	public static final String	PROP_JDBC_DRIVER = "jdbcDriver";
	public static final String	PROP_JDBC_CONN_STRING = "jdbcConnString";
	public static final String	PROP_JDBC_USER = "jdbcUser";
	public static final String	PROP_JDBC_PASSWORD = "jdbcPassword";
	
	
	private static final Timer			timer = new Timer(true);
	private static JSystemTray			tray;
	private static NanoServiceFactory	factory;
	private static LocaleChangeListener	lcl;
	private static Maintenance			maint;
	private static RequestEngine		engine;
	
	public static void main(final String[] args) {
		try{final ArgParser						parser = new ApplicationArgParser().parse(args);
			final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
														 NanoServiceFactory.NANOSERVICE_PORT, parser.getValue(ARG_PORT, String.class)
														,NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":xmlReadOnly:root://"+Application.class.getCanonicalName()+"/chav1961/minicalendar/helptree.xml"
														,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
														,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
													));
		
			try(final InputStream				is = URI.create("root://"+Application.class.getCanonicalName()+"/chav1961/minicalendar/model/application.xml").toURL().openStream();
				final InputStream				dbIs = Application.class.getResourceAsStream("model.json");
				final Reader					dbRdr = new InputStreamReader(dbIs, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final ContentMetadataInterface	dbModel = ContentModelFactory.forJsonDescription(dbRdr);
				
				final Localizer					localizer = LocalizerFactory.getLocalizer(xda.getRoot().getLocalizerAssociated());
				final JPopupMenu				trayMenu = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.traymenu")), JPopupMenu.class);
				final SubstitutableProperties	appProps = SubstitutableProperties.of(parser.getValue(ARG_PROPFILE_LOCATION, File.class));
				final int						portNumber = parser.getValue(ARG_PORT, int.class); 

				PureLibSettings.PURELIB_LOCALIZER.push(localizer);
				
				tray = new JSystemTray(localizer, APP_NAME, Application.class.getResource("tray.png").toURI(), APP_TOOLTIP, trayMenu, false);
				factory = new NanoServiceFactory(tray, props);
				engine = new RequestEngine(xda.getRoot(), localizer, tray, appProps);
			
				tray.addActionListener((e)->callBrowser(portNumber));
				lcl = (oldLocale,newLocale)->tray.localeChanged(oldLocale, newLocale);
				PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(lcl);
				maint = new Maintenance(appProps, dbModel.getRoot());

				factory.deploy(PATH_CONTENT, engine);				
				timer.schedule(maint, 30000, 30000);
				
				factory.start();
			} catch (URISyntaxException | EnvironmentException | IOException | ContentException | SQLException exc) {
				System.exit(128);			
			}
		} catch (CommandLineParametersException exc) {
			System.exit(129);			
		}
	}

	private static void callBrowser(final int portNumber) {
		if (Desktop.isDesktopSupported()) {
			try{Desktop.getDesktop().browse(URI.create("http://localhost:"+portNumber+"/static/index.html"));
			} catch (IOException e) {
				tray.message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		else {
			tray.message(Severity.error, "Desktop is not supported");
		}
	}

	public static void terminate(final String[] args) {
		maint.cancel();
		timer.purge();
		
		try{factory.stop();
			factory.undeploy(PATH_CONTENT);
		} catch (IOException exc) {
			tray.message(Severity.error, exc, exc.getLocalizedMessage());
		}
		tray.close();
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new IntegerArg(ARG_PORT, true, "Port to use for browser", ARG_PORT_DEFAULT),
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", ARG_PROPFILE_LOCATION_DEFAULT)
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
}

package chav1961.minicalendar.install.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.InstallationError;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.char2char.SubstitutableWriter;
import chav1961.purelib.ui.interfaces.ErrorProcessing;


// https://www.computerhope.com/issues/ch000848.htm
// http://www.rhd.ru/docs/manuals/enterprise/RHEL-4-Manual/desktop-guide/ch-intro-gconf-overview.html

public class PrepareRegistryAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_ACTION_NAME = "PrepareRegistryAction.actionname";
	
	private final Localizer				localizer;
	private final ProgressIndicatorImpl	pii;
	private final ErrorProcessing<InstallationDescriptor, InstallationError>	err;
	
	private State		state = State.UNPREPARED;
	
	public PrepareRegistryAction(final Localizer localizer, final ErrorProcessing<InstallationDescriptor, InstallationError> err) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (err == null) {
			throw new NullPointerException("Error processing can't be null");
		}
		else {
			this.localizer = localizer;
			this.pii = new ProgressIndicatorImpl(localizer);
			this.err = err;
		}
	}
	
	@Override
	public String getActionName() {
		return KEY_ACTION_NAME;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public ProgressIndicator getProgressIndicator() {
		return pii;
	}

	@Override
	public Class<?>[] getAncestors() {
		return new Class<?>[] {PrepareServiceAction.class};
	}

	@Override
	public void prepare(final LoggerFacade logger) throws Exception {
		state = State.AWAITING;
	}

	@Override
	public boolean execute(final LoggerFacade logger, final InstallationDescriptor content, final Object... parameters) throws Exception {
		switch (PureLibSettings.CURRENT_OS) {
			case LINUX		:
				state = State.FAILED;
				return false;
			case MACOS		:
				state = State.FAILED;
				return false;
			case UNKNOWN	:
				state = State.FAILED;
				return false;
			case WINDOWS	:
				return executeWindows(logger, content);
			default :
				throw new UnsupportedOperationException("OS ["+PureLibSettings.CURRENT_OS+"] is not supported yet"); 
		}
	}

	@Override
	public void markAsFailed() {
		state = State.FAILED;
	}

	@Override
	public void unprepare(final LoggerFacade logger) throws Exception {
		state = State.UNPREPARED;
	}

	private boolean executeWindows(final LoggerFacade logger, final InstallationDescriptor content) throws IOException {
		final File				tempDir = new File(System.getProperty("java.io.tmpdir"));
		final File				configFile = File.createTempFile("regedit", ".conf"); 
		
		fillConfigFile(configFile, content);
		
		final ProcessBuilder	pb = new ProcessBuilder("regedit", configFile.getName()).directory(tempDir).redirectError(Redirect.INHERIT);
		final Process			p = pb.start();
		final Thread			t = new Thread(()->{
									final StringBuilder			sb = new StringBuilder();
									
									try(final InputStream		is = p.getInputStream();
										final Reader			rdr = new InputStreamReader(is);
										final BufferedReader	brdr = new BufferedReader(rdr)) {
									
										String		line;
										
										while (!Thread.interrupted() && (line = brdr.readLine()) != null) {
											sb.append(line).append(System.lineSeparator());
										}
										logger.message(Severity.info, sb.toString());
									} catch (IOException e) {
										logger.message(Severity.error, sb.toString());
									}
								});
		t.setDaemon(true);
		t.start();

		try{p.waitFor();
			if (p.exitValue() == 0) {
				state = State.COMPLETED;
				return true;
			}
			else {
				state = State.FAILED;
				return false;
			}
		} catch (InterruptedException e) {
			t.interrupt();
			logger.message(Severity.error, "Installation thread cancelled");
			p.destroyForcibly();
			state = State.FAILED;
			return false;
		} finally {
			configFile.delete();
		}
	}
	
	private void fillConfigFile(final File configFile, final InstallationDescriptor content) throws IOException {
		final SubstitutableProperties	sp = new SubstitutableProperties();
		
		try(final InputStream			is = this.getClass().getResourceAsStream("addregistry.txt");
			final Reader				rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final OutputStream			os = new FileOutputStream(configFile);
			final Writer				wr = new OutputStreamWriter(os, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final SubstitutableWriter	swr = new SubstitutableWriter(wr, sp)) {
			
			Utils.copyStream(rdr, swr);			
		}
	}
}

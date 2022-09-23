package chav1961.minicalendar.install.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.minicalendar.install.InstallationError;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.CsvStaxParser;
import chav1961.purelib.streams.interfaces.CsvStaxParserLexType;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class CopyApplicationContentAction implements ActionInterface<InstallationDescriptor>{
	private static final String		KEY_ACTION_NAME = "CopyApplicationContentAction.actionname";
	private static final String		KEY_ACTION_COPY = "CopyApplicationContentAction.copy";
	
	private final Localizer				localizer;
	private final ProgressIndicatorImpl	pii;
	private final ErrorProcessing<InstallationDescriptor, InstallationError>	err;
	private final List<String[]>		copyPairs = new ArrayList<>();
	
	private State		state = State.UNPREPARED;
	
	public CopyApplicationContentAction(final Localizer localizer, final ErrorProcessing<InstallationDescriptor, InstallationError> err) {
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
		return new Class<?>[] {CleanDirectoryAction.class};
	}

	@Override
	public void prepare(final LoggerFacade logger) throws Exception {
		copyPairs.clear();
		
		try(final InputStream	is = this.getClass().getResourceAsStream("copylist.csv");
			final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final CsvStaxParser	parser = new CsvStaxParser(rdr, String.class, String.class, long.class)) {
			
			String[]	pair = null;
			int			count = 0;
			
			for (CsvStaxParserLexType item : parser) {
				switch (item) {
					case INTEGER_VALUE	:
						break;
					case STRING_VALUE	:
						if (count % 2 == 0) {
							pair = new String[] {parser.stringValue() , null};
						}
						else {
							pair[1] = parser.stringValue();
							copyPairs.add(pair);
						}
						count++;
						break;
					default:
						throw new IllegalArgumentException("Unsupported type ["+item+"] in the CSV content at line "+parser.row()+" col "+parser.col());
				}
				
			}
		}
		state = State.AWAITING;
	}

	@Override
	public boolean execute(final LoggerFacade logger, final InstallationDescriptor content, final Object... parameters) throws Exception {
		pii.start(KEY_ACTION_COPY, copyPairs.size());
		for (int index = 0; index < copyPairs.size(); index++) {
			final File	target = new File(content.workDir, copyPairs.get(index)[1]);
			
			target.getParentFile().mkdirs();
			try(final InputStream	is = URI.create(copyPairs.get(index)[1]).toURL().openStream();
				final OutputStream	os = new FileOutputStream(target)) {
				
				Utils.copyStream(is, os);
			}
			pii.processed(index);
		}
		pii.end();
		
		state = State.COMPLETED;
		return true;
	}

	@Override
	public void markAsFailed() {
		state = State.FAILED;
	}

	@Override
	public void unprepare(final LoggerFacade logger) throws Exception {
		copyPairs.clear();
		state = State.UNPREPARED;
	}
}

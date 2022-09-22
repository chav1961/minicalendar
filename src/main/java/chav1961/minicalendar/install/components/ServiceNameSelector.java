package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.JTextFieldWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class ServiceNameSelector extends JPanel implements LocaleChangeListener, ModuleAccessor {
	private static final long serialVersionUID = 1L;
	private static final String		KEY_SERVICE_LABEL = "ServiceNameSelector.service.label";
	private static final String		KEY_SERVICE_TOOLTIP = "ServiceNameSelector.service.tooltip";
	private static final String		KEY_SERVICE_HELP = "ServiceNameSelector.service.help";

	private final Localizer				localizer;
	private final ContentNodeMetadata	serviceMeta;
	private final JLabel				serviceLabel = new JLabel();
	private final JTextFieldWithMeta	serviceField;

	private String		service = "mincal";
	
	public ServiceNameSelector(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final URI	localizerUri = URI.create(Localizer.LOCALIZER_SCHEME+":xml:"+localizer.getLocalizerId());
			
			this.localizer = localizer;
			this.serviceMeta = new MutableContentNodeMetadata("service", String.class, "service", localizerUri, KEY_SERVICE_LABEL, KEY_SERVICE_TOOLTIP, KEY_SERVICE_HELP, new FieldFormat(String.class, "30ms"), URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"), null);
			
			try{
				this.serviceField = (JTextFieldWithMeta)SwingUtils.prepareRenderer(serviceMeta, localizer, FieldFormat.ContentType.StringContent, new JComponentMonitor() {
												@Override
												public boolean process(MonitorEvent event, ContentNodeMetadata metadata, JComponentInterface component, Object... parameters) throws ContentException {
													switch (event) {
														case Loading	:
															component.assignValueToComponent(service);
															break;
														case Validation	:
															final String	temp = ((String)component.getChangedValueFromComponent()).trim();
															
															if (temp.isEmpty()) {
																SwingUtils.getNearestLogger(ServiceNameSelector.this).message(Severity.warning, "Service can't be empty");
																return false;
															}
															else {
																service = temp;
																return true;
															}
														default:
															break;
													
													}
													return true;
												}
											});
			} catch (SyntaxException e) {
				throw new IllegalArgumentException(e); 
			}
			buildScreen();
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	public String getService() {
		return service;
	}
	
	public void setService(final String service) {
		this.service = service;
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}

	private void buildScreen() {
		final JPanel	panel = new JPanel(new BorderLayout(5, 5));
		
		panel.add(serviceLabel, BorderLayout.WEST);
		panel.add(serviceField, BorderLayout.CENTER);
		add(panel);
	}
	
	
	private void fillLocalizedStrings() {
		serviceLabel.setText(localizer.getValue(KEY_SERVICE_LABEL));
		serviceField.setToolTipText(localizer.getValue(KEY_SERVICE_TOOLTIP));
	}


}

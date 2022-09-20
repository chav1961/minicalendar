package chav1961.minicalendar.install.components;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class RadioButtonSelector extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = 1L;

	private final Localizer			localizer;
	private final JRadioButton[]	buttons;
	private final JLabel[]			labels;
	private final Icon[]			icons;
	private final String[]			options;
	private final String[]			tooltips;
	private final ButtonGroup		group = new ButtonGroup(); 
	
	
	public RadioButtonSelector(final Localizer localizer, final ContentNodeMetadata... items) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (items == null || items.length == 0 || Utils.checkArrayContent4Nulls(items) >= 0) {
			throw new IllegalArgumentException("Items are null, empty or contains nulls inside"); 
		}
		else {
			this.localizer = localizer;
			this.buttons = new JRadioButton[items.length];
			this.labels = new JLabel[items.length];
			this.icons = new Icon[items.length];
			this.options = new String[items.length];
			this.tooltips = new String[items.length];
			
			for (int index = 0; index < items.length; index++) {
				if (items[index].getIcon() != null) {
					try{icons[index] = new ImageIcon(items[index].getIcon().toURL());
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				options[index] = items[index].getLabelId();
				tooltips[index] = items[index].getTooltipId();
			}
			
			buildButtons();
		}
	}

	public RadioButtonSelector(final Localizer localizer, final IconTooltipAndOption... items) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (items == null || items.length == 0 || Utils.checkArrayContent4Nulls(items) >= 0) {
			throw new IllegalArgumentException("Items are null, empty or contains nulls inside"); 
		}
		else {
			this.localizer = localizer;
			this.buttons = new JRadioButton[items.length];
			this.labels = new JLabel[items.length];
			this.icons = new Icon[items.length];
			this.options = new String[items.length];
			this.tooltips = new String[items.length];

			for (int index = 0; index < items.length; index++) {
				icons[index] = items[index].icon;
				options[index] = items[index].option;
				tooltips[index] = items[index].tooltip;
			}
			
			buildButtons();
		}
	}
	
	public RadioButtonSelector(final Localizer localizer, final String... items) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (items == null || items.length == 0 || Utils.checkArrayContent4Nulls(items) >= 0) {
			throw new IllegalArgumentException("Items are null, empty or contains nulls inside"); 
		}
		else {
			this.localizer = localizer;
			this.buttons = new JRadioButton[items.length];
			this.labels = new JLabel[items.length];
			this.icons = new Icon[items.length];
			this.options = items.clone();
			this.tooltips = new String[items.length];
			
			buildButtons();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizationStrings();
	}
	
	public void setSelectionIndex(final int index) {
		if (index < 0 || index >= buttons.length) {
			throw new IllegalArgumentException("Index value ["+index+"] out of range 0.."+(buttons.length-1)); 
		}
		else {
			group.setSelected(buttons[index].getModel(), true);
		}
	}
	
	public int getSelectionIndex() {
		for (int index = 0; index < buttons.length; index++) {
			if (group.isSelected(buttons[index].getModel())) {
				return index;
			}
		}
		return -1;
	}

	private void buildButtons() {
		setLayout(new GridLayout(buttons.length, 1));
		
		for (int index = 0; index < buttons.length; index++) {
			final JRadioButton	button = new JRadioButton();
			final JLabel		label = icons[index] != null ? new JLabel("", icons[index], JLabel.LEFT) : new JLabel();
			final JPanel		panel = new JPanel(new BorderLayout(5, 5));
			
			buttons[index] = button;
			labels[index] = label;
			group.add(button);
			panel.add(button, BorderLayout.WEST);
			panel.add(label, BorderLayout.CENTER);
			add(panel);
		}
		fillLocalizationStrings();
	}

	
	private void fillLocalizationStrings() {
		for (int index = 0; index < labels.length; index++) {
			labels[index].setText(localizer.getValue(options[index]));
		}
		for (int index = 0; index < buttons.length; index++) {
			if (tooltips[index] != null) {
				buttons[index].setToolTipText(localizer.getValue(tooltips[index]));
			}
		}
	}
	
	public static class IconTooltipAndOption {
		private final Icon		icon;
		private final String	tooltip;
		private final String	option;

		public IconTooltipAndOption(final Icon icon, final String tooltip, final String option) {
			this.icon = icon;
			this.tooltip = tooltip;
			this.option = option;
		}
	}


}

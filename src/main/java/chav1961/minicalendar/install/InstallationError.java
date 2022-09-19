package chav1961.minicalendar.install;

public enum InstallationError {
	ERROR_1("");
	
	private final String	localizationKey;
	
	private InstallationError(final String localizationKey) {
		this.localizationKey = localizationKey;
	}
	
	public String getLocalizationKey() {
		return localizationKey;
	}
}

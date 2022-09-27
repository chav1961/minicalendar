package chav1961.minicalendar.database;

public class NotificationTypes {
	public long		nt_Id;
	public String	nt_HornType;
	public String	nt_MessageType;
	
	
	@Override
	public String toString() {
		return "NotificationTypes [nt_Id=" + nt_Id + ", nt_HornType=" + nt_HornType + ", nt_MessageType=" + nt_MessageType + "]";
	}
}

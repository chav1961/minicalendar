package chav1961.minicalendar.database;

import java.sql.Timestamp;

public class Users {
	public long				us_Id;
	public String			us_Name;
	public Timestamp		us_Created;
	public Timestamp		us_LastModified;
	public int				us_CanLogon;
	
	@Override
	public String toString() {
		return "Users [us_Id=" + us_Id + ", us_Name=" + us_Name + ", us_Created=" + us_Created + ", us_LastModified=" + us_LastModified + ", us_CanLogon=" + us_CanLogon + "]";
	}
}

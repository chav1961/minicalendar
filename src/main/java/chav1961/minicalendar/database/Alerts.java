package chav1961.minicalendar.database;

import java.sql.Timestamp;

public class Alerts {
	public long			al_Id;
	public long			ev_Id;
	public Timestamp	al_Created;
	public String		al_State;
	
	@Override
	public String toString() {
		return "Alerts [al_Id=" + al_Id + ", ev_Id=" + ev_Id + ", al_Created=" + al_Created + ", al_State=" + al_State + "]";
	}
}

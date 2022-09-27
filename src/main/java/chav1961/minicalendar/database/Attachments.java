package chav1961.minicalendar.database;

import java.util.Arrays;

public class Attachments {
	public long			at_Id;
	public Events		ev_Id;
	public String		at_Type;
	public String		at_Reference;
	public byte[]		at_Content;
	
	@Override
	public String toString() {
		return "Attachments [at_Id=" + at_Id + ", ev_Id=" + ev_Id + ", at_Type=" + at_Type + ", at_Reference="
				+ at_Reference + ", at_Content=" + Arrays.toString(at_Content) + "]";
	}
}

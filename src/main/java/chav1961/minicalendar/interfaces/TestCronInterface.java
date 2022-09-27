package chav1961.minicalendar.interfaces;

import java.util.Calendar;
import java.util.Date;

import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
public interface TestCronInterface {
	boolean test(final Calendar date);
}

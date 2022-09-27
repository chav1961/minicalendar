package chav1961.minicalendar.install;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import chav1961.purelib.basic.Utils;

public class InstallUtils {
	public static File extractDriverFile(final URL jdbcDriverURL) throws IOException {
		final File	temp = File.createTempFile("jdbc", ".jar");
		
		try(final InputStream	is = jdbcDriverURL.openStream();
			final OutputStream	os = new FileOutputStream(temp)) {
			
			Utils.copyStream(is, os);
		}
		temp.deleteOnExit();
		return temp;
	}
}

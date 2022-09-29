package chav1961.minicalendar.install.actions;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import chav1961.minicalendar.install.InstallationDescriptor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class CleanDirectoryActionTest {
	private final File	f = new File(System.getProperty("java.io.tmpdir"),"test");
	
	@Before
	public void prepare() throws IOException {
		f.mkdirs();
		File.createTempFile("test", ".txt", f);
	}

	@After
	public void unprepare() {
		Utils.deleteDir(f);
	}
	
	@Test
	public void test() throws Exception {
		final CleanDirectoryAction		cda = new CleanDirectoryAction(PureLibSettings.PURELIB_LOCALIZER);
		final InstallationDescriptor	desc = new InstallationDescriptor();

		try {new CleanDirectoryAction(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		desc.workDir = f;
		Assert.assertEquals(ActionInterface.State.UNPREPARED, cda.getState());
		
		cda.prepare(PureLibSettings.CURRENT_LOGGER);
		Assert.assertEquals(ActionInterface.State.AWAITING, cda.getState());

		try {cda.prepare(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertTrue(cda.execute(PureLibSettings.CURRENT_LOGGER, desc));
		Assert.assertEquals(ActionInterface.State.COMPLETED, cda.getState());
		
		try {cda.execute(null, desc);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {cda.execute(PureLibSettings.CURRENT_LOGGER, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		cda.markAsFailed();
		Assert.assertEquals(ActionInterface.State.FAILED, cda.getState());
		
		cda.unprepare(PureLibSettings.CURRENT_LOGGER);
		Assert.assertEquals(ActionInterface.State.UNPREPARED, cda.getState());
	}
}

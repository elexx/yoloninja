package tuwien.inso.mnsa.midlet.debug;

import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetProperties;

public class UidPrinter implements TargetListener {

	private static final Logger LOG = Logger.getLogger("UidPrinter");

	public void targetDetected(TargetProperties[] targetProperties) {
		for (int i = 0; i < targetProperties.length; i++) {
			LOG.print(targetProperties[i].getUid());
		}
	}

}

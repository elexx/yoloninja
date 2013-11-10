package tuwien.inso.mnsa.rxtxtcp;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;

public class PortFinder {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Enumeration<CommPortIdentifier> ports = (Enumeration<CommPortIdentifier>) CommPortIdentifier.getPortIdentifiers();

		System.out.println("Listing ports:");
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = ports.nextElement();

			System.out.println(port.getName() + " (" + port.getPortType() + ", current owner " + port.getCurrentOwner() + ")");
		}
		System.out.println("Port listing done.");
	}
}

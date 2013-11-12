package tuwien.inso.mnsa.midlet.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

import tuwien.inso.mnsa.midlet.Logger;
import tuwien.inso.mnsa.protocol.Message;

public class USBConnection {

	private static final Logger LOG = Logger.getLogger("Connection");

	private static final String COMMPORT_IDENTIFIER = "comm:USB1";

	private CommConnection connection = null;
	private InputStream inStream = null;
	private OutputStream outStream = null;

	private final boolean isActive = false;

	public final void startCommunication() {
		if (isActive) {
			LOG.print("got started twice - returning");
			return;
		}

		try {
			connection = openConnection();
			inStream = connection.openInputStream();
			outStream = connection.openOutputStream();
		} catch (IOException e) {
			LOG.print(e);
			return;
		}

		LOG.print("Listening USB port...");


		try {
			Message request = Message.createFrom(inStream);
			LOG.print(request.toString());
			//TODO to something
			Message response = Message.createFrom(Message.TYPE_TEST, (byte) 2, new byte[] { (byte) 0x00, (byte) 0x00 });
			response.write(outStream);
			outStream.flush();
		} catch (IOException e) {
			LOG.print(e);
			return;
		} finally {
			try {
				inStream.close();
			} catch (IOException ignored) {
			}
			try {
				outStream.close();
			} catch (IOException ignored) {
			}
			try {
				connection.close();
			} catch (IOException ignored) {
			}
		}

		LOG.print("SUCCEEDED.");
	}

	private CommConnection openConnection() throws IOException {
		String ports = System.getProperty("microedition.commports");

		int index = ports.indexOf("USB", 0);
		if (index == -1) {
			throw new RuntimeException("No USB port found in the device");
		}

		LOG.print("opening " + COMMPORT_IDENTIFIER);
		CommConnection connection = (CommConnection) Connector.open(COMMPORT_IDENTIFIER);
		return connection;
	}
}

package tuwien.inso.mnsa.midlet.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import tuwien.inso.mnsa.midlet.Logger;
import tuwien.inso.mnsa.protocol.Message;

public class USBConnection implements Runnable {

	private static final Logger LOG = Logger.getLogger("Connection");

	private static final String COMMPORT_IDENTIFIER = "comm:USB1";

	private final CardConnection cardConnection;
	private CommConnection commConnection;
	private InputStream inStream;
	private OutputStream outStream;

	private volatile boolean isRunning;
	private volatile boolean inCommunication;


	public USBConnection(CardConnection cardConnection) {
		this.cardConnection = cardConnection;
	}

	public void run() {
		isRunning = true;
		while (isRunning) {
			try {
				commConnection = openConnection();
				inStream = commConnection.openInputStream();
				outStream = commConnection.openOutputStream();
			} catch (IOException e) {
				LOG.print(e);
				return;
			}

			LOG.print("Listening USB port...");

			inCommunication = true;
			while (inCommunication) {
				try {
					Message request = Message.createFrom(inStream);
					Message response = null;
					LOG.print(request.toString());

					switch(request.getMessageType()) {
					case Message.TYPE_APDU_COMMAND:
						byte[] responsePayload = cardConnection.exchangeData(request.getPayload());
						response = Message.createFrom(Message.TYPE_APDU_RESPONSE, (byte) responsePayload.length, responsePayload);
						break;

					case Message.TYPE_TEST:
						LOG.print("Got TEST Message");
						response = request;
						break;

					default:
						LOG.print("Got unknown Message [0x" + Integer.toHexString(request.getMessageType() & 0xFF) + "]");
						break;
					}

					if (response != null) {
						LOG.print("answering with " + response.toString());
						response.write(outStream);
						outStream.flush();
					}
				} catch (IOException e) {
					LOG.print(e);
					closeSilently(inStream);
					closeSilently(outStream);
					closeSilently(commConnection);
					isRunning = false;
					inCommunication = false;
				} catch (ContactlessException e) {
					LOG.print(e);
				}
			}
		}

	}

	public void stop() {
		inCommunication = false;
		isRunning = false;
	}

	public void close() {
		if (!isRunning) {
			closeSilently(inStream);
			closeSilently(outStream);
			closeSilently(commConnection);
		}
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

	private static void closeSilently(InputStream stream) {
		try {
			stream.close();
		} catch (IOException ignored) {
		}
	}

	private static void closeSilently(OutputStream stream) {
		try {
			stream.close();
		} catch (IOException ignored) {
		}
	}

	private static void closeSilently(Connection connection) {
		try {
			connection.close();
		} catch (IOException ignored) {
		}
	}
}

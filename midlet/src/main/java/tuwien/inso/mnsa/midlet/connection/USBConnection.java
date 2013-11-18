package tuwien.inso.mnsa.midlet.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import tuwien.inso.mnsa.midlet.debug.Logger;
import tuwien.inso.mnsa.protocol.Message;

public class USBConnection implements Runnable {

	private static final Logger LOG = Logger.getLogger("USBConnection");

	private static final String COMMPORT_IDENTIFIER = "comm:USB1";

	private final CardConnector cardConnector;
	private CommConnection commConnection;
	private InputStream inStream;
	private OutputStream outStream;

	private volatile boolean isRunning;
	private volatile boolean inCommunication;

	public USBConnection(CardConnector cardConnector) {
		this.cardConnector = cardConnector;
	}

	public void run() {
		isRunning = true;
		while (isRunning) {
			try {
				commConnection = openConnection();
				inStream = commConnection.openInputStream();
				outStream = commConnection.openOutputStream();
			} catch (IOException e) {
				LOG.print("comm conn", e);
				return;
			}

			LOG.print("Listening USB port...");

			inCommunication = true;
			while (inCommunication) {
				Message request;
				try {
					request = Message.createFrom(inStream);
				} catch (IOException e) {
					LOG.print("read message" + e);
					closeSilently(inStream);
					closeSilently(outStream);
					closeSilently(commConnection);
					isRunning = false;
					inCommunication = false;
					break;
				}

				Message response = null;
				LOG.print("Request: " + request.toString());

				byte messageType = request.getMessageType();

				switch (messageType) {
				case Message.TYPE_TEST:
					LOG.print("Got TEST Message");
					response = request;
					break;

				case Message.TYPE_APDU:
					byte[] responsePayload;
					try {
						if (cardConnector.isCardPresent()) {
							responsePayload = cardConnector.exchangeData(request.getPayload());
							response = Message.createFrom(messageType, (byte) responsePayload.length, responsePayload);
						} else {
							LOG.print("TYPE_APDU request, but no card present");
							response = Message.createWithoutPayload(Message.TYPE_ERROR);
						}
					} catch (IOException e) {
						LOG.print("apdu", e);
						response = Message.createWithoutPayload(Message.TYPE_ERROR);
					} catch (ContactlessException e) {
						LOG.print("apdu", e);
						response = Message.createWithoutPayload(Message.TYPE_ERROR);
					}

					break;

				case Message.TYPE_ATR:
					if (cardConnector.isCardPresent()) {
						byte[] uid = cardConnector.getUid().getBytes();
						response = Message.createFrom(messageType, (byte) uid.length, uid);
					} else {
						LOG.print("TYPE_ATR request, but no card present");
						response = Message.createWithoutPayload(Message.TYPE_ERROR);
					}
					break;

				case Message.TYPE_CARD:
					responsePayload = new byte[1];
					responsePayload[0] = (byte) (cardConnector.isCardPresent() ? 1 : 0);
					response = Message.createFrom(messageType, (byte) 1, responsePayload);
					break;

				case Message.TYPE_OPEN:
					try {
						cardConnector.open();
						response = Message.createWithoutPayload(messageType);
					} catch (IOException e) {
						LOG.print("open", e);
						response = Message.createWithoutPayload(Message.TYPE_ERROR);
					}
					break;

				case Message.TYPE_CLOSE:
					try {
						cardConnector.open();
						response = Message.createWithoutPayload(messageType);
					} catch (IOException e) {
						LOG.print("close", e);
						response = Message.createWithoutPayload(Message.TYPE_ERROR);
					}
					break;

				default:
					LOG.print("Got unknown Message [0x" + Integer.toHexString(messageType & 0xFF) + "]");
					break;
				}

				try {
					if (response != null) {
						LOG.print("answering with " + response.toString());
						response.write(outStream);
						outStream.flush();
					}
				} catch (IOException e) {
					LOG.print("sending response", e);
					closeSilently(inStream);
					closeSilently(outStream);
					closeSilently(commConnection);
					isRunning = false;
					inCommunication = false;
					break;
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

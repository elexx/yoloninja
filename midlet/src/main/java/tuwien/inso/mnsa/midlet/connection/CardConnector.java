package tuwien.inso.mnsa.midlet.connection;

import java.io.IOException;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetProperties;
import javax.microedition.contactless.TargetType;
import javax.microedition.contactless.sc.ISO14443Connection;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import tuwien.inso.mnsa.midlet.debug.Logger;

public class CardConnector implements TargetListener {

	private static final Logger LOG = Logger.getLogger("CardConnection");

	private TargetProperties cardProperties;
	private ISO14443Connection connection;

	public void close() {
		if (connection != null)
			closeSilently(connection);
		connection = null;
	}

	public boolean isCardPresent() {
		return connection != null;
	}

	public byte[] exchangeData(byte[] request) throws IOException, ContactlessException {
		if (!isOpen()) {
			throw new IOException("no connection open");
		}

		try {
			return connection.exchangeData(request);
		} catch (IOException e) {
			closeSilently(connection);
			connection = null;
			cardProperties = null;
			throw e;
		}
	}

	public String getUid() {
		if (cardProperties != null)
			return cardProperties.getUid();
		else
			return null;
	}

	public void targetDetected(TargetProperties[] targetProperties) {
		if (targetProperties.length == 0) {
			return;
		}

		TargetProperties properties = targetProperties[0];
		LOG.print("UID read: " + properties.getUid());

		if (properties.hasTargetType(TargetType.ISO14443_CARD) && properties.getConnectionNames().length > 0) {
			close();
			cardProperties = properties;

			LOG.print("card detected");
		}
	}

	public void open() throws IOException {
		if (!isCardPresent()) {
			throw new IOException("no card present");
		}

		if (!isOpen()) {
			String url = cardProperties.getUrl(cardProperties.getConnectionNames()[0]);
			LOG.print("openning connection " + url);
			connection = (ISO14443Connection) Connector.open(url);
		}
	}

	public boolean isOpen() {
		return connection != null;
	}

	private static void closeSilently(Connection connection) {
		try {
			connection.close();
		} catch (IOException ignored) {
		}
	}
}

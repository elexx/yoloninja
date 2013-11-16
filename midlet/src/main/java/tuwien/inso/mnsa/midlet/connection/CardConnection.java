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

public class CardConnection implements TargetListener {

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

		TargetProperties tmp = targetProperties[0];
		LOG.print("UID read: " + tmp.getUid());

		if (tmp.hasTargetType(TargetType.ISO14443_CARD) && tmp.getConnectionNames().length > 0) {
			if (connection != null) {
				closeSilently(connection);
			}
			try {
				String url = tmp.getUrl(tmp.getConnectionNames()[0]);
				LOG.print("openning connection " + url);
				connection = (ISO14443Connection) Connector.open(url);
				cardProperties = tmp;
			} catch (IOException e) {
				LOG.print("could not create iso14443 connection: " + e.toString());
				return;
			}

			LOG.print("opened connection");
		}
	}

	private static void closeSilently(Connection connection) {
		try {
			connection.close();
		} catch (IOException ignored) {
		}
	}
}

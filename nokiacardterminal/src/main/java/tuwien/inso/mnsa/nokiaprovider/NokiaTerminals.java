package tuwien.inso.mnsa.nokiaprovider;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.inso.mnsa.nokiaprovider.intern.Connection;

@SuppressWarnings("restriction")
public class NokiaTerminals extends CardTerminals {

	private static final Logger LOG = LoggerFactory.getLogger(NokiaTerminals.class);

	private final List<CardTerminal> terminals;
	private final Connection connection;

	NokiaTerminals(Connection connection) {
		this.connection = connection;
		terminals = new LinkedList<>();
	}

	/**
	 * Returns only one terminal with state ALL|CARD_PRESENT|CARD_INSERTION, in
	 * other case returns empty list.
	 */
	@Override
	public List<CardTerminal> list(State state) throws CardException {
		if (!connection.isConnected()) {
			try {
				LOG.debug("connecting to terminal");
				terminals.clear();
				connection.connect();
				terminals.add(new NokiaTerminal(connection));
			} catch (IOException e) {
				LOG.error("list", e);
			}
		}

		switch (state) {
		case ALL:
		case CARD_PRESENT:
		case CARD_INSERTION:
			return terminals;

		default:
			return new LinkedList<>();
		}
	}

	/**
	 * Immediately returns true
	 */
	@Override
	public boolean waitForChange(long l) throws CardException {
		return true;
	}
}

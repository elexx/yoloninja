package tuwien.inso.mnsa.nokiaprovider;

import java.io.IOException;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

import tuwien.inso.mnsa.nokiaprovider.intern.Connection;

/**
 * CardTerminal implementation class.
 */
@SuppressWarnings("restriction")
public class NokiaTerminal extends CardTerminal {

	public final static String NAME = "NokiaTerminal.Terminal";
	private static NokiaCard card = null;

	private final Connection connection;

	public NokiaTerminal(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String getName() {
		return connection.getName();
	}

	@Override
	public Card connect(String string) throws CardException {
		try {
			connection.openCardConnection();
		} catch (IOException e) {
			throw new CardException(e);
		}

		if (NokiaTerminal.card == null)
			NokiaTerminal.card = new NokiaCard(connection);
		return NokiaTerminal.card;
	}

	/**
	 * Always returns true
	 */
	@Override
	public boolean isCardPresent() throws CardException {
		try {
			return connection.isCardPresent();
		} catch (IOException e) {
			throw new CardException(e);
		}
	}

	/**
	 * Immediately returns true
	 */
	@Override
	public boolean waitForCardPresent(long l) throws CardException {
		return true;
	}

	/**
	 * Immediately returns true
	 */
	@Override
	public boolean waitForCardAbsent(long l) throws CardException {
		return false;
	}
}

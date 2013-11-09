package tuwien.inso.mnsa.nokiaprovider;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * CardTerminal implementation class.
 */
@SuppressWarnings("restriction")
public class NokiaTerminal extends CardTerminal {

	public final static String NAME = "NokiaTerminal.Terminal";
	private static NokiaCard card = null;

	@Override
	public String getName() {
		return NokiaTerminal.NAME;
	}

	@Override
	public Card connect(String string) throws CardException {
		if (NokiaTerminal.card == null)
			NokiaTerminal.card = new NokiaCard();
		return NokiaTerminal.card;
	}

	/**
	 * Always returns true
	 */
	@Override
	public boolean isCardPresent() throws CardException {
		return true;
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

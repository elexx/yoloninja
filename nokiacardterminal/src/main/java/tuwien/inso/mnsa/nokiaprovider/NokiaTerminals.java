package tuwien.inso.mnsa.nokiaprovider;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

@SuppressWarnings("restriction")
public class NokiaTerminals extends CardTerminals {

	/**
	 * Returns only one terminal with state ALL|CARD_PRESENT|CARD_INSERTION, in
	 * other case returns empty list.
	 */
	@Override
	public List<CardTerminal> list(State state) throws CardException {
		List<CardTerminal> terminals = new ArrayList<CardTerminal>();
		switch (state) {
		case ALL:
		case CARD_PRESENT:
		case CARD_INSERTION:
			terminals.add(new NokiaTerminal());
			break;
		default:
			break;

		}
		return terminals;
	}

	/**
	 * Immediately returns true
	 */
	@Override
	public boolean waitForChange(long l) throws CardException {
		return true;
	}
}

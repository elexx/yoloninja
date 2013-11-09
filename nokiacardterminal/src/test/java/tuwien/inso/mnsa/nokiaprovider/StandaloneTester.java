package tuwien.inso.mnsa.nokiaprovider;

import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class StandaloneTester {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(StandaloneTester.class);

	public static void main(String[] args) throws Exception {
		TerminalFactory terminalFactory = TerminalFactory.getInstance("NokiaProvider", null);

		CardTerminals cardTerminals = terminalFactory.terminals();
		List<CardTerminal> cardTerminalList = cardTerminals.list();

		LOGGER.debug("Connected NokiaTerminals ({}): {}", cardTerminalList.size(), cardTerminalList);

		for (CardTerminal ct : cardTerminalList) {
			// don't care about the protocol (either T=0 or T=1)
			Card card = ct.connect("*");
			LOGGER.debug("ATR: {}", card.getATR());
		}
	}
}

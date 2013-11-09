package tuwien.inso.mnsa.nokiaprovider;

import static org.junit.Assert.assertNotNull;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class NokiaTest {

	private final static Logger LOGGER = LoggerFactory.getLogger(NokiaTest.class);

	@Before
	public void startup() {
		Security.addProvider(new NokiaProvider());
	}

	@After
	public void teardown() {
		Security.removeProvider("NokiaProvider");
	}

	@Test
	public void testProviderList() {
		Provider[] providers = Security.getProviders();

		LOGGER.debug("Providers ({}): {}", providers.length, providers);

		Provider nokiaProvider = Security.getProvider("NokiaProvider");
		assertNotNull(nokiaProvider);
	}

	@Test
	public void testNokiaProvider() throws NoSuchAlgorithmException, NoSuchProviderException, CardException {
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
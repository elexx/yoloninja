package tuwien.inso.mnsa.nokiaprovider;

import java.io.IOException;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.inso.mnsa.nokiaprovider.intern.Connection;

/**
 * Card implementation class.
 */
@SuppressWarnings("restriction")
public class NokiaCard extends Card {

	private static final Logger LOG = LoggerFactory.getLogger(NokiaCard.class);

	private static final String T0_PROTOCOL = "T=0";
	// default ATR - NXP JCOP 31/36K
	private static final String DEFAULT_ATR = "3BFA1800008131FE454A434F5033315632333298";

	private final Connection connection;

	private final CardChannel basicChannel;

	public NokiaCard(Connection connection) {
		this.connection = connection;
		basicChannel = new NokiaChannel(this, 0);
	}

	@Override
	public ATR getATR() {
		try {
			return connection.getATR();
		} catch (IOException e) {
			LOG.debug("no connection to card", e);
			return new ATR(NokiaCard.DEFAULT_ATR.getBytes());
		}
	}

	/**
	 * Always returns T=0.
	 */
	@Override
	public String getProtocol() {
		return NokiaCard.T0_PROTOCOL;
	}

	@Override
	public CardChannel getBasicChannel() {
		return basicChannel;
	}

	/**
	 * Always returns basic channel with id = 0
	 * 
	 * @throws CardException
	 */
	@Override
	public CardChannel openLogicalChannel() throws CardException {
		return basicChannel;
	}

	/**
	 * Do nothing.
	 */
	@Override
	public void beginExclusive() throws CardException {
	}

	/**
	 * Do nothing.
	 */
	@Override
	public void endExclusive() throws CardException {
	}

	@Override
	public byte[] transmitControlCommand(int i, byte[] bytes) throws CardException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Do nothing.
	 */
	@Override
	public void disconnect(boolean bln) throws CardException {
	}

	public ResponseAPDU transmitCommand(CommandAPDU capdu) throws CardException {
		try {
			return connection.transceive(capdu);
		} catch (IOException e) {
			throw new CardException(e);
		}
	}
}

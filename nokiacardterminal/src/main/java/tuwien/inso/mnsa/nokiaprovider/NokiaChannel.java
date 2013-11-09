package tuwien.inso.mnsa.nokiaprovider;

import java.nio.ByteBuffer;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * CardChannel implementation class.
 */
@SuppressWarnings("restriction")
public class NokiaChannel extends CardChannel {
	private final NokiaCard card;
	private final int channel;

	public NokiaChannel(NokiaCard card, int channel) {
		this.card = card;
		this.channel = channel;
	}

	@Override
	public Card getCard() {
		return card;
	}

	@Override
	public int getChannelNumber() {
		return channel;
	}

	@Override
	public ResponseAPDU transmit(CommandAPDU capdu) throws CardException {
		return card.transmitCommand(capdu);
	}

	@Override
	public int transmit(ByteBuffer in, ByteBuffer out) throws CardException {
		ResponseAPDU response = transmit(new CommandAPDU(in));
		byte[] binaryResponse = response.getBytes();
		out.put(binaryResponse);
		return binaryResponse.length;
	}

	/**
	 * Do nothing.
	 */
	@Override
	public void close() throws CardException {
	}
}

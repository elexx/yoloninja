package tuwien.inso.mnsa.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A protocol based on http://www.win.tue.nl/pinpasjc/docs/apis/offcard/com/ibm/jc/terminal/RemoteJCTerminal.html
 * 
 * <LI>1byte MTY Message type</LI> <LI>1byte LN payload length</LI> <LI>nbyte PLD payload, max 0xFF bytes</LI>
 */
public class Message {

	public static final byte TYPE_TEST = 0x00;
	public static final byte TYPE_APDU_COMMAND = 0x01;
	public static final byte TYPE_APDU_RESPONSE = 0x02;

	private static final short OFFSET_MTY = 0;
	private static final short OFFSET_LN = 1;
	private static final short OFFSET_PY = 2;

	private final byte messageType;
	private final byte length;
	private final byte[] payload;

	private Message(byte messageType, byte length, byte[] payload) {
		this.messageType = messageType;
		this.length = length;
		this.payload = payload;
	}

	public static Message createFrom(byte messageType, byte length, byte[] payload) {
		return new Message(messageType, length, payload);
	}

	public static Message createFrom(InputStream inStream) throws IOException {
		byte[] buffer = new byte[2];
		inStream.read(buffer, 0, 2);

		byte messageType = buffer[OFFSET_MTY];
		byte length = buffer[OFFSET_LN];

		byte[] payload = new byte[messageType];
		inStream.read(payload, 0, length);

		return new Message(messageType, length, payload);
	}

	public void write(OutputStream outStream) throws IOException {
		outStream.write(messageType);
		outStream.write(length);
		outStream.write(payload, 0, length);
	}

	public byte getMessageType() {
		return messageType;
	}

	public short getLength() {
		return length;
	}

	public byte[] getPayload() {
		return payload;
	}

}

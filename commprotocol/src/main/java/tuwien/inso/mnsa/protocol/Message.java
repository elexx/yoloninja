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
	public static final byte TYPE_APDU = 0x01;
	public static final byte TYPE_ATR = 0x02;
	public static final byte TYPE_ERROR = 0x7F;

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

	public static Message createWithoutPayload(byte messageType) {
		return new Message(messageType, (byte) 0, null);
	}

	public static Message createFrom(byte messageType, byte length, byte[] payload) {
		return new Message(messageType, length, payload);
	}

	public static Message createFrom(InputStream inStream) throws IOException {
		byte[] buffer = new byte[2];
		readExacltyToLength(inStream, buffer, 0, 2);

		byte messageType = buffer[OFFSET_MTY];
		byte length = buffer[OFFSET_LN];

		byte[] payload = null;
		if (length > 0) {
			payload = new byte[length];
			readExacltyToLength(inStream, payload, 0, length);
		}
		return new Message(messageType, length, payload);
	}

	public void write(OutputStream outStream) throws IOException {
		outStream.write(messageType);
		outStream.write(length);
		if (length > 0)
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

	public String toString() {
		return "MTY[" + messageType + "] LN[" + length + "] PY[" + payload + "]";
	}

	private static void readExacltyToLength(InputStream inStream, byte[] output, int offset, int length) throws IOException {
		int total = 0;
		while (total < length) {
			int read = inStream.read(output, offset + total, length - total);
			if (read == -1)
				break;
			total += read;
		}
	}
}

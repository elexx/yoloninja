package tuwien.inso.mnsa.nokiaprovider.intern;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import javax.smartcardio.ATR;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.inso.mnsa.protocol.Message;

@SuppressWarnings("restriction")
public class SocketConnection implements Connection {

	private static final Logger LOG = LoggerFactory.getLogger(SocketConnection.class);

	private final SocketAddress address;
	private Socket socket;
	private InputStream inStream;
	private OutputStream outStream;

	public SocketConnection(SocketAddress address) {
		this.address = address;
	}

	@Override
	public void connect() throws IOException {
		if (!isConnected()) {
			socket = new Socket();
			socket.connect(address);
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		}
	}

	@Override
	public void disconnect() {
		if (isConnected()) {
			closeSilently(outStream);
			closeSilently(inStream);
			closeSilently(socket);
		}
	}

	@Override
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	@Override
	public ResponseAPDU transceive(CommandAPDU request) throws IOException {
		byte[] apdubuffer = request.getBytes();
		if (apdubuffer.length > 0xff) {
			throw new IOException("too long APDU");
		}

		byte length = (byte) (apdubuffer.length & 0xff);

		Message requestMessage = Message.createFrom(Message.TYPE_APDU, length, apdubuffer);
		requestMessage.write(outStream);
		outStream.flush();

		Message responseMessage = Message.createFrom(inStream);
		assertNoErrorOrThrowException(responseMessage);

		ResponseAPDU response = new ResponseAPDU(responseMessage.getPayload());
		return response;
	}

	@Override
	public ATR getATR() throws IOException {
		Message requestMessage = Message.createWithoutPayload(Message.TYPE_ATR);
		requestMessage.write(outStream);
		outStream.flush();

		Message responseMessage = Message.createFrom(inStream);
		assertNoErrorOrThrowException(responseMessage);

		return new ATR(responseMessage.getPayload());
	}

	@Override
	public boolean isCardPresent() throws IOException {
		Message requestMessage = Message.createWithoutPayload(Message.TYPE_CARD);
		requestMessage.write(outStream);
		outStream.flush();
		
		Message responseMessage = Message.createFrom(inStream);
		assertNoErrorOrThrowException(responseMessage);
		
		byte[] payload = responseMessage.getPayload();
		return payload.length == 1 && payload[0] == 1;
	}

	private void assertNoErrorOrThrowException(Message m) throws IOException {
		if (m.getMessageType() == Message.TYPE_ERROR) {
			throw new IOException("Phone sent TYPE_ERROR - check phone output for more information. (Maybe no card present?)");
		}
	}

	private void closeSilently(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ignored) {
		}
	}

	@Override
	public String getName() {
		return address.toString();
	}

}

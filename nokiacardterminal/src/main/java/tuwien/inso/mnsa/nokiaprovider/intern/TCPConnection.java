package tuwien.inso.mnsa.nokiaprovider.intern;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.inso.mnsa.protocol.Message;

@SuppressWarnings("restriction")
public class TCPConnection implements Connection {

	private static final Logger LOG = LoggerFactory.getLogger(TCPConnection.class);

	private final SocketAddress address;
	private Socket socket;
	private InputStream inStream;
	private OutputStream outStream;

	public TCPConnection(SocketAddress address) {
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
			throw new IOException("to long APDU");
		}

		byte length = (byte) (apdubuffer.length & 0xff);

		Message requestMessage = Message.createFrom(Message.TYPE_APDU_COMMAND, length, apdubuffer);
		requestMessage.write(outStream);

		Message responseMessage = Message.createFrom(inStream);

		ResponseAPDU response = new ResponseAPDU(responseMessage.getPayload());
		return response;
	}

	private void closeSilently(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ignored) {
		}
	}

}

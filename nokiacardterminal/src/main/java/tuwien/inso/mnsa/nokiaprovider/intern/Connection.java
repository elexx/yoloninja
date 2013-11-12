package tuwien.inso.mnsa.nokiaprovider.intern;

import java.io.IOException;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

@SuppressWarnings("restriction")
public interface Connection {

	void connect() throws IOException;

	void disconnect();

	boolean isConnected();

	ResponseAPDU transceive(CommandAPDU capdu) throws IOException;

}
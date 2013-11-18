package tuwien.inso.mnsa.nokiaprovider.intern;

import java.io.IOException;

import javax.smartcardio.ATR;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

@SuppressWarnings("restriction")
public interface Connection {

	void connect() throws IOException;

	void disconnect();

	boolean isConnected();
	
	boolean isCardPresent() throws IOException;

	ResponseAPDU transceive(CommandAPDU capdu) throws IOException;

	ATR getATR() throws IOException;

	String getName();

	void closeCardConnection() throws IOException;

	void openCardConnection() throws IOException;

}

package tuwien.inso.mnsa.nokiaprovider.intern;

import java.io.IOException;

import javax.smartcardio.ATR;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

@SuppressWarnings("restriction")
public interface Connection {

	/**
	 * Establishes this connection. does nothing if the connection is already
	 * established.
	 * 
	 * @return
	 * @throws IOException
	 */
	void connect() throws IOException;

	/**
	 * Closes this connection. does nothing if the connection is already closed.
	 * 
	 * @return
	 * @throws IOException
	 */
	void disconnect();

	/**
	 * Returns true if this connection is open
	 * 
	 * @return
	 * @throws IOException
	 */
	boolean isConnected();

	/**
	 * Returns true if there has been a card present and no communication error
	 * happened since then and the connection has not been closed.
	 * 
	 * @throws IOException
	 */
	boolean isCardPresent() throws IOException;

	/**
	 * Sends an APDU to the card and returns the response.
	 * 
	 * @return
	 * @throws IOException
	 *             if no card is present, no logical connection to it is
	 *             established or some communication error occurred.
	 */
	ResponseAPDU transceive(CommandAPDU capdu) throws IOException;

	/**
	 * Gets the ATR.
	 * 
	 * @return
	 * @throws IOException
	 */
	ATR getATR() throws IOException;

	/**
	 * Returns the unique name of this connection.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Closes the previously established connection (channel) to the card. If no
	 * connection is established, this method does nothing.
	 * 
	 * @throws IOException
	 *             If a problem occurs during the closing of the channel
	 */
	void closeCardConnection() throws IOException;

	/**
	 * Opens the logical connection (channel) to the card (to enable sending
	 * APDUs down the channel). If the connection is already established, this
	 * method does nothing.
	 * 
	 * @throws IOException
	 *             If a problem occurs during the opening of the channel
	 */
	void openCardConnection() throws IOException;

}

package tuwien.inso.mnsa.nokiaprovider;

import java.net.InetSocketAddress;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;

import tuwien.inso.mnsa.nokiaprovider.intern.Connection;
import tuwien.inso.mnsa.nokiaprovider.intern.TCPConnection;

@SuppressWarnings("restriction")
public class NokiaFactorySpi extends TerminalFactorySpi {

	private final CardTerminals cardTerminals;

	public NokiaFactorySpi() {
		this(null);
	}

	public NokiaFactorySpi(Object parameter) {
		InetSocketAddress address = (parameter instanceof InetSocketAddress) ? (InetSocketAddress) parameter : new InetSocketAddress("localhost", 9787);

		Connection connection = new TCPConnection(address);
		cardTerminals = new NokiaTerminals(connection);
	}

	@Override
	protected CardTerminals engineTerminals() {
		return cardTerminals;
	}

}
package tuwien.inso.mnsa.nokiaprovider;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactorySpi;

@SuppressWarnings("restriction")
public class NokiaFactorySpi extends TerminalFactorySpi {

	public NokiaFactorySpi() {
		// initialize as appropriate
	}

	public NokiaFactorySpi(Object parameter) {
		// initialize as appropriate
	}

	@Override
	protected CardTerminals engineTerminals() {
		return new NokiaTerminals();
	}

}
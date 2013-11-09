package tuwien.inso.mnsa.nokiaprovider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

import javax.smartcardio.TerminalFactorySpi;

@SuppressWarnings("restriction")
public class NokiaProvider extends Provider {
	private static final long serialVersionUID = -6904960468717297112L;

	public static final String PROVIDER_NAME = "NokiaProvider";
	public static final double PROVIDER_VERSION = 1.0d;
	public static final String PROVIDER_INFO = "Nokia Phone Terminal Provider";
	public static final Class<? extends TerminalFactorySpi> FACTORY_SPI_CLASS = NokiaFactorySpi.class;

	public NokiaProvider() {
		super(NokiaProvider.PROVIDER_NAME, NokiaProvider.PROVIDER_VERSION, NokiaProvider.PROVIDER_INFO);

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				put("TerminalFactory." + NokiaProvider.PROVIDER_NAME, NokiaProvider.FACTORY_SPI_CLASS.getCanonicalName());
				return null;
			}
		});
	}
}

package tuwien.inso.mnsa.rxtxtcp;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	public static void main(String[] args) {
		String resource;

		if (args.length == 0) {
			resource = null;
		} else if (args.length == 1) {
			resource = args[0];
		} else {
			LOG.error("Usage: Server [ports.conf location]");
			return;
		}

		showPorts();

		PortDefinition[] ports;
		try {
			if (resource == null)
				ports = ConfigParser.getPortDefinitions().toArray(new PortDefinition[0]);
			else {
				try (FileInputStream fis = new FileInputStream(resource)) {
					ports = ConfigParser.getPortDefinitions(fis).toArray(new PortDefinition[0]);
				}
			}
		} catch (IOException e) {
			LOG.error("Error while reading port configuration file", e);
			return;
		}

		for (PortDefinition definition : ports) {
			LOG.debug("Proxying clients from {} to {}", definition.getTcpPort(), definition.getDeviceName());
			Server server = new Server(definition);
			Thread thread = new Thread(server, definition.getDeviceName() + " at " + definition.getTcpPort());

			thread.start();
		}
	}

	private static void showPorts() {
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

		int portn = 0;
		LOG.info("Listing ports:");
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = ports.nextElement();
			portn++;

			LOG.info(port.getName() + " (" + port.getPortType() + ", current owner " + port.getCurrentOwner() + ")");
		}
		LOG.info("Port listing done, found " + portn + " ports.");
	}

	private final PortDefinition portDefinition;

	private Server(PortDefinition portDefinition) {
		this.portDefinition = portDefinition;
	}

	private void work() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(portDefinition.getTcpPort())) {
			while (true) {
				try (Socket client = serverSocket.accept()) {
					LOG.debug("received client: {}", client.getRemoteSocketAddress());
					
					ClientHandler handler = new ClientHandler(client, portDefinition);
					try {
						handler.connect();
					} catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException e) {
						LOG.debug("client handler:", e);
					}

					LOG.debug("client closing: {} ", client.getRemoteSocketAddress());
				}
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {

				work();

			} catch (Throwable e) {
				LOG.debug("Exception in listener thread ", e);

				try {
					Thread.sleep(1500);
					// open to discussion: should an exception cause the thread to stop completely
					// or just resume operation after a little while?
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

}

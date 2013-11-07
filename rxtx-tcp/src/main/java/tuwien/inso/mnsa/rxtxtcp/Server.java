package tuwien.inso.mnsa.rxtxtcp;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server implements Runnable {

	public static void main(String[] args) {
		String resource;

		if (args.length == 0) {
			resource = null;
		} else if (args.length == 1) {
			resource = args[0];
		} else {
			System.err.println("Usage: Server [ports.conf location]");
			return;
		}

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
			System.err.println("Error while reading port configuration file");
			e.printStackTrace();
			return;
		}

		for (PortDefinition definition : ports) {
			Server server = new Server(definition);
			Thread thread = new Thread(server, definition.getDeviceName() + " at " + definition.getTcpPort());
			
			thread.start();
		}
	}

	private PortDefinition portDefinition;

	private Server(PortDefinition portDefinition) {
		this.portDefinition = portDefinition;
	}

	private void work() throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(portDefinition.getTcpPort())) {
			while (true) {
				try (Socket client = serverSocket.accept()) {
					log("received client: " + client.getRemoteSocketAddress().toString());

					ClientHandler handler = new ClientHandler(client, portDefinition);
					try {
						handler.connect();
					} catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException e) {
						log("client handler " + client.getRemoteSocketAddress() + " threw " + e.toString());
					}

					log("client closing: " + client.getRemoteSocketAddress().toString());
				}
			}
		}
	}

	public static void log(String string) {
		// TODO: logging framework?
		System.out.println(new Date().toString() + " --- " + string);
	}

	@Override
	public void run() {
		while (true) {
			try {

				work();

			} catch (Exception ex) {
				System.err.println("Exception in listener thread " + Thread.currentThread().getName() + ": " + ex);
				ex.printStackTrace();

				try {
					Thread.sleep(1500);
					// open to discussion: should an exception cause the thread to stop completely
					// or just resume operation after a little while?
				} catch (InterruptedException iEx) {
				}
			}
		}
	}

}

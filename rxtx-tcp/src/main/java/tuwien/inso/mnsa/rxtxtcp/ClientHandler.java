package tuwien.inso.mnsa.rxtxtcp;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
	private final Socket client;
	private final PortDefinition port;

	public static final int COPY_BLOCK_SIZE = 1024 * 5;
	public static final int COMMUNICATION_TIMEOUT = 500;

	public ClientHandler(Socket client, PortDefinition portDefinition) {
		this.client = client;
		this.port = portDefinition;
	}

	public void connect() throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException {
		SerialPort serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(port.getDeviceName()).open("rxtx-tcp", 100);
		try {
			serialPort.enableReceiveTimeout(COMMUNICATION_TIMEOUT);
			client.setSoTimeout(COMMUNICATION_TIMEOUT);
			client.setKeepAlive(true);

			InputStream commIn = serialPort.getInputStream();
			OutputStream commOut = serialPort.getOutputStream();

			InputStream netIn = client.getInputStream();
			OutputStream netOut = client.getOutputStream();

			CopyThread toClient = new CopyThread(this, commIn, netOut);
			CopyThread fromClient = new CopyThread(this, netIn, commOut);

			Thread toClientT = new Thread(toClient, Thread.currentThread().getName() + "-toClient");
			Thread fromClientT = new Thread(fromClient, Thread.currentThread().getName() + "-fromClient");

			toClientT.start();
			fromClientT.start();

			while (toClient.running && fromClient.running) {
				try {
					synchronized (this) {
						this.wait(5000);
					}
				} catch (InterruptedException iEx) {
					// interrupted by toClient or fromClient, recheck in while condition
				}
			}

			Server.log("exit condition: " + toClient.running + " / " + fromClient.running);

			// 1: sleep some time, to let the two channels close in a nice manner
			// 2: then, close the streams, probably forcing any blocking i/o to cancel
			// 3: sleep some more to let them actually notice that their i/o has been cancelled
			// 4: interrupt threads ("nicest" way of cancelling i/o)
			// 5: sleep some more to let them notice that still-pending i/o has been interrupted
			// 6: stop threads, if necessary

			sleep(50);

			close(netIn);
			close(netOut);
			close(commIn);
			close(commOut);

			sleep(50);

			toClientT.interrupt();
			fromClientT.interrupt();

			sleep(50);

			stop(toClientT);
			stop(fromClientT);

			sleep(50);

			if (toClient.running)
				Server.log("client thread " + toClientT.getName() + " (to client) not finished normally (zombie thread)");
			if (fromClient.running)
				Server.log("client thread " + fromClientT.getName() + " (from client) not finished normally (zombie thread)");

			if (toClient.throwable != null)
				Server.log("client thread " + toClientT.getName() + " (to client) finished abnormally: " + toClient.throwable.toString());
			if (fromClient.throwable != null)
				Server.log("client thread " + fromClientT.getName() + " (from client) finished abnormally: " + fromClient.throwable.toString());

			Server.log("client exiting");
		} finally {
			serialPort.close();
		}
	}

	@SuppressWarnings("deprecation")
	private void stop(Thread thread) {
		if (thread.isAlive())
			thread.stop();
	}

	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (Exception ignored) {
		}
	}

	private void sleep(int i) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException iEx) {
		}
	}

	private static class CopyThread implements Runnable {
		private final Object toNotify;
		private final InputStream input;
		private final OutputStream output;

		private boolean running = true;
		private Throwable throwable;

		private CopyThread(Object toNotify, InputStream input, OutputStream output) {
			this.toNotify = toNotify;
			this.input = input;
			this.output = output;
		}

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[1];

				while (true) {
					try {
						int read = input.read(buffer);

						if (read == -1)
							break;
						
						if(read == 1) {
							System.out.println(Thread.currentThread().getName() + " - read: " + buffer[0] + " (" + new String(new char[]{(char) buffer[0]}) + ")");
						}
						output.write(buffer, 0, read);
						output.flush();
					} catch (SocketTimeoutException ignored) {
					}
				}

				throwable = null;
			} catch (Throwable t) {
				throwable = t;
			} finally {
				running = false;

				if (toNotify != null) {
					synchronized (toNotify) {
						toNotify.notifyAll();
					}
				}
			}
		}
	}
}

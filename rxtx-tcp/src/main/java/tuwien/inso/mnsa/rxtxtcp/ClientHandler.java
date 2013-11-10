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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

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

			LOG.debug("exit condition: {} / {}", toClient.running, fromClient.running);

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
				LOG.debug("client thread {} (to client) not finished normally (zombie thread)", toClientT.getName());
			if (fromClient.running)
				LOG.debug("client thread {} (from client) not finished normally (zombie thread)", fromClientT.getName());
			if (toClient.throwable != null)
				LOG.debug("client thread {} (to client) finished abnormally: {}", toClientT.getName(), toClient.throwable.toString());
			if (fromClient.throwable != null)
				LOG.debug("client thread {} (from client) finished abnormally: {}", fromClientT.getName(), fromClient.throwable.toString());

			LOG.info("client exiting");
		} finally {
			serialPort.close();
		}
	}

	@SuppressWarnings("deprecation")
	private void stop(Thread thread) {
		//TODO: dont use deprecated APIs

		// this is meant to be the last-resort method of forcing
		// a thread to die. since it didn't respond to interrupting
		// or closing its COM port, we know no other way of killing it.
		//
		// argued by [1], however, is the fact that a thread will not
		// respond to thread.stop() if it doesn't respond to 
		// thread.interrupt() - this has to be investigated further.
		//
		// anyway, this situation will probably never happen and
		// when it does, the purpose of .stop() is to avoid leaving
		// serial ports open by all means (the next alternative here
		// would be to kill the VM itself)
		//
		// [1] http://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html

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
				byte[] buffer = new byte[COPY_BLOCK_SIZE];

				while (true) {
					try {
						int read = input.read(buffer);

						if (read == -1)
							break;

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

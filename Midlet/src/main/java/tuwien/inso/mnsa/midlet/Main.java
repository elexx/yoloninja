package tuwien.inso.mnsa.midlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;

public class Main extends MIDlet implements CommandListener {
	private final Command exitCommand;
	private final Display display;
	private Logger log;
	private CommConnection comm = null;
	private InputStream is = null;
	private OutputStream os = null;
	private Thread thread;

	public Main() {
		display = Display.getDisplay(this);
		exitCommand = new Command("Exit", Command.EXIT, 1);
	}

	public void startApp() {
		TextBox textbox = new TextBox("USBTest", "", 8000, 0);
		textbox.addCommand(exitCommand);
		textbox.setCommandListener(this);
		display.setCurrent(textbox);
		log = new Logger(textbox);
		openUSBConnection();
	}

	public void pauseApp() {}

	public void destroyApp(boolean unconditional) {}

	public void commandAction(Command c, Displayable s) {
		if (c == exitCommand) {
			log.println("Exiting...");
			notifyDestroyed();
		}
	}

	public void openUSBConnection() {
		try {
			String ports = System.getProperty("microedition.commports");
			log.println("Available Ports: " + ports);

			int index = ports.indexOf("USB", 0);
			if (index == -1) {
				throw new RuntimeException("No USB port found in the device");
			}

			log.println("opening comm:USB1");
			comm = (CommConnection) Connector.open("comm:USB1");

			log.println("opening output stream");
			os = comm.openOutputStream();

			log.println("writing to USB1");
			String text = "Hello from USBTest MIDlet! Anybody there at USB?\r\n";
			os.write(text.getBytes());
			os.flush();

			log.println("opening input stream");
			is = comm.openInputStream();

			log.println("send EXIT to quit");

			thread = new Thread() {
				public void run() {
					listenUSB();
				}
			};
			thread.start();

		} catch (IOException e) {
			log.println("IOException: " + e.getMessage());
			return;
		}
	}

	public void listenUSB() {
		log.println("Listening USB port...");
		try {
			byte[] buffer = new byte[1];
			StringBuffer message = new StringBuffer();
			String lastMessage = null;

			do {
				if (is.available() == 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ignored) {}
					continue;
				}

				int readCount = is.read(buffer);
				if (readCount > 0) {
					byte b = buffer[0];
					log.print(Integer.toHexString(b & 0xFFFF) + " ");

					// If \r or \n echo a \r\n
					if (b == 0x0D || b == 0x0A) {
						os.write(0x0D);
						os.write(0x0A);
						lastMessage = message.toString();
						log.println("\nMessage: " + lastMessage);
						message.delete(0, message.length());
					} else {
						os.write(b);
						message.append(new String(buffer, 0, 1));
					}

					os.flush();
				}
			} while (lastMessage == null || !lastMessage.endsWith("EXIT"));

			os.write("Goodbye!".getBytes());
			os.flush();
			os.close();
			is.close();
			comm.close();
		} catch (IOException e) {
			log.println("IOException: " + e.getMessage());
		}
		log.println("SUCCEEDED.");
	}
}

package tuwien.inso.mnsa.midlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.contactless.DiscoveryManager;
import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetProperties;
import javax.microedition.contactless.TargetType;
import javax.microedition.contactless.sc.ISO14443Connection;
import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;

public class Main extends MIDlet implements CommandListener, TargetListener {
	private final Command exitCommand;
	private Logger log;
	private CommConnection comm = null;
	private InputStream is = null;
	private OutputStream os = null;
	private Thread thread;

	public Main() {
		exitCommand = new Command("Exit", Command.EXIT, 1);
	}

	public void startApp() {
		TextBox textbox = new TextBox("USBTest", "", 8000, 0);
		textbox.addCommand(exitCommand);
		textbox.setCommandListener(this);
		log = new Logger(textbox);

		Display.getDisplay(this).setCurrent(textbox);
		openUSBConnection();

		try {
			DiscoveryManager dm = DiscoveryManager.getInstance();
			dm.addTargetListener(this, TargetType.ISO14443_CARD);
			dm.addTargetListener(this, TargetType.NDEF_TAG);
			dm.addTargetListener(this, TargetType.RFID_TAG);
		} catch (ContactlessException ce) {
			log.println("Unable to register TargetListener: " + ce.toString());
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void commandAction(Command c, Displayable s) {
		if (c == exitCommand) {
			log.println("Exiting...");
			notifyDestroyed();
		}
	}

	public void targetDetected(TargetProperties[] targetProperties) {
		if (targetProperties.length == 0) {
			return;
		}

		TargetProperties tmp = targetProperties[0];
		log.println("UID read: " + tmp.getUid());
		boolean isIso = false;

		for (int i = 0; i < tmp.getTargetTypes().length; i++) {
			TargetType type = tmp.getTargetTypes()[i];
			log.println("target type: " + type.toString());

			if (type.equals(TargetType.ISO14443_CARD))
				isIso = true;
		}

		for (int i = 0; i < tmp.getConnectionNames().length; i++) {
			log.println("conn name: " + tmp.getConnectionNames()[i].getName());
		}

		if (!isIso)
			return;

		ISO14443Connection connection;
		try {
			Class firstConnection = tmp.getConnectionNames()[0];
			String url = tmp.getUrl(firstConnection);
			log.println("openning connection " + url + "...");
			connection = (ISO14443Connection) Connector.open(url);
		} catch (IOException e) {
			log.println("could not create iso14443 connection: " + e.toString());
			return;
		}

		log.println("opened connection");

		//		byte[] SELECT = { 0x00, // CLA Class 
		//		(byte) 0xA4, // INS Instruction 
		//		0x04, // P1 Parameter 1
		//		0x00, // P2 Parameter 2
		//		(byte) 0xA0, // Length
		//		0x63, 0x64, 0x63, 0x00, 0x00, 0x00, 0x00, 0x32, 0x32, 0x31 // aid
		//		};

		//byte[] SELECT = { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		byte[] SELECT = { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x3f, (byte) 0x00 };

		byte[] resp = null;
		try {
			resp = connection.exchangeData(SELECT);
		} catch (Exception e) {
			log.println("could not send apdu: " + e.toString());
		}

		// this is not working yet :-( the card should respond with
		// 90 00, yet it responds with 6a 7a
		
		if (resp != null) {
			log.println("returned " + resp.length + " bytes");
			for (int i = 0; i < resp.length; i++)
				log.print(resp[i] + " ");
			log.println("<end>");
		}

		try {
			connection.close();
		} catch (IOException ignored) {
		}
		log.println("closed connection");
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
					} catch (InterruptedException ignored) {
					}
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

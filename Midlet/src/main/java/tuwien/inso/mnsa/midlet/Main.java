package tuwien.inso.mnsa.midlet;

import java.io.IOException;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.contactless.DiscoveryManager;
import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetProperties;
import javax.microedition.contactless.TargetType;
import javax.microedition.contactless.sc.ISO14443Connection;
import javax.microedition.io.Connector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

public class Main extends MIDlet implements TargetListener {

	private static final Logger LOG = Logger.getLogger("Main");

	private Form form;

	public void startApp() {
		form = new Form("Cardterminal Form");
		Display.getDisplay(this).setCurrent(form);
		Logger.init(form);

		final Command exitCommand = new Command("Exit", Command.EXIT, 1);
		form.addCommand(exitCommand);

		form.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == exitCommand) {
					LOG.print("Exiting...");
					notifyDestroyed();
				}
			}
		});

		try {
			DiscoveryManager dm = DiscoveryManager.getInstance();
			dm.addTargetListener(this, TargetType.ISO14443_CARD);
			dm.addTargetListener(this, TargetType.NDEF_TAG);
			dm.addTargetListener(this, TargetType.RFID_TAG);
		} catch (ContactlessException ce) {
			LOG.print("Unable to register TargetListener: " + ce.toString());
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public void targetDetected(TargetProperties[] targetProperties) {
		if (targetProperties.length == 0) {
			return;
		}

		TargetProperties tmp = targetProperties[0];
		LOG.print("UID read: " + tmp.getUid());
		boolean isIso = false;

		for (int i = 0; i < tmp.getTargetTypes().length; i++) {
			TargetType type = tmp.getTargetTypes()[i];
			LOG.print("target type: " + type.toString());

			if (type.equals(TargetType.ISO14443_CARD))
				isIso = true;
		}

		for (int i = 0; i < tmp.getConnectionNames().length; i++) {
			LOG.print("conn name: " + tmp.getConnectionNames()[i].getName());
		}

		if (!isIso)
			return;

		ISO14443Connection connection;
		try {
			Class firstConnection = tmp.getConnectionNames()[0];
			String url = tmp.getUrl(firstConnection);
			LOG.print("openning connection " + url + "...");
			connection = (ISO14443Connection) Connector.open(url);
		} catch (IOException e) {
			LOG.print("could not create iso14443 connection: " + e.toString());
			return;
		}

		LOG.print("opened connection");

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
			LOG.print("could not send apdu: " + e.toString());
		}

		// this is not working yet :-( the card should respond with
		// 90 00, yet it responds with 6a 7a

		if (resp != null) {
			LOG.print("returned " + resp.length + " bytes");
			for (int i = 0; i < resp.length; i++)
				LOG.print(resp[i] + " ");
			LOG.print("<end>");
		}

		try {
			connection.close();
		} catch (IOException ignored) {
		}
		LOG.print("closed connection");
	}
}

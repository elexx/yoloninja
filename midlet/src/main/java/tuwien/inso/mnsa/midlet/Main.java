package tuwien.inso.mnsa.midlet;

import javax.microedition.contactless.ContactlessException;
import javax.microedition.contactless.DiscoveryManager;
import javax.microedition.contactless.TargetListener;
import javax.microedition.contactless.TargetType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

import tuwien.inso.mnsa.midlet.connection.CardConnector;
import tuwien.inso.mnsa.midlet.connection.USBConnection;
import tuwien.inso.mnsa.midlet.debug.Logger;
import tuwien.inso.mnsa.midlet.debug.UidPrinter;

public class Main extends MIDlet {

	private static final Logger LOG = Logger.getLogger("Main");

	private Form form;
	private CardConnector cardConnector;
	private USBConnection usbConnection;

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

		cardConnector = new CardConnector();
		usbConnection = new USBConnection(cardConnector);

		TargetListener uidPrinter = new UidPrinter();

		new Thread(usbConnection).start();

		try {
			DiscoveryManager dm = DiscoveryManager.getInstance();
			dm.addTargetListener(cardConnector, TargetType.ISO14443_CARD);
			dm.addTargetListener(uidPrinter, TargetType.NDEF_TAG);
			dm.addTargetListener(uidPrinter, TargetType.RFID_TAG);
		} catch (ContactlessException ce) {
			LOG.print("Unable to register TargetListener: " + ce.toString());
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		cardConnector.close();
		usbConnection.stop();
		usbConnection.close();
	}
}

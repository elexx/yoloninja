package tuwien.inso;

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

/**
 * MIDlet reads System properties Comm ports, opens CommConnection to port COM0
 * attempts to get and set Baud rate, writes text string over CommConnection
 * then listens and displays bytes received from CommConnection.
 */
public class USBTest extends MIDlet implements CommandListener {
    private final Command startCommand;
    private final Command exitCommand;
    private final Display display;
    private TextBox textbox;
    private CommConnection comm = null;
    private InputStream is = null;
    private OutputStream os = null;
    private Thread thread;

    public USBTest() {
        display = Display.getDisplay(this);
        startCommand = new Command ("Start", Command.SCREEN, 1);
        exitCommand = new Command("Exit", Command.EXIT, 1);
    }

    /**
     * Start up the MIDlet by creating the TextBox and associating
     * the exit command and listener.
     */
    public void startApp() {
        textbox = new TextBox("USBTest", "", 8000, 0);
        textbox.addCommand(startCommand);
        textbox.addCommand(exitCommand);
        textbox.setCommandListener(this);
        display.setCurrent(textbox);
        openUSBConnection();
    }

    public void pauseApp() { }

    /**
     * Destroy must cleanup everything not handled by the garbage collector.
     */
    public void destroyApp(boolean unconditional) { }

    /*
     * Respond to commands, including exit
     * On the exit command, cleanup and notify that the MIDlet has been destroyed.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == startCommand) {
            thread = new Thread() {
                public void run() {
                    listenUSB();
                }
            };
            thread.start();
        }
        if (c == exitCommand) {
            Tracer.outln("Exiting...", textbox);
            notifyDestroyed();
        }
    }

    public void openUSBConnection() {
        try {
			Tracer.outln("USBTestMVN: Testing CommConnection for USB ports", textbox);
            Tracer.outln("Calling System.getProperty(microedition.commports)", textbox);
            String ports = System.getProperty("microedition.commports");
            Tracer.outln("got ports="+ports, textbox);
            int index = ports.indexOf("USB", 0);
            if(index == -1) {
                throw new RuntimeException("No USB port found in the device");
            }
            Tracer.outln("Going to call Connector.open(comm:USB2)", textbox);
            comm = (CommConnection)Connector.open("comm:USB2");
            int orgBaudRate = comm.getBaudRate();
            Tracer.outln("Calling getBaudRate(): " + orgBaudRate, textbox);
            Tracer.outln("Calling openOutputStream()", textbox);
            os = comm.openOutputStream();
            Tracer.outln("writing to USB2", textbox);
            String text = "Hello from USBTest MIDlet! Anybody there at USB?\r\n";
            os.write(text.getBytes());
            os.flush();
            Tracer.outln("Calling openInputStream()", textbox);
            is = comm.openInputStream();
            Tracer.outln("attempting read up to 500 bytes from USB2 (quits when received string:  EXIT and <enter>)\n", textbox);
        } catch (IOException e) {
            Tracer.outln("IOException: " + e.getMessage(), textbox);
            return;
        }
    }

    public void listenUSB() {
        Tracer.outln("Listening USB port...", textbox);
        try {
            byte[] buffer = new byte[500];
            StringBuffer message = new StringBuffer();
            for(int i = 0; i < 500;) {
                try {
                    Thread.sleep(10);
                }
                catch(InterruptedException ie) { }
                int available = is.available();
                if(available == 0) {
                    continue;
                }
                String outText = "";
                int count = is.read(buffer, i, available);
                if(count > 0) {
                    outText = new String(buffer, i, count);
                    i = i + count;
                    message.append(outText);
                    if (outText.endsWith("\n")) {
                        String messageString = message.toString();
                        Tracer.outln("Message: " + messageString, textbox);
                        message.delete(0, message.length());
                    }
                }
                String total = new String(buffer,0,i);
                if ((i > 3) && (-1 != total.indexOf("EXIT\r\n"))) {
                    Tracer.outln("Closing...", textbox);
                    break;
                }
            }
            Tracer.outln("Calling OutputStream.close()", textbox);
            os.close();
            Tracer.outln("Calling InputStream.close()", textbox);
            is.close();
            Tracer.outln("Calling connection.close()", textbox);
            comm.close();
        }
        catch (IOException ioe) {
            Tracer.outln("IOException: " + ioe.getMessage(), textbox);
        }
        Tracer.outln("SUCCEEDED.", textbox);
    }
}

    class Tracer {
        private static TextBox myTextBox;
        public Tracer (TextBox t) {
            myTextBox = t;
        }

        public static void outln (String msg, TextBox t) {
            t.setString((t.getString()).concat(msg + "\n"));
        }

        public static void out (String msg, TextBox t) {
            t.setString((t.getString()).concat(msg));
        }
    }

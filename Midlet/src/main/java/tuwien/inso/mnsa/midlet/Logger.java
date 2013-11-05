package tuwien.inso.mnsa.midlet;

import javax.microedition.lcdui.TextBox;

public class Logger {
	private final TextBox textBox;

	public Logger(TextBox textBox) {
		this.textBox = textBox;
	}

	public void println(String msg) {
		print(msg + "\n");
	}

	public void print(String msg) {
		textBox.setString((textBox.getString()).concat(msg));
	}
}

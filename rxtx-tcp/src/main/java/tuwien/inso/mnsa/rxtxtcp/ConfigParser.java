package tuwien.inso.mnsa.rxtxtcp;

import gnu.io.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class ConfigParser {
	public static final String PORTS_CONF = "ports.conf";

	public static List<PortDefinition> getPortDefinitions() throws IOException {
		ClassLoader classLoader = ConfigParser.class.getClassLoader();

		if (new File(PORTS_CONF).canRead()) {
			try (InputStream is = new FileInputStream(PORTS_CONF)) {
				return getPortDefinitions(is);
			}
		}

		try (InputStream is = classLoader.getResourceAsStream(PORTS_CONF)) {
			if (is != null)
				return getPortDefinitions(is);
		}

		throw new FileNotFoundException(PORTS_CONF + " not found (neither in working directory nor in classpath)");
	}

	public static List<PortDefinition> getPortDefinitions(InputStream is) {
		List<PortDefinition> ports = new LinkedList<>();
		try (Scanner sc = new Scanner(is, "utf-8")) {
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				List<String> parts = explode(line);

				if (parts.size() == 0)
					continue;
				else if (parts.size() == 1)
					throw new IllegalArgumentException("line \"" + line + "\" is corrupt (at least two parameters required)");

				int port = Integer.parseInt(parts.get(0));
				String device = parts.get(1);

				int baud = 9600;
				int data = 8;
				int parity;
				int stop;
				int flow;

				String parity_ = "none", stop_ = "1", flow_ = "none";

				if (parts.size() >= 3)
					baud = Integer.parseInt(parts.get(2));
				if (parts.size() >= 4)
					data = Integer.parseInt(parts.get(3));
				if (parts.size() >= 5)
					parity_ = parts.get(4);
				if (parts.size() >= 6)
					stop_ = parts.get(5);
				if (parts.size() >= 7)
					flow_ = parts.get(6);

				data = convertData(data);
				parity = convertParity(parity_);
				stop = convertStop(stop_);
				flow = convertFlow(flow_);

				ports.add(new PortDefinition(port, device, baud, data, parity, stop, flow));
			}
		}
		return ports;
	}

	static List<String> explode(String line) {
		List<String> list = new LinkedList<>();
		StringBuilder currentPart = new StringBuilder();

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\\') {
				if (i + 1 >= line.length()) {
					// lines ending with with a single \ have their
					// trailing \ silently removed
				} else {
					char escaped = line.charAt(++i);

					currentPart.append(escaped);
				}
			} else if (c == ':') {
				list.add(currentPart.toString().trim());
				currentPart = new StringBuilder();
			} else if (c == '#') {
				break;
			} else {
				currentPart.append(c);
			}
		}

		String suffix = currentPart.toString().trim();
		if (suffix.length() > 0)
			list.add(suffix);

		return list;
	}

	static int convertData(int data) {
		switch (data) {
		case 5:
			return SerialPort.DATABITS_5;
		case 6:
			return SerialPort.DATABITS_6;
		case 7:
			return SerialPort.DATABITS_7;
		case 8:
			return SerialPort.DATABITS_8;
		default:
			throw new IllegalArgumentException("Invalid data bits: " + data);
		}
	}

	static int convertParity(String parity) {
		switch (parity.toLowerCase()) {
		case "none":
		case "n":
			return SerialPort.PARITY_NONE;

		case "even":
		case "e":
			return SerialPort.PARITY_EVEN;

		case "odd":
		case "o":
			return SerialPort.PARITY_ODD;

		case "space":
		case "s":
			return SerialPort.PARITY_SPACE;

		case "mark":
		case "m":
			return SerialPort.PARITY_MARK;

		default:
			throw new IllegalArgumentException("Invalid parity definition: " + parity);
		}
	}

	static int convertStop(String stop) {
		switch (stop) {
		case "1":
			return SerialPort.STOPBITS_1;

		case "1.5":
		case "1,5":
			return SerialPort.STOPBITS_1_5;

		case "2":
			return SerialPort.STOPBITS_2;

		default:
			throw new IllegalArgumentException("Invalid stop bit definition: " + stop);
		}
	}

	static int convertFlow(String flow) {
		switch (flow.toLowerCase()) {
		case "none":
		case "n":
			return SerialPort.FLOWCONTROL_NONE;

		case "rtscts-in":
			return SerialPort.FLOWCONTROL_RTSCTS_IN;

		case "rtscts-out":
			return SerialPort.FLOWCONTROL_RTSCTS_OUT;

		case "xonxoff-in":
			return SerialPort.FLOWCONTROL_XONXOFF_IN;

		case "xonxoff-out":
			return SerialPort.FLOWCONTROL_XONXOFF_OUT;

		default:
			throw new IllegalArgumentException("Invalid flow control definition: " + flow);
		}
	}
}

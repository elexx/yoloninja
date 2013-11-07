package tuwien.inso.mnsa.rxtxtcp;

import static org.junit.Assert.assertArrayEquals;
import gnu.io.SerialPort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ConfigParserTest {

	@Test
	public void testExplodeRegular() {
		assertArrayEquals(new String[] { "a", "b", "c", "d" }, ConfigParser.explode("a:b:c:d").toArray());
		assertArrayEquals(new String[] { "a", "b", "c", "d" }, ConfigParser.explode("a:b:c:d:").toArray());
		assertArrayEquals(new String[] { "a", "b", "c", "d" }, ConfigParser.explode("a:b:c:d: ").toArray());
	}

	@Test
	public void testExplodeEmpty() {
		assertArrayEquals(new String[] {}, ConfigParser.explode("").toArray());
		assertArrayEquals(new String[] {}, ConfigParser.explode(" ").toArray());
		assertArrayEquals(new String[] {}, ConfigParser.explode("\t").toArray());
		assertArrayEquals(new String[] {}, ConfigParser.explode("# bla").toArray());
		assertArrayEquals(new String[] {}, ConfigParser.explode(" # bla").toArray());
	}

	@Test
	public void testExplodeEndLineComment() {
		assertArrayEquals(new String[] { "a", "b", "c" }, ConfigParser.explode("a:b:c # comment").toArray());
		assertArrayEquals(new String[] { "a", "b", "c" }, ConfigParser.explode("a:b:c# comment").toArray());
		assertArrayEquals(new String[] { "a", "b", "c" }, ConfigParser.explode("a:b:c:# comment").toArray());
	}

	@Test
	public void testExplodeEscaping() {
		assertArrayEquals(new String[] { "a", "b", "c:d" }, ConfigParser.explode("a:b:c\\:d").toArray());
		assertArrayEquals(new String[] { "a", "b", "c\\d" }, ConfigParser.explode("a:b:c\\\\d").toArray());
		assertArrayEquals(new String[] { "a", "b", "c#d" }, ConfigParser.explode("a:b:c\\#d").toArray());
	}

	@Test
	public void testParse() {
		List<PortDefinition> got = ConfigParser.getPortDefinitions(build("100:COM1", "101:COM2", "102:COM3:9600", "103:COM4:9600:8", "103:COM5:9600:7:n", "104:COM6:9600:6:e:1", "105:COM7:9600:5:o:1.5:none", "106:COM8:9600:8:m:2:rtscts-in"));

		List<PortDefinition> expected = new ArrayList<>(8);
		expected.add(new PortDefinition(100, "COM1", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(101, "COM2", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(102, "COM3", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(103, "COM4", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(103, "COM5", 9600, SerialPort.DATABITS_7, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(104, "COM6", 9600, SerialPort.DATABITS_6, SerialPort.PARITY_EVEN, SerialPort.STOPBITS_1, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(105, "COM7", 9600, SerialPort.DATABITS_5, SerialPort.PARITY_ODD, SerialPort.STOPBITS_1_5, SerialPort.FLOWCONTROL_NONE));
		expected.add(new PortDefinition(106, "COM8", 9600, SerialPort.DATABITS_8, SerialPort.PARITY_MARK, SerialPort.STOPBITS_2, SerialPort.FLOWCONTROL_RTSCTS_IN));

		assertArrayEquals(expected.toArray(), got.toArray());
	}

	private InputStream build(String... strings) {
		StringBuilder builder = new StringBuilder();
		for (String line : strings) {
			builder.append(line);
			builder.append('\n');
		}

		try {
			return new ByteArrayInputStream(builder.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unexcpeted exception: " + e, e);
		}
	}

}

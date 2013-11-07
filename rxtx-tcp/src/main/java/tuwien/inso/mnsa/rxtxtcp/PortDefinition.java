package tuwien.inso.mnsa.rxtxtcp;

public class PortDefinition {
	private int tcpPort;
	private String deviceName;
	private int baud, data, parity, stop, flow;

	PortDefinition(int tcpPort, String deviceName, int baud, int data, int parity, int stop, int flow) {
		this.tcpPort = tcpPort;
		this.deviceName = deviceName;
		this.baud = baud;
		this.data = data;
		this.parity = parity;
		this.stop = stop;
		this.flow = flow;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public int getBaud() {
		return baud;
	}

	public int getData() {
		return data;
	}

	public int getParity() {
		return parity;
	}

	public int getStop() {
		return stop;
	}

	public int getFlow() {
		return flow;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PortDefinition))
			return false;

		PortDefinition that = (PortDefinition) obj;

		boolean ok = this.deviceName.equals(that.deviceName);
		ok &= this.tcpPort == that.tcpPort;
		ok &= this.baud == that.baud;
		ok &= this.data == that.data;
		ok &= this.parity == that.parity;
		ok &= this.stop == that.stop;
		ok &= this.flow == that.flow;
		return ok;
	}
	
	@Override
	public int hashCode() {
		return this.deviceName.hashCode();
	}
}

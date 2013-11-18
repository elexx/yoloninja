# yoloninja - A Nokia 6212 NFC cardreader and maybe more

Developed as part of the exercises of the "(Mobile) Network Services and Applications" lecture at Vienna University of Technology (TU Vienna) by Michael Borkowski and Alexander Falb.

## Native requirements

Since the responsible serial communication library, NRJavaSerial, loads its native dependencies dynamically at runtime, no native pre-requisites for this project exist.

## Setup

Since the developement phone (Nokia 6212) has limited driver support for non-Windows operating systems (regarding the serial interface), we modularized the project a little bit:

Instead of communicating directly with a serial port, the Nokia SPI communicates (via TCP/IP) with a proxy-like server, which relays the data from and to a serial port (essentially an RS232-over-IP connection). This server module is called "rxtx-tcp".

### Step 1: Setup the rxtx-tcp module (the RS232-proxy)

The rxtx-tcp module looks for a file called ports.conf. This file is looked for in the working directory. If such file is not found, it is looked for in the classpath (we provided a "default" file which uses COM1). In this file, one can setup the ports (serial and network) which are "connected". The file is parsed line-by-line and the lines have the following format:

	<tcp port>:<serial device name>:<baud>:<data bits>:<parity>:<stop bits>:<flow-control>

Exapmles:

	7989:COM1:9600:8:n:1
	7989:/dev/ttyUSB1:9600:8:n:1
	7989:COM3

Note that all parameters except for the TCP port and the serial device name are optional and filled with default values (`9600:8:n:1`) if ommitted.

### Step 2: Start the rxtx-tcp module

After configuring it, the rxtx-tcp module can be started:

	> java -jar rxtx-tcp-1.0-SNAPSHOT-jar-with-dependencies.jar
	20:13:44.632 [main] INFO  tuwien.inso.mnsa.rxtxtcp.Server - Listing ports:
	20:13:44.635 [main] INFO  tuwien.inso.mnsa.rxtxtcp.Server - COM1 (1, current owner null)
	20:13:44.635 [main] INFO  tuwien.inso.mnsa.rxtxtcp.Server - COM4 (1, current owner null)
	20:13:44.635 [main] INFO  tuwien.inso.mnsa.rxtxtcp.Server - Port listing done, found 2 ports.
	20:13:44.644 [main] DEBUG tuwien.inso.mnsa.rxtxtcp.Server - Proxying clients from 7989 to COM1


### Step 3: Setup the SPI provider

One would usually configure the SPI provider to use the correct serial port, however, since we use RS232-over-network, we will configure the SPI provider to use the correct TCP connection (host/port combination). This is done by passing the terminal an `InetSocketAddress` object.

Exapmle:

	TerminalFactory terminalFactory = TerminalFactory.getInstance("NokiaProvider", new InetSocketAddress("192.168.1.184", 7989));

This command instanciates a new connection to the given host/port combination. As defined in the Java specifications, a host name can also be used instead of an IP address:

	TerminalFactory terminalFactory = TerminalFactory.getInstance("NokiaProvider", new InetSocketAddress("athena", 7989));

After this, the terminalFactory can be used normally.

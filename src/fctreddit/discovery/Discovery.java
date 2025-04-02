package fctreddit.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * <p>
 * A class to perform service discovery, based on periodic service contact
 * endpoint announcements over multicast communication.
 * </p>
 * 
 * <p>
 * Servers announce their *name* and contact *uri* at regular intervals. The
 * server actively collects received announcements.
 * </p>
 * 
 * <p>
 * Service announcements have the following format:
 * </p>
 * 
 * <p>
 * &lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;
 * </p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	// The pre-aggreed multicast endpoint assigned to perform discovery.
	// Allowed IP Multicast range: 224.0.0.1 - 239.255.255.255
	static final public InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_ANNOUNCE_PERIOD = 1000;
	static final int DISCOVERY_RETRY_TIMEOUT = 5000;
	static final int MAX_DATAGRAM_SIZE = 65536;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private final InetSocketAddress addr;
	private final String serviceName;
	private final String serviceURI;
	private final MulticastSocket ms;

	// Map to store service announcements with their last received timestamp
	private final Map<String, Map<URI, Long>> services = new ConcurrentHashMap<>();

	/**
	 * @param serviceName the name of the service to announce
	 * @param serviceURI  an uri string - representing the contact endpoint of the
	 *                    service being announced
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 */
	public Discovery(InetSocketAddress addr, String serviceName, String serviceURI) throws SocketException, UnknownHostException, IOException {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI = serviceURI;

		if (this.addr == null) {
			throw new RuntimeException("A multinet address has to be provided.");
		} 
		
		this.ms = new MulticastSocket(addr.getPort());
		NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		this.ms.setNetworkInterface(networkInterface);
		this.ms.joinGroup(addr, networkInterface);
	}

	public Discovery(InetSocketAddress addr) throws SocketException, UnknownHostException, IOException {
		this(addr, null, null);
	}

	/**
	 * Starts sending service announcements at regular intervals...
	 * @throws IOException 
	 */
	public void start() {
		//If this discovery instance was initialized with information about a service, start the thread that makes the
		//periodic announcement to the multicast address.
		if (this.serviceName != null && this.serviceURI != null) {

			Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", addr, serviceName,
					serviceURI));

			byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
			DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

			try {
				// start thread to send periodic announcements
				new Thread(() -> {
					for (;;) {
						try {
							ms.send(announcePkt);
							//Log.info("Sent announcement: " + new String(announceBytes));
							Thread.sleep(DISCOVERY_ANNOUNCE_PERIOD);
						} catch (Exception e) {
							Log.severe("Error sending announcement: " + e.getMessage());
						}
					}
				}).start();
			} catch (Exception e) {
				Log.severe("Error starting announcement thread: " + e.getMessage());
			}
		}

		// start thread to collect announcements received from the network.
		new Thread(() -> {
			DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
			for (;;) {
				try {
					pkt.setLength(MAX_DATAGRAM_SIZE);
					ms.receive(pkt);
					String msg = new String(pkt.getData(), 0, pkt.getLength());
					//Log.info("Received announcement: " + msg);
					String[] msgElems = msg.split(DELIMITER);
					if (msgElems.length == 2) { // periodic announcement
						String receivedServiceName = msgElems[0];
						URI receivedServiceURI = URI.create(msgElems[1]);
						long currentTime = System.currentTimeMillis();

						services.computeIfAbsent(receivedServiceName, k -> new ConcurrentHashMap<>())
								.put(receivedServiceURI, currentTime);

						//System.out.printf("FROM %s (%s) : %s\n", pkt.getAddress().getHostName(),
								//pkt.getAddress().getHostAddress(), msg);

					}
				} catch (IOException e) {
					Log.warning("Error receiving announcement: " + e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Returns the known services.
	 * 
	 * @param serviceName the name of the service being discovered
	 * @param minReplies  - minimum number of requested URIs. Blocks until the
	 *                    number is satisfied.
	 * @return an array of URI with the service instances discovered.
	 * 
	 */
	public URI[] knownUrisOf(String serviceName, int minReplies) {
		while (true) {
			Map<URI, Long> serviceInstances = services.get(serviceName);
			if (serviceInstances != null) {
				long currentTime = System.currentTimeMillis();
				serviceInstances.entrySet().removeIf(entry -> currentTime - entry.getValue() > DISCOVERY_RETRY_TIMEOUT);

				if (serviceInstances.size() >= minReplies) {
					return serviceInstances.keySet().toArray(new URI[0]);
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		return new URI[0];
	}

	// Main just for testing purposes
	public static void main(String[] args) throws Exception {
		//Discovery discovery = new Discovery(DISCOVERY_ADDR, "test",
		//		"http://" + InetAddress.getLocalHost().getHostAddress());
		//discovery.start();
		
	}
}

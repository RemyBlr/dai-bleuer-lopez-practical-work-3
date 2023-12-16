package ch.heigvd;

import ch.heigvd.handlers.ClientHandler;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Command(
        name = "event-server",
        description = "Start an event server to receive multicast and unicast messages"
)
public class EventNotifier implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "UDP port to listen on for unicast")
    private int unicastPort = 9876;

    @Option(names = {"-m", "--multicast"}, description = "Multicast address and port (e.g., 230.0.0.1:9877)")
    private String multicastAddress = "239.0.0.1:9877";

    @Option(names = {"-t", "--threads"}, description = "Number of threads to use")
    private int threadsNbr = 10;

    private static final int MAX_UDP_PACKET_SIZE = 65507;

    @Override
    public Integer call() {
        ExecutorService executor = null;

        try (DatagramSocket unicastSocket = new DatagramSocket(unicastPort);
             MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(multicastAddress.split(":")[1]))) {

            InetAddress multicastGroup = InetAddress.getByName(multicastAddress.split(":")[0]);
            multicastSocket.joinGroup(multicastGroup);

            executor = Executors.newFixedThreadPool(threadsNbr);

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + unicastPort;
            System.out.println("Event server started (" + myself + ")");
            System.out.println("Multicast group: " + multicastAddress);

            byte[] multicastReceiveData = new byte[MAX_UDP_PACKET_SIZE];
            byte[] unicastReceiveData = new byte[MAX_UDP_PACKET_SIZE];

            while (true) {
                // Multicast messages
                DatagramPacket multicastPacket = new DatagramPacket(multicastReceiveData, multicastReceiveData.length);
                multicastSocket.receive(multicastPacket);
                executor.submit(new MulticastClientHandler(multicastPacket,
                                                           multicastAddress));

                // Unicast messages
                DatagramPacket unicastPacket = new DatagramPacket(unicastReceiveData, unicastReceiveData.length);
                unicastSocket.receive(unicastPacket);
                executor.submit(new UnicastClientHandler(unicastPacket, myself));
            }

        } catch (Exception e) {
            // Log the exception using a logging framework
            e.printStackTrace();
            return 1;
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    static class UnicastClientHandler extends ClientHandler {
        public UnicastClientHandler(DatagramPacket packet, String myself) {
            super(packet, myself);
        }
    }

    static class MulticastClientHandler extends ClientHandler {
        public MulticastClientHandler(DatagramPacket packet, String myself) {
            super(packet, myself);
            super.run();
            System.out.println("Multicast receiver (" + myself + ") received " +
                                       "message: " + packet.toString());
        }
    }
}


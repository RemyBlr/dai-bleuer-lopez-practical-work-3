package ch.heigvd;

import ch.heigvd.handlers.ClientHandler;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Command(name = "event-server", description = "Start an event server to receive multicast and unicast messages")
public class EventNotifier implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "UDP port to listen on for unicast")
    private int unicastPort = 9876;

    @Option(names = {"-m", "--multicast"}, description = "Multicast address and port (e.g., 230.0.0.1:9877)")
    private String multicastAddress = "239.0.0.1:9877";

    @Option(names = {"-t", "--threads"}, description = "Number of threads to use")
    private int threadsNbr = 10;

    private static final int MAX_UDP_PACKET_SIZE = 65507;

    private static List<Event> receivedEvents = new ArrayList<>();

    @Override
    public Integer call() {
        ExecutorService executor = null;

        try (DatagramSocket unicastSocket = new DatagramSocket(unicastPort);
             MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(multicastAddress.split(":")[1]))) {

            InetAddress multicastGroup = InetAddress.getByName(multicastAddress.split(":")[0]);
            multicastSocket.joinGroup(multicastGroup);

            executor = Executors.newFixedThreadPool(threadsNbr);

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" +
                    unicastPort;
            System.out.println("Event server started (" + myself + ")");
            System.out.println("Multicast group: " + multicastAddress);

            byte[] multicastReceiveData = new byte[MAX_UDP_PACKET_SIZE];
            byte[] unicastReceiveData = new byte[MAX_UDP_PACKET_SIZE];

            // Thread for multicast messages
            executor.submit(() -> {
                while (true) {
                    DatagramPacket multicastPacket = new DatagramPacket(multicastReceiveData, multicastReceiveData.length);
                    multicastSocket.receive(multicastPacket);
                    handleMulticastMessage(multicastPacket);
                }
            });

            // Thread for unicast messages
            executor.submit(() -> {
                while (true) {
                    DatagramPacket unicastPacket = new DatagramPacket(unicastReceiveData, unicastReceiveData.length);
                    unicastSocket.receive(unicastPacket);
                    handleUnicastMessage(unicastPacket, myself);
                }
            });

            // Keep the main thread alive
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    private void handleMulticastMessage(DatagramPacket packet) {
        Event event = parseEventFromMessage(new String(packet.getData(),
                                                       packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8));

        if (event != null)
            receivedEvents.add(event);

        System.out.println("Multicast receiver received " +
                                   "message: \n" + event);
    }

    private void handleUnicastMessage(DatagramPacket packet, String myself) {
        System.out.println("Unicast receiver received " +
                                   "message: " + packet.toString());

        String message = new String(packet.getData(),
                                    packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

        byte[] responseData = new byte[MAX_UDP_PACKET_SIZE];

        if (message.equals("list")) {

            responseData = receivedEvents.toString().getBytes();

        } else {
            Event eventAsked =  getEventInfo(message);
            if (eventAsked != null)
                responseData = eventAsked.toString().getBytes();
            else
                // empty response
                responseData=new byte[0];
        }

        try (DatagramSocket socket = new DatagramSocket()) {

            DatagramPacket responsePacket = new DatagramPacket(responseData,
                                                               responseData.length, packet.getAddress(), packet.getPort());
            socket.send(responsePacket);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private Event getEventInfo(String eventName) {
        for (Event event : receivedEvents) {
            if (event.getName().equalsIgnoreCase(eventName)) {
                return event;
            }
        }

        return null;
    }


    private static Event parseEventFromMessage(String message) {
        String[] parts = message.split("\n");
        if (parts.length != 4) {
            System.out.println("Invalid event format.");
            return null;
        }

        String eventName = parts[0].split(":")[1].trim();
        String eventDate = parts[1].split(":")[1].trim();
        String eventLocation = parts[2].split(":")[1].trim();
        String eventDescription = parts[3].split(":")[1].trim();

        return new Event(eventName, eventDate, eventLocation, eventDescription);
    }
}


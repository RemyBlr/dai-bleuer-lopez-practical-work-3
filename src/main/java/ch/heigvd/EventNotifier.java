package ch.heigvd;

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

/**
 * This class implements a command that can be used to start an event server to receive multicast and unicast messages
 * @Authors Bleuer Rémy, Lopez Esteban
 */
@Command(name = "event-server", description = "Start an event server to receive multicast and unicast messages")
public class EventNotifier implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "UDP port to listen on for unicast")
    private int unicastPort = 9876;

    @Option(names = {"-m", "--multicast"}, description = "Multicast address and port (e.g., 230.0.0.1:9877)")
    private String multicastAddress = "239.0.0.1:9877";

    @Option(names = {"-t", "--threads"}, description = "Number of threads to use")
    private int threadsNbr = 10;

    // Maximum size of a UDP packet
    private static final int MAX_UDP_PACKET_SIZE = 65507;

    // List of received events
    private static List<Event> receivedEvents = new ArrayList<>();

    @Override
    public Integer call() {
        ExecutorService executor = null;

        // Create a DatagramSocket for receiving unicast messages
        try (DatagramSocket unicastSocket = new DatagramSocket(unicastPort);
             MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(multicastAddress.split(":")[1]))) {

            // Specify the multicast group and port
            InetAddress multicastGroup = InetAddress.getByName(multicastAddress.split(":")[0]);
            multicastSocket.joinGroup(multicastGroup);

            // Create a thread pool
            executor = Executors.newFixedThreadPool(threadsNbr);

            // Display server information
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + unicastPort;
            System.out.println("Event server started (" + myself + ")");
            System.out.println("Multicast group: " + multicastAddress);

            // Buffers for receiving data
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

    /**
     * This method handles a multicast message
     * @param packet the packet containing the multicast message
     */
    private void handleMulticastMessage(DatagramPacket packet) {
        Event event = parseEventFromMessage(new String(packet.getData(),
                                                       packet.getOffset(),
                                                       packet.getLength(),
                                                       StandardCharsets.UTF_8));

        if (event != null)
            receivedEvents.add(event);

        System.out.println("Multicast receiver received message: \n" + event);
    }

    /**
     * This method handles a unicast message
     * @param packet the packet containing the unicast message
     * @param myself the address of the server
     */
    private void handleUnicastMessage(DatagramPacket packet, String myself) {

        String message = new String(packet.getData(),
                packet.getOffset(),
                packet.getLength(),
                StandardCharsets.UTF_8);

        System.out.println("Unicast receiver received message: " + message);

        byte[] responseData = new byte[MAX_UDP_PACKET_SIZE];

        if (message.equals("list")) {
            // Display the list of events
            responseData = receivedEvents.toString().getBytes();

        } else {
            // Get the details of an event
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

    /**
     * This method gets the details of an event by its name
     * @param eventName the name of the event
     * @return the event with the specified name
     */
    private Event getEventInfo(String eventName) {
        for (Event event : receivedEvents) {
            if (event.getName().equalsIgnoreCase(eventName)) {
                return event;
            }
        }

        return null;
    }


    /**
     * This method parses an event from a messages
     * @param message the message to parse
     * @return the event contained in the message
     */
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


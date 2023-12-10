package ch.heigvd.receivers;

import ch.heigvd.Event;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Command(
        name = "unicast-receiver",
        description = "Start an UDP unicast receiver"
)
public class UnicastReceiver implements Callable<Integer> {

    @Option(names = {"-p", "--port"}, description = "UDP port to listen on")
    private int port = 9876; // Default port, change as needed

    @Option(names = {"-t", "--threads"}, description = "Number of threads to use")
    private int threadsNbr = 10;

    @Override
    public Integer call() {
        ExecutorService executor = null;

        try (DatagramSocket socket = new DatagramSocket(port)) {
            executor = Executors.newFixedThreadPool(threadsNbr);

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + port;
            System.out.println("Unicast receiver started (" + myself + ")");

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(packet);
                executor.submit(new ClientHandler(packet, myself));
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

    @Command(
            name = "get-event",
            description = "Get information about a specific event using unicast"
    )
    static class GetEventCommand implements Runnable {

        @Parameters(index = "0", description = "List of all events")
        private String eventsList;
        @Parameters(index = "1", description = "List all event names")
        private String eventName;
        @Parameters(index = "2", description = "List all event dates")
        private String eventDate;
        @Parameters(index = "3", description = "List all event locations")
        private String eventLocation;
        @Parameters(index = "4", description = "List all event descriptions")
        private String eventDescription;

        @Option(names = {"-i", "--info"}, description = "information about a specfic event (Event name)")
        private String eventInfo;

        @Override
        public void run() {
            // TODO logic to send a unicast request to the server
        }
    }

    static class ClientHandler implements Runnable {

        private final DatagramPacket packet;
        private final String myself;

        public ClientHandler(DatagramPacket packet, String myself) {
            this.packet = packet;
            this.myself = myself;
        }

        @Override
        public void run() {
            String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
            System.out.println("Unicast receiver (" + myself + ") received message: " + message);

            // Event example
            // Java Conference, 01.03.2023, Lausanne, une conférence sur le meilleur langage de programmation
            String[] parts = message.split(",");
            if (parts.length != 4) {
                System.out.println("Invalid request format.");
                return;
            }

            String eventName = parts[0];
            String eventDate = parts[1];
            String eventLocation = parts[2];
            String eventDescription = parts[3];

            Event event = retrieveEventDetails(eventName, eventDate, eventLocation, eventDescription);

            sendUnicastResponse(event, packet.getAddress(), packet.getPort());
        }
    }

    private static Event retrieveEventDetails(String eventName, String eventDate, String eventLocation, String eventDescription) {
        // TODO implement logic to retrieve event details from the server
        return null;
    }

    private static void sendUnicastResponse(Event event, InetAddress clientAddress, int clientPort) {
        if (event != null) {
            String response = event.toString();
            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);

            try (DatagramSocket responseSocket = new DatagramSocket()) {
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                responseSocket.send(responsePacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new UnicastReceiver())
                .addSubcommand(new GetEventCommand())
                .execute(args);
        System.exit(exitCode);
    }
}

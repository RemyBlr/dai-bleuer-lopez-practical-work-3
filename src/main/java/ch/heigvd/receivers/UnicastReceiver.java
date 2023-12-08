package ch.heigvd.receivers;

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

        @Parameters(index = "0", description = "Event name")
        private String eventName;

        @Parameters(index = "1", description = "Event date")
        private String eventDate;

        @Option(names = {"-l", "--location"}, description = "Event location")
        private String eventLocation;

        @Override
        public void run() {
            System.out.println("Getting information about event: " + eventName);
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
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new UnicastReceiver())
                .addSubcommand(new GetEventCommand())
                .execute(args);
        System.exit(exitCode);
    }
}

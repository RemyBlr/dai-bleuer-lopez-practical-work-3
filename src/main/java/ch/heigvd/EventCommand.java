package ch.heigvd;

import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;


@CommandLine.Command(
        name = "get-event",
        description = "Get information about a specific event using unicast"
)
public class EventCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--port"}, description = "UDP port to " +
            "send the request to")
    private int port = 9876; // Default port, change as needed

    @CommandLine.Option(names = {"-i", "--ip"}, description = "IP address to send the request to")
    private String ip = InetAddress.getLocalHost().getHostAddress(); //
    // Default port, change as needed

    public EventCommand() throws UnknownHostException {}

    private static final int MAX_UDP_PACKET_SIZE = 1024;


    @Override
    public void run() {
        System.out.println("Connecting to the server...");

        while (true) {
            System.out.println("1. List all events");
            System.out.println("2. Get details for a specific event by name");
            System.out.println("3. Exit");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    sendUnicastMessage("list");
                    break;
                case 2:
                    getEventDetailsByName();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Possible choices are listed above");
            }
        }
    }

    private void sendUnicastMessage(String request) {
        byte[] responseData = request.getBytes(StandardCharsets.UTF_8);

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket responsePacket = new DatagramPacket(responseData,
                                                               responseData.length, InetAddress.getByName(ip), port);
            socket.send(responsePacket);

            byte[] receiveData = new byte[MAX_UDP_PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);

            String message = new String(packet.getData(),
                                        packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

            if (message.isEmpty() || message.equals("[]")) {
                System.out.println("No events");
            } else {

                // message cleaning
                message = message.substring(1, message.length() - 1).trim();
                message = message.replace(",", "\n");


                if (request.equals("list")) {
                    System.out.println("List of all events:\n");
                } else {
                    System.out.println("Event details:\n");
                }
                System.out.println(message + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEventDetailsByName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event name: ");
        String eventName = scanner.nextLine();

        sendUnicastMessage(eventName);
    }
}

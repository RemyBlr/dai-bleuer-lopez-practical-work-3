package ch.heigvd;

import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;


/**
 * This class implements a command that can be used to get information about a specific event using unicast
 * It can also be used to list all events
 *
 * @Authors Bleuer Rémy, Lopez Esteban
 */
@CommandLine.Command(
        name = "get-event",
        description = "Get information about a specific event using unicast"
)
public class EventCommand implements Runnable {

    @CommandLine.Option(names = {"-p", "--port"}, description = "UDP port to send the request to")
    private int port = 9876; // Default port, change as needed

    @CommandLine.Option(names = {"-i", "--ip"}, description = "IP address to send the request to")
    private String ip = InetAddress.getLocalHost().getHostAddress(); //

    public EventCommand() throws UnknownHostException {
    }

    // Maximum size of a UDP packet
    private static final int MAX_UDP_PACKET_SIZE = 1024;


    @Override
    public void run() {
        System.out.println("Connecting to the server...");

        while (true) {
            // Display menu
            System.out.println("1. List all events");
            System.out.println("2. Get details for a specific event by name");
            System.out.println("3. Exit");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            // User choice
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

    /**
     * This method sends a unicast message to the server
     * @param request the request to send to the server
     */
    private void sendUnicastMessage(String request) {
        // Convert the request to a byte array
        byte[] responseData = request.getBytes(StandardCharsets.UTF_8);

        // Send the request
        try (DatagramSocket socket = new DatagramSocket()) {
            // Create a packet to send
            DatagramPacket responsePacket = new DatagramPacket(responseData,
                    responseData.length,
                    InetAddress.getByName(ip),
                    port);
            socket.send(responsePacket);

            // Receive the response
            byte[] receiveData = new byte[MAX_UDP_PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(packet);

            // Convert the response to a string
            String message = new String(packet.getData(),
                    packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

            // Check if the response is empty
            if (message.isEmpty() || message.equals("[]")) {
                System.out.println("No events");
            } else {

                // message cleaning
                message = message.substring(1, message.length() - 1).trim();
                message = message.replace(",", "\n");


                // Display the response
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

    /**
     * This method gets the details of an event by its name
     * It asks the user to enter the name of the event
     */
    private void getEventDetailsByName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event name: ");
        String eventName = scanner.nextLine();
        // Send the request
        sendUnicastMessage(eventName);
    }
}

package ch.heigvd.handlers;


import ch.heigvd.Event;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final DatagramPacket packet;
    private final String myself;

    public ClientHandler(DatagramPacket packet, String myself) {
        this.packet = packet;
        this.myself = myself;
    }

    @Override
    public void run() {
        String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
        System.out.println("Message recieved (" + myself + ") " +
                                   "received message: " + message);

        // Event example
        // Java Conference, 01.03.2023, Lausanne, une conférence sur le meilleur langage de programmation
        String[] parts = message.split("\n");
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

    private static Event retrieveEventDetails(String eventName, String eventDate, String eventLocation, String eventDescription) {
        // TODO implement logic to retrieve event details from the server
        return null;
    }
}

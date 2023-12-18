package ch.heigvd.handlers;


import ch.heigvd.Event;
import org.w3c.dom.events.EventTarget;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class ClientHandler implements Runnable {

    private final DatagramPacket packet;
    private final String myself;

    private static LinkedList<Event> events = new LinkedList<>();


    public ClientHandler(DatagramPacket packet, String myself) {
        this.packet = packet;
        this.myself = myself;
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

    public DatagramPacket getPacket() {
        return packet;
    }

    @Override
    public void run() {

       System.out.println("Unicast receiver received " +
                "message: " + packet.toString());
    }
}

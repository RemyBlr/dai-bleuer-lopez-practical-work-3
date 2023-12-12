package ch.heigvd.receivers;

import ch.heigvd.EventCommand;
import ch.heigvd.handlers.ClientHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    public static void main(String[] args) {
        int exitCode = new CommandLine(new UnicastReceiver())
                .addSubcommand(new EventCommand())
                .execute(args);
        System.exit(exitCode);
    }
}

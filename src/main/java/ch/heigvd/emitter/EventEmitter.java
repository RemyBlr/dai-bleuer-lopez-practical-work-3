package ch.heigvd.emitter;

import ch.heigvd.Event;
import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This class implements a command that can be used to start an event server to receive multicast and unicast messages
 *
 * @Authors Bleuer Rémy, Lopez Esteban
 */
@CommandLine.Command(
        name = "event-emitter",
        description = "Start an event server to receive multicast and unicast messages"
)
public class EventEmitter implements Runnable {

    @CommandLine.Option(names = {"-p", "--port"}, description = "UDP port to send the request to")
    private static int port = 9877;

    @CommandLine.Option(names = {"-m", "--multicast"}, description = "Multicast address")
    private static String multicastAddress = "239.0.0.1";

    @CommandLine.Option(names = {"-a", "--auto"}, description = "Automatically sends random events")
    private static boolean isAuto = false;

    @Override
    public void run() {
        try {
            // Specify the multicast group and port
            InetAddress multicastGroup = InetAddress.getByName(multicastAddress);

            // Create a DatagramSocket for sending multicast messages
            try (DatagramSocket socket = new DatagramSocket()) {
                // scanner for user input
                Scanner scanner;
                String message;

                while (true) {
                    // New event
                    Event event = null;

                    // --auto option
                    if (!isAuto) {
                        scanner = new Scanner(System.in);

                        // Read a message from the user
                        System.out.print("Entrer evenement à ajouter : ");
                        message = scanner.nextLine();

                        if ("exit".equalsIgnoreCase(message)) {
                            break;
                        }

                        String[] parts = message.split(",");

                        if (parts.length != 4) {
                            System.out.println("Invalid request format.");
                            continue;
                        }

                        // get individual parts of the message
                        String eventName = parts[0];
                        String eventDate = parts[1];
                        String eventLocation = parts[2];
                        String eventDescription = parts[3];

                        event = new Event(eventName, eventDate, eventLocation, eventDescription);

                    } else {
                        // Random strings to create random events
                        String[] cities = {"Lausanne", "Geneve", "Zurich",
                                "Berne", "Fribourg", "Sion", "Neuchatel",
                                "Bale", "Lucerne", "Lugano", "Bellinzone",
                                "Saint-Gall", "Coire", "Soleure", "Schaffhouse",
                                "Herisau", "Appenzell", "Altdorf", "Sarnen", "Glaris",
                                "Aarau", "Frauenfeld", "Delemont", "Porrentruy", "Bienne",
                                "Yverdon", "Vevey", "Montreux", "Nyon", "Morges", "Sierre",
                                "Martigny", "Monthey", "Bulle", "Payerne", "Aigle", "Sion", "Sierre"};

                        String[] dates = {"01.01.2021", "02.01.2021", "03.01.2021", "04.01.2021", "05.01.2021",
                                "06.01.2021", "07.01.2021", "08.01.2021", "09.01.2021", "10.01.2021", "11.01.2021",
                                "12.01.2021", "13.01.2021", "14.01.2021", "15.01.2021", "16.01.2021", "17.01.2021",
                                "18.01.2021", "19.01.2021", "20.01.2021", "21.01.2021", "22.01.2021", "23.01.2021",
                                "24.01.2021", "25.01.2021", "26.01.2021", "27.01.2021", "28.01.2021", "29.01.2021",
                                "30.01.2021", "31.01.2021"};

                        String[] eventName = {"Java Conference", "C++ Conference", "Python Conference", "C# Conference",
                                "PHP Conference", "HTML Conference", "CSS Conference", "JavaScript Conference",
                                "SQL Conference", "Ruby Conference", "Swift Conference", "Kotlin Conference",
                                "R Conference", "Go Conference", "Scala Conference", "Rust Conference",
                                "Perl Conference", "Objective-C Conference", "TypeScript Conference",
                                "Assembly Conference", "Dart Conference", "MATLAB Conference", "Visual Basic Conference",
                                "Delphi Conference", "PL/SQL Conference", "Ada Conference", "Lisp Conference",
                                "Fortran Conference", "COBOL Conference", "Logo Conference", "Prolog Conference",
                                "Bash Conference", "PowerShell Conference", "Lua Conference", "Scratch Conference",
                                "ABAP Conference", "Erlang Conference", "Haskell Conference", "Groovy Conference",
                                "Clojure Conference", "F# Conference", "RPG Conference", "Scheme Conference",
                                "Smalltalk Conference", "Tcl Conference", "VHDL Conference", "Verilog Conference",
                                "LabVIEW Conference", "Pascal Conference", "ActionScript Conference", "Forth Conference",
                                "Transact-SQL Conference", "Apex Conference", "Rexx Conference", "Awk Conference",
                                "Elixir Conference", "Eiffel Conference"};

                        String[] description = {"une conference sur un langage inconnu", "une conf sur un langage moyen",
                                "une conf sur un langage trop bien", "Une conf comme ci comme ça",
                                "une conférence sur le meilleur langage de programmation"};

                        event = new Event(eventName[(int) (Math.random() * eventName.length)], dates[(int) (Math.random() * dates.length)], cities[(int) (Math.random() * cities.length)], description[(int) (Math.random() * description.length)]);
                    }

                    // Convert the message to bytes
                    byte[] data = event.getBytes();

                    // Create a DatagramPacket with the message and destination information
                    DatagramPacket packet = new DatagramPacket(data, data.length, multicastGroup, port);

                    // Send the packet
                    socket.send(packet);

                    System.out.println("Multicast message sent: \n" + event.toString());

                    try {
                        Thread.sleep(5000); // 5 seconds
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

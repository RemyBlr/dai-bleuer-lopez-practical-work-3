package ch.heigvd;

import picocli.CommandLine;

import java.util.List;
import java.util.Scanner;


@CommandLine.Command(
        name = "get-event",
        description = "Get information about a specific event using unicast"
)
public class EventCommand implements Runnable {

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
                    listAllEvents();
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
    private void listAllEvents() {
        List<Event> receivedEvents = EventNotifier.getReceivedEvents();

        if (receivedEvents.isEmpty()) {
            System.out.println("No events");
        } else {
            System.out.println("List of all events:");
            for (Event event : receivedEvents) {
                System.out.println(event.toString());
            }
        }
    }

    private void getEventDetailsByName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event name: ");
        String eventName = scanner.nextLine();

        List<Event> receivedEvents = EventNotifier.getReceivedEvents();

        for (Event event : receivedEvents) {
            if (event.getName().equalsIgnoreCase(eventName)) {
                System.out.println("Details for '" + eventName + "':");
                System.out.println(event.toString());
                return;
            }
        }

        System.out.println("Event '" + eventName + "' not found.");
    }
}

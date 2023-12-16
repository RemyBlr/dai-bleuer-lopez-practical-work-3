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
        // Implement logic to connect to the server
        System.out.println("Connecting to the server...");

        // Display options only if connected
        while (true) {
            // Display menu options
            System.out.println("1. List all events");
            System.out.println("2. Get details for a specific event");
            System.out.println("3. Exit");

            // Get user input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    // List all events
                    listAllEvents();
                    break;
                case 2:
                    // Get details for a specific event
                    getEventDetails();
                    break;
                case 3:
                    // Exit
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
    private void listAllEvents() {
        List<Event> receivedEvents = EventNotifier.getReceivedEvents();

        if (receivedEvents.isEmpty()) {
            System.out.println("No events received.");
        } else {
            System.out.println("List of all events:");
            for (Event event : receivedEvents) {
                System.out.println(event.toString());
            }
        }
    }

    private void getEventDetails() {
        // Implement logic to get details for a specific event
        // ...

        // Example: prompt user for event name and retrieve details
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event name: ");
        String eventName = scanner.nextLine();

        // Implement logic to retrieve details for the specified event
        // ...

        // Display event details
        System.out.println("Details for event '" + eventName + "':");
        // ...
    }
}

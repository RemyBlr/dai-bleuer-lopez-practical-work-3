package ch.heigvd;

import picocli.CommandLine;


@CommandLine.Command(
        name = "get-event",
        description = "Get information about a specific event using unicast"
)
public class EventCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "List of all events")
    private String eventsList;
    @CommandLine.Parameters(index = "1", description = "List all event names")
    private String eventName;
    @CommandLine.Parameters(index = "2", description = "List all event dates")
    private String eventDate;
    @CommandLine.Parameters(index = "3", description = "List all event locations")
    private String eventLocation;
    @CommandLine.Parameters(index = "4", description = "List all event descriptions")
    private String eventDescription;

    @CommandLine.Option(names = {"-i", "--info"}, description = "information about a specfic event (Event name)")
    private String eventInfo;


    @Override
    public void run() {
        // TODO logic to send a unicast request to the server
    }
}

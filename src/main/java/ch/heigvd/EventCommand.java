package ch.heigvd;

import picocli.CommandLine;


@CommandLine.Command(
        name = "get-event",
        description = "Get information about a specific event using unicast"
)
public class EventCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "List of all events")
    private String eventsList;

    @CommandLine.Option(names = {"-i", "--info"}, description = "information about a specfic event (Event name)")
    private String eventInfo;


    @Override
    public void run() {
        // TODO logic to send a unicast request to the server
    }
}

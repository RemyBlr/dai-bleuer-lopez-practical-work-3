package ch.heigvd;

public class Event {
    private String name;
    private String date;
    private String location;
    private String description;

    public Event(String _name, String _date, String _location, String _description) {
        this.name = _name;
        this.date = _date;
        this.location = _location;
        this.description = _description;
    }

    public String toString() {
        return "Event: " + this.name +
                "\nDate: " + this.date +
                "\nLocation: " + this.location +
                "\nDescription: " + this.description +
                "\n";
    }

    public byte[] getBytes() {
        return this.toString().getBytes();
    }

    public String getName() {
        return this.name;
    }
}

package inspection.model;

/**
 * Represents one candidate location from the inspection dataset.
 */
public class Location implements Comparable<Location> {

    private final String locationId;
    private final int priorityScore;

    public Location(String locationId, int priorityScore) {
        this.locationId = locationId;
        this.priorityScore = priorityScore;
    }

    public String getLocationId() {
        return locationId;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    /**
     * Compares two locations using the Task A ranking rule:
     * descending by priority score, then ascending by location ID.
     */
    @Override
    public int compareTo(Location other) {
        if (this.priorityScore != other.priorityScore) {
            return Integer.compare(other.priorityScore, this.priorityScore); // descending
        }
        return this.locationId.compareTo(other.locationId); // ascending tie-break
    }

    @Override
    public String toString() {
        return locationId + " (score=" + priorityScore + ")";
    }
}

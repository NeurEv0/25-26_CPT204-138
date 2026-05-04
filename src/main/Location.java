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

    @Override
    public int compareTo(Location other) {
        // Descending by priority_score
        if (this.priorityScore != other.priorityScore) {
            // the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
            return Integer.compare(other.priorityScore, this.priorityScore);
        }
        // Ascending by location_id for ties
        return this.locationId.compareTo(other.locationId);
    }

    @Override
    public String toString() {
        return locationId + " (" + priorityScore + ")";
    }
}

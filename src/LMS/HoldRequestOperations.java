package LMS;

import java.util.ArrayList;

/**
 * HoldRequestOperations class for managing hold requests.
 * Provides methods to add and remove hold requests from a queue.
 */
public class HoldRequestOperations {

    public ArrayList<HoldRequest> holdRequests;

    public HoldRequestOperations() {
        holdRequests = new ArrayList<>();
    }

    /**
     * Adds a hold request to the queue
     * @param hr The hold request to add
     */
    public void addHoldRequest(HoldRequest hr) {
        holdRequests.add(hr);
    }

    /**
     * Removes the first (oldest) hold request from the queue
     */
    public void removeHoldRequest() {
        if (!holdRequests.isEmpty()) {
            HoldRequest hr = holdRequests.get(0);
            // Delete from database
            hr.deleteFromDatabase();
            holdRequests.remove(0);
        }
    }

    /**
     * Removes a specific hold request
     * @param hr The hold request to remove
     */
    public void removeSpecificHoldRequest(HoldRequest hr) {
        if (holdRequests.contains(hr)) {
            hr.deleteFromDatabase();
            holdRequests.remove(hr);
        }
    }

    /**
     * Gets all hold requests
     * @return The list of hold requests
     */
    public ArrayList<HoldRequest> getHoldRequests() {
        return holdRequests;
    }

    /**
     * Checks if there are any hold requests
     * @return true if there are hold requests, false otherwise
     */
    public boolean hasHoldRequests() {
        return !holdRequests.isEmpty();
    }

    /**
     * Gets the count of hold requests
     * @return The number of hold requests
     */
    public int getHoldRequestCount() {
        return holdRequests.size();
    }
}
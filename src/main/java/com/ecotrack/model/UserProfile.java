package com.ecotrack.model;

/**
 * Local session profile metadata (in-memory only; no authentication or database).
 */
public class UserProfile {
    private String displayName = "EcoTrack User";
    private String locationLabel = "Local Desktop Session";
    private int householdSize = 1;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public void setLocationLabel(String locationLabel) {
        if (locationLabel != null && !locationLabel.trim().isEmpty()) {
            this.locationLabel = locationLabel.trim();
        }
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int householdSize) {
        this.householdSize = Math.max(1, householdSize);
    }
}

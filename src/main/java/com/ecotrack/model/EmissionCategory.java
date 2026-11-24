package com.ecotrack.model;

/**
 * Represents the four primary personal carbon emission categories tracked by EcoTrack.
 */
public enum EmissionCategory {
    TRANSPORT("Transportation", "Commuting, vehicle fuel combustion, and transit travel"),
    ELECTRICITY("Household Electricity", "Grid electricity consumption in kWh"),
    FOOD("Food Habits", "Dietary footprint approximations based on diet profile and meal frequency"),
    WASTE("Waste & Recycling", "Net household waste after accounting for recycling/composting diversion");

    private final String displayName;
    private final String description;

    EmissionCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

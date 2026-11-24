package com.ecotrack.model;

/**
 * Rule-based sustainability recommendation derived from the user's entered emissions.
 */
public class Recommendation {
    public enum Priority {
        HIGH("High Impact"),
        MEDIUM("Moderate Impact"),
        LOW("Habit Refinement");

        private final String label;

        Priority(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final EmissionCategory category;
    private final Priority priority;
    private final String title;
    private final String actionSummary;
    private final String rationale;
    private final String estimatedSavingsNote;

    public Recommendation(
            EmissionCategory category,
            Priority priority,
            String title,
            String actionSummary,
            String rationale,
            String estimatedSavingsNote) {
        this.category = category;
        this.priority = priority;
        this.title = title;
        this.actionSummary = actionSummary;
        this.rationale = rationale;
        this.estimatedSavingsNote = estimatedSavingsNote;
    }

    public EmissionCategory getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getActionSummary() {
        return actionSummary;
    }

    public String getRationale() {
        return rationale;
    }

    public String getEstimatedSavingsNote() {
        return estimatedSavingsNote;
    }
}

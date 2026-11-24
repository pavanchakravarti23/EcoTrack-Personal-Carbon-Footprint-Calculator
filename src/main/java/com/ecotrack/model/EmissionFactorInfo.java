package com.ecotrack.model;

/**
 * Metadata and numerical value for an emission factor, preserving traceability
 * to its source, year, geographical applicability, and methodology type.
 */
public class EmissionFactorInfo {
    private final String key;
    private final String label;
    private double value;
    private final String unit;
    private final String methodologyType;
    private final String source;
    private final String year;
    private final String geographicalCoverage;
    private final boolean illustrative;

    public EmissionFactorInfo(
            String key,
            String label,
            double value,
            String unit,
            String methodologyType,
            String source,
            String year,
            String geographicalCoverage,
            boolean illustrative) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.unit = unit;
        this.methodologyType = methodologyType;
        this.source = source;
        this.year = year;
        this.geographicalCoverage = geographicalCoverage;
        this.illustrative = illustrative;
    }

    public EmissionFactorInfo copy() {
        return new EmissionFactorInfo(
                key, label, value, unit, methodologyType, source, year, geographicalCoverage, illustrative);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public String getMethodologyType() {
        return methodologyType;
    }

    public String getSource() {
        return source;
    }

    public String getYear() {
        return year;
    }

    public String getGeographicalCoverage() {
        return geographicalCoverage;
    }

    public boolean isIllustrative() {
        return illustrative;
    }
}

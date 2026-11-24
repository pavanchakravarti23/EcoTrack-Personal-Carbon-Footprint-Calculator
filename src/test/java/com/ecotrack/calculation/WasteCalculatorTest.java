package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class WasteCalculatorTest {

    @Test
    public void partitionsDivertedAndDisposedMassWithoutDoubleCounting() {
        EmissionFactors factors = new EmissionFactors();
        WasteCalculator calc = new WasteCalculator(factors);
        ActivityData data = new ActivityData();
        data.setWasteEntered(true);
        data.setWasteEntry(ActivityData.WasteStream.PAPER, 10.0, 60.0); // 6 kg recycled, 4 kg disposed
        data.setWasteEntry(ActivityData.WasteStream.ORGANIC, 5.0, 100.0); // 5 kg composted, 0 kg disposed

        Map<ActivityData.WasteStream, WasteCalculator.StreamBreakdown> map = calc.calculateStreamBreakdowns(data);
        WasteCalculator.StreamBreakdown paper = map.get(ActivityData.WasteStream.PAPER);
        assertEquals(10.0, paper.getWeeklyTotalKg(), 0.001);
        assertEquals(6.0, paper.getWeeklyDivertedKg(), 0.001);
        assertEquals(4.0, paper.getWeeklyDisposedKg(), 0.001);
        assertEquals(paper.getWeeklyTotalKg(), paper.getWeeklyDivertedKg() + paper.getWeeklyDisposedKg(), 0.0001);

        double expectedWeeklyPaper = 4.0 * factors.getDisposalFactor(ActivityData.WasteStream.PAPER)
                + 6.0 * factors.getDiversionFactor(ActivityData.WasteStream.PAPER);
        assertEquals(expectedWeeklyPaper * EmissionFactors.WEEKS_PER_MONTH, paper.getMonthlyEmissionsKg(), 0.001);

        // Overall diversion = (6 + 5) / (10 + 5) = 73.333%
        assertEquals((11.0 / 15.0) * 100.0, calc.calculateOverallDiversionPercentage(data), 0.01);
    }

    @Test
    public void generalWasteCannotBeMarkedRecycled() {
        WasteCalculator calc = new WasteCalculator(new EmissionFactors());
        ActivityData data = new ActivityData();
        data.setWasteEntered(true);
        data.setWasteEntry(ActivityData.WasteStream.GENERAL, 8.0, 50.0);

        WasteCalculator.StreamBreakdown general = calc.calculateStreamBreakdowns(data)
                .get(ActivityData.WasteStream.GENERAL);
        assertEquals(0.0, general.getWeeklyDivertedKg(), 0.0001);
        assertEquals(8.0, general.getWeeklyDisposedKg(), 0.0001);
    }
}

package com.ecotrack.service;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.components.CustomBarChart;
import com.ecotrack.components.CustomPieChart;
import com.ecotrack.components.CustomProjectionChart;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.Recommendation;
import com.ecotrack.model.UserProfile;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class ReportServiceTest {

    @Test
    public void exportsValidPdfAndTxtReportsAndRendersChartsCleanly() throws Exception {
        CarbonCalculator calc = new CarbonCalculator();
        RecommendationService recService = new RecommendationService(calc.getWasteCalculator());
        AnalyticsService analyticsService = new AnalyticsService();
        ReportService reportService = new ReportService();

        UserProfile profile = new UserProfile();
        profile.setDisplayName("Test Reviewer");

        ActivityData data = new ActivityData();
        data.setTransportEntered(true);
        data.setTransportMode(ActivityData.TransportMode.BUS);
        data.setDistancePerTrip(12.0);
        data.setTravelFrequency(ActivityData.TravelFrequency.WEEKDAYS);

        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(210.0);
        data.setElectricityEmissionFactor(0.45);

        data.setFoodEntered(true);
        data.setDietType(ActivityData.DietType.VEGETARIAN);

        data.setWasteEntered(true);
        data.setWasteEntry(ActivityData.WasteStream.ORGANIC, 5.0, 40.0);

        CarbonResult result = calc.calculate(data);
        List<Recommendation> recs = recService.generateRecommendations(data, result);

        // Verify custom Graphics2D charts paint cleanly
        CustomPieChart pie = new CustomPieChart("Pie");
        pie.setCarbonResult(result);
        paintToImage(pie, 500, 320);

        CustomBarChart bar = new CustomBarChart("Bar");
        bar.setCategoryData(result);
        paintToImage(bar, 500, 320);

        CustomProjectionChart proj = new CustomProjectionChart("Proj");
        proj.setProjectionData(true,
                analyticsService.buildCumulativeBaselineProjection(result),
                analyticsService.buildCumulativeReducedProjection(result, 0.20));
        paintToImage(proj, 600, 320);

        File txtFile = File.createTempFile("ecotrack_test_", ".txt");
        File pdfFile = File.createTempFile("ecotrack_test_", ".pdf");
        txtFile.deleteOnExit();
        pdfFile.deleteOnExit();

        reportService.exportTxtReport(txtFile, profile, data, result, recs, calc.getEmissionFactors());
        reportService.exportPdfReport(pdfFile, profile, data, result, recs, calc.getEmissionFactors());

        assertTrue(txtFile.length() > 300);
        String txtContent = Files.readString(txtFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(txtContent.contains("ECOTRACK - PERSONAL CARBON FOOTPRINT REPORT"));
        assertTrue(txtContent.contains("DISCLAIMER"));

        assertTrue(pdfFile.length() > 1000);
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        String pdfHeader = new String(pdfBytes, 0, Math.min(8, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertTrue(pdfHeader.startsWith("%PDF-1.4"));
        String pdfTail = new String(pdfBytes, Math.max(0, pdfBytes.length - 32),
                Math.min(32, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertTrue(pdfTail.contains("%%EOF"));
    }

    private void paintToImage(Component comp, int w, int h) {
        comp.setSize(w, h);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        comp.paint(g2);
        g2.dispose();
    }
}

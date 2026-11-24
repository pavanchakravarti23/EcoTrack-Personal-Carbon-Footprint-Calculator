package com.ecotrack.service;

import com.ecotrack.calculation.EmissionFactors;
import com.ecotrack.components.CustomBarChart;
import com.ecotrack.components.CustomPieChart;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.model.EmissionFactorInfo;
import com.ecotrack.model.Recommendation;
import com.ecotrack.model.UserProfile;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.PdfDocumentWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates offline PDF and TXT carbon footprint reports containing branding,
 * category breakdowns, Graphics2D chart renders, rule-based recommendations,
 * calculation methodology, emission factor references, and clear disclaimers.
 */
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void exportTxtReport(
            File targetFile,
            UserProfile profile,
            ActivityData data,
            CarbonResult result,
            List<Recommendation> recommendations,
            EmissionFactors factors) throws IOException {
        if (targetFile == null || data == null || result == null) {
            throw new IllegalArgumentException("Target file, activity data, and result must not be null.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================\n");
        sb.append("  ECOTRACK - PERSONAL CARBON FOOTPRINT REPORT\n");
        sb.append("====================================================================\n");
        sb.append("Generated On       : ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("Session Profile    : ").append(profile != null ? profile.getDisplayName() : "EcoTrack User").append("\n");
        sb.append("Calculation Period : ").append(result.getPeriod().getDisplayName()).append("\n");
        sb.append("--------------------------------------------------------------------\n\n");

        sb.append("1. EMISSION SUMMARY\n");
        sb.append("   Total (").append(result.getPeriod().getDisplayName()).append(") : ")
                .append(NumberFormatter.formatKg(result.getTotalPeriodEmissionsKg())).append("\n");
        sb.append("   Monthly Equivalent : ")
                .append(NumberFormatter.formatKg(result.getMonthlyEquivalentKg())).append("\n");
        sb.append("   Annual Projection  : ")
                .append(NumberFormatter.formatKg(result.getAnnualProjectionKg())).append("\n");
        sb.append("   Summary Note       : ").append(result.getSummaryNote()).append("\n\n");

        sb.append("2. CATEGORY-WISE BREAKDOWN\n");
        sb.append(String.format("   %-24s %15s %10s\n", "Category", "Emissions", "Share"));
        sb.append("   -----------------------------------------------------\n");
        for (EmissionCategory cat : EmissionCategory.values()) {
            sb.append(String.format("   %-24s %15s %10s\n",
                    cat.getDisplayName(),
                    NumberFormatter.formatKg(result.getEmission(cat)),
                    NumberFormatter.formatPercent(result.getPercentage(cat))));
        }
        sb.append("\n");

        sb.append("3. PRACTICAL RECOMMENDATIONS (RULE-BASED)\n");
        if (recommendations == null || recommendations.isEmpty()) {
            sb.append("   No specific recommendations available (enter activity data first).\n\n");
        } else {
            int idx = 1;
            for (Recommendation rec : recommendations) {
                sb.append("   [").append(idx++).append("] (").append(rec.getPriority().getLabel()).append(") ")
                        .append(rec.getTitle()).append("\n");
                sb.append("       Action  : ").append(rec.getActionSummary()).append("\n");
                sb.append("       Context : ").append(rec.getRationale()).append("\n");
                sb.append("       Impact  : ").append(rec.getEstimatedSavingsNote()).append("\n\n");
            }
        }

        sb.append("4. CALCULATION METHODOLOGY\n");
        sb.append("   General Formula : Emissions (kg CO2e) = Activity Data x Emission Factor\n");
        sb.append("   - Transport     : Tailpipe fuel combustion (Petrol/Diesel/Motorcycle) uses\n");
        sb.append("                     distance / km-per-litre x fuel factor / occupancy; Bus & Train\n");
        sb.append("                     use passenger-km factors; EV uses kWh/100km x grid factor / occupancy;\n");
        sb.append("                     Bicycle & Walking have 0 kg CO2e direct operational emissions.\n");
        sb.append("   - Electricity   : Monthly kWh x user-selected or user-entered grid factor.\n");
        sb.append("   - Food          : Representative dietary LCA baseline or weekly meal-frequency\n");
        sb.append("                     approximations (explicitly an approximate estimate).\n");
        sb.append("   - Waste         : Stream mass is partitioned into diverted (recycled/composted)\n");
        sb.append("                     vs. disposed mass before applying distinct factors (no double counting).\n\n");

        sb.append("5. EMISSION FACTOR REFERENCES\n");
        if (factors != null) {
            sb.append("   Selected Electricity Factor : ")
                    .append(NumberFormatter.formatFactor(data.getElectricityEmissionFactor()))
                    .append(" kg CO2e/kWh (").append(data.getElectricityFactorSourceLabel()).append(")\n");
            for (EmissionFactorInfo info : factors.getAllFactors().values()) {
                sb.append("   - ").append(info.getLabel()).append(": ")
                        .append(NumberFormatter.formatFactor(info.getValue())).append(" ").append(info.getUnit())
                        .append(" | ").append(info.getSource())
                        .append(info.isIllustrative() ? " [Illustrative Configurable]" : "")
                        .append("\n");
            }
        }
        sb.append("\n");

        sb.append("6. DISCLAIMER\n");
        sb.append("   All figures produced by EcoTrack are educational and planning estimates\n");
        sb.append("   based on user-entered inputs and configurable default emission factors.\n");
        sb.append("   They do not represent certified laboratory or regulatory GHG audits.\n");
        sb.append("====================================================================\n");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        }
    }

    public void exportPdfReport(
            File targetFile,
            UserProfile profile,
            ActivityData data,
            CarbonResult result,
            List<Recommendation> recommendations,
            EmissionFactors factors) throws IOException {
        if (targetFile == null || data == null || result == null) {
            throw new IllegalArgumentException("Target file, activity data, and result must not be null.");
        }

        PdfDocumentWriter pdf = new PdfDocumentWriter();
        Color darkHeader = new Color(0x15, 0x1B, 0x19);
        Color emerald = new Color(0x23, 0xC4, 0x7E);
        Color darkText = new Color(0x1E, 0x26, 0x23);
        Color mutedText = new Color(0x52, 0x61, 0x5A);
        Color cardBg = new Color(0xF2, 0xF6, 0xF4);
        Color border = new Color(0xD3, 0xDF, 0xD9);

        // Top Brand Header
        pdf.fillRect(0, 0, PdfDocumentWriter.PAGE_WIDTH, 86, darkHeader);
        pdf.fillRect(0, 82, PdfDocumentWriter.PAGE_WIDTH, 4, emerald);
        pdf.drawText("EcoTrack - Personal Carbon Footprint Report", 36, 36, 18, true, Color.WHITE);
        pdf.drawText("Generated: " + LocalDateTime.now().format(DATE_FORMAT)
                + "  |  Period: " + result.getPeriod().getDisplayName()
                + "  |  Profile: " + (profile != null ? profile.getDisplayName() : "EcoTrack User"),
                36, 60, 10, false, new Color(0xC2, 0xD4, 0xCB));

        float y = 108;

        // Summary Metric Cards
        float cardW = 165;
        float gap = 14;
        drawSummaryBox(pdf, 36, y, cardW, 60, cardBg, border,
                "TOTAL (" + result.getPeriod().getDisplayName().toUpperCase() + ")",
                NumberFormatter.formatKg(result.getTotalPeriodEmissionsKg()), darkText, emerald);
        drawSummaryBox(pdf, 36 + cardW + gap, y, cardW, 60, cardBg, border,
                "MONTHLY EQUIVALENT",
                NumberFormatter.formatKg(result.getMonthlyEquivalentKg()), darkText, darkText);
        drawSummaryBox(pdf, 36 + (cardW + gap) * 2, y, cardW, 60, cardBg, border,
                "ANNUAL PROJECTION",
                NumberFormatter.formatKg(result.getAnnualProjectionKg()), darkText, darkText);

        y += 78;
        y = pdf.drawWrappedText(result.getSummaryNote(), 36, y, 523, 10.5f, 14f, true, darkText, 55);
        y += 8;

        // Category Breakdown Table
        pdf.drawText("1. Category-Wise Emissions Breakdown", 36, y, 13, true, darkText);
        y += 12;
        pdf.fillRect(36, y, 523, 22, darkHeader);
        pdf.drawText("Category", 46, y + 15, 9.5f, true, Color.WHITE);
        pdf.drawText("Emissions (kg CO2e)", 320, y + 15, 9.5f, true, Color.WHITE);
        pdf.drawText("Share (%)", 475, y + 15, 9.5f, true, Color.WHITE);
        y += 22;

        boolean alt = false;
        for (EmissionCategory cat : EmissionCategory.values()) {
            pdf.fillRect(36, y, 523, 22, alt ? cardBg : Color.WHITE);
            pdf.strokeRect(36, y, 523, 22, border, 0.5f);
            pdf.drawText(cat.getDisplayName(), 46, y + 15, 9.5f, false, darkText);
            pdf.drawText(NumberFormatter.formatKg(result.getEmission(cat)), 320, y + 15, 9.5f, true, darkText);
            pdf.drawText(NumberFormatter.formatPercent(result.getPercentage(cat)), 475, y + 15, 9.5f, false, mutedText);
            y += 22;
            alt = !alt;
        }
        y += 18;

        // Render Graphics2D Charts offscreen and embed into PDF
        if (result.isHasAnyData()) {
            pdf.drawText("2. Visual Emissions Breakdown (Graphics2D Charts)", 36, y, 13, true, darkText);
            y += 10;
            BufferedImage pieImg = renderChartToImage(createPieChartForExport(result), 520, 340);
            BufferedImage barImg = renderChartToImage(createBarChartForExport(result), 520, 340);
            pdf.drawImage(pieImg, 36, y, 255, 168);
            pdf.drawImage(barImg, 304, y, 255, 168);
            y += 184;
        }

        // Recommendations
        if (y > 680) {
            pdf.newPage();
            y = 50;
        }
        pdf.drawText("3. Practical Recommendations (Rule-Based)", 36, y, 13, true, darkText);
        y += 16;
        if (recommendations == null || recommendations.isEmpty()) {
            y = pdf.drawWrappedText("No recommendations generated yet. Enter activity values first.",
                    36, y, 523, 9.5f, 13f, false, mutedText, 55);
        } else {
            for (Recommendation rec : recommendations) {
                if (y > 740) {
                    pdf.newPage();
                    y = 50;
                }
                pdf.drawText("[" + rec.getPriority().getLabel() + "] " + rec.getTitle(), 36, y, 10, true, darkText);
                y += 13;
                y = pdf.drawWrappedText(rec.getActionSummary() + " (" + rec.getRationale() + ")",
                        44, y, 515, 9.2f, 12.5f, false, mutedText, 55);
                y += 6;
            }
        }

        // Page 2+: Methodology, Emission Factor References, Disclaimer
        pdf.newPage();
        y = 50;
        pdf.drawText("4. Calculation Methodology", 36, y, 13, true, darkText);
        y += 16;
        String methodology = "General Formula: Emissions (kg CO2e) = Activity Data x Emission Factor.\n"
                + "- Transportation: Fuel-combustion modes (Petrol Car, Diesel Car, Motorcycle) compute fuel litres from distance / efficiency (km/L) multiplied by fuel combustion factors and divided by occupancy. Bus and Train use passenger-kilometre factors. Electric Vehicles couple kWh/100 km with the configured grid factor. Bicycle and Walking have 0 kg CO2e direct operational emissions.\n"
                + "- Electricity: Monthly kWh multiplied by the user-selected or user-entered grid factor (kg CO2e/kWh).\n"
                + "- Food: Representative dietary LCA baselines or user-entered weekly meal frequencies (approximations).\n"
                + "- Waste: Generated mass per stream is cleanly split into diverted (recycled/composted) and residual disposed mass before applying separate factors to avoid double-counting.";
        y = pdf.drawWrappedText(methodology, 36, y, 523, 9.5f, 13.5f, false, darkText, 55);
        y += 14;

        pdf.drawText("5. Emission Factor References & Active Settings", 36, y, 13, true, darkText);
        y += 16;
        y = pdf.drawWrappedText("Active Electricity Factor: "
                + NumberFormatter.formatFactor(data.getElectricityEmissionFactor())
                + " kg CO2e/kWh (" + data.getElectricityFactorSourceLabel() + ")",
                36, y, 523, 9.5f, 13f, true, darkText, 55);
        y += 6;

        if (factors != null) {
            for (EmissionFactorInfo info : factors.getAllFactors().values()) {
                if (y > 765) {
                    pdf.newPage();
                    y = 50;
                }
                String line = "- " + info.getLabel() + ": "
                        + NumberFormatter.formatFactor(info.getValue()) + " " + info.getUnit()
                        + " [" + info.getSource() + "]";
                y = pdf.drawWrappedText(line, 36, y, 523, 8.7f, 12f, false, mutedText, 55);
            }
        }

        y += 12;
        if (y > 730) {
            pdf.newPage();
            y = 50;
        }
        pdf.fillRect(36, y, 523, 56, cardBg);
        pdf.strokeRect(36, y, 523, 56, border, 0.8f);
        pdf.drawText("Disclaimer About Estimated Values", 46, y + 16, 10, true, darkText);
        pdf.drawWrappedText(
                "All EcoTrack outputs are educational estimates derived from user inputs and configurable default factors. They are intended for personal awareness and academic demonstration, not certified regulatory GHG inventories.",
                46, y + 30, 503, 8.8f, 12f, false, mutedText, 30);

        pdf.saveToFile(targetFile);
    }

    private void drawSummaryBox(
            PdfDocumentWriter pdf,
            float x,
            float y,
            float w,
            float h,
            Color bg,
            Color border,
            String label,
            String value,
            Color labelColor,
            Color valueColor) {
        pdf.fillRect(x, y, w, h, bg);
        pdf.strokeRect(x, y, w, h, border, 0.8f);
        pdf.drawText(label, x + 10, y + 20, 8f, true, labelColor);
        pdf.drawText(value, x + 10, y + 44, 13f, true, valueColor);
    }

    private CustomPieChart createPieChartForExport(CarbonResult result) {
        CustomPieChart chart = new CustomPieChart("Share by Category");
        chart.setCarbonResult(result);
        return chart;
    }

    private CustomBarChart createBarChartForExport(CarbonResult result) {
        CustomBarChart chart = new CustomBarChart("Category Emissions (kg CO2e)");
        chart.setCategoryData(result);
        return chart;
    }

    private BufferedImage renderChartToImage(Component comp, int width, int height) {
        comp.setSize(width, height);
        comp.doLayout();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        comp.paint(g2);
        g2.dispose();
        return img;
    }
}

# EcoTrack – Personal Carbon Footprint Calculator: Project Explanation

## 1. What Problem the Project Solves
Personal lifestyle choices across commuting, household electricity use, dietary habits, and daily waste generation collectively drive a substantial share of urban greenhouse gas (GHG) emissions. Most existing calculators are web-hosted, require accounts, store data on external servers, or present opaque black-box numbers without explaining the underlying units or formulas.

**EcoTrack** solves this by providing a **100% offline, standalone Java Swing desktop application** that enables individuals to:
- Enter daily/monthly/weekly activity data across four core domains.
- Estimate greenhouse gas emissions in **kilograms of carbon dioxide equivalent ($\text{kg CO}_2\text{e}$)**.
- Visualize emission distributions and annual projections through interactive **Java Graphics2D** charts.
- Explore practical, rule-based reduction recommendations and test lifestyle changes safely in a **What-If Simulator**.
- Export structured **PDF** and **TXT** reports locally for academic or personal documentation.

---

## 2. Why Java Swing Was Selected
1. **True Offline Desktop Execution:** Java Swing ships natively inside the standard JDK (`javax.swing` and `java.awt`), requiring zero browser engine, Node.js runtime, or web server.
2. **Custom Vector Graphics with Graphics2D:** Swing's `JComponent.paintComponent(Graphics g)` combined with `Graphics2D` anti-aliasing enables responsive, interactive custom charts (donut pie chart, bar charts, and 12-month trajectory curves) without external charting libraries.
3. **Cross-Platform Portability:** A single compiled JAR or class tree runs on Windows, Linux, and macOS wherever a Java 17+ JRE/JDK is installed.
4. **Strong Object-Oriented Architecture:** Swing encourages clean separation of concerns between UI panels (`ui/`), custom widgets (`components/`), domain models (`model/`), calculation logic (`calculation/`), and services (`service/`).

---

## 3. How the Application Works (End-to-End Flow)
1. **Startup (`EcoTrackApp.java`):** Configures anti-aliased text rendering, applies `ThemeManager` dark-charcoal/emerald defaults, and instantiates `MainFrame` on Swing's **Event Dispatch Thread (EDT)** via `SwingUtilities.invokeLater`.
2. **Empty Initial State:** No fabricated results are displayed on launch. `DashboardPanel` displays `--` placeholders until at least one calculator module is submitted.
3. **Module Input & Validation:** When the user fills a calculator form and clicks Calculate, `InputValidator` parses numbers safely, rejects empty, negative, non-numeric, or out-of-range values, and updates the shared in-memory `ActivityData` object.
4. **Centralized Recalculation (`MainFrame.recalculateAndRefreshAll`):** Invokes `CarbonCalculator.calculate(activityData)` to produce an immutable `CarbonResult`, runs `RecommendationService` to generate ranked advice, and refreshes every panel and chart simultaneously.
5. **Non-Blocking Report Export:** Exporting PDF or TXT uses `JFileChooser` for destination selection and executes `ReportService` inside a `SwingWorker` background thread so the UI remains responsive.

---

## 4. How Each Module Works
- **Module 1 – Dashboard (`DashboardPanel`):** Displays welcome header, module completion count, 5 KPI `StatCard`s, a `CustomPieChart`, category share bars, latest calculation summary, environmental tips, and quick navigation buttons.
- **Module 2 – Transportation (`TransportPanel`):** Supports 8 modes (`Petrol Car`, `Diesel Car`, `Motorcycle`, `Bus`, `Train`, `Electric Vehicle`, `Bicycle`, `Walking`), km/miles unit conversion, travel frequency, period selection, fuel efficiency (`km/L`), EV draw (`kWh/100 km`), and vehicle occupancy.
- **Module 3 – Electricity (`ElectricityPanel`):** Accepts monthly `kWh` usage and offers clearly labelled illustrative grid factor presets plus a **Custom User-Entered Factor** input.
- **Module 4 – Food Habits (`FoodPanel`):** Evaluates four diet categories (`Plant-based`, `Vegetarian`, `Mixed`, `Meat-heavy`) via representative daily baselines or editable weekly meal frequencies, accompanied by an explicit approximation disclaimer.
- **Module 5 – Waste & Recycling (`WastePanel`):** Evaluates 6 streams (`Organic`, `Paper`, `Plastic`, `Metal`, `Glass`, `General`) with weekly mass and recycling/composting percentages, outputting a stream-by-stream table.
- **Module 6 – Analytics (`AnalyticsPanel`):** Houses 4 live `Graphics2D` charts: Pie/Donut distribution, Category Comparison bar chart, Monthly Comparison bar chart, and 12-Month Cumulative Annual Projection chart.
- **Module 7 – Smart Recommendations (`RecommendationsPanel`):** Rule-based recommendations ranked by priority (`HIGH`, `MEDIUM`, `LOW`) derived from the user's highest contributor and specific inputs.
- **Module 8 – What-If Simulator (`SimulatorPanel`):** Clones `ActivityData` via `copy()` to preview hypothetical travel, mode, electricity, diet, and recycling changes without mutating the active session unless explicitly applied.
- **Module 9 – Reports (`ReportService`, `PdfDocumentWriter`):** Generates local TXT and multi-page PDF 1.4 reports embedding rendered `Graphics2D` chart images, tables, methodology, factor references, and disclaimers.
- **Module 10 – Settings & About (`SettingsPanel`, `AboutPanel`):** Manages in-memory session profile, editable emission factor registry, session reset, and academic documentation.

---

## 5. How Carbon Calculations Are Performed
Core equation:
$$\text{Emissions (kg CO}_2\text{e)} = \text{Activity Data} \times \text{Emission Factor}$$

- **Transportation:**
  - *Fuel-combustion vehicles (Petrol Car, Diesel Car, Motorcycle):* $\text{Emissions} = \frac{(\text{Monthly km} / \text{km per L}) \times \text{Fuel Factor}}{\text{Occupancy}}$
  - *Passenger transit (Bus, Train):* $\text{Emissions} = \text{Monthly km} \times \text{Passenger-km Factor}$
  - *Electric Vehicle:* $\text{Emissions} = \frac{(\text{Monthly km} / 100) \times \text{kWh per 100 km} \times \text{Grid Factor}}{\text{Occupancy}}$
  - *Bicycle & Walking:* $0.0\ \text{kg CO}_2\text{e}$ direct operational emissions.
- **Electricity:** $\text{Monthly Emissions} = \text{Monthly kWh} \times \text{Grid Factor}$ (Annual = $\text{Monthly} \times 12$).
- **Food:** Either $\text{Daily Baseline} \times 30$ or $(\text{Weekly Meals} \times \text{Meal Factor}) \times \frac{52}{12}$.
- **Waste (Anti-Double-Counting):** Each stream's mass $M$ with diversion rate $r \in [0, 1]$ is strictly partitioned into $M_{\text{diverted}} = M \cdot r$ and $M_{\text{disposed}} = M - M_{\text{diverted}}$, then evaluated separately:
  $$\text{Emissions} = (M_{\text{disposed}} \times F_{\text{disposal}}) + (M_{\text{diverted}} \times F_{\text{diversion}})$$

---

## 6. How Graphics2D Charts Work
Each chart (`CustomPieChart`, `CustomBarChart`, `CustomProjectionChart`) extends `StyledCardPanel` (`JPanel`) and overrides `paintComponent(Graphics g)`:
1. Casts `Graphics` to a defensive `Graphics2D` copy (`(Graphics2D) g.create()`), enabling `KEY_ANTIALIASING` and `KEY_TEXT_ANTIALIASING`.
2. Computes dynamic coordinate layouts from `getWidth()` and `getHeight()` so charts resize responsively.
3. Draws geometry (`Arc2D.Double` for donut slices, `fillRoundRect` for bars, `Path2D.Double` and dashed `BasicStroke` for trajectories).
4. Registers `MouseMotionListener` and `MouseListener` hit-testing (`hitTest` via polar angle & radius on the pie chart, `Rectangle2D.Double.contains` on bars, and nearest X-slot on the projection chart) to highlight hovered elements and render floating tooltips.

---

## 7. How the Recommendation Engine Works
`RecommendationService` uses deterministic `if/else` threshold and ranking rules—**not artificial intelligence**:
- Identifies `CarbonResult.getHighestCategory()` and each category's percentage share.
- Assigns `HIGH` priority to the top contributor or any category contributing $\ge 35\%$ (or waste diversion $< 30\%$).
- Emits mode-specific advice (e.g., carpooling for solo petrol/diesel drivers, off-peak/renewable charging for EVs, cooling/standby reduction for $\ge 250\ \text{kWh/month}$ electricity, plant-forward meal swaps for meat-heavy/mixed diets, and composting/segregation when waste diversion is below $50\%$).

---

## 8. How PDF/TXT Reports Are Generated
- **TXT Reports:** Written using UTF-8 `BufferedWriter` with clean fixed-width tables and full methodology/reference sections.
- **PDF Reports:** Built by `PdfDocumentWriter.java` directly against the **PDF 1.4 specification** using standard Type1 `Helvetica` / `Helvetica-Bold` fonts, RGB vector drawing commands, automatic multi-page wrapping, and `DeflaterOutputStream` (`FlateDecode`) compressed `BufferedImage` chart XObjects—requiring **zero external PDF libraries**.

---

## 9. Why No Backend or Database Is Required
EcoTrack targets individual desktop estimation and academic demonstration. All domain state (`ActivityData`, `UserProfile`, `EmissionFactors`, `CarbonResult`) fits cleanly inside in-memory Java objects (`EnumMap`, `LinkedHashMap`, `ArrayList`). Retaining outputs is handled through portable local **PDF** and **TXT** exports, eliminating database installation, credentials, network latency, and privacy exposure.

---

## 10. Important Java Concepts Used
- **Object-Oriented Programming (OOP):** Encapsulation, enums with fields and behavior (`TransportMode`, `DietType`, `CalculationPeriod`), immutable result snapshots (`CarbonResult`, `SimulationResult`), and composition (`CarbonCalculator` coordinating 4 specialized calculators).
- **Java Collections Framework:** `EnumMap`, `LinkedHashMap`, `ArrayList`, unmodifiable map views.
- **Java Swing & AWT:** `JFrame`, `CardLayout`, `GridBagLayout`, `BoxLayout`, `JScrollPane`, `JTable`, `JFileChooser`, `JOptionPane`, custom `Icon` and `BasicScrollBarUI`.
- **Graphics2D Custom Painting:** Anti-aliased shapes, strokes, font metrics, hit-testing, offscreen `BufferedImage` rendering.
- **Concurrency:** Swing Event Dispatch Thread (`SwingUtilities.invokeLater`) and background `SwingWorker` for non-blocking file export.
- **I/O & Compression:** `FileOutputStream`, `BufferedWriter`, `DeflaterOutputStream` for PDF image streams.
- **Automated Testing:** JUnit 4 unit and UI integration tests (`@Test`, `@Before`, expected exception assertions).

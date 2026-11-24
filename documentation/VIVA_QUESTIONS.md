# EcoTrack – Viva Questions and Answers

1. **Q: What is EcoTrack and what problem does it address?**
   **A:** EcoTrack is an offline Java Swing desktop application that estimates an individual's personal greenhouse gas footprint ($\text{kg CO}_2\text{e}$) across transportation, household electricity, food habits, and waste, visualizes major sources, and suggests practical reductions.

2. **Q: Why did you build EcoTrack as a Java Swing desktop app instead of a web app?**
   **A:** Java Swing runs completely offline using the standard JDK, needs no server or browser stack, gives direct low-level drawing control via `Graphics2D` for custom charts, and demonstrates core OOP and desktop UI architecture cleanly.

3. **Q: Does EcoTrack use any database like MySQL or SQLite?**
   **A:** No. All session data is held in memory using Java objects (`ActivityData`, `EnumMap`, `LinkedHashMap`, `ArrayList`), and permanent records are exported locally as PDF or TXT files.

4. **Q: What is $\text{kg CO}_2\text{e}$ and why is it used?**
   **A:** Kilograms of carbon dioxide equivalent ($\text{kg CO}_2\text{e}$) is a standard unit that expresses the warming impact of different greenhouse gases (such as $\text{CO}_2$, $\text{CH}_4$, and $\text{N}_2\text{O}$) on a common carbon-dioxide scale.

5. **Q: What is the core mathematical formula used in EcoTrack?**
   **A:** $\text{Emissions (kg CO}_2\text{e)} = \text{Activity Data} \times \text{Emission Factor}$, with unit conversions and period normalization applied consistently in double precision.

6. **Q: How does `TransportCalculator` avoid using one naive formula for every vehicle?**
   **A:** It branches by `CalculationMethod`: petrol/diesel/motorcycle use distance divided by fuel efficiency ($\text{km/L}$) times fuel combustion factor divided by occupancy; bus/train use passenger-km factors; electric vehicles couple $\text{kWh}/100\ \text{km}$ with the active electricity grid factor; bicycle/walking return $0.0\ \text{kg CO}_2\text{e}$.

7. **Q: How are miles converted to kilometres in the transportation module?**
   **A:** `ActivityData.DistanceUnit.MILES` multiplies input distance by `1.60934` before monthly frequency scaling and emission calculation.

8. **Q: How does EcoTrack handle carpooling or shared vehicles?**
   **A:** For personal fuel vehicles and EVs, calculated vehicle tailpipe/grid emissions are divided by `vehicleOccupancy` ($\ge 1$), attributing an equal per-passenger share.

9. **Q: Why does the Electricity module label its default presets as "Illustrative"?**
   **A:** Grid emission factors vary by country, state, and year. To avoid falsely claiming an illustrative number as an official Indian national grid factor, EcoTrack marks built-in presets (`0.82`, `0.45`, `0.15` $\text{kg CO}_2\text{e/kWh}$) as illustrative scenarios and allows users to enter official local utility or CEA/EPA figures via Custom mode.

10. **Q: How are weekly, monthly, and annual calculation periods reconciled?**
    **A:** Each calculator computes a normalized monthly value (using $30.0\ \text{days/month}$ and $52/12\ \text{weeks/month}$), then scales by `CalculationPeriod.getMultiplierFromMonthly()` ($12/52$ for Weekly, $1.0$ for Monthly, $12.0$ for Annual).

11. **Q: How does the Food calculator work, and what are its limitations?**
    **A:** Users can either choose a representative diet baseline (`Plant-based`, `Vegetarian`, `Mixed`, `Meat-heavy`) or enter custom weekly meal counts. Because food supply chains vary widely, the UI and reports explicitly state that these are representative approximations.

12. **Q: How does the Waste calculator prevent double-counting recycled and disposed waste?**
    **A:** Each waste stream's total weekly mass is partitioned strictly into $M_{\text{diverted}} = M \times (\text{recycling\%}/100)$ and $M_{\text{disposed}} = M - M_{\text{diverted}}$. Diversion processing factors apply only to $M_{\text{diverted}}$ and disposal factors apply only to $M_{\text{disposed}}$.

13. **Q: Why is General Residual Waste locked at 0% recycling?**
    **A:** Mixed residual household waste is not source-separated for recycling, so treating part of it as recycled would understate disposal emissions.

14. **Q: What happens when the application first opens with no user inputs?**
    **A:** `ActivityData.hasAnyDataEntered()` returns `false`, so `DashboardPanel` displays an honest empty state (`--`) and empty chart states rather than fabricated numbers.

15. **Q: How are custom charts implemented without third-party charting libraries?**
    **A:** `CustomPieChart`, `CustomBarChart`, and `CustomProjectionChart` extend `StyledCardPanel` and override `paintComponent(Graphics g)` using `Graphics2D`, `Arc2D`, `Path2D`, `BasicStroke`, and `RenderingHints.KEY_ANTIALIASING`.

16. **Q: How do interactive hover tooltips work on the custom `Graphics2D` charts?**
    **A:** Mouse coordinates from `MouseMotionListener` are tested against slice polar angles/radii (pie chart), stored `Rectangle2D.Double` hit boxes (bar chart), or horizontal slot indices (projection chart), triggering `repaint()` with a floating tooltip overlay.

17. **Q: Is the Recommendation module powered by Artificial Intelligence?**
    **A:** No. `RecommendationService` is a deterministic, rule-based engine using category share thresholds, highest-contributor ranking, and waste diversion percentages.

18. **Q: How does the What-If Simulator avoid corrupting the user's real inputs?**
    **A:** `SimulationService` calls `originalData.copy()` to create a deep copy, applies hypothetical adjustments to the clone, and returns a `SimulationResult`. The original `ActivityData` is updated only if the user clicks "Apply Changes to Session" and confirms.

19. **Q: Why did you use `SwingWorker` for PDF and TXT report export?**
    **A:** File I/O and rendering `BufferedImage` chart streams for PDF generation can take noticeable time; running `ReportService` inside `SwingWorker.doInBackground()` keeps the Swing Event Dispatch Thread responsive.

20. **Q: How is the PDF report generated offline without external libraries?**
    **A:** `PdfDocumentWriter` constructs a valid PDF 1.4 binary structure directly (catalog, pages, Type1 Helvetica fonts, vector rectangles/text streams, `DeflaterOutputStream` Flate-compressed RGB chart images, cross-reference `xref` table, and `%%EOF` trailer).

21. **Q: Why must Swing UI updates happen on the Event Dispatch Thread (EDT)?**
    **A:** Swing components are not thread-safe. Creating frames via `SwingUtilities.invokeLater` and updating labels inside `SwingWorker.done()` prevents race conditions and visual glitches.

22. **Q: How does `InputValidator` protect the application from invalid inputs?**
    **A:** It trims text, catches `NumberFormatException`, rejects `NaN`/`Infinity`, negative values, empty strings, values exceeding `1,000,000,000`, and percentages outside `0–100%`, throwing descriptive `IllegalArgumentException` messages shown via `JOptionPane`.

23. **Q: How is the application layout organized in `MainFrame`?**
    **A:** `MainFrame` uses `BorderLayout` with a persistent `Sidebar` on the west, a `HeaderPanel` at the north of the center column, and a `CardLayout` deck wrapped in themed `JScrollPane`s so all 10 screens remain responsive at different window sizes.

24. **Q: What is the role of `ThemeManager`?**
    **A:** It centralizes the `#111514` dark-charcoal and `#23C47E` emerald-green color tokens, cross-platform font selection, `UIManager` defaults, input styling, and custom dark scrollbar UI.

25. **Q: Why are vector icons drawn via `IconFactory` instead of Unicode emojis?**
    **A:** OS emoji rendering varies across Windows/Linux JDK fonts and often turns into monochrome squares in Swing; `Graphics2D` vector `Icon` implementations scale cleanly at any size.

26. **Q: How can a user customize emission factors during a session?**
    **A:** In `SettingsPanel`, users can select any factor row in the `EmissionFactors` registry table, edit its value, or click "Restore Defaults", which triggers immediate recalculation across all panels.

27. **Q: What happens when "Reset Current Session" is clicked?**
    **A:** After user confirmation, `ActivityData.resetAll()` clears all flags and restores initial values, every panel syncs from the cleared model, and the Dashboard returns to its clean empty state.

28. **Q: What Java collections are used in EcoTrack and why?**
    **A:** `EnumMap` for category and waste-stream lookups (compact, O(1), type-safe), `LinkedHashMap` for ordered emission factors and comparison chart series, and `ArrayList` for recommendations and 12-month projection points.

29. **Q: How did you verify the correctness of EcoTrack?**
    **A:** Using JUnit 4 unit and integration tests covering valid calculations, unit conversions, zero/negative/empty/oversized inputs, anti-double-counting waste math, non-mutating What-If simulations, PDF/TXT export assertions, and live `MainFrame` navigation.

30. **Q: What are the key limitations and possible future enhancements of EcoTrack?**
    **A:** Limitations include in-memory session storage (intentional absence of a database), illustrative default factors for non-user-entered regions, and simplified dietary approximations. Future enhancements could include optional encrypted local JSON/XML profile persistence, multi-trip transportation logs, and region-specific verified emission-factor packs.

package com.smartfarm.controller;

import java.util.List;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;

import com.smartfarm.util.SceneSwitcher;
import com.smartfarm.service.FarmService;
import com.smartfarm.service.WorkerService;
import com.smartfarm.service.TransactionService;
import com.smartfarm.util.SessionManager;
import com.smartfarm.model.Harvest;
import com.smartfarm.model.Transaction;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label pageTitle;
    @FXML private Label userName;
    @FXML private StackPane contentArea;
    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;

    @FXML private VBox navButtons;
    @FXML private Region slideHighlight;

    @FXML private Button btnDashboard;
    @FXML private Button btnFields;
    @FXML private Button btnHarvests;
    @FXML private Button btnWorkers;
    @FXML private Button btnTransactions;
    @FXML private Button btnFertilizers;
    @FXML private Button btnHistory;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;
    @FXML private Button btnQuit;

    private boolean isDarkMode = false;
    private Button activeButton;
    private TranslateTransition currentMove;

    @FXML
    public void initialize() {

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(contentArea.widthProperty());
        clip.heightProperty().bind(contentArea.heightProperty());
        contentArea.setClip(clip);

        slideHighlight.minWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.prefWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.maxWidthProperty().bind(btnDashboard.widthProperty());
        slideHighlight.minHeightProperty().bind(btnDashboard.heightProperty());
        slideHighlight.prefHeightProperty().bind(btnDashboard.heightProperty());
        slideHighlight.maxHeightProperty().bind(btnDashboard.heightProperty());

        navButtons.heightProperty().addListener((o, ov, nv) -> {
            if (activeButton != null) moveHighlight(activeButton, false);
        });
        navButtons.widthProperty().addListener((o, ov, nv) -> {
            if (activeButton != null) moveHighlight(activeButton, false);
        });

        if (userName != null) {
            String sessionName = SessionManager.getUserName();
            userName.setText(sessionName != null && !sessionName.isBlank() ? sessionName : "Admin");
        }

        showDashboard();
        syncThemeState();
    }

    private void syncThemeState() {
        isDarkMode = SceneSwitcher.isDarkMode();
        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
        }
    }

    private void setActive(Button button, String title) {
        Button previous = activeButton;
        activeButton = button;
        pageTitle.setText(title);

        if (previous != null && previous != button) {
            previous.getStyleClass().remove("nav-active");
        }

        moveHighlight(button, previous != null);
    }

    private void moveHighlight(Button button, boolean animate) {
        double h = button.getHeight();
        if (h <= 0) {
            Platform.runLater(() -> moveHighlight(button, false));
            return;
        }

        double targetY = button.getBoundsInParent().getMinY();

        if (animate) {
            if (currentMove != null) currentMove.stop();
            currentMove = new TranslateTransition(Duration.millis(300), slideHighlight);
            currentMove.setToY(targetY);
            currentMove.setInterpolator(Interpolator.EASE_BOTH);
            currentMove.setOnFinished(e -> ensureActiveClass(button));
            currentMove.play();
        } else {
            slideHighlight.setTranslateY(targetY);
            ensureActiveClass(button);
        }
    }

    private void ensureActiveClass(Button button) {
        if (button == activeButton && !button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
    }

    private void setContent(Node content) {
        contentArea.getChildren().setAll(content);

        content.setTranslateX(40);
        content.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), content);
        slide.setFromX(40);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(300), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(slide, fade).play();
    }

    @FXML private void showDashboard() {
        setActive(btnDashboard, "Dashboard");
        setContent(buildDashboard());
    }

    private Node buildDashboard() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");
        root.getChildren().addAll(buildKpiRow(), buildMiddleRow(), buildBottomRow());
        return root;
    }

    private GridPane buildKpiRow() {
        int totalFields = FarmService.getTotalFieldsCount();
        int activeWorkers = WorkerService.getActiveWorkersCount();
        double totalHarvest = WorkerService.getTotalGoodQuantity();
        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();

        GridPane g = row(25, 25, 25, 25);
        VBox c1 = buildKpiCard("\uD83C\uDF3F", String.valueOf(totalFields), "Total Farms", null);
        VBox c2 = buildKpiCard("\uD83D\uDC77", String.valueOf(activeWorkers), "Active Workers", null);
        VBox c3 = buildKpiCard("\uD83C\uDF3E", String.format("%,.0f", totalHarvest), "Total Harvest (kg)", null);
        VBox c4 = buildKpiCard("\uD83D\uDCB0", String.format("\u20AA %,.0f", summary.revenue), "Revenue", null);
        addCells(g, c1, c2, c3, c4);
        return g;
    }

    private GridPane buildMiddleRow() {
        GridPane g = row(38, 31, 31);
        addCells(g, buildBalanceCard(), buildEarningsCard(), buildProfileCard());
        return g;
    }

    private GridPane buildBottomRow() {
        GridPane g = row(38, 31, 31);
        addCells(g, buildRecentHarvestsCard(), buildRecentTransactionsCard(), buildQuickActionsCard());
        return g;
    }

    private VBox buildKpiCard(String icon, String value, String label, Node trailing) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("stat-icon");

        HBox top = new HBox(10, ic);
        top.setAlignment(Pos.CENTER_LEFT);
        if (trailing != null) {
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            top.getChildren().addAll(sp, trailing);
        }

        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        VBox card = styledCard(top, val, lbl);
        card.setSpacing(8);
        card.setPrefHeight(118);
        return card;
    }

    private VBox buildBalanceCard() {
        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();
        double total = summary.revenue + summary.expenses;
        double salesPct = total > 0 ? (summary.revenue / total) * 100 : 0;
        double purchasesPct = 100 - salesPct;

        Label title = new Label("Balance");
        title.getStyleClass().add("card-title");
        Label badge = new Label(summary.netProfit >= 0 ? "On track" : "Over budget");
        badge.getStyleClass().add(summary.netProfit >= 0 ? "pill-up" : "pill-down");
        HBox header = new HBox(8, title, badge);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane ring = buildDualRing(120, 14, salesPct, purchasesPct);

        VBox legend = new VBox(14,
                legendRow("dot-sale", "Sales", String.format("\u20AA %,.0f", summary.revenue)),
                legendRow("dot-purchase", "Purchases", String.format("\u20AA %,.0f", summary.expenses)));
        legend.setAlignment(Pos.CENTER_LEFT);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox body = new HBox(ring, sp, legend);
        body.setAlignment(Pos.CENTER_LEFT);

        VBox card = styledCard(header, body);
        card.setSpacing(14);
        card.setPrefHeight(200);
        return card;
    }

    private VBox buildEarningsCard() {
        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();
        double margin = summary.revenue > 0 ? (summary.netProfit / summary.revenue) * 100 : 0;
        if (margin < 0) margin = 0;
        if (margin > 100) margin = 100;

        Label title = new Label("Earnings");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Net profit");
        sub.getStyleClass().add("card-sub");
        Label big = new Label(String.format("\u20AA %,.0f", summary.netProfit));
        big.getStyleClass().add("big-value");
        Label pct = new Label(String.format("%s %.0f%% margin", summary.netProfit >= 0 ? "\u25B2" : "\u25BC", margin));
        pct.getStyleClass().add(summary.netProfit >= 0 ? "pct-up" : "pct-down");

        VBox left = new VBox(4, title, sub, big, pct);
        left.setAlignment(Pos.CENTER_LEFT);

        StackPane ring = buildRing(92, 12, margin, "ring-progress", String.format("%.0f%%", margin), "Margin");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox body = new HBox(left, sp, ring);
        body.setAlignment(Pos.CENTER_LEFT);

        VBox card = styledCard(body);
        card.setPrefHeight(200);
        return card;
    }

    private VBox buildProfileCard() {
        String fullName = SessionManager.getUserName();
        String initials = getInitials(fullName);

        int fieldsCount = FarmService.getTotalFieldsCount();
        int workersCount = WorkerService.getTotalWorkersCount();
        int harvestsCount = WorkerService.getTotalHarvestsCount();

        Label avatar = new Label(initials);
        avatar.getStyleClass().add("avatar");
        Label name = new Label(fullName);
        name.getStyleClass().add("card-title");
        Label role = new Label("Farm Administrator");
        role.getStyleClass().add("card-sub");

        VBox nameBox = new VBox(2, name, role);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        HBox head = new HBox(12, avatar, nameBox);
        head.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(
                miniStat(String.valueOf(fieldsCount), "Fields"),
                miniStat(String.valueOf(workersCount), "Workers"),
                miniStat(String.valueOf(harvestsCount), "Harvests"));
        stats.setAlignment(Pos.CENTER);

        VBox card = styledCard(head, divider(), stats);
        card.setSpacing(16);
        card.setPrefHeight(200);
        return card;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private String[] getWageUnitsForJob(String jobType) {
        if ("IRRIGATOR".equals(jobType)) {
            return new String[]{"cup"};
        }
        if ("HARVESTER".equals(jobType)) {
            return new String[]{"kg"};
        }
        if ("PLOWER".equals(jobType)) {
            return new String[]{"dunum"};
        }
        return new String[]{"kg", "cup", "dunum"};
    }

    private void applyWageUnitsForJob(ComboBox<String> unitBox, String jobType) {
        String current = unitBox.getValue();
        unitBox.getItems().setAll(getWageUnitsForJob(jobType));
        if (current != null && unitBox.getItems().contains(current)) {
            unitBox.setValue(current);
        } else if (!unitBox.getItems().isEmpty()) {
            unitBox.setValue(unitBox.getItems().get(0));
        }
    }

    private String[] getItemUnitsForType(String itemType) {
  return new String[]{"kg/cup", "liter/cup", "liter/dunum", "kg/dunum"};
    }

    private void applyItemUnitsForType(ComboBox<String> unitBox, String itemType) {
        String current = unitBox.getValue();
        unitBox.getItems().setAll(getItemUnitsForType(itemType));
        if (current != null && unitBox.getItems().contains(current)) {
            unitBox.setValue(current);
        } else if (!unitBox.getItems().isEmpty()) {
            unitBox.setValue(unitBox.getItems().get(0));
        }
    }

    private VBox buildRecentHarvestsCard() {
        Label title = new Label("Recent Harvests");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(12);
        List<Harvest> harvests = WorkerService.getAllHarvests();
        int limit = Math.min(3, harvests.size());

        if (limit == 0) {
            Label empty = new Label("No harvests recorded yet");
            empty.getStyleClass().add("card-sub");
            list.getChildren().add(empty);
        } else {
            for (int i = 0; i < limit; i++) {
                Harvest h = harvests.get(i);
                String qty = String.format("%,.0f %s", h.getQuantityGood(), h.getUnit());
                String dateStr = formatRelativeDate(h.getHarvestDate());
                list.getChildren().add(harvestRow("\uD83C\uDF3E", h.getCropName(), dateStr, qty));
            }
        }

        VBox card = styledCard(title, list);
        card.setSpacing(14);
        card.setPrefHeight(210);
        return card;
    }

    private VBox buildRecentTransactionsCard() {
        Label title = new Label("Recent Transactions");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(12);
        List<Transaction> transactions = TransactionService.getAllTransactions();
        int limit = Math.min(3, transactions.size());

        if (limit == 0) {
            Label empty = new Label("No transactions recorded yet");
            empty.getStyleClass().add("card-sub");
            list.getChildren().add(empty);
        } else {
            for (int i = 0; i < limit; i++) {
                Transaction t = transactions.get(i);
                String sign = "PURCHASE".equals(t.getType()) || "PAYMENT".equals(t.getType()) ? "-" : "+";
                String amount = String.format("%s\u20AA %,.0f", sign, t.getAmount());
                String dateStr = formatRelativeDate(t.getTransactionDate());
                list.getChildren().add(txRow(t.getType(), amount, dateStr, "", true));
            }
        }

        VBox card = styledCard(title, list);
        card.setSpacing(14);
        card.setPrefHeight(210);
        return card;
    }

    private String formatRelativeDate(java.time.LocalDate date) {
        if (date == null) return "";
        long days = java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now());
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 14) return "1 week ago";
        return (days / 7) + " weeks ago";
    }

    private VBox buildQuickActionsCard() {
        Label title = new Label("Quick Actions");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        Button b1 = quickBtn("\u2795  Add Field", this::showFields);
        Button b2 = quickBtn("\uD83D\uDC77  Add Worker", () -> { showWorkers(); showAddWorkerDialog(); });
        Button b3 = quickBtn("\uD83C\uDF3E  View Harvests", this::showHarvests);
        Button b4 = quickBtn("\uD83D\uDCCA  View Reports", this::showReports);

        grid.add(b1, 0, 0);
        grid.add(b2, 1, 0);
        grid.add(b3, 0, 1);
        grid.add(b4, 1, 1);

        VBox card = styledCard(title, grid);
        card.setSpacing(14);
        card.setPrefHeight(210);
        return card;
    }

    private VBox styledCard(Node... children) {
        VBox v = new VBox(children);
        v.getStyleClass().add("dash-card");
        v.setSpacing(10);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }

    private GridPane row(double... widths) {
        GridPane g = new GridPane();
        g.setHgap(18);
        for (double w : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(w);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    private void addCells(GridPane g, Node... cells) {
        for (int i = 0; i < cells.length; i++) {
            GridPane.setHgrow(cells[i], Priority.ALWAYS);
            if (cells[i] instanceof Region) {
                ((Region) cells[i]).setMaxWidth(Double.MAX_VALUE);
            }
            g.add(cells[i], i, 0);
        }
    }

    private Label pill(String text, boolean up) {
        Label l = new Label(text);
        l.getStyleClass().add(up ? "pill-up" : "pill-down");
        return l;
    }

    private Region divider() {
        Region r = new Region();
        r.getStyleClass().add("divider");
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    private VBox miniStat(String value, String label) {
        Label v = new Label(value);
        v.getStyleClass().add("mini-stat-value");
        Label l = new Label(label);
        l.getStyleClass().add("mini-stat-label");
        VBox b = new VBox(2, v, l);
        b.setAlignment(Pos.CENTER);
        b.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, Priority.ALWAYS);
        return b;
    }

    private HBox legendRow(String dotClass, String name, String amount) {
        Region dot = new Region();
        dot.getStyleClass().add(dotClass);
        dot.setMinSize(10, 10);
        dot.setMaxSize(10, 10);
        Label n = new Label(name);
        n.getStyleClass().add("mini-stat-label");
        Label a = new Label(amount);
        a.getStyleClass().add("mini-stat-value");
        VBox txt = new VBox(1, n, a);
        HBox h = new HBox(8, dot, txt);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private HBox harvestRow(String icon, String crop, String date, String qty) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("row-icon");
        Label c = new Label(crop);
        c.getStyleClass().add("list-primary");
        Label d = new Label(date);
        d.getStyleClass().add("list-sub");
        VBox txt = new VBox(1, c, d);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label q = new Label(qty);
        q.getStyleClass().add("list-primary");
        HBox h = new HBox(10, ic, txt, sp, q);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private HBox txRow(String type, String amount, String date, String change, boolean up) {
        Label badge = new Label(type);
        badge.getStyleClass().addAll("badge", badgeClass(type));
        Label amt = new Label(amount);
        amt.getStyleClass().add("list-primary");
        Label d = new Label(date);
        d.getStyleClass().add("list-sub");
        VBox txt = new VBox(1, amt, d);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox h;
        if (change == null || change.isEmpty()) {
            h = new HBox(10, badge, txt, sp);
        } else {
            Label ch = pill(change, up);
            h = new HBox(10, badge, txt, sp, ch);
        }
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private String badgeClass(String type) {
        if ("SALE".equals(type)) return "badge-sale";
        if ("PURCHASE".equals(type)) return "badge-purchase";
        return "badge-payment";
    }

    private Button quickBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("quick-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setMaxHeight(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());
        GridPane.setHgrow(b, Priority.ALWAYS);
        GridPane.setVgrow(b, Priority.ALWAYS);
        return b;
    }

    private Pane sparkBox(double[] ys) {
        double w = 70, h = 32;
        Polyline p = new Polyline();
        double max = ys[0], min = ys[0];
        for (double y : ys) {
            if (y > max) max = y;
            if (y < min) min = y;
        }
        double range = Math.max(1e-6, max - min);
        for (int i = 0; i < ys.length; i++) {
            double x = w * i / (ys.length - 1);
            double y = h - ((ys[i] - min) / range) * h;
            p.getPoints().addAll(x, y);
        }
        p.getStyleClass().add("sparkline");
        Pane pane = new Pane(p);
        pane.setMinSize(w, h);
        pane.setPrefSize(w, h);
        pane.setMaxSize(w, h);
        return pane;
    }

    private StackPane buildRing(double size, double stroke, double percent,
                                String progressClass, String centerText, String subText) {
        double c = size / 2;
        double r = (size - stroke) / 2;

        Circle track = new Circle(c, c, r);
        track.setFill(Color.TRANSPARENT);
        track.getStyleClass().add("ring-track");
        track.setStrokeWidth(stroke);

        Arc arc = new Arc(c, c, r, r, 90, -percent * 3.6);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.getStyleClass().add(progressClass);
        arc.setStrokeWidth(stroke);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);

        Pane ring = new Pane(track, arc);
        ring.setMinSize(size, size);
        ring.setPrefSize(size, size);
        ring.setMaxSize(size, size);

        Label v = new Label(centerText);
        v.getStyleClass().add("ring-center");
        Label s = new Label(subText);
        s.getStyleClass().add("ring-center-sub");
        VBox center = new VBox(v, s);
        center.setAlignment(Pos.CENTER);

        StackPane sp = new StackPane(ring, center);
        sp.setMinSize(size, size);
        sp.setMaxSize(size, size);
        return sp;
    }

    private StackPane buildDualRing(double size, double stroke, double aPct, double bPct) {
        double c = size / 2;
        double r = (size - stroke) / 2;

        Arc a = new Arc(c, c, r, r, 90, -aPct * 3.6);
        a.setType(ArcType.OPEN);
        a.setFill(Color.TRANSPARENT);
        a.getStyleClass().add("ring-sales");
        a.setStrokeWidth(stroke);
        a.setStrokeLineCap(StrokeLineCap.BUTT);

        Arc b = new Arc(c, c, r, r, 90 - aPct * 3.6, -bPct * 3.6);
        b.setType(ArcType.OPEN);
        b.setFill(Color.TRANSPARENT);
        b.getStyleClass().add("ring-purchases");
        b.setStrokeWidth(stroke);
        b.setStrokeLineCap(StrokeLineCap.BUTT);

        Pane ring = new Pane(a, b);
        ring.setMinSize(size, size);
        ring.setPrefSize(size, size);
        ring.setMaxSize(size, size);

        Label v = new Label(Math.round(aPct) + "%");
        v.getStyleClass().add("ring-center");
        Label s = new Label("Sales");
        s.getStyleClass().add("ring-center-sub");
        VBox center = new VBox(v, s);
        center.setAlignment(Pos.CENTER);

        StackPane sp = new StackPane(ring, center);
        sp.setMinSize(size, size);
        sp.setMaxSize(size, size);
        return sp;
    }

    @FXML private void showFields() {
        setActive(btnFields, "Fields & Crops");
        setContent(buildFields());
    }

    private Node buildFields() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        int totalFields = FarmService.getTotalFieldsCount();
        double totalDunums = FarmService.getTotalDunums();
        int activeCrops = FarmService.getActiveCropsCount();

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83C\uDF3F", String.valueOf(totalFields), "Total Fields"),
                buildFieldStatCard("\uD83D\uDCCF", String.format("%,.0f", totalDunums), "Total Area (m\u00B2)"),
                buildFieldStatCard("\uD83C\uDF31", String.valueOf(activeCrops), "Active Crops"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search fields...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("\u2795  Add Field");
        addBtn.getStyleClass().add("action-btn");
        addBtn.setOnAction(e -> showAddFieldDialog());

        toolbar.getChildren().addAll(searchField, sp, addBtn);

        VBox fieldsList = new VBox(14);
        List<com.smartfarm.model.Field> fields = FarmService.getAllFields();
        populateFieldsReal(fieldsList, fields, "");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            populateFieldsReal(fieldsList, fields, newVal.trim().toLowerCase());
        });

        root.getChildren().addAll(stats, toolbar, fieldsList);
        return root;
    }

    private void showAddFieldDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Field");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        TextField nameField = new TextField();
        nameField.setPromptText("Field name (e.g. Field D-1)");

        TextField sizeField = new TextField();
        sizeField.setPromptText("Enter size");

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.getItems().addAll(com.smartfarm.util.AreaUnitConverter.ALL_UNITS);
        unitBox.setValue(com.smartfarm.util.AreaUnitConverter.SQUARE_METER);
        unitBox.setMaxWidth(Double.MAX_VALUE);

        HBox sizeRow = new HBox(8, sizeField, unitBox);
        HBox.setHgrow(sizeField, Priority.ALWAYS);

        TextField locationField = new TextField();
        locationField.setPromptText("Location");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                new Label("Field Name"), nameField,
                new Label("Size"), sizeRow,
                new Label("Location"), locationField,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            FarmService.Result result = FarmService.addField(
                    nameField.getText(), sizeField.getText(), unitBox.getValue(), locationField.getText());

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showFields();
        }
    }

    private void populateFieldsReal(VBox container, List<com.smartfarm.model.Field> fields, String query) {
        container.getChildren().clear();
        for (com.smartfarm.model.Field f : fields) {
            List<com.smartfarm.model.Crop> crops = FarmService.getCropsByField(f.getFieldId());

            if (!query.isEmpty()) {
                String cropNames = crops.stream().map(com.smartfarm.model.Crop::getName)
                        .reduce((a, b) -> a + " " + b).orElse("");
                String combined = (f.getName() + " " + f.getLocation() + " " + cropNames).toLowerCase();
                if (!combined.contains(query)) continue;
            }

            container.getChildren().add(buildFieldCard(
                    f.getFieldId(), f.getName(), f.getLocation() != null && !f.getLocation().isBlank() ? f.getLocation() : "Unknown",
                    String.format("%,.0f", f.getSizeDunums()), "N/A", crops));
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No fields found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
        }
    }

    private VBox buildFieldStatCard(String icon, String value, String label) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("stat-icon");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        VBox card = styledCard(ic, val, lbl);
        card.setSpacing(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(110);
        return card;
    }

    private VBox buildFieldCard(int fieldId, String name, String location, String dunums, String lastIrrigation, List<com.smartfarm.model.Crop> crops) {
        Label nameLabel = new Label("\uD83C\uDF3F " + name);
        nameLabel.getStyleClass().add("field-name");

        Label descLabel = new Label(location);
        descLabel.getStyleClass().add("card-sub");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Button deleteFieldBtn = new Button("\uD83D\uDDD1");
        deleteFieldBtn.getStyleClass().add("filter-btn");
        deleteFieldBtn.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px; -fx-padding: 4 8 4 8;");
        deleteFieldBtn.setOnAction(e -> confirmDeleteField(fieldId, name));

        HBox header = new HBox(8, nameLabel, descLabel, sp1, deleteFieldBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox infoRow = new HBox(24,
                fieldInfo("\uD83D\uDCCF", "Size", dunums + " m\u00B2"),
                fieldInfo("\uD83D\uDCA7", "Last Irrigation", lastIrrigation));
        infoRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = styledCard(header, divider(), infoRow);
        card.setSpacing(12);

        Label cropsTitle = new Label("\uD83C\uDF31 Planted Crops");
        cropsTitle.getStyleClass().add("crops-title");

        Region cropSp = new Region();
        HBox.setHgrow(cropSp, Priority.ALWAYS);

        Button addCropBtn = new Button("\u2795 Add Crop");
        addCropBtn.getStyleClass().add("filter-btn");
        addCropBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
        addCropBtn.setOnAction(e -> showAddCropDialog(fieldId));

        HBox cropsHeader = new HBox(cropsTitle, cropSp, addCropBtn);
        cropsHeader.setAlignment(Pos.CENTER_LEFT);

        HBox cropsRow = new HBox(10);
        cropsRow.setAlignment(Pos.CENTER_LEFT);
        cropsRow.setStyle("-fx-cursor: hand;");

        if (!crops.isEmpty()) {
            for (com.smartfarm.model.Crop c : crops) {
                Label chip = new Label(c.getName() + (c.getQuantity() != null && !c.getQuantity().isEmpty() ? " (" + c.getQuantity() + ")" : ""));
                chip.getStyleClass().add("crop-chip");
                chip.setStyle(chip.getStyle() + "-fx-cursor: hand;");
                chip.setOnMouseClicked(e -> showEditCropDialog(c));
                cropsRow.getChildren().add(chip);
            }
        } else {
            Label none = new Label("No crops planted yet");
            none.getStyleClass().add("card-sub");
            cropsRow.getChildren().add(none);
        }

        card.getChildren().addAll(divider(), cropsHeader, cropsRow);

        return card;
    }

    private void confirmDeleteField(int fieldId, String fieldName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Field");
        alert.setHeaderText("Delete " + fieldName + "?");
        alert.setContentText("This cannot be undone. Fields with active crops cannot be deleted.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FarmService.Result r = FarmService.deleteField(fieldId);
            if (r.success) {
                showFields();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Cannot Delete");
                error.setContentText(r.message);
                error.showAndWait();
            }
        }
    }

    private void showEditCropDialog(com.smartfarm.model.Crop crop) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Crop: " + crop.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ButtonType deleteType = new ButtonType("Delete Crop", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().add(deleteType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("GROWING", "READY", "HARVESTED");
        statusBox.setValue(crop.getStatus());
        statusBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                new Label("Crop Name: " + crop.getName()),
                new Label("Type: " + crop.getType()),
                new Label("Update Status"), statusBox,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            FarmService.Result result = FarmService.updateCropStatus(crop.getCropId(), statusBox.getValue());
            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();

        if (resultBtn.isPresent() && resultBtn.get() == deleteType) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Crop");
            confirm.setHeaderText("Delete " + crop.getName() + "?");
            confirm.setContentText("This cannot be undone. Crops with harvest records cannot be deleted.");
            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                FarmService.Result deleteResult = FarmService.deleteCrop(crop.getCropId());
                if (!deleteResult.success) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Cannot Delete");
                    error.setContentText(deleteResult.message);
                    error.showAndWait();
                }
                showFields();
            }
        } else if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showFields();
        }
    }

    private void showAddCropDialog(int fieldId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Crop");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        TextField nameField = new TextField();
        nameField.setPromptText("Crop name (e.g. Tomatoes)");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("VEGETABLE", "FRUIT", "GRAIN", "TREE");
        typeBox.setValue("VEGETABLE");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker plantedDatePicker = new DatePicker(java.time.LocalDate.now());
        plantedDatePicker.setMaxWidth(Double.MAX_VALUE);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity (e.g. 2 dunums, 450 trees)");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                new Label("Crop Name"), nameField,
                new Label("Type"), typeBox,
                new Label("Planted Date"), plantedDatePicker,
                new Label("Quantity"), quantityField,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            FarmService.Result result = FarmService.addCrop(
                    fieldId, nameField.getText(), typeBox.getValue(),
                    plantedDatePicker.getValue(), quantityField.getText());

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showFields();
        }
    }

    private VBox fieldInfo(String icon, String label, String value) {
        Label ic = new Label(icon);
        ic.getStyleClass().add("field-info-icon");
        Label lbl = new Label(label);
        lbl.getStyleClass().add("mini-stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("mini-stat-value");
        val.setStyle("-fx-font-size: 13px;");
        VBox box = new VBox(2, new HBox(4, ic, lbl), val);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    @FXML private void showHarvests() {
        setActive(btnHarvests, "Harvests");
        setContent(buildHarvests());
    }

    private String harvestFilter = "PENDING";

    private Node buildHarvests() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        int totalHarvests = WorkerService.getTotalHarvestsCount();
        double totalKg = WorkerService.getTotalGoodQuantity();
        int pendingCount = WorkerService.getPendingHarvestsCount();

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83C\uDF3E", String.valueOf(totalHarvests), "Total Harvests"),
                buildFieldStatCard("\u2696\uFE0F", String.format("%,.0f", totalKg), "Approved kg"),
                buildFieldStatCard("\u23F3", String.valueOf(pendingCount), "Pending Review"));

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        String[] fNames = {"Pending", "Approved", "Rejected", "All"};
        String[] fKeys = {"PENDING", "APPROVED", "REJECTED", "ALL"};
        String[] fIcons = {"\u23F3", "\u2705", "\u274C", "\uD83D\uDCCB"};
        for (int i = 0; i < fNames.length; i++) {
            Button fb = new Button(fIcons[i] + "  " + fNames[i]);
            String key = fKeys[i];
            fb.getStyleClass().add("filter-btn");
            if (harvestFilter.equals(key)) fb.getStyleClass().add("filter-active");
            fb.setOnAction(e -> { harvestFilter = key; showHarvests(); });
            filterBar.getChildren().add(fb);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        filterBar.getChildren().addAll(spacer, searchField);

        List<Harvest> harvests = WorkerService.getHarvestsByStatus(harvestFilter);
        VBox list = new VBox(12);
        populateHarvestsReal(list, harvests, "");

        searchField.textProperty().addListener((obs, o, n) -> populateHarvestsReal(list, harvests, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, filterBar, list);
        return root;
    }

    private String findBestCrop(List<Harvest> harvests) {
        if (harvests.isEmpty()) return "N/A";
        java.util.Map<String, Double> totals = new java.util.HashMap<>();
        for (Harvest h : harvests) {
            totals.merge(h.getCropName(), h.getQuantityGood(), Double::sum);
        }
        return totals.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("N/A");
    }

    private void showAddWorkerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Worker");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label helpLabel = new Label("Search by the worker's registered name, email, or phone");
        helpLabel.getStyleClass().add("card-sub");
        helpLabel.setStyle("-fx-font-size: 11px;");

        TextField identifierField = new TextField();
        identifierField.setPromptText("Name, email, or phone");

        ComboBox<String> jobBox = new ComboBox<>();
        jobBox.getItems().addAll("IRRIGATOR", "HARVESTER", "PLOWER");
        jobBox.setValue("HARVESTER");
        jobBox.setMaxWidth(Double.MAX_VALUE);

        TextField wageField = new TextField();
        wageField.setPromptText("Wage per unit (e.g. 8)");

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.setMaxWidth(Double.MAX_VALUE);
        applyWageUnitsForJob(unitBox, jobBox.getValue());
        jobBox.valueProperty().addListener((o, ov, nv) -> applyWageUnitsForJob(unitBox, nv));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                helpLabel,
                new Label("Worker Identifier"), identifierField,
                new Label("Job Type"), jobBox,
                new Label("Wage per Unit"), wageField,
                new Label("Wage Unit"), unitBox,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            WorkerService.Result result = WorkerService.assignNewWorker(
                    identifierField.getText(), jobBox.getValue(), wageField.getText(), unitBox.getValue());

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showWorkers();
        }
    }

    private String cropIcon(String cropName) {
        if (cropName == null) return "\uD83C\uDF31";
        String n = cropName.toLowerCase();
        if (n.contains("tomato")) return "\uD83C\uDF45";
        if (n.contains("potato")) return "\uD83E\uDD54";
        if (n.contains("carrot")) return "\uD83E\uDD55";
        if(n.contains("corn") || n.contains("maize")) return "\uD83C\uDF3D";
        if (n.contains("wheat") || n.contains("barley")) return "\uD83C\uDF3E";
        if (n.contains("olive")) return "\ud83c\udf33";
        if (n.contains("cucumber")) return "\uD83E\uDD52";
        if (n.contains("citrus")) return "\uD83C\uDF4A";
        if (n.contains("fig")) return "\uD83C\uDF5E";
        if (n.contains("grape")) return "\uD83C\uDF47";
        if (n.contains("banana")) return "\uD83C\uDF4C";
        if (n.contains("apple")) return "\uD83C\uDF4E";
        if (n.contains("pear")) return "\uD83C\uDF50";
        if (n.contains("peach")) return "\uD83C\uDF51";
        if (n.contains("cherry")) return "\uD83C\uDF52";
        if (n.contains("strawberry")) return "\uD83C\uDF53";
        if (n.contains("watermelon")) return "\uD83C\uDF49";
        if (n.contains("pumpkin")) return "\uD83C\uDF83";
        return "\uD83C\uDF31";
    }

    private void populateHarvestsReal(VBox container, List<Harvest> data, String query) {
        container.getChildren().clear();
        for (Harvest h : data) {
            if (!query.isEmpty() && !(h.getCropName() + " " + h.getFieldName() + " " + h.getWorkerName()).toLowerCase().contains(query)) continue;

            Label ic = new Label(cropIcon(h.getCropName()));
            ic.getStyleClass().add("row-icon");
            Label crop = new Label(h.getCropName());
            crop.getStyleClass().add("list-primary");
            Label field = new Label(h.getWorkerName() + " \u2022 " + h.getFieldName() + " \u2022 " + formatRelativeDate(h.getHarvestDate()));
            field.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, crop, field);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label good = new Label(String.format("\u2705 %.0f %s", h.getQuantityGood(), h.getUnit()));
            good.getStyleClass().add("pct-up");
            good.setStyle("-fx-font-size: 13px;");
            Label damaged = new Label(String.format("\u274C %.0f %s", h.getQuantityDamaged(), h.getUnit()));
            damaged.getStyleClass().add("pct-down");
            damaged.setStyle("-fx-font-size: 12px;");
            VBox nums = new VBox(2, good, damaged);
            nums.setAlignment(Pos.CENTER_RIGHT);

            HBox row = new HBox(12, ic, txt, sp, nums);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");

            if ("PENDING".equals(h.getStatus())) {
                Button approveBtn = new Button("\u2714");
                approveBtn.getStyleClass().add("filter-btn");
                approveBtn.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 4 10 4 10;");
                approveBtn.setOnAction(e -> {
                    WorkerService.approveHarvest(h.getHarvestId());
                    showHarvests();
                });

                Button rejectBtn = new Button("\u2716");
                rejectBtn.getStyleClass().add("filter-btn");
                rejectBtn.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-padding: 4 10 4 10;");
                rejectBtn.setOnAction(e -> {
                    WorkerService.rejectHarvest(h.getHarvestId());
                    showHarvests();
                });

                VBox actions = new VBox(4, approveBtn, rejectBtn);
                row.getChildren().add(actions);
            } else {
                Label statusBadge = new Label("APPROVED".equals(h.getStatus()) ? "Approved" : "Rejected");
                statusBadge.getStyleClass().add("APPROVED".equals(h.getStatus()) ? "pill-up" : "pill-down");
                row.getChildren().add(statusBadge);
            }

            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No harvests found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
        }
    }

    @FXML private void showWorkers() {
        setActive(btnWorkers, "Workers");
        setContent(buildWorkers());
    }

    private Node buildWorkers() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        int totalWorkers = WorkerService.getTotalWorkersCount();
        int activeWorkers = WorkerService.getActiveWorkersCount();
        int fieldsCount = FarmService.getTotalFieldsCount();

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83D\uDC77", String.valueOf(totalWorkers), "Total Workers"),
                buildFieldStatCard("\u2705", String.valueOf(activeWorkers), "Active"),
                buildFieldStatCard("\uD83C\uDF3F", String.valueOf(fieldsCount), "Fields Covered"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search workers...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);

        Region workerSp = new Region();
        HBox.setHgrow(workerSp, Priority.ALWAYS);

        Button addWorkerBtn = new Button("\u2795  Add Worker");
        addWorkerBtn.getStyleClass().add("action-btn");
        addWorkerBtn.setOnAction(e -> showAddWorkerDialog());

        toolbar.getChildren().addAll(searchField, workerSp, addWorkerBtn);

        List<com.smartfarm.model.FarmWorker> workers = WorkerService.getAllWorkers();
        VBox list = new VBox(12);
        populateWorkersReal(list, workers, "");

        searchField.textProperty().addListener((obs, o, n) -> populateWorkersReal(list, workers, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, toolbar, list);
        return root;
    }

    private void populateWorkersReal(VBox container, List<com.smartfarm.model.FarmWorker> data, String query) {
        container.getChildren().clear();
        for (com.smartfarm.model.FarmWorker w : data) {
            if (!query.isEmpty() && !(w.getUserName() + " " + w.getJobType()).toLowerCase().contains(query)) continue;

            Label avatar = new Label(getInitials(w.getUserName()));
            avatar.getStyleClass().add("avatar");
            avatar.setStyle("-fx-font-size: 14px; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
            Label name = new Label(w.getUserName());
            name.getStyleClass().add("list-primary");
            String rawJob = w.getJobType() != null ? w.getJobType() : "WORKER";
            String jobLabel = rawJob.charAt(0) + rawJob.substring(1).toLowerCase();
            Label role = new Label(jobLabel);
            role.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, name, role);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label wage = new Label(String.format("\uD83D\uDCB0 \u20AA %.0f/%s", w.getWagePerUnit(), w.getWageUnit()));
            wage.getStyleClass().add("mini-stat-value");
            wage.setStyle("-fx-font-size: 13px;");
            Label wageLabel = new Label("Wage per unit");
            wageLabel.getStyleClass().add("list-sub");
            Label status = new Label(w.isActive() ? "Active" : "Inactive");
            status.getStyleClass().add(w.isActive() ? "pill-up" : "pill-down");
            VBox right = new VBox(3, wage, wageLabel, status);
            right.setAlignment(Pos.CENTER_RIGHT);

            Button deleteBtn = new Button("\uD83D\uDDD1");
            deleteBtn.getStyleClass().add("filter-btn");
            deleteBtn.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px; -fx-padding: 4 8 4 8;");
            int fwId = w.getFwId();
            String workerName = w.getUserName();
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Worker");
                alert.setHeaderText("Remove " + workerName + " from the farm?");
                alert.setContentText("If this worker has harvest or log records, deletion will fail \u2014 deactivate instead in that case.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    WorkerService.Result r = WorkerService.deleteWorker(fwId);
                    if (r.success) {
                        showWorkers();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Cannot Delete");
                        error.setContentText(r.message);
                        error.showAndWait();
                    }
                }
            });

            HBox row = new HBox(12, avatar, txt, sp, right, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");
            row.setOnMouseClicked(e -> {
                if (e.getTarget() == deleteBtn || (e.getTarget() instanceof Node && isDescendantOf((Node) e.getTarget(), deleteBtn))) {
                    return;
                }
                showEditWorkerDialog(w);
            });
            row.setStyle(row.getStyle() + "; -fx-cursor: hand;");
            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No workers found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
        }
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        Node current = node;
        while (current != null) {
            if (current == ancestor) return true;
            current = current.getParent();
        }
        return false;
    }

    private void showEditWorkerDialog(com.smartfarm.model.FarmWorker worker) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Worker: " + worker.getUserName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ComboBox<String> jobBox = new ComboBox<>();
        jobBox.getItems().addAll("IRRIGATOR", "HARVESTER", "PLOWER");
        jobBox.setValue(worker.getJobType());
        jobBox.setMaxWidth(Double.MAX_VALUE);

        TextField wageField = new TextField(String.valueOf(worker.getWagePerUnit()));

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.setMaxWidth(Double.MAX_VALUE);
        applyWageUnitsForJob(unitBox, jobBox.getValue());
        if (unitBox.getItems().contains(worker.getWageUnit())) {
            unitBox.setValue(worker.getWageUnit());
        }
        jobBox.valueProperty().addListener((o, ov, nv) -> applyWageUnitsForJob(unitBox, nv));

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "INACTIVE");
        statusBox.setValue(worker.getStatus());
        statusBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                new Label("Job Type"), jobBox,
                new Label("Wage per Unit"), wageField,
                new Label("Wage Unit"), unitBox,
                new Label("Status"), statusBox,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            WorkerService.Result result = WorkerService.updateWorker(
                    worker.getFwId(), jobBox.getValue(), wageField.getText(), unitBox.getValue(), statusBox.getValue());

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showWorkers();
        }
    }

    @FXML private void showTransactions() {
        setActive(btnTransactions, "Transactions");
        setContent(buildTransactions());
    }

    private String txFilter = "ALL";

    private Node buildTransactions() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83D\uDCB0", String.format("\u20AA %,.0f", summary.revenue), "Total Revenue"),
                buildFieldStatCard("\uD83D\uDED2", String.format("\u20AA %,.0f", summary.expenses), "Total Expenses"),
                buildFieldStatCard("\uD83D\uDCCA", String.format("\u20AA %,.0f", summary.netProfit), "Net Profit"));

        HBox txToolbar = new HBox(12);
        txToolbar.setAlignment(Pos.CENTER_RIGHT);
        Button addTxBtn = new Button("\u2795  Add Transaction");
        addTxBtn.getStyleClass().add("action-btn");
        addTxBtn.setOnAction(e -> showAddTransactionDialog());
        txToolbar.getChildren().add(addTxBtn);

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        String[] fNames = {"All", "Sale", "Purchase", "Payment"};
        String[] fKeys = {"ALL", "SALE", "PURCHASE", "PAYMENT"};
        String[] fIcons = {"\uD83D\uDCCB", "\uD83D\uDCB5", "\uD83D\uDED2", "\uD83D\uDCB3"};
        for (int i = 0; i < fNames.length; i++) {
            Button fb = new Button(fIcons[i] + "  " + fNames[i]);
            String key = fKeys[i];
            fb.getStyleClass().add("filter-btn");
            if (txFilter.equals(key)) fb.getStyleClass().add("filter-active");
            fb.setOnAction(e -> { txFilter = key; showTransactions(); });
            filterBar.getChildren().add(fb);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);
        filterBar.getChildren().addAll(spacer, searchField);

        List<Transaction> transactions = TransactionService.getTransactionsByType(txFilter);
        VBox list = new VBox(12);
        populateTransactionsReal(list, transactions, "");

        searchField.textProperty().addListener((obs, o, n) ->
                populateTransactionsReal(list, transactions, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, txToolbar, filterBar, list);
        return root;
    }

    private void showAddTransactionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Transaction");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("SALE", "PURCHASE", "PAYMENT");
        typeBox.setValue("SALE");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount in NIS");

        TextField descField = new TextField();
        descField.setPromptText("Description (e.g. Sold 500kg tomatoes)");

        Label workerLabel = new Label("Worker (for Payment)");
        workerLabel.getStyleClass().add("mini-stat-label");
        workerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<com.smartfarm.model.FarmWorker> workerBox = new ComboBox<>();
        workerBox.getItems().addAll(WorkerService.getAllWorkers());
        workerBox.setConverter(new javafx.util.StringConverter<com.smartfarm.model.FarmWorker>() {
            public String toString(com.smartfarm.model.FarmWorker w) { return w != null ? w.getUserName() : ""; }
            public com.smartfarm.model.FarmWorker fromString(String s) { return null; }
        });
        workerBox.setMaxWidth(Double.MAX_VALUE);
        workerLabel.setVisible(false); workerLabel.setManaged(false);
        workerBox.setVisible(false); workerBox.setManaged(false);

        typeBox.valueProperty().addListener((o, ov, nv) -> {
            boolean isPayment = "PAYMENT".equals(nv);
            workerLabel.setVisible(isPayment); workerLabel.setManaged(isPayment);
            workerBox.setVisible(isPayment); workerBox.setManaged(isPayment);
        });

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                new Label("Type"), typeBox,
                new Label("Amount (NIS)"), amountField,
                new Label("Description"), descField,
                workerLabel, workerBox,
                errorLabel);

        dialog.getDialogPane().setContent(content);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) {
                    errorLabel.setText("Amount must be greater than 0");
                    event.consume();
                    return;
                }
            } catch (Exception ex) {
                errorLabel.setText("Amount must be a valid number");
                event.consume();
                return;
            }

            TransactionService.Result result;
            switch (typeBox.getValue()) {
                case "SALE":
                    result = TransactionService.recordSale(amount, descField.getText(), null);
                    break;
                case "PAYMENT":
                    if (workerBox.getValue() == null) {
                        errorLabel.setText("Please select a worker");
                        event.consume();
                        return;
                    }
                    result = TransactionService.recordPayment(amount, descField.getText(), workerBox.getValue().getUserId());
                    break;
                case "PURCHASE":
                default:
                    result = TransactionService.recordPurchase(amount, descField.getText());
                    break;
            }

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showTransactions();
        }
    }

    private void populateTransactionsReal(VBox container, List<Transaction> data, String query) {
        container.getChildren().clear();
        for (Transaction t : data) {
            String desc = t.getDescription() != null ? t.getDescription() : "";
            if (!query.isEmpty() && !desc.toLowerCase().contains(query)) continue;

            Label badge = new Label(t.getType());
            badge.getStyleClass().addAll("badge", badgeClass(t.getType()));
            Label title = new Label(txIcon(t.getType()) + "  " + desc);
            title.getStyleClass().add("list-primary");
            Label dateLbl = new Label(formatRelativeDate(t.getTransactionDate()));
            dateLbl.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, title, dateLbl);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            String sign = "SALE".equals(t.getType()) ? "+" : "-";
            Label amount = new Label(String.format("%s\u20AA %,.0f", sign, t.getAmount()));
            amount.getStyleClass().add("SALE".equals(t.getType()) ? "pct-up" : "pct-down");
            amount.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
            VBox right = new VBox(4, amount);
            right.setAlignment(Pos.CENTER_RIGHT);

            Button deleteBtn = new Button("\uD83D\uDDD1");
            deleteBtn.getStyleClass().add("filter-btn");
            deleteBtn.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
            int txId = t.getTransactionId();
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Transaction");
                alert.setHeaderText("Delete this transaction?");
                alert.setContentText("This cannot be undone.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    TransactionService.deleteTransaction(txId);
                    showTransactions();
                }
            });

            HBox row = new HBox(12, badge, txt, sp, right, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");
            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No transactions found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
        }
    }

    private String txIcon(String type) {
        switch (type) {
            case "SALE": return "\uD83D\uDCB5";
            case "PURCHASE": return "\uD83D\uDED2";
            case "PAYMENT": return "\uD83D\uDCB3";
            default: return "\uD83D\uDCB0";
        }
    }

    @FXML private void showFertilizers() {
        setActive(btnFertilizers, "Fertilizers");
        setContent(buildFertilizers());
    }

    private Node buildFertilizers() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        int totalItems = FarmService.getTotalItemsCount();
        int lowStock = FarmService.getLowStockCount();
        int inStock = totalItems - lowStock;

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83E\uDDEA", String.valueOf(totalItems), "Total Items"),
                buildFieldStatCard("\u2705", String.valueOf(inStock), "In Stock"),
                buildFieldStatCard("\u26A0\uFE0F", String.valueOf(lowStock), "Low Stock"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search fertilizers...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);

        Region fmSp = new Region();
        HBox.setHgrow(fmSp, Priority.ALWAYS);

        Button addItemBtn = new Button("\u2795  Add Item");
        addItemBtn.getStyleClass().add("action-btn");
        addItemBtn.setOnAction(e -> showAddFertilizerDialog());

        toolbar.getChildren().addAll(searchField, fmSp, addItemBtn);

        List<com.smartfarm.model.FertilizerMedicine> items = FarmService.getAllFertilizersAndMedicines();
        VBox list = new VBox(12);
        populateFertilizersReal(list, items, "");

        searchField.textProperty().addListener((obs, o, n) -> populateFertilizersReal(list, items, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, toolbar, list);
        return root;
    }

    private void showAddFertilizerDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Fertilizer or Medicine");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        List<com.smartfarm.model.Field> fields = FarmService.getAllFields();

        Label fieldLabel = new Label("Field (optional \u2014 leave empty for whole farm)");
        fieldLabel.getStyleClass().add("mini-stat-label");
        fieldLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        ComboBox<com.smartfarm.model.Field> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(fields);
        fieldBox.setConverter(new javafx.util.StringConverter<com.smartfarm.model.Field>() {
            public String toString(com.smartfarm.model.Field f) { return f != null ? f.getName() : ""; }
            public com.smartfarm.model.Field fromString(String s) { return null; }
        });
        fieldBox.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField();
        nameField.setPromptText("Name (e.g. NPK Fertilizer)");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("FERTILIZER", "MEDICINE");
        typeBox.setValue("FERTILIZER");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField compositionField = new TextField();
        compositionField.setPromptText("Composition (e.g. NPK 20-20-20)");

        Label activeLabel = new Label("Active Ingredient (Medicine only)");
        activeLabel.getStyleClass().add("mini-stat-label"); activeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        TextField activeField = new TextField();
        activeField.setPromptText("e.g. Copper hydroxide");

        Label diseaseLabel = new Label("Target Disease/Pest (Medicine only)");
        diseaseLabel.getStyleClass().add("mini-stat-label"); diseaseLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        TextField diseaseField = new TextField();
        diseaseField.setPromptText("e.g. Olive leaf spot");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity (e.g. 12 kg/cup)");

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.setMaxWidth(Double.MAX_VALUE);
        applyItemUnitsForType(unitBox, typeBox.getValue());

        CheckBox organicBox = new CheckBox("Organic");

        TextField notesField = new TextField();
        notesField.setPromptText("Notes (optional)");

        Runnable toggleMedicineFields = () -> {
            boolean isMedicine = "MEDICINE".equals(typeBox.getValue());
            activeLabel.setVisible(isMedicine); activeLabel.setManaged(isMedicine);
            activeField.setVisible(isMedicine); activeField.setManaged(isMedicine);
            diseaseLabel.setVisible(isMedicine); diseaseLabel.setManaged(isMedicine);
            diseaseField.setVisible(isMedicine); diseaseField.setManaged(isMedicine);
            organicBox.setVisible(!isMedicine); organicBox.setManaged(!isMedicine);
            applyItemUnitsForType(unitBox, typeBox.getValue());
        };
        typeBox.valueProperty().addListener((o, ov, nv) -> toggleMedicineFields.run());
        toggleMedicineFields.run();

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");

        content.getChildren().addAll(
                fieldLabel, fieldBox,
                new Label("Name"), nameField,
                new Label("Type"), typeBox,
                new Label("Composition"), compositionField,
                activeLabel, activeField,
                diseaseLabel, diseaseField,
                new Label("Quantity"), quantityField,
                new Label("Unit"), unitBox,
                organicBox,
                new Label("Notes"), notesField,
                errorLabel);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(420);
        scrollPane.setStyle("-fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scrollPane);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            double quantity;
            try {
                quantity = Double.parseDouble(quantityField.getText().trim());
                if (quantity <= 0) {
                    errorLabel.setText("Quantity must be greater than 0");
                    event.consume();
                    return;
                }
            } catch (Exception ex) {
                errorLabel.setText("Quantity must be a valid number");
                event.consume();
                return;
            }

            Integer fieldId = fieldBox.getValue() != null ? fieldBox.getValue().getFieldId() : null;

            FarmService.Result result = FarmService.addFertilizerOrMedicine(
                    fieldId, nameField.getText(), typeBox.getValue(), compositionField.getText(),
                    activeField.getText(), diseaseField.getText(), quantity, unitBox.getValue(),
                    organicBox.isSelected(), notesField.getText());

            if (!result.success) {
                errorLabel.setText(result.message);
                event.consume();
            }
        });

        Optional<ButtonType> resultBtn = dialog.showAndWait();
        if (resultBtn.isPresent() && resultBtn.get() == ButtonType.OK) {
            showFertilizers();
        }
    }

    private void populateFertilizersReal(VBox container, List<com.smartfarm.model.FertilizerMedicine> data, String query) {
        container.getChildren().clear();
        for (com.smartfarm.model.FertilizerMedicine fm : data) {
            String fieldName = fm.getFieldName() != null ? fm.getFieldName() : "All Fields";
            if (!query.isEmpty() && !(fm.getName() + " " + fm.getType() + " " + fieldName).toLowerCase().contains(query)) continue;

            String icon = "FERTILIZER".equals(fm.getType()) ? "\uD83E\uDDEA" : "\uD83D\uDC8A";
            Label ic = new Label(icon);
            ic.getStyleClass().add("row-icon");
            Label name = new Label(fm.getName());
            name.getStyleClass().add("list-primary");
            String typeLabel = fm.getType().charAt(0) + fm.getType().substring(1).toLowerCase();
            String dateStr = formatRelativeDate(fm.getAppliedDate());
            Label info = new Label(typeLabel + " \u2022 " + fieldName + " \u2022 " + dateStr);
            info.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, name, info);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label qty = new Label(String.format("%.0f %s", fm.getQuantity(), fm.getUnit()));
            qty.getStyleClass().add("mini-stat-value");
            qty.setStyle("-fx-font-size: 13px;");
            Label stock = new Label(fm.isLowStock() ? "Low" : "Good");
            stock.getStyleClass().add(fm.isLowStock() ? "pill-down" : "pill-up");
            VBox right = new VBox(4, qty, stock);
            right.setAlignment(Pos.CENTER_RIGHT);

            Button deleteBtn = new Button("\uD83D\uDDD1");
            deleteBtn.getStyleClass().add("filter-btn");
            deleteBtn.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px; -fx-padding: 4 8 4 8;");
            int fmId = fm.getFmId();
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Item");
                alert.setHeaderText("Delete " + fm.getName() + "?");
                alert.setContentText("This cannot be undone.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    FarmService.deleteFertilizerOrMedicine(fmId);
                    showFertilizers();
                }
            });

            HBox row = new HBox(12, ic, txt, sp, right, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");
            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No items found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
        }
    }

    @FXML private void showReports() {
        setActive(btnReports, "Reports");
        setContent(buildReports());
    }

    private Node buildReports() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        Label subtitle = new Label("Farm Performance Overview");
        subtitle.getStyleClass().add("card-title");

        int fieldsCount = FarmService.getTotalFieldsCount();
        int workersCount = WorkerService.getTotalWorkersCount();
        int harvestsCount = WorkerService.getTotalHarvestsCount();
        List<Transaction> allTx = TransactionService.getAllTransactions();

        GridPane topRow = row(25, 25, 25, 25);
        addCells(topRow,
                buildFieldStatCard("\uD83C\uDF3F", String.valueOf(fieldsCount), "Fields"),
                buildFieldStatCard("\uD83D\uDC77", String.valueOf(workersCount), "Workers"),
                buildFieldStatCard("\uD83C\uDF3E", String.valueOf(harvestsCount), "Harvests"),
                buildFieldStatCard("\uD83D\uDCB0", String.valueOf(allTx.size()), "Transactions"));

        List<Harvest> allHarvests = WorkerService.getAllHarvests();
        double totalGood = allHarvests.stream().mapToDouble(Harvest::getQuantityGood).sum();
        double totalDamaged = allHarvests.stream().mapToDouble(Harvest::getQuantityDamaged).sum();
        double quality = (totalGood + totalDamaged) > 0 ? (totalGood / (totalGood + totalDamaged)) * 100 : 100;

        TransactionService.FinancialSummary summary = TransactionService.getFinancialSummary();
        double budgetUsed = summary.revenue > 0 ? Math.min(100, (summary.expenses / summary.revenue) * 100) : 0;

        GridPane midRow = row(50, 50);
        addCells(midRow,
                buildReportRingCard("Harvest Quality", quality, "ring-progress", String.format("%.0f%%", quality), "Quality"),
                buildReportRingCard("Budget Usage", budgetUsed, "ring-purchases", String.format("%.0f%%", budgetUsed), "Used"));

        VBox cropTable = styledCard();
        cropTable.setSpacing(12);
        Label tableTitle = new Label("\uD83C\uDFC6 Top Crops by Yield");
        tableTitle.getStyleClass().add("card-title");
        cropTable.getChildren().add(tableTitle);

        java.util.Map<String, Double> cropTotals = new java.util.LinkedHashMap<>();
        for (Harvest h : allHarvests) {
            cropTotals.merge(h.getCropName(), h.getQuantityGood(), Double::sum);
        }
        double grandTotal = cropTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        List<java.util.Map.Entry<String, Double>> topCrops = cropTotals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        if (topCrops.isEmpty()) {
            Label empty = new Label("No harvest data yet");
            empty.getStyleClass().add("card-sub");
            cropTable.getChildren().add(empty);
        } else {
            int rank = 1;
            for (java.util.Map.Entry<String, Double> entry : topCrops) {
                double pct = grandTotal > 0 ? (entry.getValue() / grandTotal) * 100 : 0;

                Label rankLbl = new Label(String.valueOf(rank));
                rankLbl.getStyleClass().add("stat-icon");
                rankLbl.setStyle("-fx-font-size: 14px; -fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32;");
                Label cname = new Label(cropIcon(entry.getKey()) + " " + entry.getKey());
                cname.getStyleClass().add("list-primary");
                Region csp = new Region();
                HBox.setHgrow(csp, Priority.ALWAYS);
                Label cyield = new Label(String.format("%,.0f kg", entry.getValue()));
                cyield.getStyleClass().add("mini-stat-value");
                cyield.setStyle("-fx-font-size: 13px;");
                Label cpct = pill(String.format("%.0f%%", pct), true);
                HBox crow = new HBox(12, rankLbl, cname, csp, cyield, cpct);
                crow.setAlignment(Pos.CENTER_LEFT);
                cropTable.getChildren().add(crow);
                rank++;
            }
        }

        GridPane finRow = row(50, 50);
        VBox revenueCard = styledCard();
        revenueCard.setSpacing(10);
        Label revTitle = new Label("\uD83D\uDCB5 Recent Revenue");
        revTitle.getStyleClass().add("card-title");
        revenueCard.getChildren().add(revTitle);

        List<Transaction> sales = allTx.stream().filter(t -> "SALE".equals(t.getType())).limit(5).collect(java.util.stream.Collectors.toList());
        if (sales.isEmpty()) {
            Label empty = new Label("No sales recorded yet");
            empty.getStyleClass().add("card-sub");
            revenueCard.getChildren().add(empty);
        } else {
            for (Transaction t : sales) {
                Label rname = new Label(t.getDescription() != null ? t.getDescription() : "Sale");
                rname.getStyleClass().add("list-primary");
                Region rsp = new Region();
                HBox.setHgrow(rsp, Priority.ALWAYS);
                Label ramt = new Label(String.format("\u20AA %,.0f", t.getAmount()));
                ramt.getStyleClass().add("pct-up");
                ramt.setStyle("-fx-font-size: 13px;");
                HBox rrow = new HBox(8, rname, rsp, ramt);
                rrow.setAlignment(Pos.CENTER_LEFT);
                revenueCard.getChildren().add(rrow);
            }
        }

        VBox expenseCard = styledCard();
        expenseCard.setSpacing(10);
        Label expTitle = new Label("\uD83D\uDED2 Recent Expenses");
        expTitle.getStyleClass().add("card-title");
        expenseCard.getChildren().add(expTitle);

        List<Transaction> expenses = allTx.stream()
                .filter(t -> "PURCHASE".equals(t.getType()) || "PAYMENT".equals(t.getType()))
                .limit(5).collect(java.util.stream.Collectors.toList());
        if (expenses.isEmpty()) {
            Label empty = new Label("No expenses recorded yet");
            empty.getStyleClass().add("card-sub");
            expenseCard.getChildren().add(empty);
        } else {
            for (Transaction t : expenses) {
                Label xname = new Label(t.getDescription() != null ? t.getDescription() : t.getType());
                xname.getStyleClass().add("list-primary");
                Region xsp = new Region();
                HBox.setHgrow(xsp, Priority.ALWAYS);
                Label xamt = new Label(String.format("\u20AA %,.0f", t.getAmount()));
                xamt.getStyleClass().add("pct-down");
                xamt.setStyle("-fx-font-size: 13px;");
                HBox xrow = new HBox(8, xname, xsp, xamt);
                xrow.setAlignment(Pos.CENTER_LEFT);
                expenseCard.getChildren().add(xrow);
            }
        }

        addCells(finRow, revenueCard, expenseCard);

        root.getChildren().addAll(subtitle, topRow, midRow, cropTable, finRow);
        return root;
    }

    private VBox buildReportRingCard(String title, double percent, String ringClass, String centerText, String subText) {
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        StackPane ring = buildRing(110, 14, percent, ringClass, centerText, subText);
        VBox card = styledCard(t, ring);
        card.setSpacing(14);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(200);
        return card;
    }

    @FXML private void showSettings() {
        setActive(btnSettings, "Settings");
        setContent(buildSettings());
    }

    private Node buildSettings() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        VBox profileCard = styledCard();
        profileCard.setSpacing(14);
        Label profileTitle = new Label("\uD83D\uDC64 Profile Information");
        profileTitle.getStyleClass().add("card-title");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(14);
        profileGrid.setVgap(12);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            profileGrid.getColumnConstraints().add(cc);
        }

        com.smartfarm.model.User adminUser = com.smartfarm.service.AuthService.getUserById(SessionManager.getUserId());
        String currentName = adminUser != null && adminUser.getName() != null ? adminUser.getName() : SessionManager.getUserName();
        String currentEmail = adminUser != null && adminUser.getEmail() != null ? adminUser.getEmail() : "";
        String currentPhone = adminUser != null && adminUser.getPhone() != null ? adminUser.getPhone() : "";

        TextField nameField = settingsField(currentName);
        TextField emailFieldS = settingsField(currentEmail);
        TextField phoneField = settingsField(currentPhone);
        TextField roleField = settingsField("\uD83D\uDD12 Admin");
        roleField.setEditable(false);
        roleField.setDisable(true);

        Label settingsMsg = new Label("");
        settingsMsg.getStyleClass().add("card-sub");

        profileGrid.add(settingsLabel("Full Name"), 0, 0);
        profileGrid.add(nameField, 0, 1);
        profileGrid.add(settingsLabel("Email"), 1, 0);
        profileGrid.add(emailFieldS, 1, 1);
        profileGrid.add(settingsLabel("Phone"), 0, 2);
        profileGrid.add(phoneField, 0, 3);
        profileGrid.add(settingsLabel("Role"), 1, 2);
        profileGrid.add(roleField, 1, 3);

        Button saveBtn = new Button("\uD83D\uDCBE  Save Changes");
        saveBtn.getStyleClass().add("action-btn");
        saveBtn.setOnAction(e -> {
            com.smartfarm.service.AuthService.AuthResult result = com.smartfarm.service.AuthService.updateProfile(
                    SessionManager.getUserId(), nameField.getText(), emailFieldS.getText(), phoneField.getText());
            settingsMsg.setText(result.message);
            if (result.success) {
                SessionManager.login(SessionManager.getUserId(), nameField.getText().trim(), true);
                if (userName != null) {
                    userName.setText(nameField.getText().trim());
                }
                saveBtn.setText("\u2714  Saved!");
                saveBtn.setStyle("-fx-background-color: #1B5E20;");
                PauseTransition p = new PauseTransition(Duration.millis(1500));
                p.setOnFinished(ev -> {
                    saveBtn.setText("\uD83D\uDCBE  Save Changes");
                    saveBtn.setStyle("");
                });
                p.play();
            }
        });

        profileCard.getChildren().addAll(profileTitle, profileGrid, settingsMsg, saveBtn);

        VBox themeCard = styledCard();
        themeCard.setSpacing(14);
        Label themeTitle = new Label("\uD83C\uDFA8 Appearance");
        themeTitle.getStyleClass().add("card-title");
        Label themeDesc = new Label("Toggle between light and dark theme");
        themeDesc.getStyleClass().add("card-sub");
        Button themeBtn = new Button(isDarkMode ? "\u2600\uFE0F  Switch to Light Mode" : "\uD83C\uDF19  Switch to Dark Mode");
        themeBtn.getStyleClass().add("quick-btn");
        themeBtn.setMaxWidth(300);
        themeBtn.setOnAction(e -> handleThemeToggle());
        themeCard.getChildren().addAll(themeTitle, themeDesc, themeBtn);

        VBox aboutCard = styledCard();
        aboutCard.setSpacing(10);
        Label aboutTitle = new Label("\u2139\uFE0F About");
        aboutTitle.getStyleClass().add("card-title");
        Label ver = new Label("Smart Farm Management System v1.0");
        ver.getStyleClass().add("list-primary");
        Label dev = new Label("Developed by Mohammad Fares & Yamen Aburob");
        dev.getStyleClass().add("card-sub");
        Label tech = new Label("JavaFX 21 \u2022 PostgreSQL \u2022 Maven");
        tech.getStyleClass().add("card-sub");
        Label uni = new Label("University Project \u2022 Computer Engineering");
        uni.getStyleClass().add("card-sub");
        aboutCard.getChildren().addAll(aboutTitle, ver, dev, tech, uni);

        root.getChildren().addAll(profileCard, themeCard, aboutCard);
        return root;
    }

    private TextField settingsField(String text) {
        TextField f = new TextField(text);
        f.getStyleClass().add("search-field");
        f.setMaxWidth(Double.MAX_VALUE);
        return f;
    }

    private Label settingsLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("mini-stat-label");
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        return l;
    }

    @FXML private void showHistory() {
        setActive(btnHistory, "History");
        setContent(buildHistory());
    }

    private String historyFilter = "ALL";

    private static class TimelineEntry {
        String type, icon, title, desc, value, direction;
        java.time.LocalDate date;
        TimelineEntry(String type, String icon, String title, String desc, java.time.LocalDate date, String value, String direction) {
            this.type = type; this.icon = icon; this.title = title; this.desc = desc;
            this.date = date; this.value = value; this.direction = direction;
        }
    }

    private Node buildHistory() {
        VBox root = new VBox(16);
        root.getStyleClass().add("dash-root");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        String[] filters = {"All", "Harvest", "Transaction", "Field Activity"};
        String[] filterKeys = {"ALL", "HARVEST", "TRANSACTION", "FIELD"};
        String[] filterIcons = {"\uD83D\uDCCB", "\uD83C\uDF3E", "\uD83D\uDCB0", "\uD83C\uDF31"};

        for (int i = 0; i < filters.length; i++) {
            Button fb = new Button(filterIcons[i] + "  " + filters[i]);
            String key = filterKeys[i];
            fb.getStyleClass().add("filter-btn");
            if (historyFilter.equals(key)) {
                fb.getStyleClass().add("filter-active");
            }
            fb.setOnAction(e -> {
                historyFilter = key;
                showHistory();
            });
            filterBar.getChildren().add(fb);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label();
        countLabel.getStyleClass().add("card-sub");

        filterBar.getChildren().addAll(spacer, countLabel);

        VBox timeline = new VBox(0);
        timeline.getStyleClass().add("timeline-container");

        List<TimelineEntry> entries = new java.util.ArrayList<>();

        for (Harvest h : WorkerService.getAllHarvests()) {
            String desc = String.format("%.0f %s collected from %s by %s", h.getQuantityGood(), h.getUnit(), h.getFieldName(), h.getWorkerName());
            String value = String.format("+%.0f %s", h.getQuantityGood(), h.getUnit());
            entries.add(new TimelineEntry("HARVEST", cropIcon(h.getCropName()), "Harvested " + h.getCropName(), desc, h.getHarvestDate(), value, "up"));
        }

        for (Transaction t : TransactionService.getAllTransactions()) {
            String title = "SALE".equals(t.getType()) ? "Sale Completed" : "PURCHASE".equals(t.getType()) ? "Purchase Made" : "Payment Sent";
            String sign = "SALE".equals(t.getType()) ? "+" : "-";
            String value = String.format("%s\u20AA %,.0f", sign, t.getAmount());
            String dir = "SALE".equals(t.getType()) ? "up" : "down";
            entries.add(new TimelineEntry("TRANSACTION", txIcon(t.getType()), title,
                    t.getDescription() != null ? t.getDescription() : "", t.getTransactionDate(), value, dir));
        }

        for (com.smartfarm.model.FarmLog log : FarmService.getAllLogs()) {
            String title;
            switch (log.getLogType()) {
                case "IRRIGATION": title = "Irrigation Performed"; break;
                case "PLOWING": title = "Field Plowed"; break;
                case "PLANTING": title = "Planting Completed"; break;
                case "FERTILIZING": title = "Fertilizer Applied"; break;
                default: title = "Note Added"; break;
            }
            String desc = log.getFieldName() + " \u2022 " + log.getWorkerName()
                    + (log.getDescription() != null && !log.getDescription().isEmpty() ? " \u2022 " + log.getDescription() : "");
            String value = log.getQuantity() != null ? String.format("%.0f", log.getQuantity()) : "";
            entries.add(new TimelineEntry("FIELD", "\uD83C\uDF31", title, desc, log.getLogDate(), value, "up"));
        }

        entries.sort((a, b) -> {
            if (a.date == null) return 1;
            if (b.date == null) return -1;
            return b.date.compareTo(a.date);
        });

        int count = 0;
        List<TimelineEntry> filtered = new java.util.ArrayList<>();
        for (TimelineEntry e : entries) {
            if (!historyFilter.equals("ALL") && !e.type.equals(historyFilter)) continue;
            filtered.add(e);
        }

        for (int i = 0; i < filtered.size(); i++) {
            TimelineEntry e = filtered.get(i);
            count++;
            boolean isLast = (i == filtered.size() - 1);
            timeline.getChildren().add(buildTimelineItem(e.type, e.icon, e.title, e.desc,
                    formatRelativeDate(e.date), e.value, e.direction, isLast));
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("No activity recorded yet");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            timeline.getChildren().add(empty);
        }

        countLabel.setText(count + " activities");

        ScrollPane scroll = new ScrollPane(timeline);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().addAll("content-scroll");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(filterBar, scroll);
        return root;
    }

    private HBox buildTimelineItem(String type, String icon, String title, String desc,
                                   String time, String value, String direction, boolean isLast) {
        VBox dotLine = new VBox();
        dotLine.setAlignment(Pos.TOP_CENTER);
        dotLine.setMinWidth(40);
        dotLine.setMaxWidth(40);

        Region dot = new Region();
        dot.getStyleClass().addAll("timeline-dot", "timeline-dot-" + type.toLowerCase());
        dot.setMinSize(14, 14);
        dot.setMaxSize(14, 14);

        if (!isLast) {
            Region line = new Region();
            line.getStyleClass().add("timeline-line");
            line.setMinWidth(2);
            line.setMaxWidth(2);
            VBox.setVgrow(line, Priority.ALWAYS);
            dotLine.getChildren().addAll(dot, line);
        } else {
            dotLine.getChildren().add(dot);
        }

        Label ic = new Label(icon);
        ic.getStyleClass().addAll("row-icon", "row-icon-" + type.toLowerCase());

        Label t = new Label(title);
        t.getStyleClass().add("list-primary");

        Label d = new Label(desc);
        d.getStyleClass().add("list-sub");
        d.setWrapText(true);

        Label tm = new Label("\uD83D\uDD52 " + time);
        tm.getStyleClass().add("timeline-time");

        VBox textBox = new VBox(3, t, d, tm);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox card = new HBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("timeline-card");
        card.getChildren().addAll(ic, textBox);

        if (value != null && !value.isEmpty()) {
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label val = new Label(value);
            val.getStyleClass().add("up".equals(direction) ? "pct-up" : "pct-down");
            val.setStyle("-fx-font-size: 14px;");
            card.getChildren().addAll(sp, val);
        }

        HBox row = new HBox(0, dotLine, card);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        row.setPadding(new Insets(0, 0, 0, 0));
        return row;
    }

    @FXML
    private void handleQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Quit");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("All unsaved changes will be lost.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            com.smartfarm.service.AuthService.logout();
            stage = (Stage) btnQuit.getScene().getWindow();
            SceneSwitcher.switchTo(stage, "/fxml/login.fxml");
        }
    }

    @FXML
    private void handleThemeToggle() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), themeToggleBtn);
        rotate.setByAngle(360);
        rotate.play();

        isDarkMode = !isDarkMode;
        SceneSwitcher.setDarkMode(isDarkMode);

        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().add("dark-mode");
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().remove("dark-mode");
        }
    }
}
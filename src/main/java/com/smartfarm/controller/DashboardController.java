package com.smartfarm.controller;

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
        GridPane g = row(25, 25, 25, 25);
        VBox c1 = buildKpiCard("\uD83C\uDF3F", "6", "Total Farms",
                sparkBox(new double[]{3, 4, 3.5, 5, 4.5, 6, 5.5, 7}));
        VBox c2 = buildKpiCard("\uD83D\uDC77", "18", "Active Workers", null);
        VBox c3 = buildKpiCard("\uD83C\uDF3E", "12,450", "Total Harvest (kg)", null);
        VBox c4 = buildKpiCard("\uD83D\uDCB0", "\u20AA 24,800", "Revenue", pill("+8.2%", true));
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
        Label title = new Label("Balance");
        title.getStyleClass().add("card-title");
        Label badge = new Label("On track");
        badge.getStyleClass().add("pill-up");
        HBox header = new HBox(8, title, badge);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane ring = buildDualRing(120, 14, 68, 32);

        VBox legend = new VBox(14,
                legendRow("dot-sale", "Sales", "\u20AA 42,300"),
                legendRow("dot-purchase", "Purchases", "\u20AA 19,900"));
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
        Label title = new Label("Earnings");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Total revenue");
        sub.getStyleClass().add("card-sub");
        Label big = new Label("\u20AA 6,078");
        big.getStyleClass().add("big-value");
        Label pct = new Label("\u25B2 34% vs last month");
        pct.getStyleClass().add("pct-up");

        VBox left = new VBox(4, title, sub, big, pct);
        left.setAlignment(Pos.CENTER_LEFT);

        StackPane ring = buildRing(92, 12, 78, "ring-progress", "78%", "Margin");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox body = new HBox(left, sp, ring);
        body.setAlignment(Pos.CENTER_LEFT);

        VBox card = styledCard(body);
        card.setPrefHeight(200);
        return card;
    }

    private VBox buildProfileCard() {
        Label avatar = new Label("MF");
        avatar.getStyleClass().add("avatar");
        Label name = new Label("Mohammad Fares");
        name.getStyleClass().add("card-title");
        Label email = new Label("manager@smartfarm.ps");
        email.getStyleClass().add("card-sub");

        VBox nameBox = new VBox(2, name, email);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        HBox head = new HBox(12, avatar, nameBox);
        head.setAlignment(Pos.CENTER_LEFT);

        HBox stats = new HBox(miniStat("6", "Farms"), miniStat("18", "Workers"), miniStat("34", "Harvests"));
        stats.setAlignment(Pos.CENTER);

        VBox card = styledCard(head, divider(), stats);
        card.setSpacing(16);
        card.setPrefHeight(200);
        return card;
    }

    private VBox buildRecentHarvestsCard() {
        Label title = new Label("Recent Harvests");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(12,
                harvestRow("\uD83C\uDF45", "Tomatoes", "Today", "850 kg"),
                harvestRow("\uD83C\uDF3E", "Wheat", "2 days ago", "3,200 kg"),
                harvestRow("\uD83E\uDED2", "Olives", "5 days ago", "1,150 kg"));

        VBox card = styledCard(title, list);
        card.setSpacing(14);
        card.setPrefHeight(210);
        return card;
    }

    private VBox buildRecentTransactionsCard() {
        Label title = new Label("Recent Transactions");
        title.getStyleClass().add("card-title");

        VBox list = new VBox(12,
                txRow("SALE", "\u20AA 1,200", "Today", "+5%", true),
                txRow("PURCHASE", "\u20AA 320", "Yesterday", "-2%", false),
                txRow("PAYMENT", "\u20AA 450", "2 days ago", "-1%", false));

        VBox card = styledCard(title, list);
        card.setSpacing(14);
        card.setPrefHeight(210);
        return card;
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

        Button b1 = quickBtn("\u2795  Add Farm", this::showFields);
        Button b2 = quickBtn("\uD83D\uDC77  Add Worker", this::showWorkers);
        Button b3 = quickBtn("\uD83C\uDF3E  New Harvest", this::showHarvests);
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
        Label ch = pill(change, up);
        HBox h = new HBox(10, badge, txt, sp, ch);
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

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83C\uDF3F", "6", "Total Fields"),
                buildFieldStatCard("\uD83D\uDCCF", "32", "Total Dunums"),
                buildFieldStatCard("\uD83C\uDF31", "9", "Active Crops"));

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

        toolbar.getChildren().addAll(searchField, sp, addBtn);

        String[][] fields = {
                {"Field A-1", "Olive Farm", "8", "Good", "\uD83D\uDFE2", "3 days ago",
                        "Olives\u200E\u200E,\u200E\u200EWheat", "\uD83E\uDED2 Olives (450 trees)|\uD83C\uDF3E Wheat (3 dunums)"},
                {"Field A-2", "Vegetable Garden", "5", "Excellent", "\uD83D\uDFE2", "Today",
                        "Tomatoes\u200E\u200E,\u200E\u200ECucumbers", "\uD83C\uDF45 Tomatoes (2 dunums)|\uD83E\uDD52 Cucumbers (2 dunums)"},
                {"Field B-1", "Grain Zone", "10", "Fair", "\uD83D\uDFE1", "1 week ago",
                        "Wheat\u200E\u200E,\u200E\u200EBarley", "\uD83C\uDF3E Wheat (6 dunums)|\uD83C\uDF3E Barley (4 dunums)"},
                {"Field C-1", "Fruit Orchard", "4", "Good", "\uD83D\uDFE2", "2 days ago",
                        "Citrus\u200E\u200E,\u200E\u200EFigs", "\uD83C\uDF4A Citrus (150 trees)|\uD83C\uDF5E Figs (80 trees)"},
                {"Field C-3", "New Plot", "5", "Not Tested", "\u26AA", "Never",
                        "None", ""},
        };

        VBox fieldsList = new VBox(14);
        populateFields(fieldsList, fields, "");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            populateFields(fieldsList, fields, newVal.trim().toLowerCase());
        });

        root.getChildren().addAll(stats, toolbar, fieldsList);
        return root;
    }

    private void populateFields(VBox container, String[][] fields, String query) {
        container.getChildren().clear();
        for (String[] f : fields) {
            if (!query.isEmpty()) {
                String combined = (f[0] + " " + f[1] + " " + f[6]).toLowerCase();
                if (!combined.contains(query)) continue;
            }
            container.getChildren().add(buildFieldCard(f[0], f[1], f[2], f[3], f[4], f[5], f[7]));
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

    private VBox buildFieldCard(String name, String desc, String dunums, String soil,
                                String soilDot, String lastIrrigation, String cropsData) {
        Label nameLabel = new Label("\uD83C\uDF3F " + name);
        nameLabel.getStyleClass().add("field-name");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("card-sub");

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Label statusBadge = new Label(soilDot + " " + soil);
        statusBadge.getStyleClass().add("soil-badge");

        HBox header = new HBox(8, nameLabel, descLabel, sp1, statusBadge);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox infoRow = new HBox(24,
                fieldInfo("\uD83D\uDCCF", "Size", dunums + " dunums"),
                fieldInfo("\uD83C\uDF0D", "Soil", soil),
                fieldInfo("\uD83D\uDCA7", "Last Irrigation", lastIrrigation));
        infoRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = styledCard(header, divider(), infoRow);
        card.setSpacing(12);

        if (cropsData != null && !cropsData.isEmpty()) {
            Label cropsTitle = new Label("\uD83C\uDF31 Planted Crops");
            cropsTitle.getStyleClass().add("crops-title");

            HBox cropsRow = new HBox(10);
            cropsRow.setAlignment(Pos.CENTER_LEFT);

            String[] crops = cropsData.split("\\|");
            for (String crop : crops) {
                Label chip = new Label(crop.trim());
                chip.getStyleClass().add("crop-chip");
                cropsRow.getChildren().add(chip);
            }

            card.getChildren().addAll(divider(), cropsTitle, cropsRow);
        }

        return card;
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

    private Node buildHarvests() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83C\uDF3E", "34", "Total Harvests"),
                buildFieldStatCard("\u2696\uFE0F", "18,420", "Total kg"),
                buildFieldStatCard("\uD83C\uDFC6", "Tomatoes", "Best Crop"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search harvests...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);
        toolbar.getChildren().add(searchField);

        String[][] data = {
                {"\uD83C\uDF45", "Tomatoes", "Field A-2", "850", "0", "Today"},
                {"\uD83C\uDF3E", "Wheat", "Field B-1", "3,200", "120", "2 days ago"},
                {"\uD83E\uDED2", "Olives", "Field A-1", "1,150", "45", "5 days ago"},
                {"\uD83E\uDD52", "Cucumbers", "Field A-2", "620", "30", "1 week ago"},
                {"\uD83C\uDF4A", "Citrus", "Field C-1", "2,800", "90", "1 week ago"},
                {"\uD83C\uDF3E", "Barley", "Field B-1", "4,100", "200", "2 weeks ago"},
                {"\uD83C\uDF45", "Tomatoes", "Field A-2", "780", "25", "2 weeks ago"},
                {"\uD83E\uDED2", "Olives", "Field A-1", "2,900", "60", "3 weeks ago"},
        };

        VBox list = new VBox(12);
        populateHarvests(list, data, "");

        searchField.textProperty().addListener((obs, o, n) -> populateHarvests(list, data, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, toolbar, list);
        return root;
    }

    private void populateHarvests(VBox container, String[][] data, String query) {
        container.getChildren().clear();
        for (String[] d : data) {
            if (!query.isEmpty() && !(d[1] + " " + d[2]).toLowerCase().contains(query)) continue;
            Label ic = new Label(d[0]);
            ic.getStyleClass().add("row-icon");
            Label crop = new Label(d[1]);
            crop.getStyleClass().add("list-primary");
            Label field = new Label(d[2] + " \u2022 " + d[5]);
            field.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, crop, field);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label good = new Label("\u2705 " + d[3] + " kg");
            good.getStyleClass().add("pct-up");
            good.setStyle("-fx-font-size: 13px;");
            Label damaged = new Label("\u274C " + d[4] + " kg");
            damaged.getStyleClass().add("pct-down");
            damaged.setStyle("-fx-font-size: 12px;");
            VBox nums = new VBox(2, good, damaged);
            nums.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(12, ic, txt, sp, nums);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");
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

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83D\uDC77", "18", "Total Workers"),
                buildFieldStatCard("\u2705", "15", "Active"),
                buildFieldStatCard("\uD83C\uDF3F", "5", "Fields Covered"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search workers...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);
        toolbar.getChildren().add(searchField);

        String[][] data = {
                {"AH", "Ahmad Hassan", "Irrigator", "Field A-1", "\u20AA 15/liter", "Active"},
                {"MK", "Mohammad Khaled", "Harvester", "Field A-2", "\u20AA 8/kg", "Active"},
                {"SK", "Sami Khalil", "Plower", "Field B-1", "\u20AA 50/dunum", "Active"},
                {"OA", "Omar Ali", "Irrigator", "Field C-1", "\u20AA 15/liter", "Active"},
                {"YS", "Yousef Saleh", "Harvester", "Field A-1", "\u20AA 10/kg", "Active"},
                {"KN", "Khaled Nasser", "Harvester", "Field B-1", "\u20AA 8/kg", "Inactive"},
                {"RA", "Rami Ahmad", "Plower", "Field C-1", "\u20AA 55/dunum", "Inactive"},
                {"TM", "Tariq Mahmoud", "Irrigator", "Field A-2", "\u20AA 12/liter", "Active"},
        };

        VBox list = new VBox(12);
        populateWorkers(list, data, "");

        searchField.textProperty().addListener((obs, o, n) -> populateWorkers(list, data, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, toolbar, list);
        return root;
    }

    private void populateWorkers(VBox container, String[][] data, String query) {
        container.getChildren().clear();
        for (String[] d : data) {
            if (!query.isEmpty() && !(d[1] + " " + d[2] + " " + d[3]).toLowerCase().contains(query)) continue;
            Label avatar = new Label(d[0]);
            avatar.getStyleClass().add("avatar");
            avatar.setStyle("-fx-font-size: 14px; -fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;");
            Label name = new Label(d[1]);
            name.getStyleClass().add("list-primary");
            Label role = new Label(d[2] + " \u2022 " + d[3]);
            role.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, name, role);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label wage = new Label("\uD83D\uDCB0 " + d[4]);
            wage.getStyleClass().add("mini-stat-value");
            wage.setStyle("-fx-font-size: 13px;");
            Label wageLabel = new Label("Wage per unit");
            wageLabel.getStyleClass().add("list-sub");
            Label status = new Label(d[5]);
            status.getStyleClass().add("Active".equals(d[5]) ? "pill-up" : "pill-down");
            VBox right = new VBox(3, wage, wageLabel, status);
            right.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(12, avatar, txt, sp, right);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("timeline-card");
            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label empty = new Label("No workers found");
            empty.getStyleClass().add("card-sub");
            empty.setStyle("-fx-padding: 30 0 30 0; -fx-font-size: 14px;");
            container.getChildren().add(empty);
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

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83D\uDCB0", "\u20AA 42,300", "Total Revenue"),
                buildFieldStatCard("\uD83D\uDED2", "\u20AA 19,900", "Total Expenses"),
                buildFieldStatCard("\uD83D\uDCCA", "\u20AA 22,400", "Net Profit"));

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

        String[][] data = {
                {"SALE", "\uD83D\uDCB5", "Sold Tomatoes", "500 kg to Central Market", "+\u20AA 1,200", "Today", "+5%", "up"},
                {"PURCHASE", "\uD83D\uDED2", "Bought Fertilizer", "NPK Mix for Field A-2", "-\u20AA 320", "Yesterday", "-2%", "down"},
                {"PAYMENT", "\uD83D\uDCB3", "Worker Salary", "Ahmad Hassan - Irrigation", "-\u20AA 450", "2 days ago", "", ""},
                {"SALE", "\uD83D\uDCB5", "Sold Wheat", "1,000 kg to distributor", "+\u20AA 2,400", "3 days ago", "+12%", "up"},
                {"PURCHASE", "\uD83D\uDED2", "Bought Seeds", "Wheat seeds for Field B-1", "-\u20AA 180", "4 days ago", "-1%", "down"},
                {"PAYMENT", "\uD83D\uDCB3", "Worker Salary", "Mohammad Khaled - Harvest", "-\u20AA 640", "5 days ago", "", ""},
                {"SALE", "\uD83D\uDCB5", "Sold Olives", "800 kg to olive press", "+\u20AA 3,200", "1 week ago", "+8%", "up"},
                {"PURCHASE", "\uD83D\uDED2", "Equipment Repair", "Irrigation pump maintenance", "-\u20AA 550", "1 week ago", "-3%", "down"},
                {"SALE", "\uD83D\uDCB5", "Sold Citrus", "1,200 kg to market", "+\u20AA 4,800", "2 weeks ago", "+15%", "up"},
                {"PAYMENT", "\uD83D\uDCB3", "Worker Salary", "Sami Khalil - Plowing", "-\u20AA 500", "2 weeks ago", "", ""},
        };

        VBox list = new VBox(12);
        populateTransactions(list, data, txFilter, "");

        searchField.textProperty().addListener((obs, o, n) ->
                populateTransactions(list, data, txFilter, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, filterBar, list);
        return root;
    }

    private void populateTransactions(VBox container, String[][] data, String filter, String query) {
        container.getChildren().clear();
        for (String[] d : data) {
            if (!"ALL".equals(filter) && !d[0].equals(filter)) continue;
            if (!query.isEmpty() && !(d[2] + " " + d[3]).toLowerCase().contains(query)) continue;
            Label badge = new Label(d[0]);
            badge.getStyleClass().addAll("badge", badgeClass(d[0]));
            Label title = new Label(d[2]);
            title.getStyleClass().add("list-primary");
            Label desc = new Label(d[3] + " \u2022 " + d[5]);
            desc.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, title, desc);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label amount = new Label(d[4]);
            amount.getStyleClass().add(d[4].startsWith("+") ? "pct-up" : "pct-down");
            amount.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
            VBox right = new VBox(4, amount);
            right.setAlignment(Pos.CENTER_RIGHT);
            if (d[6] != null && !d[6].isEmpty()) {
                Label pct = pill(d[6], "up".equals(d[7]));
                right.getChildren().add(pct);
            }
            HBox row = new HBox(12, badge, txt, sp, right);
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

    @FXML private void showFertilizers() {
        setActive(btnFertilizers, "Fertilizers");
        setContent(buildFertilizers());
    }

    private Node buildFertilizers() {
        VBox root = new VBox(18);
        root.getStyleClass().add("dash-root");

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                buildFieldStatCard("\uD83E\uDDEA", "12", "Total Items"),
                buildFieldStatCard("\u2705", "9", "In Stock"),
                buildFieldStatCard("\u26A0\uFE0F", "3", "Low Stock"));

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("\uD83D\uDD0D Search fertilizers...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);
        toolbar.getChildren().add(searchField);

        String[][] data = {
                {"\uD83E\uDDEA", "NPK Fertilizer", "Fertilizer", "150 kg", "Field A-2", "Good", "3 days ago"},
                {"\uD83E\uDDEA", "Urea", "Fertilizer", "80 kg", "Field B-1", "Good", "1 week ago"},
                {"\uD83D\uDC8A", "Fungicide", "Medicine", "12 liters", "Field A-1", "Low", "2 weeks ago"},
                {"\uD83E\uDDEA", "Potassium Sulfate", "Fertilizer", "200 kg", "Field C-1", "Good", "5 days ago"},
                {"\uD83D\uDC8A", "Insecticide", "Medicine", "5 liters", "Field A-2", "Low", "3 weeks ago"},
                {"\uD83E\uDDEA", "Compost", "Fertilizer", "500 kg", "All Fields", "Good", "1 week ago"},
                {"\uD83D\uDC8A", "Herbicide", "Medicine", "8 liters", "Field B-1", "Low", "1 month ago"},
                {"\uD83E\uDDEA", "Phosphate", "Fertilizer", "120 kg", "Field C-1", "Good", "2 days ago"},
        };

        VBox list = new VBox(12);
        populateFertilizers(list, data, "");

        searchField.textProperty().addListener((obs, o, n) -> populateFertilizers(list, data, n.trim().toLowerCase()));

        root.getChildren().addAll(stats, toolbar, list);
        return root;
    }

    private void populateFertilizers(VBox container, String[][] data, String query) {
        container.getChildren().clear();
        for (String[] d : data) {
            if (!query.isEmpty() && !(d[1] + " " + d[2] + " " + d[4]).toLowerCase().contains(query)) continue;
            Label ic = new Label(d[0]);
            ic.getStyleClass().add("row-icon");
            Label name = new Label(d[1]);
            name.getStyleClass().add("list-primary");
            Label info = new Label(d[2] + " \u2022 " + d[4] + " \u2022 " + d[6]);
            info.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, name, info);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label qty = new Label(d[3]);
            qty.getStyleClass().add("mini-stat-value");
            qty.setStyle("-fx-font-size: 13px;");
            Label stock = new Label(d[5]);
            stock.getStyleClass().add("Good".equals(d[5]) ? "pill-up" : "pill-down");
            VBox right = new VBox(4, qty, stock);
            right.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(12, ic, txt, sp, right);
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

        GridPane topRow = row(25, 25, 25, 25);
        addCells(topRow,
                buildFieldStatCard("\uD83C\uDF3F", "6", "Fields"),
                buildFieldStatCard("\uD83D\uDC77", "18", "Workers"),
                buildFieldStatCard("\uD83C\uDF3E", "34", "Harvests"),
                buildFieldStatCard("\uD83D\uDCB0", "23", "Transactions"));

        GridPane midRow = row(50, 50);
        addCells(midRow, buildReportRingCard("Harvest Quality", 92, "ring-progress", "92%", "Quality"),
                buildReportRingCard("Budget Usage", 67, "ring-purchases", "67%", "Used"));

        VBox cropTable = styledCard();
        cropTable.setSpacing(12);
        Label tableTitle = new Label("\uD83C\uDFC6 Top Crops by Yield");
        tableTitle.getStyleClass().add("card-title");
        cropTable.getChildren().add(tableTitle);

        String[][] crops = {
                {"1", "\uD83C\uDF3E Barley", "4,100 kg", "22%"},
                {"2", "\uD83C\uDF3E Wheat", "3,200 kg", "17%"},
                {"3", "\uD83E\uDED2 Olives", "4,050 kg", "22%"},
                {"4", "\uD83C\uDF4A Citrus", "2,800 kg", "15%"},
                {"5", "\uD83C\uDF45 Tomatoes", "1,630 kg", "9%"},
        };

        for (String[] c : crops) {
            Label rank = new Label(c[0]);
            rank.getStyleClass().add("stat-icon");
            rank.setStyle("-fx-font-size: 14px; -fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32;");
            Label cname = new Label(c[1]);
            cname.getStyleClass().add("list-primary");
            Region csp = new Region();
            HBox.setHgrow(csp, Priority.ALWAYS);
            Label cyield = new Label(c[2]);
            cyield.getStyleClass().add("mini-stat-value");
            cyield.setStyle("-fx-font-size: 13px;");
            Label cpct = pill(c[3], true);
            HBox crow = new HBox(12, rank, cname, csp, cyield, cpct);
            crow.setAlignment(Pos.CENTER_LEFT);
            cropTable.getChildren().add(crow);
        }

        GridPane finRow = row(50, 50);
        VBox revenueCard = styledCard();
        revenueCard.setSpacing(10);
        Label revTitle = new Label("\uD83D\uDCB5 Revenue Breakdown");
        revTitle.getStyleClass().add("card-title");
        revenueCard.getChildren().add(revTitle);

        String[][] rev = {
                {"Tomatoes", "\u20AA 1,200", "up"},
                {"Wheat", "\u20AA 2,400", "up"},
                {"Olives", "\u20AA 3,200", "up"},
                {"Citrus", "\u20AA 4,800", "up"},
        };
        for (String[] r : rev) {
            Label rname = new Label(r[0]);
            rname.getStyleClass().add("list-primary");
            Region rsp = new Region();
            HBox.setHgrow(rsp, Priority.ALWAYS);
            Label ramt = new Label(r[1]);
            ramt.getStyleClass().add("pct-up");
            ramt.setStyle("-fx-font-size: 13px;");
            HBox rrow = new HBox(8, rname, rsp, ramt);
            rrow.setAlignment(Pos.CENTER_LEFT);
            revenueCard.getChildren().add(rrow);
        }

        VBox expenseCard = styledCard();
        expenseCard.setSpacing(10);
        Label expTitle = new Label("\uD83D\uDED2 Expense Breakdown");
        expTitle.getStyleClass().add("card-title");
        expenseCard.getChildren().add(expTitle);

        String[][] exp = {
                {"Fertilizers", "\u20AA 520", "down"},
                {"Seeds", "\u20AA 180", "down"},
                {"Equipment", "\u20AA 550", "down"},
                {"Salaries", "\u20AA 1,590", "down"},
        };
        for (String[] x : exp) {
            Label xname = new Label(x[0]);
            xname.getStyleClass().add("list-primary");
            Region xsp = new Region();
            HBox.setHgrow(xsp, Priority.ALWAYS);
            Label xamt = new Label(x[1]);
            xamt.getStyleClass().add("pct-down");
            xamt.setStyle("-fx-font-size: 13px;");
            HBox xrow = new HBox(8, xname, xsp, xamt);
            xrow.setAlignment(Pos.CENTER_LEFT);
            expenseCard.getChildren().add(xrow);
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

        TextField nameField = settingsField("Mohammad Fares");
        TextField emailFieldS = settingsField("manager@smartfarm.ps");
        TextField phoneField = settingsField("+970 599 123 456");
        TextField roleField = settingsField("Admin");
        roleField.setEditable(false);
        roleField.setStyle("-fx-opacity: 0.7;");

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
            saveBtn.setText("\u2714  Saved!");
            saveBtn.setStyle("-fx-background-color: #1B5E20;");
            PauseTransition p = new PauseTransition(Duration.millis(1500));
            p.setOnFinished(ev -> {
                saveBtn.setText("\uD83D\uDCBE  Save Changes");
                saveBtn.setStyle("");
            });
            p.play();
        });

        profileCard.getChildren().addAll(profileTitle, profileGrid, saveBtn);

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
        Label dev = new Label("Developed by Mohammad Fares & Partner");
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

    private Node buildHistory() {
        VBox root = new VBox(16);
        root.getStyleClass().add("dash-root");

        HBox filterBar = new HBox(8);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        String[] filters = {"All", "Harvest", "Transaction", "Worker", "Field"};
        String[] filterKeys = {"ALL", "HARVEST", "TRANSACTION", "WORKER", "FIELD"};
        String[] filterIcons = {"\uD83D\uDCCB", "\uD83C\uDF3E", "\uD83D\uDCB0", "\uD83D\uDC77", "\uD83C\uDF31"};

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

        String[][] activities = {
                {"HARVEST", "\uD83C\uDF45", "Harvested Tomatoes", "850 kg collected from Field A-2", "Today, 10:30 AM", "+850 kg", "up"},
                {"TRANSACTION", "\uD83D\uDCB0", "Sale Completed", "Sold 500 kg tomatoes to Market", "Today, 09:15 AM", "+\u20AA 1,200", "up"},
                {"WORKER", "\uD83D\uDC77", "Worker Assigned", "Ahmad joined Field B-1 as irrigator", "Yesterday, 04:00 PM", "", ""},
                {"FIELD", "\uD83C\uDF31", "New Field Added", "Field C-3 registered (5 dunums)", "Yesterday, 02:30 PM", "+5 dunums", "up"},
                {"HARVEST", "\uD83C\uDF3E", "Harvested Wheat", "3,200 kg collected from Field B-1", "2 days ago, 11:00 AM", "+3,200 kg", "up"},
                {"TRANSACTION", "\uD83D\uDCB0", "Purchase Made", "Bought fertilizer for Field A-2", "2 days ago, 09:00 AM", "-\u20AA 320", "down"},
                {"WORKER", "\uD83D\uDC77", "Worker Removed", "Khaled left Field A-1", "3 days ago, 05:00 PM", "", ""},
                {"TRANSACTION", "\uD83D\uDCB0", "Payment Sent", "Worker salary payment", "3 days ago, 03:00 PM", "-\u20AA 450", "down"},
                {"HARVEST", "\uD83E\uDED2", "Harvested Olives", "1,150 kg collected from Field D-1", "5 days ago, 08:00 AM", "+1,150 kg", "up"},
                {"FIELD", "\uD83C\uDF31", "Field Updated", "Field A-1 soil status changed to Good", "5 days ago, 07:30 AM", "", ""},
                {"TRANSACTION", "\uD83D\uDCB0", "Sale Completed", "Sold 1,000 kg wheat to distributor", "1 week ago, 10:00 AM", "+\u20AA 2,400", "up"},
                {"HARVEST", "\uD83C\uDF3E", "Harvested Cucumbers", "620 kg collected from Field C-1", "1 week ago, 09:00 AM", "+620 kg", "up"},
        };

        int count = 0;
        for (int i = 0; i < activities.length; i++) {
            String[] a = activities[i];
            if (!historyFilter.equals("ALL") && !a[0].equals(historyFilter)) continue;
            count++;
            boolean isLast = (i == activities.length - 1);
            timeline.getChildren().add(buildTimelineItem(a[0], a[1], a[2], a[3], a[4], a[5], a[6], isLast));
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
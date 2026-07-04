package com.smartfarm.controller;

import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
    @FXML private Button btnStorage;
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
        setContent(new Label("Fields & Crops content coming soon..."));
    }

    @FXML private void showHarvests() {
        setActive(btnHarvests, "Harvests");
        setContent(new Label("Harvests content coming soon..."));
    }

    @FXML private void showWorkers() {
        setActive(btnWorkers, "Workers");
        setContent(new Label("Workers content coming soon..."));
    }

    @FXML private void showTransactions() {
        setActive(btnTransactions, "Transactions");
        setContent(new Label("Transactions content coming soon..."));
    }

    @FXML private void showStorage() {
        setActive(btnStorage, "Storage");
        setContent(new Label("Storage content coming soon..."));
    }

    @FXML private void showFertilizers() {
        setActive(btnFertilizers, "Fertilizers");
        setContent(new Label("Fertilizers content coming soon..."));
    }

    @FXML private void showHistory() {
        setActive(btnHistory, "History");
        setContent(new Label("History content coming soon..."));
    }

    @FXML private void showReports() {
        setActive(btnReports, "Reports");
        setContent(new Label("Reports content coming soon..."));
    }

    @FXML private void showSettings() {
        setActive(btnSettings, "Settings");
        setContent(new Label("Settings content coming soon..."));
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
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(loader.load());

                stage = (Stage) btnQuit.getScene().getWindow();
                stage.setScene(scene);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleThemeToggle() {
        RotateTransition rotate = new RotateTransition(Duration.millis(300), themeToggleBtn);
        rotate.setByAngle(360);
        rotate.play();

        isDarkMode = !isDarkMode;

        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().add("dark-mode");
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            themeToggleBtn.getScene().getRoot().getStyleClass().remove("dark-mode");
        }
    }
}
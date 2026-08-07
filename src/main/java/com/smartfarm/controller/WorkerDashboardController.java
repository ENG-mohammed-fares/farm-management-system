package com.smartfarm.controller;

import java.time.LocalDate;
import java.time.YearMonth;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.smartfarm.util.SceneSwitcher;
import com.smartfarm.util.SessionManager;
import com.smartfarm.service.WorkerService;
import com.smartfarm.service.FarmService;
import com.smartfarm.service.TransactionService;
import com.smartfarm.model.FarmWorker;
import com.smartfarm.model.Harvest;
import com.smartfarm.model.Field;
import com.smartfarm.model.Crop;
import com.smartfarm.model.Transaction;
import java.util.List;

public class WorkerDashboardController {

    @FXML private javafx.scene.layout.AnchorPane rootPane;
    @FXML private Label pageTitle;
    @FXML private Label userName;
    @FXML private StackPane contentArea;
    @FXML private Button themeToggleBtn;
    @FXML private ImageView themeIcon;
    @FXML private VBox navButtons;
    @FXML private Region slideHighlight;

    @FXML private Button btnDashboard;
    @FXML private Button btnMyWork;
    @FXML private Button btnLogWork;
    @FXML private Button btnEarnings;
    @FXML private Button btnCalendar;
    @FXML private Button btnSettings;
    @FXML private Button btnQuit;

    private boolean isDarkMode = false;
    private Button activeButton;
    private TranslateTransition currentMove;
    private YearMonth currentMonth = YearMonth.now();
    private java.time.LocalDate selectedDate = java.time.LocalDate.now();

    @FXML
    public void initialize() {
        syncThemeState();

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

        if (userName != null) {
            String sessionName = SessionManager.getUserName();
            userName.setText(sessionName != null && !sessionName.isBlank() ? sessionName : "Worker");
        }

        showDashboard();
    }

    private void setActive(Button button, String title) {
        Button previous = activeButton;
        activeButton = button;
        pageTitle.setText(title);
        if (previous != null && previous != button) previous.getStyleClass().remove("nav-active");
        moveHighlight(button, previous != null);
    }

    private void moveHighlight(Button button, boolean animate) {
        if (button.getHeight() <= 0) { Platform.runLater(() -> moveHighlight(button, false)); return; }
        double targetY = button.getBoundsInParent().getMinY();
        if (animate) {
            if (currentMove != null) currentMove.stop();
            currentMove = new TranslateTransition(Duration.millis(300), slideHighlight);
            currentMove.setToY(targetY);
            currentMove.setInterpolator(Interpolator.EASE_BOTH);
            currentMove.setOnFinished(e -> ensureActive(button));
            currentMove.play();
        } else {
            slideHighlight.setTranslateY(targetY);
            ensureActive(button);
        }
    }

    private void ensureActive(Button b) {
        if (b == activeButton && !b.getStyleClass().contains("nav-active")) b.getStyleClass().add("nav-active");
    }

    private void setContent(Node content) {
        contentArea.getChildren().setAll(content);
        content.setTranslateX(40); content.setOpacity(0);
        TranslateTransition s = new TranslateTransition(Duration.millis(300), content);
        s.setFromX(40); s.setToX(0); s.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition f = new FadeTransition(Duration.millis(300), content);
        f.setFromValue(0); f.setToValue(1); f.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(s, f).play();
    }

    private VBox card(Node... ch) { VBox v = new VBox(ch); v.getStyleClass().add("dash-card"); v.setSpacing(10); v.setMaxWidth(Double.MAX_VALUE); return v; }
    private GridPane row(double... ws) { GridPane g = new GridPane(); g.setHgap(18); for (double w : ws) { ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(w); c.setHgrow(Priority.ALWAYS); g.getColumnConstraints().add(c); } return g; }
    private void addCells(GridPane g, Node... n) { for (int i = 0; i < n.length; i++) { GridPane.setHgrow(n[i], Priority.ALWAYS); if (n[i] instanceof Region) ((Region) n[i]).setMaxWidth(Double.MAX_VALUE); g.add(n[i], i, 0); } }
    private Region divider() { Region r = new Region(); r.getStyleClass().add("divider"); r.setMaxWidth(Double.MAX_VALUE); return r; }

    private VBox statCard(String icon, String val, String lbl) {
        Label ic = new Label(icon); ic.getStyleClass().add("stat-icon");
        Label v = new Label(val); v.getStyleClass().add("stat-value");
        Label l = new Label(lbl); l.getStyleClass().add("stat-label");
        VBox c = card(ic, v, l); c.setSpacing(8); c.setAlignment(Pos.CENTER); c.setPrefHeight(110); return c;
    }

    @FXML private void showDashboard() {
        setActive(btnDashboard, "Dashboard");
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        int fwId = SessionManager.getFwId();
        FarmWorker worker = WorkerService.getWorkerByFwId(fwId);
        WorkerService.EarningsSummary earnings = WorkerService.getWorkerEarnings(fwId);

        double totalReceived = TransactionService.getTransactionsByUser(SessionManager.getUserId()).stream()
                .filter(t -> "PAYMENT".equals(t.getType()))
                .mapToDouble(Transaction::getAmount).sum();
        double remaining = earnings.totalEarned - totalReceived;

        VBox profileCard = card();
        profileCard.setSpacing(14);
        Label avatar = new Label(getInitials(SessionManager.getUserName())); avatar.getStyleClass().add("avatar");
        Label name = new Label(SessionManager.getUserName()); name.getStyleClass().add("card-title");
        String jobLabel = worker != null ? capitalize(worker.getJobType()) + "  |  " + String.format("\u20AA%.0f/%s", worker.getWagePerUnit(), worker.getWageUnit()) : "N/A";
        Label role = new Label(jobLabel); role.getStyleClass().add("card-sub");
        VBox nameBox = new VBox(2, name, role); nameBox.setAlignment(Pos.CENTER_LEFT);
        HBox head = new HBox(12, avatar, nameBox); head.setAlignment(Pos.CENTER_LEFT);
        Label statusL = new Label(worker != null && worker.isActive() ? "ACTIVE" : "INACTIVE");
        statusL.getStyleClass().add(worker != null && worker.isActive() ? "pill-up" : "pill-down");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox headRow = new HBox(head, sp, statusL); headRow.setAlignment(Pos.CENTER_LEFT);
        profileCard.getChildren().add(headRow);

        List<Harvest> myHarvests = WorkerService.getHarvestsByWorker(fwId);
        double thisMonthQty = myHarvests.stream()
                .filter(h -> h.getHarvestDate() != null && h.getHarvestDate().getMonth() == java.time.LocalDate.now().getMonth()
                        && h.getHarvestDate().getYear() == java.time.LocalDate.now().getYear())
                .mapToDouble(Harvest::getQuantityGood).sum();

        GridPane stats = row(25, 25, 25, 25);
        addCells(stats,
                statCard("kg", String.format("%,.0f", thisMonthQty), "This Month"),
                statCard("NIS", String.format("%,.0f", earnings.totalEarned), "Earned"),
                statCard("NIS", String.format("%,.0f", totalReceived), "Received"),
                statCard("NIS", String.format("%,.0f", remaining), "Remaining"));

        VBox recentCard = card();
        recentCard.setSpacing(12);
        Label recentTitle = new Label("Recent Activity"); recentTitle.getStyleClass().add("card-title");
        recentCard.getChildren().add(recentTitle);

        int limit = Math.min(3, myHarvests.size());
        if (limit == 0) {
            Label empty = new Label("No activity yet"); empty.getStyleClass().add("card-sub");
            recentCard.getChildren().add(empty);
        } else {
            for (int i = 0; i < limit; i++) {
                Harvest h = myHarvests.get(i);
                Label cr = new Label(h.getCropName()); cr.getStyleClass().add("list-primary");
                Label info = new Label(h.getFieldName() + " | " + formatRelativeDate(h.getHarvestDate())); info.getStyleClass().add("list-sub");
                VBox txt = new VBox(2, cr, info);
                Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
                Label qty = new Label(String.format("%.0f %s", h.getQuantityGood(), h.getUnit())); qty.getStyleClass().add("mini-stat-value"); qty.setStyle("-fx-font-size: 13px;");
                VBox right = new VBox(3, qty); right.setAlignment(Pos.CENTER_RIGHT);
                HBox rw = new HBox(12, txt, s2, right); rw.setAlignment(Pos.CENTER_LEFT);
                rw.getStyleClass().add("timeline-card");
                recentCard.getChildren().add(rw);
            }
        }
        recentCard.setPrefHeight(220);

        double paidPct = earnings.totalEarned > 0 ? (totalReceived / earnings.totalEarned) * 100 : 0;
        if (paidPct > 100) paidPct = 100;
        if (paidPct < 0) paidPct = 0;

        VBox ringCard = card();
        ringCard.setSpacing(14); ringCard.setAlignment(Pos.CENTER);
        Label ringTitle = new Label("Payment Progress"); ringTitle.getStyleClass().add("card-title");
        double c = 60, r2 = 48;
        Circle track = new Circle(c, c, r2); track.setFill(Color.TRANSPARENT); track.getStyleClass().add("ring-track"); track.setStrokeWidth(14);
        Arc arc = new Arc(c, c, r2, r2, 90, -paidPct * 3.6); arc.setType(ArcType.OPEN); arc.setFill(Color.TRANSPARENT);
        arc.getStyleClass().add("ring-progress"); arc.setStrokeWidth(14); arc.setStrokeLineCap(StrokeLineCap.ROUND);
        Pane ring = new Pane(track, arc); ring.setMinSize(120, 120); ring.setPrefSize(120, 120); ring.setMaxSize(120, 120);
        Label pctL = new Label(String.format("%.0f%%", paidPct)); pctL.getStyleClass().add("ring-center");
        Label pctS = new Label("Paid"); pctS.getStyleClass().add("ring-center-sub");
        VBox ctr = new VBox(pctL, pctS); ctr.setAlignment(Pos.CENTER);
        StackPane ringStack = new StackPane(ring, ctr); ringStack.setMinSize(120, 120); ringStack.setMaxSize(120, 120);
        ringCard.getChildren().addAll(ringTitle, ringStack);
        ringCard.setPrefHeight(220);

        GridPane bottomRow = row(60, 40);
        addCells(bottomRow, recentCard, ringCard);

        root.getChildren().addAll(profileCard, stats, bottomRow);
        setContent(root);
    }

    private String getInitials(String UserName) {
        if (UserName == null || UserName.trim().isEmpty()) return "?";
        String[] parts = UserName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private String formatRelativeDate(java.time.LocalDate date) {
        if (date == null) return "";
        long days = java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now());
        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 14) return "1 week ago";
        if (days < 30) return (days / 7) + " weeks ago";
        return (days / 30) + " months ago";
    }

    @FXML private void showMyWork() {
        setActive(btnMyWork, "My Work");
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        HBox toolbar = new HBox(12); toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField search = new TextField(); search.setPromptText("Search..."); search.getStyleClass().add("search-field"); search.setPrefWidth(250);
        toolbar.getChildren().add(search);

        List<Harvest> myHarvests = WorkerService.getHarvestsByWorker(SessionManager.getFwId());
        double wagePerUnit = 0;
        FarmWorker worker = WorkerService.getWorkerByFwId(SessionManager.getFwId());
        if (worker != null) wagePerUnit = worker.getWagePerUnit();

        VBox list = new VBox(12);
        populateWorkReal(list, myHarvests, wagePerUnit, "");
        double finalWage = wagePerUnit;
        search.textProperty().addListener((o, ov, nv) -> populateWorkReal(list, myHarvests, finalWage, nv.trim().toLowerCase()));

        root.getChildren().addAll(toolbar, list);
        setContent(root);
    }

    private void populateWorkReal(VBox container, List<Harvest> data, double wagePerUnit, String q) {
        container.getChildren().clear();
        for (Harvest h : data) {
            if (!q.isEmpty() && !(h.getCropName() + " " + h.getFieldName()).toLowerCase().contains(q)) continue;

            Label crop = new Label(h.getCropName()); crop.getStyleClass().add("list-primary");
            Label info = new Label(h.getFieldName() + " | " + formatRelativeDate(h.getHarvestDate())); info.getStyleClass().add("list-sub");
            VBox txt = new VBox(2, crop, info);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label qty = new Label(String.format("%.0f %s", h.getQuantityGood(), h.getUnit())); qty.getStyleClass().add("mini-stat-value"); qty.setStyle("-fx-font-size: 13px;");
            String status = h.getStatus() != null ? h.getStatus() : "PENDING";
            Label statusLbl = new Label(capitalize(status));
            if ("APPROVED".equals(status)) statusLbl.getStyleClass().add("pill-up");
            else if ("REJECTED".equals(status)) statusLbl.getStyleClass().add("pill-down");
            else statusLbl.setStyle("-fx-background-color: #FFF4E5; -fx-text-fill: #E68A00; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");
            VBox right = new VBox(3, qty, statusLbl); right.setAlignment(Pos.CENTER_RIGHT);
            HBox row = new HBox(12, txt, sp, right); row.setAlignment(Pos.CENTER_LEFT); row.getStyleClass().add("timeline-card");
            container.getChildren().add(row);
        }
        if (container.getChildren().isEmpty()) {
            Label e = new Label("No records found"); e.getStyleClass().add("card-sub"); e.setStyle("-fx-padding: 30; -fx-font-size: 14px;");
            container.getChildren().add(e);
        }
    }

    @FXML private void showLogWork() {
        setActive(btnLogWork, "Log Work");

        int fwId = SessionManager.getFwId();
        FarmWorker worker = WorkerService.getWorkerByFwId(fwId);

        if (worker != null && !worker.isActive()) {
            setContent(buildInactiveNotice());
            return;
        }

        String jobType = worker != null ? worker.getJobType() : "HARVESTER";

        VBox root;
        switch (jobType) {
            case "IRRIGATOR": root = buildIrrigationForm(worker); break;
            case "PLOWER": root = buildPlowingForm(worker); break;
            case "HARVESTER":
            default: root = buildHarvestForm(worker); break;
        }
        setContent(root);
    }

    private VBox buildInactiveNotice() {
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        VBox notice = card();
        notice.setSpacing(14);
        notice.setAlignment(Pos.CENTER);
        notice.setStyle(notice.getStyle() + "-fx-padding: 40;");

        Label icon = new Label("\uD83D\uDD12");
        icon.setStyle("-fx-font-size: 40px;");

        Label title = new Label("Account Inactive");
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 18px;");

        Label desc = new Label("You cannot log new work while your account is inactive.\nPlease contact your Admin for assistance.");
        desc.getStyleClass().add("card-sub");
        desc.setStyle("-fx-text-alignment: center; -fx-font-size: 13px;");
        desc.setWrapText(true);

        notice.getChildren().addAll(icon, title, desc);
        root.getChildren().add(notice);
        return root;
    }

    private VBox buildHarvestForm(FarmWorker worker) {
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        VBox formCard = card(); formCard.setSpacing(16);
        Label title = new Label("Submit New Harvest"); title.getStyleClass().add("card-title");

        List<Field> fields = FarmService.getAllFields();

        Label fLabel = new Label("Field"); fLabel.getStyleClass().add("mini-stat-label"); fLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<Field> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(fields);
        fieldBox.setConverter(new javafx.util.StringConverter<Field>() {
            public String toString(Field f) { return f != null ? f.getName() : ""; }
            public Field fromString(String s) { return null; }
        });
        if (!fields.isEmpty()) fieldBox.setValue(fields.get(0));
        fieldBox.setMaxWidth(Double.MAX_VALUE); fieldBox.getStyleClass().add("search-field");

        Label cLabel = new Label("Crop"); cLabel.getStyleClass().add("mini-stat-label"); cLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<Crop> cropBox = new ComboBox<>();
        cropBox.setConverter(new javafx.util.StringConverter<Crop>() {
            public String toString(Crop c) { return c != null ? c.getName() : ""; }
            public Crop fromString(String s) { return null; }
        });
        cropBox.setMaxWidth(Double.MAX_VALUE); cropBox.getStyleClass().add("search-field");

        Runnable refreshCrops = () -> {
            cropBox.getItems().clear();
            if (fieldBox.getValue() != null) {
                List<Crop> crops = FarmService.getCropsByField(fieldBox.getValue().getFieldId());
                cropBox.getItems().addAll(crops);
                if (!crops.isEmpty()) cropBox.setValue(crops.get(0));
            }
        };
        refreshCrops.run();
        fieldBox.valueProperty().addListener((o, ov, nv) -> refreshCrops.run());

        Label uLabel = new Label("Unit"); uLabel.getStyleClass().add("mini-stat-label"); uLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.getItems().addAll("kg", "piece");
        unitBox.setValue("kg");
        unitBox.setMaxWidth(Double.MAX_VALUE); unitBox.getStyleClass().add("search-field");

        Label qLabel = new Label("Good Quantity"); qLabel.getStyleClass().add("mini-stat-label"); qLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField qtyField = new TextField(); qtyField.setPromptText("Enter quantity..."); qtyField.getStyleClass().add("search-field");

        Label dLabel = new Label("Damaged Quantity"); dLabel.getStyleClass().add("mini-stat-label"); dLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField dmgField = new TextField(); dmgField.setPromptText("0"); dmgField.getStyleClass().add("search-field");

        Label nLabel = new Label("Notes (optional)"); nLabel.getStyleClass().add("mini-stat-label"); nLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField notesField = new TextField(); notesField.setPromptText("Any notes..."); notesField.getStyleClass().add("search-field");

        Label resultLabel = new Label(); resultLabel.setStyle("-fx-font-size: 13px;");

        Label previewTitle = new Label("Estimated Earnings"); previewTitle.getStyleClass().add("card-sub");
        Label previewCalc = new Label("-- NIS"); previewCalc.getStyleClass().add("big-value");
        VBox preview = new VBox(4, previewTitle, previewCalc); preview.setAlignment(Pos.CENTER);
        preview.setStyle("-fx-padding: 10; -fx-background-color: #F5F7F5; -fx-background-radius: 10;");

        int fwId = SessionManager.getFwId();
        double wagePerUnit = worker != null ? worker.getWagePerUnit() : 0;

        qtyField.textProperty().addListener((o, ov, nv) -> {
            try {
                double qty = Double.parseDouble(nv.trim());
                double wage = qty * wagePerUnit;
                previewCalc.setText(String.format("%.0f NIS", wage));
            } catch (Exception ex) { previewCalc.setText("-- NIS"); }
        });

        Button submitBtn = new Button("Submit Harvest"); submitBtn.getStyleClass().add("action-btn"); submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (fieldBox.getValue() == null || cropBox.getValue() == null) {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText("Please select a field and crop");
                return;
            }

            submitBtn.setDisable(true); submitBtn.setText("Submitting...");
            PauseTransition p = new PauseTransition(Duration.millis(600));
            p.setOnFinished(ev -> {
                WorkerService.Result result = WorkerService.submitHarvest(
                        fieldBox.getValue().getFieldId(), cropBox.getValue().getCropId(), fwId,
                        qtyField.getText(), dmgField.getText(), unitBox.getValue(), notesField.getText());

                submitBtn.setDisable(false); submitBtn.setText("Submit Harvest");

                if (result.success) {
                    resultLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 13px;");
                    resultLabel.setText(result.message);
                    qtyField.clear(); dmgField.clear(); notesField.clear();
                    previewCalc.setText("-- NIS");
                } else {
                    resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                    resultLabel.setText(result.message);
                }
            });
            p.play();
        });

        formCard.getChildren().addAll(title, fLabel, fieldBox, cLabel, cropBox, uLabel, unitBox,
                qLabel, qtyField, dLabel, dmgField, nLabel, notesField, divider(), preview, resultLabel, submitBtn);

        root.getChildren().add(formCard);
        return root;
    }

    private VBox buildIrrigationForm(FarmWorker worker) {
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        VBox formCard = card(); formCard.setSpacing(16);
        Label title = new Label("Log Irrigation"); title.getStyleClass().add("card-title");

        List<Field> fields = FarmService.getAllFields();

        Label fLabel = new Label("Field"); fLabel.getStyleClass().add("mini-stat-label"); fLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<Field> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(fields);
        fieldBox.setConverter(new javafx.util.StringConverter<Field>() {
            public String toString(Field f) { return f != null ? f.getName() : ""; }
            public Field fromString(String s) { return null; }
        });
        if (!fields.isEmpty()) fieldBox.setValue(fields.get(0));
        fieldBox.setMaxWidth(Double.MAX_VALUE); fieldBox.getStyleClass().add("search-field");

        Label qLabel = new Label("Water Quantity (cup)"); qLabel.getStyleClass().add("mini-stat-label"); qLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField qtyField = new TextField(); qtyField.setPromptText("Enter cups..."); qtyField.getStyleClass().add("search-field");

        Label nLabel = new Label("Notes (optional)"); nLabel.getStyleClass().add("mini-stat-label"); nLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField notesField = new TextField(); notesField.setPromptText("Any notes..."); notesField.getStyleClass().add("search-field");

        Label resultLabel = new Label(); resultLabel.setStyle("-fx-font-size: 13px;");

        Label previewTitle = new Label("Estimated Earnings"); previewTitle.getStyleClass().add("card-sub");
        Label previewCalc = new Label("-- NIS"); previewCalc.getStyleClass().add("big-value");
        VBox preview = new VBox(4, previewTitle, previewCalc); preview.setAlignment(Pos.CENTER);
        preview.setStyle("-fx-padding: 10; -fx-background-color: #F5F7F5; -fx-background-radius: 10;");

        Label waterCostNote = new Label("\u2139 Water cost (4 NIS/cup) is billed to the farm separately");
        waterCostNote.getStyleClass().add("card-sub");
        waterCostNote.setStyle("-fx-font-size: 11px;");

        double wagePerUnit = worker != null ? worker.getWagePerUnit() : 0;

        qtyField.textProperty().addListener((o, ov, nv) -> {
            try {
                double qty = Double.parseDouble(nv.trim());
                double wage = qty * wagePerUnit;
                previewCalc.setText(String.format("%.0f NIS", wage));
            } catch (Exception ex) { previewCalc.setText("-- NIS"); }
        });

        int fwId = SessionManager.getFwId();

        Button submitBtn = new Button("Submit Irrigation Log"); submitBtn.getStyleClass().add("action-btn"); submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (fieldBox.getValue() == null) {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText("Please select a field");
                return;
            }

            Double qty;
            try {
                qty = Double.parseDouble(qtyField.getText().trim());
                if (qty <= 0) {
                    resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                    resultLabel.setText("Quantity must be greater than 0");
                    return;
                }
            } catch (Exception ex) {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText("Please enter a valid quantity");
                return;
            }

            submitBtn.setDisable(true); submitBtn.setText("Submitting...");

            FarmService.Result result = FarmService.addLog(
                    fieldBox.getValue().getFieldId(), fwId, "IRRIGATION", notesField.getText(), qty);

            submitBtn.setDisable(false); submitBtn.setText("Submit Irrigation Log");

            if (result.success) {
                resultLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 13px;");
                resultLabel.setText(result.message);
                qtyField.clear(); notesField.clear();
                previewCalc.setText("-- NIS");
            } else {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText(result.message);
            }
        });

        formCard.getChildren().addAll(title, fLabel, fieldBox, qLabel, qtyField, nLabel, notesField,
                divider(), preview, waterCostNote, resultLabel, submitBtn);

        root.getChildren().add(formCard);
        return root;
    }

    private VBox buildPlowingForm(FarmWorker worker) {
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        VBox formCard = card(); formCard.setSpacing(16);
        Label title = new Label("Log Plowing"); title.getStyleClass().add("card-title");

        List<Field> fields = FarmService.getAllFields();

        Label fLabel = new Label("Field"); fLabel.getStyleClass().add("mini-stat-label"); fLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<Field> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(fields);
        fieldBox.setConverter(new javafx.util.StringConverter<Field>() {
            public String toString(Field f) { return f != null ? f.getName() : ""; }
            public Field fromString(String s) { return null; }
        });
        if (!fields.isEmpty()) fieldBox.setValue(fields.get(0));
        fieldBox.setMaxWidth(Double.MAX_VALUE); fieldBox.getStyleClass().add("search-field");

        Label qLabel = new Label("Area Plowed (dunum)"); qLabel.getStyleClass().add("mini-stat-label"); qLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField qtyField = new TextField(); qtyField.setPromptText("Enter dunums..."); qtyField.getStyleClass().add("search-field");

        Label nLabel = new Label("Notes (optional)"); nLabel.getStyleClass().add("mini-stat-label"); nLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField notesField = new TextField(); notesField.setPromptText("Any notes..."); notesField.getStyleClass().add("search-field");

        Label resultLabel = new Label(); resultLabel.setStyle("-fx-font-size: 13px;");

        Label previewTitle = new Label("Estimated Earnings"); previewTitle.getStyleClass().add("card-sub");
        Label previewCalc = new Label("-- NIS"); previewCalc.getStyleClass().add("big-value");
        VBox preview = new VBox(4, previewTitle, previewCalc); preview.setAlignment(Pos.CENTER);
        preview.setStyle("-fx-padding: 10; -fx-background-color: #F5F7F5; -fx-background-radius: 10;");

        double wagePerUnit = worker != null ? worker.getWagePerUnit() : 0;

        qtyField.textProperty().addListener((o, ov, nv) -> {
            try {
                double qty = Double.parseDouble(nv.trim());
                double wage = qty * wagePerUnit;
                previewCalc.setText(String.format("%.0f NIS", wage));
            } catch (Exception ex) { previewCalc.setText("-- NIS"); }
        });

        int fwId = SessionManager.getFwId();

        Button submitBtn = new Button("Submit Plowing Log"); submitBtn.getStyleClass().add("action-btn"); submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (fieldBox.getValue() == null) {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText("Please select a field");
                return;
            }

            Double qty;
            try {
                qty = Double.parseDouble(qtyField.getText().trim());
                if (qty <= 0) {
                    resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                    resultLabel.setText("Area must be greater than 0");
                    return;
                }
            } catch (Exception ex) {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText("Please enter a valid area");
                return;
            }

            submitBtn.setDisable(true); submitBtn.setText("Submitting...");

            FarmService.Result result = FarmService.addLog(
                    fieldBox.getValue().getFieldId(), fwId, "PLOWING", notesField.getText(), qty);

            submitBtn.setDisable(false); submitBtn.setText("Submit Plowing Log");

            if (result.success) {
                resultLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 13px;");
                resultLabel.setText(result.message);
                qtyField.clear(); notesField.clear();
                previewCalc.setText("-- NIS");
            } else {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
                resultLabel.setText(result.message);
            }
        });

        formCard.getChildren().addAll(title, fLabel, fieldBox, qLabel, qtyField, nLabel, notesField,
                divider(), preview, resultLabel, submitBtn);

        root.getChildren().add(formCard);
        return root;
    }

    @FXML private void showEarnings() {
        setActive(btnEarnings, "My Earnings");
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        int fwId = SessionManager.getFwId();
        WorkerService.EarningsSummary earnings = WorkerService.getWorkerEarnings(fwId);
        List<Transaction> payments = TransactionService.getTransactionsByUser(SessionManager.getUserId()).stream()
                .filter(t -> "PAYMENT".equals(t.getType()))
                .collect(java.util.stream.Collectors.toList());
        double totalReceived = payments.stream().mapToDouble(Transaction::getAmount).sum();
        double remaining = earnings.totalEarned - totalReceived;
        double paidPct = earnings.totalEarned > 0 ? (totalReceived / earnings.totalEarned) * 100 : 0;
        if (paidPct > 100) paidPct = 100;
        if (paidPct < 0) paidPct = 0;

        GridPane stats = row(33.33, 33.33, 33.33);
        addCells(stats,
                statCard("NIS", String.format("%,.0f", earnings.totalEarned), "Total Earned"),
                statCard("NIS", String.format("%,.0f", totalReceived), "Received"),
                statCard("NIS", String.format("%,.0f", remaining), "Remaining"));

        VBox ringCard = card();
        ringCard.setSpacing(14); ringCard.setAlignment(Pos.CENTER);
        Label ringTitle = new Label("Payment Progress"); ringTitle.getStyleClass().add("card-title");
        double c = 60, r2 = 48;
        Circle track = new Circle(c, c, r2); track.setFill(Color.TRANSPARENT); track.getStyleClass().add("ring-track"); track.setStrokeWidth(14);
        Arc arc = new Arc(c, c, r2, r2, 90, -paidPct * 3.6); arc.setType(ArcType.OPEN); arc.setFill(Color.TRANSPARENT);
        arc.getStyleClass().add("ring-progress"); arc.setStrokeWidth(14); arc.setStrokeLineCap(StrokeLineCap.ROUND);
        Pane ring = new Pane(track, arc); ring.setMinSize(120, 120); ring.setPrefSize(120, 120); ring.setMaxSize(120, 120);
        Label pctL = new Label(String.format("%.0f%%", paidPct)); pctL.getStyleClass().add("ring-center");
        Label pctS = new Label("Paid"); pctS.getStyleClass().add("ring-center-sub");
        VBox ctr = new VBox(pctL, pctS); ctr.setAlignment(Pos.CENTER);
        StackPane ringStack = new StackPane(ring, ctr); ringStack.setMinSize(120, 120); ringStack.setMaxSize(120, 120);
        ringCard.getChildren().addAll(ringTitle, ringStack);

        VBox histCard = card(); histCard.setSpacing(12);
        Label histTitle = new Label("Payment History"); histTitle.getStyleClass().add("card-title");
        histCard.getChildren().add(histTitle);

        if (payments.isEmpty()) {
            Label empty = new Label("No payments received yet"); empty.getStyleClass().add("card-sub");
            histCard.getChildren().add(empty);
        } else {
            for (Transaction py : payments) {
                Label pn = new Label(py.getDescription() != null ? py.getDescription() : "Salary Payment"); pn.getStyleClass().add("list-primary");
                Label pd = new Label(formatRelativeDate(py.getTransactionDate())); pd.getStyleClass().add("list-sub");
                VBox ptxt = new VBox(2, pn, pd);
                Region psp = new Region(); HBox.setHgrow(psp, Priority.ALWAYS);
                Label pamt = new Label(String.format("+\u20AA %,.0f", py.getAmount())); pamt.getStyleClass().add("pct-up"); pamt.setStyle("-fx-font-size: 14px;");
                HBox prow = new HBox(12, ptxt, psp, pamt); prow.setAlignment(Pos.CENTER_LEFT); prow.getStyleClass().add("timeline-card");
                histCard.getChildren().add(prow);
            }
        }

        root.getChildren().addAll(stats, ringCard, histCard);
        setContent(root);
    }

    @FXML private void showCalendar() {
        setActive(btnCalendar, "Calendar");
        setContent(buildCalendar());
    }

    private Node buildCalendar() {
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        HBox nav = new HBox(12); nav.setAlignment(Pos.CENTER);
        Button prev = new Button("<"); prev.getStyleClass().add("filter-btn");
        Label monthLabel = new Label(currentMonth.getMonth().toString() + " " + currentMonth.getYear());
        monthLabel.getStyleClass().add("card-title"); monthLabel.setStyle("-fx-font-size: 18px;");
        Button next = new Button(">"); next.getStyleClass().add("filter-btn");
        prev.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            if (!YearMonth.from(selectedDate).equals(currentMonth)) selectedDate = currentMonth.atDay(1);
            showCalendar();
        });
        next.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            if (!YearMonth.from(selectedDate).equals(currentMonth)) selectedDate = currentMonth.atDay(1);
            showCalendar();
        });
        nav.getChildren().addAll(prev, monthLabel, next);

        HBox legend = new HBox(16); legend.setAlignment(Pos.CENTER);
        legend.getChildren().addAll(
                legendDot("#1565C0", "Irrigation"),
                legendDot("#2E7D32", "Harvest"),
                legendDot("#795548", "Planting"),
                legendDot("#7B1FA2", "Fertilizing"));

        List<com.smartfarm.model.FarmLog> allLogs = FarmService.getAllLogs();
        List<Harvest> allHarvests = WorkerService.getAllHarvests();

        java.util.Map<java.time.LocalDate, java.util.Set<String>> dayColors = new java.util.HashMap<>();
        for (com.smartfarm.model.FarmLog log : allLogs) {
            if (log.getLogDate() == null) continue;
            dayColors.computeIfAbsent(log.getLogDate(), k -> new java.util.LinkedHashSet<>()).add(logTypeColor(log.getLogType()));
        }
        for (Harvest h : allHarvests) {
            if (h.getHarvestDate() == null) continue;
            dayColors.computeIfAbsent(h.getHarvestDate(), k -> new java.util.LinkedHashSet<>()).add("#2E7D32");
        }

        GridPane grid = new GridPane(); grid.setHgap(4); grid.setVgap(4);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(100.0 / 7); cc.setHgrow(Priority.ALWAYS); grid.getColumnConstraints().add(cc);
        }
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dl = new Label(days[i]); dl.getStyleClass().add("mini-stat-label"); dl.setAlignment(Pos.CENTER); dl.setMaxWidth(Double.MAX_VALUE);
            grid.add(dl, i, 0);
        }

        LocalDate first = currentMonth.atDay(1);
        int startCol = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        VBox dayDetailCard = card();
        dayDetailCard.setSpacing(10);

        for (int d2 = 1; d2 <= daysInMonth; d2++) {
            int col = (startCol + d2 - 1) % 7;
            int rowIdx = 1 + (startCol + d2 - 1) / 7;
            LocalDate cellDate = currentMonth.atDay(d2);

            VBox dayCell = new VBox(2); dayCell.setAlignment(Pos.CENTER);
            dayCell.setMinHeight(50); dayCell.setMaxWidth(Double.MAX_VALUE);
            dayCell.setCursor(javafx.scene.Cursor.HAND);

            boolean isToday = cellDate.equals(today);
            boolean isSelected = cellDate.equals(selectedDate);

            String normalBg = isDarkMode ? "#2D2D2D" : "#FAFAFA";
            String todayBg = isDarkMode ? "#1A3A1A" : "#E8F5E9";
            String normalTextColor = isDarkMode ? "#EEEEEE" : "#333333";
            String todayTextColor = isDarkMode ? "#81C784" : "#2E7D32";

            String baseStyle = "-fx-background-radius: 8; -fx-padding: 4;";
            if (isSelected) {
                baseStyle += "-fx-background-color: #2E7D32;";
            } else if (isToday) {
                baseStyle += "-fx-background-color: " + todayBg + "; -fx-border-color: #2E7D32; -fx-border-radius: 8; -fx-border-width: 2;";
            } else {
                baseStyle += "-fx-background-color: " + normalBg + ";";
            }
            dayCell.setStyle(baseStyle);

            Label dayNum = new Label(String.valueOf(d2));
            String numColor = isSelected ? "#FFFFFF" : (isToday ? todayTextColor : normalTextColor);
            dayNum.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + numColor + ";");
            dayCell.getChildren().add(dayNum);

            java.util.Set<String> colors = dayColors.get(cellDate);
            if (colors != null) {
                HBox dotsRow = new HBox(3);
                dotsRow.setAlignment(Pos.CENTER);
                for (String color : colors) {
                    Region dot = new Region();
                    dot.setMinSize(6, 6); dot.setMaxSize(6, 6);
                    String dotColor = isSelected ? "#FFFFFF" : color;
                    dot.setStyle("-fx-background-color: " + dotColor + "; -fx-background-radius: 3;");
                    dotsRow.getChildren().add(dot);
                }
                dayCell.getChildren().add(dotsRow);
            }

            final LocalDate clickedDate = cellDate;
            dayCell.setOnMouseClicked(e -> { selectedDate = clickedDate; showCalendar(); });

            grid.add(dayCell, col, rowIdx);
        }

        Label detailTitle = new Label(selectedDate.equals(today)
                ? "Today's Activity"
                : "Activity on " + selectedDate.getDayOfMonth() + " " + selectedDate.getMonth().toString().substring(0, 1)
                + selectedDate.getMonth().toString().substring(1).toLowerCase());
        detailTitle.getStyleClass().add("card-title");
        dayDetailCard.getChildren().add(detailTitle);

        int myFwId = SessionManager.getFwId();
        boolean anyEntry = false;

        for (com.smartfarm.model.FarmLog log : allLogs) {
            if (!selectedDate.equals(log.getLogDate())) continue;
            anyEntry = true;
            boolean isMine = log.getFwId() == myFwId;
            dayDetailCard.getChildren().add(buildDayEntryRow(
                    logTypeColor(log.getLogType()), logTypeLabel(log.getLogType()),
                    log.getFieldName() + " \u2022 " + log.getWorkerName(),
                    log.getQuantity() != null ? String.format("%.0f", log.getQuantity()) : "",
                    isMine));
        }

        for (Harvest h : allHarvests) {
            if (!selectedDate.equals(h.getHarvestDate())) continue;
            anyEntry = true;
            boolean isMine = h.getFwId() == myFwId;
            dayDetailCard.getChildren().add(buildDayEntryRow(
                    "#2E7D32", "Harvest \u2014 " + h.getCropName(),
                    h.getFieldName() + " \u2022 " + h.getWorkerName(),
                    String.format("%.0f %s", h.getQuantityGood(), h.getUnit()),
                    isMine));
        }

        if (!anyEntry) {
            Label empty = new Label("No activity recorded on this day");
            empty.getStyleClass().add("card-sub");
            dayDetailCard.getChildren().add(empty);
        }

        root.getChildren().addAll(nav, legend, grid, dayDetailCard);
        return root;
    }

    private String logTypeColor(String logType) {
        switch (logType) {
            case "IRRIGATION": return "#1565C0";
            case "PLANTING": return "#795548";
            case "FERTILIZING": return "#7B1FA2";
            case "PLOWING": return "#607D8B";
            default: return "#9E9E9E";
        }
    }

    private String logTypeLabel(String logType) {
        switch (logType) {
            case "IRRIGATION": return "Irrigation";
            case "PLANTING": return "Planting";
            case "FERTILIZING": return "Fertilizing";
            case "PLOWING": return "Plowing";
            default: return "Note";
        }
    }

    private HBox buildDayEntryRow(String color, String title, String subtitle, String value, boolean isMine) {
        Region dot = new Region(); dot.setMinSize(12, 12); dot.setMaxSize(12, 12);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        Label tn = new Label(title + (isMine ? "  (You)" : "")); tn.getStyleClass().add("list-primary");
        if (isMine) tn.setStyle("-fx-text-fill: #2E7D32;");
        Label td2 = new Label(subtitle); td2.getStyleClass().add("list-sub");
        VBox ttxt = new VBox(2, tn, td2);
        Region tsp = new Region(); HBox.setHgrow(tsp, Priority.ALWAYS);
        Label tst = new Label(value); tst.getStyleClass().add("mini-stat-value"); tst.setStyle("-fx-font-size: 12px;");
        HBox trow = new HBox(10, dot, ttxt, tsp, tst); trow.setAlignment(Pos.CENTER_LEFT); trow.getStyleClass().add("timeline-card");
        return trow;
    }

    private HBox legendDot(String color, String label) {
        Region d = new Region(); d.setMinSize(10, 10); d.setMaxSize(10, 10);
        d.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
        Label l = new Label(label); l.getStyleClass().add("mini-stat-label");
        HBox h = new HBox(5, d, l); h.setAlignment(Pos.CENTER_LEFT); return h;
    }

    @FXML private void showSettings() {
        setActive(btnSettings, "Settings");
        VBox root = new VBox(18); root.getStyleClass().add("dash-root");

        com.smartfarm.model.User user = com.smartfarm.service.AuthService.getUserById(SessionManager.getUserId());
        FarmWorker worker = WorkerService.getWorkerByFwId(SessionManager.getFwId());

        VBox profileCard = card(); profileCard.setSpacing(14);
        Label pTitle = new Label("My Profile"); pTitle.getStyleClass().add("card-title");

        GridPane pg = new GridPane(); pg.setHgap(14); pg.setVgap(12);
        for (int i = 0; i < 2; i++) { ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50); cc.setHgrow(Priority.ALWAYS); pg.getColumnConstraints().add(cc); }

        Label nl = new Label("Full Name"); nl.getStyleClass().add("mini-stat-label"); nl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField nf = new TextField(user != null ? user.getName() : ""); nf.getStyleClass().add("search-field"); nf.setMaxWidth(Double.MAX_VALUE);
        Label el = new Label("Email"); el.getStyleClass().add("mini-stat-label"); el.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField ef = new TextField(user != null && user.getEmail() != null ? user.getEmail() : ""); ef.getStyleClass().add("search-field"); ef.setMaxWidth(Double.MAX_VALUE);
        Label pl = new Label("Phone"); pl.getStyleClass().add("mini-stat-label"); pl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField pf = new TextField(user != null && user.getPhone() != null ? user.getPhone() : ""); pf.getStyleClass().add("search-field"); pf.setMaxWidth(Double.MAX_VALUE);
        Label rl = new Label("Role"); rl.getStyleClass().add("mini-stat-label"); rl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        TextField rf = new TextField(worker != null ? capitalize(worker.getJobType()) : "N/A"); rf.getStyleClass().add("search-field"); rf.setMaxWidth(Double.MAX_VALUE); rf.setDisable(true);

        pg.add(nl, 0, 0); pg.add(nf, 0, 1); pg.add(el, 1, 0); pg.add(ef, 1, 1);
        pg.add(pl, 0, 2); pg.add(pf, 0, 3); pg.add(rl, 1, 2); pg.add(rf, 1, 3);

        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 12px;");

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("action-btn");
        saveBtn.setOnAction(e -> {
            com.smartfarm.service.AuthService.AuthResult result = com.smartfarm.service.AuthService.updateProfile(
                    SessionManager.getUserId(), nf.getText(), ef.getText(), pf.getText());

            if (result.success) {
                resultLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 12px;");
                resultLabel.setText("\u2714 " + result.message);
                SessionManager.login(SessionManager.getUserId(), nf.getText().trim(), false);
                SessionManager.setFwId(SessionManager.getFwId());
                userName.setText(nf.getText().trim());
            } else {
                resultLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
                resultLabel.setText(result.message);
            }
        });

        profileCard.getChildren().addAll(pTitle, pg, resultLabel, saveBtn);

        VBox themeCard = card(); themeCard.setSpacing(14);
        Label tTitle = new Label("Appearance"); tTitle.getStyleClass().add("card-title");
        Button tBtn = new Button(isDarkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        tBtn.getStyleClass().add("quick-btn"); tBtn.setMaxWidth(300);
        tBtn.setOnAction(e -> handleThemeToggle());
        themeCard.getChildren().addAll(tTitle, tBtn);

        root.getChildren().addAll(profileCard, themeCard);
        setContent(root);
    }

    @FXML
    private void handleQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Quit");
        alert.setHeaderText("Are you sure you want to quit?");
        alert.setContentText("You will be returned to the login screen.");
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
        rotate.setByAngle(360); rotate.play();
        isDarkMode = !isDarkMode;
        SceneSwitcher.setDarkMode(isDarkMode);
        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            rootPane.getStyleClass().remove("dark-mode");
        }
    }

    private void syncThemeState() {
        isDarkMode = SceneSwitcher.isDarkMode();
        if (isDarkMode) {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/moon.png")));
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            themeIcon.setImage(new Image(getClass().getResourceAsStream("/images/sun.png")));
            rootPane.getStyleClass().remove("dark-mode");
        }
    }
}
package edu.busTracker.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Phase 10: JavaFX Desktop Admin Console.
 * Demonstrates:
 *   - JavaFX Application lifecycle
 *   - TableView with ObservableList
 *   - Event Handlers for UI Buttons
 *   - Platform.runLater() for background HTTP updates
 */
public class AdminDesktopApp extends Application {

    private final ObservableList<BusRow> busData = FXCollections.observableArrayList();
    private final Label statusLabel = new Label("Status: Ready (Web backend at http://localhost:8080)");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static class BusRow {
        private String busNumber;
        private String registrationNo;
        private String route;
        private String status;

        public BusRow(String busNumber, String registrationNo, String route, String status) {
            this.busNumber = busNumber;
            this.registrationNo = registrationNo;
            this.route = route;
            this.status = status;
        }

        public String getBusNumber() { return busNumber; }
        public String getRegistrationNo() { return registrationNo; }
        public String getRoute() { return route; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SRM University-AP — Bus Tracker Desktop Admin Console");

        // TableView setup
        TableView<BusRow> table = new TableView<>();
        TableColumn<BusRow, String> colNum = new TableColumn<>("Bus Number");
        colNum.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        colNum.setPrefWidth(120);

        TableColumn<BusRow, String> colReg = new TableColumn<>("Registration");
        colReg.setCellValueFactory(new PropertyValueFactory<>("registrationNo"));
        colReg.setPrefWidth(140);

        TableColumn<BusRow, String> colRoute = new TableColumn<>("Route");
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colRoute.setPrefWidth(100);

        TableColumn<BusRow, String> colStatus = new TableColumn<>("Simulation Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(160);

        table.getColumns().addAll(colNum, colReg, colRoute, colStatus);
        table.setItems(busData);

        // Pre-populate with standard university seed buses
        busData.addAll(
            new BusRow("Bus TN-2", "AP39 UH 9613", "TN-2", "Simulated GPS: Scheduled"),
            new BusRow("Bus TN-3", "AP39 UM 2725", "TN-3", "Simulated GPS: Scheduled"),
            new BusRow("Bus TN-4", "AP39 UM 2726", "TN-4", "Simulated GPS: Scheduled")
        );

        // Buttons for simulation control
        Button btnStart = new Button("▶ Start GPS Simulation");
        btnStart.setOnAction(e -> triggerBackend("/api/admin/simulation/start", "Simulation Started"));

        Button btnStop = new Button("⏸ Pause GPS Simulation");
        btnStop.setOnAction(e -> triggerBackend("/api/admin/simulation/stop", "Simulation Paused"));

        Button btnRefresh = new Button("🔄 Refresh Data");
        btnRefresh.setOnAction(e -> {
            for (BusRow r : busData) {
                r.setStatus("Simulated GPS: Running (Interpolated)");
            }
            table.refresh();
            statusLabel.setText("Status: Refreshed from local bus store");
        });

        HBox btnBar = new HBox(10, btnStart, btnStop, btnRefresh);
        btnBar.setPadding(new Insets(10, 0, 10, 0));

        VBox root = new VBox(10, new Label("Active Fleet Tracking:"), table, btnBar, statusLabel);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 600, 420);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void triggerBackend(String endpoint, String successMsg) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080" + endpoint))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> statusLabel.setText("Status: " + successMsg + " (" + resp.statusCode() + ")"));
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Status: " + successMsg + " (Offline demo mode)"));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

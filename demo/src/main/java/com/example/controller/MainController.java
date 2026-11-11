package com.example.controller;

import com.example.engine.SchedulerEngine;
import com.example.model.GanttRecord;
import com.example.model.Process;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    // -----------------------------
    // INPUTS
    // -----------------------------

    @FXML private TextField atInput;
    @FXML private TextField btInput;
    @FXML private TextField priorityInput;
    @FXML private TextField quantumInput;

    @FXML private ComboBox<String> algoSelect;

    // -----------------------------
    // TABLE
    // -----------------------------

    @FXML private TableView<Process> processTable;

    @FXML private TableColumn<Process, String> colPid;
    @FXML private TableColumn<Process, Integer> colAt;
    @FXML private TableColumn<Process, Integer> colBt;
    @FXML private TableColumn<Process, Integer> colPriority;
    @FXML private TableColumn<Process, Integer> colCt;
    @FXML private TableColumn<Process, Integer> colTat;
    @FXML private TableColumn<Process, Integer> colWt;

    // -----------------------------
    // GANTT
    // -----------------------------

    @FXML private HBox ganttContainer;

    // -----------------------------
    // QUANTUM
    // -----------------------------

    @FXML private VBox quantumBox;

    // -----------------------------
    // SUMMARY
    // -----------------------------

    @FXML private Label avgTatLabel;
    @FXML private Label avgWtLabel;
    @FXML private Label cpuUtilLabel;

    // -----------------------------
    // PLAYBACK
    // -----------------------------

    @FXML private Button playbackButton;
    @FXML private Button stepButton;
    @FXML private Slider speedSlider;

    // -----------------------------
    // DATA
    // -----------------------------

    private final ObservableList<Process> processList =
            FXCollections.observableArrayList();

    private int processCounter = 1;

    private List<GanttRecord> currentTimeline = List.of();

    private int playbackIndex = 0;

    private Timeline playbackTimeline;

    private boolean playbackRunning = false;


    // =========================================================
    // INITIALIZATION
    // =========================================================

    @FXML
    public void initialize() {

        // Algorithms
        algoSelect.setItems(
                FXCollections.observableArrayList(
                        "FCFS",
                        "Shortest Job First (SJF)",
                        "Shortest Remaining Time First (SRTF)",
                        "Round Robin",
                        "Priority Scheduling"
                )
        );

        algoSelect.getSelectionModel().selectFirst();


        // Table bindings
        colPid.setCellValueFactory(
                new PropertyValueFactory<>("pid")
        );

        colAt.setCellValueFactory(
                new PropertyValueFactory<>("arrivalTime")
        );

        colBt.setCellValueFactory(
                new PropertyValueFactory<>("burstTime")
        );

        colPriority.setCellValueFactory(
                new PropertyValueFactory<>("priority")
        );

        colCt.setCellValueFactory(
                new PropertyValueFactory<>("completionTime")
        );

        colTat.setCellValueFactory(
                new PropertyValueFactory<>("turnaroundTime")
        );

        colWt.setCellValueFactory(
                new PropertyValueFactory<>("waitingTime")
        );

        processTable.setItems(processList);


        // Algorithm listener
        algoSelect.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                        updateQuantumVisibility()
        );

        updateQuantumVisibility();


        // Rapid data entry
        btInput.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ENTER) {

                addProcess();

                if (!processList.isEmpty()) {
                    atInput.requestFocus();
                }
            }
        });


        // Playback speed
        speedSlider.setMin(0.25);
        speedSlider.setMax(2.0);
        speedSlider.setValue(1.0);


        resetPlayback();
    }


    // =========================================================
    // ADD PROCESS
    // =========================================================

    @FXML
    private void addProcess() {

        try {

            int at =
                    Integer.parseInt(
                            atInput.getText().trim()
                    );

            int bt =
                    Integer.parseInt(
                            btInput.getText().trim()
                    );

            int priority =
                    Integer.parseInt(
                            priorityInput.getText().trim()
                    );


            if (at < 0) {

                showError(
                        "Arrival Time cannot be negative."
                );

                return;
            }


            if (bt <= 0) {

                showError(
                        "Burst Time must be greater than 0."
                );

                return;
            }


            if (priority <= 0) {

                showError(
                        "Priority must be greater than 0."
                );

                return;
            }


            Process process =
                    new Process(
                            "P" + processCounter,
                            at,
                            bt,
                            priority
                    );

            processList.add(process);

            processCounter++;

            clearInputs();

            atInput.requestFocus();


        } catch (NumberFormatException e) {

            showError(
                    "Please enter valid integer values."
            );
        }
    }


    // =========================================================
    // RUN SIMULATION
    // =========================================================

    @FXML
    private void runSimulation() {

        if (processList.isEmpty()) {

            showError(
                    "Add at least one process before running."
            );

            return;
        }


        try {

            stopPlayback();


            String algorithm =
                    algoSelect.getValue();

            List<GanttRecord> timeline;


            switch (algorithm) {

                case "FCFS":

                    timeline =
                            SchedulerEngine.simulateFCFS(
                                    processList
                            );

                    break;


                case "Shortest Job First (SJF)":

                    timeline =
                            SchedulerEngine.simulateSJF(
                                    processList
                            );

                    break;


                case "Shortest Remaining Time First (SRTF)":

                    timeline =
                            SchedulerEngine.simulateSRTF(
                                    processList
                            );

                    break;


                case "Round Robin":

                    int quantum =
                            Integer.parseInt(
                                    quantumInput
                                            .getText()
                                            .trim()
                            );

                    if (quantum <= 0) {

                        showError(
                                "Time Quantum must be greater than 0."
                        );

                        return;
                    }

                    timeline =
                            SchedulerEngine.simulateRoundRobin(
                                    processList,
                                    quantum
                            );

                    break;


                case "Priority Scheduling":

                    timeline =
                            SchedulerEngine.simulatePriority(
                                    processList
                            );

                    break;


                default:

                    showError(
                            "Please select a scheduling algorithm."
                    );

                    return;
            }


            currentTimeline = timeline;

            processTable.refresh();


            // Show complete chart
            playbackIndex = timeline.size();

            drawGanttChart(
                    timeline.subList(
                            0,
                            playbackIndex
                    )
            );


            calculateSummary(timeline);

            updatePlaybackButtons();


        } catch (NumberFormatException e) {

            showError(
                    "Enter a valid Time Quantum."
            );
        }
    }


    // =========================================================
    // SUMMARY METRICS
    // =========================================================

    private void calculateSummary(
            List<GanttRecord> timeline
    ) {

        if (processList.isEmpty()
                || timeline.isEmpty()) {

            avgTatLabel.setText("0.00");
            avgWtLabel.setText("0.00");
            cpuUtilLabel.setText("0.00%");

            return;
        }


        double totalTat = 0;
        double totalWt = 0;


        for (Process process : processList) {

            totalTat +=
                    process.getTurnaroundTime();

            totalWt +=
                    process.getWaitingTime();
        }


        double averageTat =
                totalTat / processList.size();

        double averageWt =
                totalWt / processList.size();


        int busyTime = 0;

        int totalTime =
                timeline.get(
                        timeline.size() - 1
                ).getEndTime();


        for (GanttRecord record : timeline) {

            if (!record.getPid().equals("Idle")) {

                busyTime +=
                        record.getDuration();
            }
        }


        double cpuUtilization =
                totalTime == 0
                        ? 0
                        : ((double) busyTime / totalTime)
                        * 100;


        avgTatLabel.setText(
                String.format(
                        "%.2f",
                        averageTat
                )
        );

        avgWtLabel.setText(
                String.format(
                        "%.2f",
                        averageWt
                )
        );

        cpuUtilLabel.setText(
                String.format(
                        "%.2f%%",
                        cpuUtilization
                )
        );
    }


    // =========================================================
    // GANTT CHART
    // =========================================================

    private void drawGanttChart(
            List<GanttRecord> timeline
    ) {

        ganttContainer.getChildren().clear();


        Map<String, String> colorMap =
                new HashMap<>();


        String[] palette = {

                "#6366F1",
                "#10B981",
                "#F59E0B",
                "#EF4444",
                "#8B5CF6",
                "#06B6D4",
                "#EC4899",
                "#84CC16"
        };


        int colorIndex = 0;


        for (GanttRecord record : timeline) {

            String pid =
                    record.getPid();

            String color;


            if (pid.equals("Idle")) {

                color = "#E5E7EB";

            } else {

                if (!colorMap.containsKey(pid)) {

                    colorMap.put(
                            pid,
                            palette[
                                    colorIndex
                                            % palette.length
                            ]
                    );

                    colorIndex++;
                }

                color =
                        colorMap.get(pid);
            }


            addGanttBlock(
                    record,
                    color
            );
        }
    }


    // =========================================================
    // GANTT BLOCK + TOOLTIP
    // =========================================================

    private void addGanttBlock(
            GanttRecord record,
            String color
    ) {

        String pid =
                record.getPid();

        boolean idle =
                pid.equals("Idle");


        Label block =
                new Label(
                        pid
                                + "\n"
                                + record.getStartTime()
                                + " — "
                                + record.getEndTime()
                );


        block.getStyleClass().add(
                idle
                        ? "gantt-idle"
                        : "gantt-block"
        );


        block.setStyle(
                "-fx-background-color: "
                        + color
                        + ";"
        );


        block.setAlignment(
                Pos.CENTER
        );

        block.setMinHeight(72);


        int duration =
                record.getDuration();


        block.setMinWidth(
                Math.max(
                        70,
                        duration * 32
                )
        );


        // -----------------------------------------
        // TOOLTIP
        // -----------------------------------------

        String state;

        if (idle) {

            state = "CPU Idle";

        } else {

            state = "Process Executing";
        }


        Tooltip tooltip =
                new Tooltip(
                        "Process: " + pid
                                + "\n"
                                + "Start: "
                                + record.getStartTime()
                                + " ms"
                                + "\n"
                                + "End: "
                                + record.getEndTime()
                                + " ms"
                                + "\n"
                                + "Duration: "
                                + duration
                                + " ms"
                                + "\n"
                                + "State: "
                                + state
                );


        tooltip.setShowDelay(
                Duration.millis(150)
        );

        tooltip.setHideDelay(
                Duration.millis(100)
        );


        Tooltip.install(
                block,
                tooltip
        );


        ganttContainer
                .getChildren()
                .add(block);
    }


    // =========================================================
    // STEP FORWARD
    // =========================================================

    @FXML
    private void stepForward() {

        if (currentTimeline.isEmpty()) {
            return;
        }


        if (playbackIndex
                >= currentTimeline.size()) {

            playbackIndex = 0;
        }


        playbackIndex++;


        drawGanttChart(
                currentTimeline.subList(
                        0,
                        playbackIndex
                )
        );


        updatePlaybackButtons();
    }


    // =========================================================
    // PLAY / PAUSE
    // =========================================================

    @FXML
    private void togglePlayback() {

        if (currentTimeline.isEmpty()) {
            return;
        }


        if (playbackRunning) {

            stopPlayback();

            return;
        }


        // If playback already reached the end,
        // start again from the beginning.
        if (playbackIndex
                >= currentTimeline.size()) {

            playbackIndex = 0;
        }


        playbackRunning = true;

        playbackButton.setText("❚❚  Pause");


        double speed =
                speedSlider.getValue();


        double interval =
                900 / speed;


        playbackTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.millis(
                                        interval
                                ),
                                event ->
                                        stepForward()
                        )
                );


        playbackTimeline.setCycleCount(
                Timeline.INDEFINITE
        );


        playbackTimeline.play();
    }


    // =========================================================
    // STOP PLAYBACK
    // =========================================================

    private void stopPlayback() {

        if (playbackTimeline != null) {

            playbackTimeline.stop();
        }


        playbackRunning = false;

        playbackButton.setText(
                "▶  Playback"
        );
    }


    // =========================================================
    // PLAYBACK BUTTON STATE
    // =========================================================

    private void updatePlaybackButtons() {

        boolean hasData =
                !currentTimeline.isEmpty();


        stepButton.setDisable(
                !hasData
        );

        playbackButton.setDisable(
                !hasData
        );


        if (playbackIndex
                >= currentTimeline.size()) {

            playbackButton.setText(
                    "↻  Replay"
            );

        } else {

            playbackButton.setText(
                    "▶  Playback"
            );
        }
    }


    // =========================================================
    // RESET PLAYBACK
    // =========================================================

    private void resetPlayback() {

        currentTimeline = List.of();

        playbackIndex = 0;

        stopPlayback();

        stepButton.setDisable(true);

        playbackButton.setDisable(true);

        ganttContainer
                .getChildren()
                .clear();

        avgTatLabel.setText("0.00");
        avgWtLabel.setText("0.00");
        cpuUtilLabel.setText("0.00%");
    }


    // =========================================================
    // QUANTUM VISIBILITY
    // =========================================================

    private void updateQuantumVisibility() {

        boolean roundRobin =
                "Round Robin".equals(
                        algoSelect.getValue()
                );


        quantumBox.setManaged(
                roundRobin
        );

        quantumBox.setVisible(
                roundRobin
        );
    }


    // =========================================================
    // CLEAR ALL
    // =========================================================

    @FXML
    private void clearAll() {

        stopPlayback();

        processList.clear();

        processCounter = 1;

        clearInputs();

        resetPlayback();

        processTable.refresh();

        atInput.requestFocus();
    }


    // =========================================================
    // CLEAR INPUTS
    // =========================================================

    private void clearInputs() {

        atInput.clear();

        btInput.clear();

        priorityInput.clear();

        quantumInput.clear();
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle(
                "Input Error"
        );

        alert.setHeaderText(null);

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}
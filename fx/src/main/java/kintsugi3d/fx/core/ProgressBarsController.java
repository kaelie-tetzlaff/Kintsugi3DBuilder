/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch, Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.fx.core;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Stage;
import kintsugi3d.builder.core.Global;
import kintsugi3d.fx.util.Stopwatch;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressBarsController
{
    private static final Logger LOG = LoggerFactory.getLogger(ProgressBarsController.class);

    private static ProgressBarsController instance;

    private Stage stage;

    @FXML private Label localElapsedTimeLabel;
    @FXML private Label localEstimTimeRemainingLabel;

    @FXML private Label totalElapsedTimeLabel;

    @FXML private Label localTextLabel;
    @FXML private Label overallTextLabel;

    @FXML private ProgressBar overallProgressBar;
    @FXML private ProgressBar localProgressBar;

    @FXML private Button cancelButton;
    @FXML private Label pageCountLabel; //TODO: imp this, currently invisible
    @FXML private Button doneButton;

    private Stopwatch overallStopwatch;
    private Stopwatch localStopwatch;

    private String defaultTitle;

    /**
     * Typically something like "Working..."
     */
    private String defaultOverallText;

    /**
     * Typically something like "(--:--:-- Lapsed)"
     */
    private String defaultTotalTimeElapsedText;

    /**
     * Typically something like "Please wait.."
     */
    private String defaultLocalText;

    /**
     * Typically something like "Calculating remaining time... "
     */
    private String defaultEstimLocalTimeRemainingTxt;

    /**
     * Typically something like "(--:--:-- Lapsed)"
     */
    private String defaultOverallElapsedTimeTxt;

    private final BooleanProperty processingProperty = new SimpleBooleanProperty(false);

    /**
     * Source of truth for the countdown timer - stores the remaining duration
     */
    private final ObjectProperty<Duration> estimatedRemainingDuration = new SimpleObjectProperty<>(null);

    /**
     * Holds manual text overrides (e.g., "Calculating remaining time...") when set explicitly.
     * Null means no override (show the countdown). An empty string is a valid override that displays nothing.
     */
    private final StringProperty estimatedRemainingOverride = new SimpleStringProperty(null);

    /**
     * Target expiry instant for the countdown — the moment when the countdown reaches zero.
     * Null means no active countdown.
     */
    private Instant countdownExpiry;

    /**
     * Periodic timer for updating elapsed time and ticking down the countdown.
     */
    private PauseTransition timer;

    static ProgressBarsController getInstance()
    {
        //noinspection StaticVariableUsedBeforeInitialization
        return instance;
    }

    public void init(Stage stage)
    {
        this.stage = stage;
        defaultOverallText = overallTextLabel.getText();
        defaultLocalText = localTextLabel.getText();

        defaultTotalTimeElapsedText = totalElapsedTimeLabel.getText();
        defaultEstimLocalTimeRemainingTxt = localEstimTimeRemainingLabel.getText();
        defaultOverallElapsedTimeTxt = localElapsedTimeLabel.getText();

        defaultTitle = stage.getTitle();

        localEstimTimeRemainingLabel.textProperty().bind(
            Bindings.when(estimatedRemainingOverride.isNotNull())
                .then(estimatedRemainingOverride)
                .otherwise(
                    Bindings.createStringBinding(() ->
                    {
                        Duration duration = estimatedRemainingDuration.get();

                        if (duration == null || duration.isZero() || duration.isNegative())
                        {
                            return "Almost done...";
                        }
                        else
                        {
                            return String.format("%02d:%02d:%02d Remaining", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
                        }
                    },
                    estimatedRemainingDuration)));

        // remove estimated times from view if not processing
        localEstimTimeRemainingLabel.managedProperty().bind(processingProperty);
        localEstimTimeRemainingLabel.visibleProperty().bind(processingProperty);

        overallStopwatch = new Stopwatch();
        localStopwatch = new Stopwatch();

        processingProperty.bind(overallStopwatch.runningProperty());

        localProgressBar.getScene().getWindow().setOnCloseRequest(
            event -> MainWindowController.getInstance().showMiniProgressBar());

        // Keep track of whether cancellation was requested.
        AtomicBoolean cancelRequested = new AtomicBoolean(false);
        cancelButton.setOnAction(event ->
        {
            cancelRequested.set(true);
            Platform.runLater(() -> cancelButton.setText("Cancelling..."));
        });

        doneButton.setOnAction(event -> hideAllProgress());

        cancelButton.disableProperty().bind(getProcessingProperty().not());
        doneButton.disableProperty().bind(getProcessingProperty());

        Global.io().addProgressMonitor(
            new Monitor(cancelRequested, localProgressBar, overallProgressBar, overallTextLabel, localTextLabel, cancelButton));

        //noinspection AssignmentToStaticFieldFromInstanceMethod
        instance = this;
    }

    public Stage getStage()
    {
        return stage;
    }

    private void resetText()
    {
        localTextLabel.setText(defaultLocalText);
        overallTextLabel.setText(defaultOverallText);

        totalElapsedTimeLabel.setText(defaultTotalTimeElapsedText);
        localElapsedTimeLabel.setText(defaultOverallElapsedTimeTxt);

        estimatedRemainingOverride.set(defaultEstimLocalTimeRemainingTxt);
        estimatedRemainingDuration.setValue(null);
        countdownExpiry = null;

        stage.setTitle(defaultTitle);
    }

    public void showStage()
    {
        stage.show();
    }

    private void hideStage()
    {
        Platform.runLater(() -> stage.hide());
    }

    private void startStopwatches()
    {
        overallStopwatch.start();
        localStopwatch.start();

        // Use a PauseTransition to periodically update elapsed time and tick down the countdown.
        // Runs on the JavaFX application thread, so no Platform.runLater is needed.
        // Cycle count is 1; playFromStart() in onFinished handles repetition while processing.
        timer = new PauseTransition(javafx.util.Duration.millis(1000));
        timer.setCycleCount(1);
        timer.setOnFinished(event ->
        {
            tickTimer();
            if (isProcessing())
            {
                timer.playFromStart();
            }
        });
        timer.playFromStart();

        saturateProgressBars();
    }

    private void tickTimer()
    {
        // Update elapsed time labels
        long totalElapsedTime = overallStopwatch.getElapsedTime();
        long localElapsedTime = localStopwatch.getElapsedTime();

        String totalTimeTxt = nanosecToFormatTime(totalElapsedTime, true);
        String localTimeTxt = nanosecToFormatTime(localElapsedTime, true);

        totalElapsedTimeLabel.setText(String.format("%s Lapsed", totalTimeTxt));
        localElapsedTimeLabel.setText(String.format("(%s Lapsed)", localTimeTxt));

        // Tick down the countdown, if active (null override means no override, empty string is a valid override)
        if (estimatedRemainingOverride.getValue() == null && countdownExpiry != null)
        {
            // Calculate remaining duration from the target expiry timestamp
            Duration remaining = Duration.between(Instant.now(), countdownExpiry);

            if (remaining.isZero() || remaining.isNegative())
            {
                estimatedRemainingDuration.setValue(Duration.ZERO);
                countdownExpiry = null;
            }
            else
            {
                estimatedRemainingDuration.setValue(remaining);
            }
        }
    }

    private void updateTimeEstimationLabel(long estimatedRemaining)
    {
        long secondsRemaining = TimeUnit.NANOSECONDS.toSeconds(estimatedRemaining);

        // Clear any manual override when updating the estimate
        estimatedRemainingOverride.set(null);

        if (secondsRemaining > 0)
        {
            Duration duration = Duration.ofSeconds(secondsRemaining);
            estimatedRemainingDuration.setValue(duration);

            // Set the target expiry timestamp so tickTimer uses real time
            countdownExpiry = Instant.now().plus(duration);
        }
        else
        {
            estimatedRemainingDuration.setValue(Duration.ZERO);
            countdownExpiry = null;
        }
    }

    private void clickStopwatches(double progress, double maximum)
    {
        Platform.runLater(() ->
        {
            // Set manual text to default, so the binding shows the default message
            estimatedRemainingOverride.set(defaultEstimLocalTimeRemainingTxt);

            overallStopwatch.click();
            localStopwatch.click();
            updateLocalRemainingTime(progress, maximum);
        });
    }

    private void updateLocalRemainingTime(double progress, double maximum)
    {
        long avgDif = localStopwatch.getAvgDifference();

        double remainingProcesses = maximum - progress;

        long estimatedRemaining = Math.max(0, (long) (avgDif * remainingProcesses));

        updateTimeEstimationLabel(estimatedRemaining);
    }

    private static String nanosecToFormatTime(long nanoTime, boolean useSeconds)
    {
        long hours = TimeUnit.NANOSECONDS.toHours(nanoTime);

        long minutes = TimeUnit.NANOSECONDS.toMinutes(nanoTime) -
            TimeUnit.HOURS.toMinutes(hours);

        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanoTime) -
            TimeUnit.HOURS.toSeconds(hours) -
            TimeUnit.MINUTES.toSeconds(minutes);

        return useSeconds ? String.format("%02d:%02d:%02d", hours, minutes, seconds) :
            String.format("%02d:%02d", hours, minutes);
    }

    private void endStopwatches()
    {
        Platform.runLater(() ->
        {
            // Stop the periodic timer first
            if (timer != null)
            {
                timer.stop();
            }

            overallStopwatch.stop();
            localStopwatch.stop();

            //remove parenthesis from elapsed times
            totalElapsedTimeLabel.setText(totalElapsedTimeLabel.getText().replace("(", "")
                .replace(")", ""));

            localElapsedTimeLabel.setText(localElapsedTimeLabel.getText().replace("(", "")
                .replace(")", ""));

            desaturateProgressBars();
        });
    }

    private void desaturateProgressBars()
    {
        overallProgressBar.getStyleClass().add("desaturated-progress-bar");
        localProgressBar.getStyleClass().add("desaturated-progress-bar");
    }

    private void saturateProgressBars()
    {
        overallProgressBar.getStyleClass().clear();
        localProgressBar.getStyleClass().clear();

        overallProgressBar.getStyleClass().add("progress-bar");
        localProgressBar.getStyleClass().add("progress-bar");
    }

    public void stopAndClose()
    {
        endStopwatches();
        hideStage();
    }

    public boolean isProcessing()
    {
        return processingProperty.getValue();
    }

    public ReadOnlyBooleanProperty getProcessingProperty()
    {
        return processingProperty;
    }

    private void hideAllProgress()
    {
        hideStage();
        MainWindowController.getInstance().dismissMiniProgressBarAsync();
    }

    private class Monitor implements ProgressMonitor
    {
        private final AtomicBoolean cancelRequested;
        private final ProgressBar localProgressBar;
        private final ProgressBar overallProgressBar;
        private final Label overallTextLabel;
        private final Label localTextLabel;
        private final Button cancelButton;
        private double maximum;
        private double localProgress;

        // Updated instantly whereas stageCountProperty is updated asynchronously.
        private int currentStage;

        private final IntegerProperty stageCountProperty;
        private final IntegerProperty currentStageProperty;

        private String localText; //when process is finishing up, store last msg into here while displaying "Finishing up..."
        private String finishingUpText;

        Monitor(AtomicBoolean cancelRequested, ProgressBar localProgressBar, ProgressBar overallProgressBar, Label overallTextLabel, Label localTextLabel, Button cancelButton)
        {
            this.cancelRequested = cancelRequested;
            this.localProgressBar = localProgressBar;
            this.overallProgressBar = overallProgressBar;
            this.overallTextLabel = overallTextLabel;
            this.localTextLabel = localTextLabel;
            this.cancelButton = cancelButton;
            maximum = 0.0;
            localProgress = 0.0;
            stageCountProperty = new SimpleIntegerProperty(0);
            currentStageProperty = new SimpleIntegerProperty(0);
        }

        @Override
        public void allowUserCancellation() throws UserCancellationException
        {
            if (cancelRequested.get())
            {
                cancelRequested.set(false); // reset cancel flag

                MainWindowController.getInstance().dismissMiniProgressBarAsync();

                //need to end stopwatches here because they might need to be reused for another process
                //   before cancelComplete() is called
                endStopwatches();

                throw new UserCancellationException("Cancellation requested by user.");
            }
        }

        @Override
        public void cancelComplete(UserCancellationException e)
        {
            complete();
            hideAllProgress();
        }

        @Override
        public void start()
        {
            cancelRequested.set(false);

            localProgress = 0.0;
            currentStage = 0;

            Platform.runLater(() ->
            {
                stageCountProperty.setValue(0);
                currentStageProperty.setValue(0);
                localProgressBar.setProgress(maximum == 0.0 ? ProgressIndicator.INDETERMINATE_PROGRESS : 0.0);
                overallProgressBar.setProgress(maximum == 0.0 ? ProgressIndicator.INDETERMINATE_PROGRESS : 0.0);

                resetText();
                showStage();
                startStopwatches();

                MainWindowController mainWindow = MainWindowController.getInstance();
                mainWindow.resetMiniProgressBar(overallProgressBar.progressProperty(),
                    Bindings.createStringBinding(() ->
                        {
                            String currProcessTxt = overallTextLabel.textProperty().getValue();

                            // Display "Finishing up..." or something similar
                            if (currentStageProperty.getValue() > stageCountProperty.getValue() && isProcessing())
                            {
                                return localTextLabel.getText();
                            }

                            // Display "Working..." or some end message (ex. "Finished loading images")
                            // or just remove redundant "Stage 1/1"
                            if (!isProcessing() || stageCountProperty.getValue() <= 1)
                            {
                                return currProcessTxt;
                            }

                            return String.format("%s (Stage %s/%s)",
                                currProcessTxt, currentStageProperty.getValue(), stageCountProperty.getValue());
                        },
                        overallTextLabel.textProperty(), currentStageProperty, stageCountProperty,
                        localTextLabel.textProperty())); // pass localTextLabel text property so this binding updates more often
            });
        }

        @Override
        public void setProcessName(String processName)
        {
            Platform.runLater(() ->  ((Stage) overallProgressBar.getScene().getWindow()).setTitle(processName));
        }

        @Override
        public void setStageCount(int count)
        {
            Platform.runLater(() ->
            {
                stageCountProperty.setValue(count);
                refreshOverallProgress();
            });
        }

        @Override
        public void setStage(int stage, String message)
        {
            this.localProgress = 0.0;
            currentStage = stage + 1; //index from 1, copy so we can update currentStageProperty w/ Platform.runLater to avoid threading issue

            // Reset text that displays when finishing up for a new stage
            finishingUpText = FINISHING_UP_DEFAULT;

            Platform.runLater(() ->
            {
                if (currentStage <= stageCountProperty.getValue())
                {
                    LOG.info("[Stage {}/{}] {}", currentStage, stageCountProperty.getValue(), message);
                }
                else if (currentStage == stageCountProperty.getValue() + 1)
                {
                    LOG.info("[Finishing up] {}", message);
                }

                this.currentStageProperty.setValue(currentStage);
                localProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                overallTextLabel.setText(message);
                localTextLabel.setText(DEFAULT_MESSAGE);

                refreshOverallProgress();

                if(currentStage <= stageCountProperty.getValue())
                {
                    // begin new stage
                    localStopwatch = new Stopwatch();
                    localStopwatch.start();
                }
            });
        }

        @Override
        public void advanceStage(String message)
        {
            setStage(currentStage + 1, message);
        }

        @Override
        public void setFinishingUpText(String finishingUpText)
        {
            this.finishingUpText = finishingUpText;
            Platform.runLater(this::refreshLocalText);
        }

        private void refreshLocalText()
        {
            if(currentStageProperty.get() > stageCountProperty.get())
            {
                localTextLabel.setText(this.finishingUpText);
            }
        }

        private void refreshOverallProgress()
        {
            if (stageCountProperty.getValue() > 0)
            {
                //index current stage from 0 in this instance
                double overallProgress = (double) (currentStageProperty.get() - 1) / stageCountProperty.getValue();
                overallProgressBar.setProgress(overallProgress);

                refreshLocalText();
            }
        }

        @Override
        public void setMaxProgress(double maxProgress)
        {
            this.maximum = maxProgress;
            Platform.runLater(this::refreshLocalProgress);
        }

        @Override
        public void setProgress(double progress, String message)
        {
            this.localProgress = progress;

            LOG.info("[{}%] {}", new DecimalFormat("#.##").format(localProgress / maximum * 100), message);

            //remove stage/stageCount from txt if it wouldn't make sense for it to be there (ex. Stage 0/0)
            //useful for simple exports like orbit animation
            boolean removeStageNums = stageCountProperty.getValue() <= 1 || currentStageProperty.getValue() == 0;
            this.localText = removeStageNums ? message :
                String.format("Stage %s/%s -- %s", currentStageProperty.getValue(), stageCountProperty.getValue(), message);

            Platform.runLater(() ->
            {
                refreshLocalProgress();
                localTextLabel.setText(localText);
            });

            clickStopwatches(progress, maximum);
        }

        private void refreshLocalProgress()
        {
            localProgressBar.setProgress(maximum == 0.0 ? ProgressIndicator.INDETERMINATE_PROGRESS : localProgress / maximum);

            //index current stage from 0 in this instance
            double offset = (double) (currentStageProperty.getValue() - 1) / stageCountProperty.getValue();
            double overallProgress = offset + (localProgress / (maximum * stageCountProperty.getValue()));
            overallProgressBar.setProgress(maximum == 0.0 ? ProgressIndicator.INDETERMINATE_PROGRESS : overallProgress);
        }

        @Override
        public void complete()
        {
            this.maximum = 0.0;
            endStopwatches();

            MainWindowController mainWindow = MainWindowController.getInstance();
            mainWindow.setReadyToDismissMiniProgBar();

            if (!stage.isShowing())
            {
                mainWindow.showMiniProgressBar();
            }

            Platform.runLater(() ->
            {
                if (overallProgressBar.isIndeterminate())
                {
                    overallProgressBar.setProgress(1.0);
                }

                if (localProgressBar.isIndeterminate())
                {
                    localProgressBar.setProgress(1.0);
                }

                //only revert text for processes which are not lightweight
                if (localTextLabel.getText().equals(FINISHING_UP_DEFAULT))
                {
                    localTextLabel.setText(localText);
                }

                // Clear countdown state on complete
                estimatedRemainingDuration.setValue(null);

                cancelButton.setText("Cancel");
            });
        }

        @Override
        public void fail(Throwable e)
        {
            complete();
        }

        @Override
        public boolean isConflictingProcess()
        {
            if (!isProcessing())
            {
                return false;
            }

            Platform.runLater(() ->
            {
                ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
                //ButtonType stopProcess = new ButtonType("Start New Process", ButtonBar.ButtonData.YES);
                Alert alert = new Alert(AlertType.NONE,
                    "Cannot run multiple tasks at the same time.\nEither wait for the current task to complete or cancel it."
                    /*+ "Press OK to finish the current process."*/, ok/*, stopProcess*/);
                alert.setHeaderText("Conflicting Tasks");

//                    //continue current process, don't start a new one
//                    ((Button) alert.getDialogPane().lookupButton(ok)).setOnAction(event -> {
//                    });
//
//                    //cancel current process and start new one
//                    ((Button) alert.getDialogPane().lookupButton(stopProcess)).setOnAction(event -> {
//                        cancelRequested.set(true);
//                    });

                alert.showAndWait();
            });

            return true;
        }
    }
}

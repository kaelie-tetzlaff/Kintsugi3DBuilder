/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch,
 *                           Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.fx.controllers.paged;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import kintsugi3d.builder.util.FXWindowHelper;
import kintsugi3d.fx.core.JavaFXState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Internal base class that is extended for the public-facing classes
 * @param <T>
 * @param <PageType>
 */
abstract class PageControllerBase<T, PageType extends Page<?, ?>> implements PageController<T>
{
    private PageFrameController pageFrameController;
    private PageType page;
    private final BooleanProperty canAdvanceProperty = new SimpleBooleanProperty(false);
    private final StringProperty advanceLabelOverrideProperty = new SimpleStringProperty(null);
    private final BooleanProperty canConfirmProperty = new SimpleBooleanProperty(false);

    private volatile boolean alertShown = false;

    @Override
    public final PageFrameController getPageFrameController()
    {
        return pageFrameController;
    }

    @Override
    public final void setPageFrameController(PageFrameController pageFrameController)
    {
        this.pageFrameController = pageFrameController;
    }

    protected final @NotNull JavaFXState getState()
    {
        return pageFrameController.getState();
    }

    @Override
    public final PageType getPage()
    {
        return page;
    }

    public final void setPage(PageType page)
    {
        this.page = page;
    }

    @Override
    public final boolean canAdvance()
    {
        return canAdvanceProperty.get();
    }

    @Override
    public final @NotNull BooleanProperty getCanAdvanceObservable()
    {
        return canAdvanceProperty;
    }

    protected final void setCanAdvance(boolean canAdvance)
    {
        canAdvanceProperty.set(canAdvance);
    }

    @Override
    public final @NotNull StringProperty getAdvanceLabelOverrideObservable()
    {
        return advanceLabelOverrideProperty;
    }

    @Override
    public final String getAdvanceLabelOverride()
    {
        return advanceLabelOverrideProperty.get();
    }

    protected final void setAdvanceLabelOverride(@Nullable String advanceLabelOverride)
    {
        advanceLabelOverrideProperty.set(advanceLabelOverride);
    }

    /**
     * Whether or not this page can "confirm", ending the user workflow.
     * @return
     */
    @Override
    public final @NotNull BooleanProperty getCanConfirmObservable()
    {
        return canConfirmProperty;
    }

    /**
     * Gets whether or not this page can "confirm", ending the user workflow.
     * @return
     */
    @Override
    public final boolean canConfirm()
    {
        return canConfirmProperty.get();
    }

    /**
     * Sets whether or not this page can "confirm", ending the user workflow.
     * @param canConfirm
     */
    protected final void setCanConfirm(boolean canConfirm)
    {
        canConfirmProperty.set(canConfirm);
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public boolean tryAdvance()
    {
        // By default, advance without any additional logic other than what page has been assigned as nextPage.
        return true;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public boolean tryConfirm()
    {
        // Allow silent exit by default if no confirmation is implemented.
        return true;
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public boolean tryCancel()
    {
        // Close without any additional logic by default.
        return true;
    }

    private void close()
    {
        FXWindowHelper.requestClose(pageFrameController.getWindow());
    }

    private void alert(String title, String message, boolean allowContinue)
    {
        if (alertShown) // Prevent multiple alerts from showing at once
        {
            return;
        }

        alertShown = true;
        Platform.runLater(() ->
        {
            if (page.hasFallbackPage())
            {
                // "Cancel" closes the modal entirely
                ButtonType cancel = new ButtonType("Cancel", ButtonData.LEFT);

                // "Fallback" buttons navigate to a different page.
                Collection<ButtonType> fallbackButtons = page.getFallbackPages().keySet().stream()
                    .map(ButtonType::new)
                    .collect(Collectors.toList());

                Collection<ButtonType> allButtons = new ArrayList<>(fallbackButtons.size() + 2);
                allButtons.add(new ButtonType("Cancel", ButtonData.LEFT));
                allButtons.addAll(fallbackButtons);

                // If allowed, "Continue" just continues as if nothing happened (only allowed for warnings)
                ButtonType continueButton = null;
                if (allowContinue)
                {
                    continueButton = new ButtonType("Continue", ButtonData.RIGHT);
                    allButtons.add(continueButton);
                }

                Alert alert = new Alert(AlertType.NONE, message, allButtons.toArray(ButtonType[]::new));

                // Force the window back to the correct size in case of race conditions with the OS (esp. on Linux)
                ChangeListener<? super Number> forceSize =
                    (obs, oldValue, newValue) ->
                        Platform.runLater(alert.getDialogPane()::autosize);
                alert.getDialogPane().widthProperty().addListener(forceSize);
                alert.getDialogPane().heightProperty().addListener(forceSize);

                ((ButtonBase) alert.getDialogPane().lookupButton(cancel)).setOnAction(event ->
                {
                    close();
                    alertShown = false;
                });

                for (ButtonType button : fallbackButtons)
                {
                    ((ButtonBase) alert.getDialogPane().lookupButton(button)).setOnAction(event ->
                    {
                        pageFrameController.fallbackPage(button.getText());
                        alertShown = false;
                    });
                }

                if (allowContinue)
                {
                    ((ButtonBase) alert.getDialogPane().lookupButton(continueButton)).setOnAction(event -> alertShown = false);
                }

                alert.setTitle(title);
                alert.show();
            }
            else
            {
                ButtonType ok = new ButtonType("OK", ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(AlertType.NONE, message, ok);
                ((ButtonBase) alert.getDialogPane().lookupButton(ok)).setOnAction(event ->
                {
                    close();
                    alertShown = false;
                });
                alert.setTitle(title);
                alert.show();
            }
        });
    }

    protected final void error(String title, String message)
    {
        alert(title, message, false);
    }

    protected final void warning(String title, String message)
    {
        alert(title, message, true);
    }
}

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

package kintsugi3d.fx.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TextFormatter.Change;
import kintsugi3d.builder.core.Global;
import kintsugi3d.fx.core.FrontendIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A general utilities class.
 */
public final class StaticUtilities
{
    private static final Logger LOG = LoggerFactory.getLogger(StaticUtilities.class);

//    private static final Pattern NUMERIC_REGEX = Pattern.compile("-?(0|([1-9]\\d{0,7}))?(\\.\\d*)?");

    private StaticUtilities()
    {
    }

    //    this method takes in a double property, and prevents it from reaching outside of its bound.
    public static <H extends Property<Number>> @NotNull H wrapAround(double min, double max, @NotNull H property)
    {
        property.addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null && (newValue.doubleValue() < min || newValue.doubleValue() > max))
            {
                property.setValue(wrapAround(min, max, newValue.doubleValue()));
            }
        });
        return property;
    }

    public static <H extends Property<Number>> @NotNull H clamp(double min, double max, @NotNull H property)
    {
        property.addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null && (newValue.doubleValue() < min || newValue.doubleValue() > max))
            {
                property.setValue(clamp(min, max, newValue.doubleValue()));
            }
        });
        return property;
    }


    public static void makeWrapAroundNumeric(double min, double max, @NotNull TextField textField)
    {
        makeNumeric(textField);
        textField.focusedProperty().addListener((ob, o, n) ->
        {
            if (o && !n)
            {
                try
                {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(Double.toString(wrapAround(min, max, value)));
                }
                catch (NumberFormatException nfe)
                {
                    //do nothing
                }
            }
        });
    }

    public static void makeClampedNumeric(double min, double max, @NotNull TextField textField)
    {
        makeNumeric(textField);
        textField.focusedProperty().addListener((ob, o, n) ->
        {
            if (o && !n)
            {
                try
                {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(Double.toString(clamp(min, max, value)));
                }
                catch (NumberFormatException nfe)
                {
                    //do nothing
                }
            }
        });
    }

    public static void makeNumeric(@NotNull TextField textField)
    {
        textField.setTextFormatter(new TextFormatter<Double>(StaticUtilities::validateNumericChange));
    }

    private static @Nullable Change validateNumericChange(@NotNull Change change)
    {
        if (change.isDeleted() && !change.isReplaced())
        {
            return change;
        }
        String text = change.getControlNewText();
        if (text.isEmpty() || Objects.equals("-", text))
        {
            return change;
        }
        else
        {
            try
            {
                Double.parseDouble(text);
                return change;
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    public static void makeClampedInteger(double min, double max, @NotNull TextField textField)
    {
        makeNumeric(textField);
        textField.focusedProperty().addListener((ob, o, n) ->
        {
            if (o && !n)
            {
                try
                {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(Long.toString(Math.round(clamp(min, max, value))));
                }
                catch (NumberFormatException nfe)
                {
                    //do nothing
                }
            }
        });
    }

    public static void bindLogScaleToLinear(@NotNull DoubleProperty logScaleProperty, @NotNull DoubleProperty linearScaleProperty)
    {
        logScaleProperty.addListener((b, o, n) -> linearScaleProperty.set(StrictMath.pow(10, n.doubleValue())));
        linearScaleProperty.addListener((b, o, n) -> logScaleProperty.set(StrictMath.log10(n.doubleValue())));
    }

    public static void makeSquareResolutionComboBox(@NotNull ComboBox<SquareResolution> comboBox)
    {
        comboBox.setItems(FXCollections.observableArrayList(
            new SquareResolution(256), new SquareResolution(512), new SquareResolution(1024),
            new SquareResolution(2048), new SquareResolution(4096), new SquareResolution(8192)));
        comboBox.getSelectionModel().select(3); // 2048x2048
    }

    private static double wrapAround(double min, double max, double value)
    {
        double diff = max - min;
        if (diff == 0)
        {
            return max;
        }
        //System.out.printf("[%f %f %f]", min, max, value);
        double sum = value;
        while (sum < min)
        {
            sum += diff;
        }
        while (sum > max)
        {
            sum -= diff;
        }
        //System.out.println();
        return sum;
    }

    private static double clamp(double min, double max, double value)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean confirmCancel()
    {
        if (Global.io().isProjectOpen()) // Might have closed the project in which case confirmation is irrelevant
        {
            Dialog<ButtonType> alert = createConfirmDialog("Settings will be reverted to their previous values.");
            alert.setHeaderText("Discard changes?");
            var result = alert.showAndWait();
            return result.isPresent() && result.get().equals(ButtonType.OK);
        }
        else
        {
            return true;
        }
    }

    /**
     * Creates a confirm dialog according to Kintsugi style standards (i.e. Cancel button left-aligned)
     * @param contentText
     * @return
     */
    public static @NotNull Dialog<ButtonType> createConfirmDialog(String contentText)
    {
        Dialog<ButtonType> confirm = new Alert(AlertType.CONFIRMATION, contentText);

        // Try to left-align the cancel button.
        try
        {
            Button cancelButton = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelButton != null)
            {
                ButtonBar.setButtonData(cancelButton, ButtonData.LEFT);
            }
            else
            {
                LOG.error(FrontendIO.FAILED_TO_FIND_CANCEL_BUTTON);
            }
        }
        catch (ClassCastException e)
        {
            LOG.error(FrontendIO.FAILED_TO_FIND_CANCEL_BUTTON, e);
        }

        return confirm;
    }

    public static @NotNull Dialog<ButtonType> createConfirmDialog()
    {
        return createConfirmDialog("");
    }
}

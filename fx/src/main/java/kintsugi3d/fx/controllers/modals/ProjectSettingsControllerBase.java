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

package kintsugi3d.fx.controllers.modals;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import kintsugi3d.builder.state.settings.GeneralSettingsModel;
import kintsugi3d.fx.controllers.paged.NonDataPageControllerBase;
import kintsugi3d.fx.internal.ReadonlyObservableGeneralSettingsModel;
import kintsugi3d.fx.util.StaticUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

/**
 * Base class for controllers that allow the user to alter settings but the effects of the settings are only
 * apparent upon some discrete event (rather than being updated in real-time).
 * Keeps track of which settings have been bound to allow for them to be conveniently applied or reset to defaults.
 */
public abstract class ProjectSettingsControllerBase extends NonDataPageControllerBase
{
    private final ProjectSettingsManager projectSettingsManager;

    protected ProjectSettingsControllerBase()
    {
        projectSettingsManager = new ProjectSettingsManager();
    }

    protected ProjectSettingsControllerBase(ProjectSettingsManager projectSettingsManager)
    {
        this.projectSettingsManager = projectSettingsManager;
    }

    protected final ReadonlyObservableGeneralSettingsModel getLocalSettingsModel()
    {
        return projectSettingsManager.getLocalSettingsModel();
    }

    protected final GeneralSettingsModel getProjectSettingsModel()
    {
        return projectSettingsManager.getProjectSettingsModel();
    }

    protected final @NotNull Set<String> getTrackedSettings()
    {
        return projectSettingsManager.getTrackedSettings();
    }

    @Override
    public final void refresh()
    {
        // Populate local model with the current project settings.
        projectSettingsManager.refresh();
    }

    @Override
    @FXML
    public final boolean tryCancel()
    {
        if (StaticUtilities.confirmCancel())
        {
            onCancel();
            return true;
        }
        else
        {
            return false;
        }
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    protected void onCancel()
    {
        // Default no-op implementation if no cancel callback is needed.
    }

    /**
     * Locally reset settings that have been bound / tracked on this controller to their default values.
     * To have these defaults applied to the project, applyBoundSettings() will also need to be called.
     */
    @FXML
    public final void resetSettingsToDefaults()
    {
        projectSettingsManager.resetSettingsToDefaults();
    }

    /**
     * Apply settings that have been bound / tracked on this controller to the project.
     * @return
     */
    protected final void applySettings()
    {
        projectSettingsManager.applySettings();
    }

    protected final void trackSetting(String settingName)
    {
        projectSettingsManager.trackSetting(settingName);
    }

    protected final void bindFloatSetting(@NotNull TextField textField, String settingName, float minValue, float maxValue)
    {
        projectSettingsManager.bindFloatSetting(textField, settingName, minValue, maxValue);
    }

    protected final void bindNormalizedSetting(@NotNull TextField textField, String settingName)
    {
        projectSettingsManager.bindNormalizedSetting(textField, settingName);
    }

    protected final void bindIntegerSetting(@NotNull TextField textField, String settingName, int minValue, int maxValue)
    {
        projectSettingsManager.bindIntegerSetting(textField, settingName, minValue, maxValue);
    }

    protected final void bindNumericSetting(@NotNull Slider slider, String settingName)
    {
        projectSettingsManager.bindNumericSetting(slider, settingName);
    }

    protected final void bindBooleanSetting(@NotNull CheckBox checkBox, String settingName)
    {
        projectSettingsManager.bindBooleanSetting(checkBox, settingName);
    }

    protected final void bindBooleanSetting(@NotNull BooleanProperty property, String settingName)
    {
        projectSettingsManager.bindBooleanSetting(property, settingName);
    }

    protected final <T> void bindNumericComboBox(@NotNull ComboBox<T> comboBox, String settingName,
                                                 @NotNull Function<Number, T> choiceConstructor, @NotNull Function<T, Number> extractNumeric)
    {
        // Manually bind resolution both ways as a combo box.
        projectSettingsManager.bindNumericComboBox(comboBox, settingName, choiceConstructor, extractNumeric);
    }

    protected final <T> void bindTextComboBox(@NotNull ComboBox<T> comboBox, String settingName,
                                              @NotNull Function<String, T> choiceConstructor, @NotNull Function<T, String> extractText)
    {
        // Manually bind resolution both ways as a combo box.
        projectSettingsManager.bindTextComboBox(comboBox, settingName, choiceConstructor, extractText);
    }

    protected final void bindTextComboBox(@NotNull ComboBox<String> comboBox, String settingName)
    {
        projectSettingsManager.bindTextComboBox(comboBox, settingName);
    }

    protected final <T> void bindObjectComboBox(@NotNull ComboBox<T> comboBox, String settingName, @NotNull Class<T> objectClass)
    {
        projectSettingsManager.bindObjectComboBox(comboBox, settingName, objectClass);
    }
}

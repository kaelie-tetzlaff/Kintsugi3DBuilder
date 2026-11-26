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

package kintsugi3d.fx.controllers.scene.environment;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import kintsugi3d.builder.state.project.SerializableEnvironmentSettings;
import kintsugi3d.fx.util.StaticUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ObservableEnvironmentSettings extends SerializableEnvironmentSettings
{
    private final BooleanProperty envUseImage = new SimpleBooleanProperty();
    private final BooleanProperty envUseColor = new SimpleBooleanProperty(true);
    private final BooleanProperty bpUseImage = new SimpleBooleanProperty();
    private final BooleanProperty bpUseColor = new SimpleBooleanProperty();
    private final BooleanProperty imagePathsRelative = new SimpleBooleanProperty();

    private final Property<File> envImageFile = new SimpleObjectProperty<>();
    private final Property<File> bpImageFile = new SimpleObjectProperty<>();

    private final DoubleProperty envColorIntensity = StaticUtilities.clamp(0, Double.MAX_VALUE, new SimpleDoubleProperty(1.0));
    private final DoubleProperty backgroundIntensity = StaticUtilities.clamp(0, Double.MAX_VALUE, new SimpleDoubleProperty(1.0));
    private final DoubleProperty envRotation = StaticUtilities.wrapAround(-180, 180, new SimpleDoubleProperty());
    private final DoubleProperty envFilteringBias = new SimpleDoubleProperty(0);
    private final Property<Color> envColor = new SimpleObjectProperty<>(Color.valueOf("#222222"));
    private final Property<Color> bpColor = new SimpleObjectProperty<>(Color.valueOf("#222222"));
    private final StringProperty name = new SimpleStringProperty("New Environment Map");
    private final BooleanProperty locked = new SimpleBooleanProperty();
    private final BooleanProperty envLoaded = new SimpleBooleanProperty();
    private final BooleanProperty bpLoaded = new SimpleBooleanProperty();

    // ground plane
    private final BooleanProperty gpEnabled = new SimpleBooleanProperty();
    private final Property<Color> gpColor = new SimpleObjectProperty<>(Color.WHITE);
    private final DoubleProperty gpHeight = new SimpleDoubleProperty();
    private final DoubleProperty gpSize = new SimpleDoubleProperty(1.0);

    public ObservableEnvironmentSettings()
    {
    }

    public ObservableEnvironmentSettings(String name)
    {
        this.name.set(name);
    }

    @Override
    public final String toString()
    {
        if (!isNoEnvironment() && locked.getValue())
        {
            return String.format("(L) %s", name.getValue());
        }
        else
        {
            return name.getValue();
        }
    }

    public final @NotNull ObservableEnvironmentSettings duplicate()
    {
        ObservableEnvironmentSettings newEnvironment = new ObservableEnvironmentSettings();
        newEnvironment.envUseImage.setValue(envUseImage.getValue());
        newEnvironment.envUseColor.setValue(envUseColor.getValue());
        newEnvironment.bpUseImage.setValue(bpUseImage.getValue());
        newEnvironment.bpUseColor.setValue(bpUseColor.getValue());
        newEnvironment.imagePathsRelative.setValue(imagePathsRelative.getValue());
        newEnvironment.envImageFile.setValue(envImageFile.getValue());
        newEnvironment.bpImageFile.setValue(bpImageFile.getValue());
        newEnvironment.backgroundIntensity.setValue(backgroundIntensity.getValue());
        newEnvironment.envColorIntensity.setValue(envColorIntensity.getValue());
        newEnvironment.envRotation.setValue(envRotation.getValue());
        newEnvironment.envFilteringBias.setValue(envFilteringBias.getValue());
        newEnvironment.envColor.setValue(envColor.getValue());
        newEnvironment.bpColor.setValue(bpColor.getValue());
        newEnvironment.name.setValue(String.format("%s copy", name.getValue()));
        newEnvironment.locked.setValue(locked.getValue());
        newEnvironment.envLoaded.setValue(envLoaded.getValue());
        newEnvironment.bpLoaded.setValue(bpLoaded.getValue());
        newEnvironment.gpEnabled.setValue(gpEnabled.getValue());
        newEnvironment.gpColor.setValue(gpColor.getValue());
        newEnvironment.gpHeight.setValue(gpHeight.getValue());
        newEnvironment.gpSize.setValue(gpSize.getValue());
        return newEnvironment;
    }

    @Override
    public final boolean isEnvironmentImageEnabled()
    {
        return envUseImage.get();
    }

    public final @NotNull BooleanProperty envUseImageProperty()
    {
        return envUseImage;
    }

    @Override
    public final void setEnvironmentImageEnabled(boolean envUseImage)
    {
        this.envUseImage.set(envUseImage);
    }

    @Override
    public final boolean isEnvironmentColorEnabled()
    {
        return envUseColor.get();
    }

    public final @NotNull BooleanProperty envUseColorProperty()
    {
        return envUseColor;
    }

    @Override
    public final void setEnvironmentColorEnabled(boolean envUseColor)
    {
        this.envUseColor.set(envUseColor);
    }

    @Override
    public final boolean isBackplateImageEnabled()
    {
        return bpUseImage.get();
    }

    public final @NotNull BooleanProperty bpUseImageProperty()
    {
        return bpUseImage;
    }

    @Override
    public final void setBackplateImageEnabled(boolean bpUseImage)
    {
        this.bpUseImage.set(bpUseImage);
    }

    @Override
    public final boolean isBackplateColorEnabled()
    {
        return bpUseColor.get();
    }

    public final @NotNull BooleanProperty bpUseColorProperty()
    {
        return bpUseColor;
    }

    @Override
    public final void setBackplateColorEnabled(boolean bpUseColor)
    {
        this.bpUseColor.set(bpUseColor);
    }

    @Override
    public final boolean areImagePathsRelative()
    {
        return imagePathsRelative.get();
    }

    public final @NotNull BooleanProperty imagePathsRelativeProperty()
    {
        return imagePathsRelative;
    }

    @Override
    public final void setImagePathsRelative(boolean imagePathsRelative)
    {
        this.imagePathsRelative.set(imagePathsRelative);
    }

    @Override
    public final File getEnvironmentImageFile()
    {
        return envImageFile.getValue();
    }

    public final @NotNull Property<File> envImageFileProperty()
    {
        return envImageFile;
    }

    @Override
    public final void setEnvironmentImageFile(File envImageFile)
    {
        this.envImageFile.setValue(envImageFile);
    }

    @Override
    public final File getBackplateImageFile()
    {
        return bpImageFile.getValue();
    }

    public final @NotNull Property<File> bpImageFileProperty()
    {
        return bpImageFile;
    }

    @Override
    public final void setBackplateImageFile(File bpImageFile)
    {
        this.bpImageFile.setValue(bpImageFile);
    }

    @Override
    public final double getBackgroundIntensity()
    {
        return backgroundIntensity.get();
    }

    public final @NotNull DoubleProperty backgroundIntensityProperty()
    {
        return backgroundIntensity;
    }

    @Override
    public final void setBackgroundIntensity(double backgroundIntensity)
    {
        this.backgroundIntensity.set(backgroundIntensity);
    }

    @Override
    public final double getEnvironmentColorIntensity()
    {
        return envColorIntensity.get();
    }

    public final @NotNull DoubleProperty envColorIntensityProperty()
    {
        return envColorIntensity;
    }

    @Override
    public final void setEnvironmentIntensity(double envColorIntensity)
    {
        this.envColorIntensity.set(envColorIntensity);
    }

    @Override
    public final double getEnvironmentRotation()
    {
        return envRotation.get();
    }

    public final @NotNull DoubleProperty envRotationProperty()
    {
        return envRotation;
    }

    @Override
    public final void setEnvironmentRotation(double envRotation)
    {
        this.envRotation.set(envRotation);
    }

    public final @NotNull DoubleProperty envFilteringBiasProperty()
    {
        return envFilteringBias;
    }

    @Override
    public final double getEnvironmentFilteringBias()
    {
        return envFilteringBias.get();
    }

    @Override
    public final void setEnvironmentFilteringBias(double envFilteringBias)
    {
        this.envFilteringBias.set(envFilteringBias);
    }

    @Override
    public final Color getEnvironmentColor()
    {
        return envColor.getValue();
    }

    public final @NotNull Property<Color> envColorProperty()
    {
        return envColor;
    }

    @Override
    public final void setEnvironmentColor(Color envColor)
    {
        this.envColor.setValue(envColor);
    }

    @Override
    public final Color getBackplateColor()
    {
        return bpColor.getValue();
    }

    public final @NotNull Property<Color> bpColorProperty()
    {
        return bpColor;
    }

    @Override
    public final void setBackplateColor(Color bpColor)
    {
        this.bpColor.setValue(bpColor);
    }

    @Override
    public final String getName()
    {
        return name.get();
    }

    public final @NotNull StringProperty nameProperty()
    {
        return name;
    }

    @Override
    public final void setName(String name)
    {
        this.name.set(name);
    }

    @Override
    public final boolean isLocked()
    {
        return locked.get();
    }

    public final @NotNull BooleanProperty lockedProperty()
    {
        return locked;
    }

    @Override
    public final void setLocked(boolean locked)
    {
        this.locked.set(locked);
    }

    @Override
    public final boolean isEnvironmentLoaded()
    {
        return envLoaded.get();
    }

    public final @NotNull BooleanProperty envLoadedProperty()
    {
        return envLoaded;
    }

    @Override
    public final void setEnvironmentLoaded(boolean envLoaded)
    {
        this.envLoaded.set(envLoaded);
    }

    @Override
    public final boolean isBackplateLoaded()
    {
        return bpLoaded.get();
    }

    public final @NotNull BooleanProperty bpLoadedProperty()
    {
        return bpLoaded;
    }

    @Override
    public final void setBackplateLoaded(boolean bpLoaded)
    {
        this.bpLoaded.set(bpLoaded);
    }

    @Override
    public final boolean isGroundPlaneEnabled()
    {
        return gpEnabled.get();
    }

    public final @NotNull BooleanProperty gpEnabledProperty()
    {
        return gpEnabled;
    }

    @Override
    public final void setGroundPlaneEnabled(boolean gpEnabled)
    {
        this.gpEnabled.set(gpEnabled);
    }

    @Override
    public final Color getGroundPlaneCrolor()
    {
        return gpColor.getValue();
    }

    public final @NotNull Property<Color> gpColorProperty()
    {
        return gpColor;
    }

    @Override
    public final void setGroundPlaneColor(Color gpColor)
    {
        this.gpColor.setValue(gpColor);
    }

    @Override
    public final double getGroundPlaneHeight()
    {
        return gpHeight.getValue();
    }

    public final @NotNull DoubleProperty gpHeightProperty()
    {
        return gpHeight;
    }

    @Override
    public final void setGroundPlaneHeight(double gpHeight)
    {
        this.gpHeight.setValue(gpHeight);
    }

    @Override
    public final double getGroundPlaneSize()
    {
        return gpSize.getValue();
    }

    public final @NotNull DoubleProperty gpSizeProperty()
    {
        return gpSize;
    }

    @Override
    public final void setGroundPlaneSize(double gpSize)
    {
        this.gpSize.setValue(gpSize);
    }
}

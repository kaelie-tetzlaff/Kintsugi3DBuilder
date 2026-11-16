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

package kintsugi3d.fx.controllers.scene.lights;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import kintsugi3d.builder.state.project.SerializableLightSettings;
import kintsugi3d.fx.util.StaticUtilities;

public class ObservableLightSettings extends SerializableLightSettings
{
    private final DoubleProperty targetX = new SimpleDoubleProperty();
    private final DoubleProperty targetY = new SimpleDoubleProperty();
    private final DoubleProperty targetZ = new SimpleDoubleProperty();
    private final DoubleProperty azimuth = StaticUtilities.wrapAround(-180, 180, new SimpleDoubleProperty());
    private final DoubleProperty inclination = StaticUtilities.clamp(-90, 90, new SimpleDoubleProperty());
    private final DoubleProperty log10Distance = new SimpleDoubleProperty();
    private final DoubleProperty intensity = StaticUtilities.clamp(0, Double.MAX_VALUE, new SimpleDoubleProperty(1.0));
    private final BooleanProperty locked = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty("X");
    private final DoubleProperty spotSize = StaticUtilities.clamp(0.0, 90.0, new SimpleDoubleProperty(45.0));
    private final DoubleProperty spotTaper = StaticUtilities.clamp(0.0, 1.0, new SimpleDoubleProperty());
    private final Property<Color> color = new SimpleObjectProperty<>(Color.WHITE);

    private final BooleanProperty groupLocked;

    @Override
    public final boolean isGroupLocked()
    {
        return groupLocked.get();
    }

    public ObservableLightSettings(String name, BooleanProperty groupLockedProperty)
    {
        this.name.set(name);
        this.groupLocked = groupLockedProperty;
    }

    public ObservableLightSettings(BooleanProperty groupLockedProperty)
    {
        this.groupLocked = groupLockedProperty;
    }


    public final ObservableLightSettings duplicate()
    {
        ObservableLightSettings dupl = new ObservableLightSettings(name.getValue(), this.groupLocked);
        dupl.targetX.set(this.targetX.get());
        dupl.targetY.set(this.targetY.get());
        dupl.targetZ.set(this.targetZ.get());
        dupl.azimuth.set(this.azimuth.get());
        dupl.inclination.set(this.inclination.get());
        dupl.log10Distance.set(this.log10Distance.get());
        dupl.intensity.set(this.intensity.get());
        dupl.spotSize.set(this.spotSize.get());
        dupl.spotTaper.set(this.spotTaper.get());
        dupl.locked.set(this.locked.get());
        dupl.color.setValue(this.color.getValue());
        return dupl;
    }

    @Override
    public final String toString()
    {
        return name.getValue();
    }

    @Override
    public final double getTargetX()
    {
        return targetX.get();
    }

    @Override
    public final double getTargetY()
    {
        return targetY.get();
    }

    @Override
    public final double getTargetZ()
    {
        return targetZ.get();
    }

    @Override
    public final double getAzimuth()
    {
        return azimuth.get();
    }

    @Override
    public final double getInclination()
    {
        return inclination.get();
    }

    @Override
    public final double getLog10Distance()
    {
        return log10Distance.get();
    }

    @Override
    public final double getIntensity()
    {
        return intensity.get();
    }

    @Override
    public final boolean isLocked()
    {
        return locked.get();
    }

    @Override
    public final String getName()
    {
        return name.get();
    }

    @Override
    public final double getSpotSize()
    {
        return spotSize.get();
    }

    @Override
    public final double getSpotTaper()
    {
        return spotTaper.get();
    }

    @Override
    public final Color getColor()
    {
        return color.getValue();
    }

    @Override
    public final void setTargetX(double value)
    {
        this.targetX.set(value);
    }

    @Override
    public final void setTargetY(double value)
    {
        this.targetY.set(value);
    }

    @Override
    public final void setTargetZ(double value)
    {
        this.targetZ.set(value);
    }

    @Override
    public final void setAzimuth(double value)
    {
        this.azimuth.set(value);
    }

    @Override
    public final void setInclination(double value)
    {
        this.inclination.set(value);
    }

    @Override
    public final void setLog10Distance(double value)
    {
        this.log10Distance.set(value);
    }

    @Override
    public final void setIntensity(double value)
    {
        this.intensity.set(value);
    }

    @Override
    public final void setLocked(boolean value)
    {
        this.locked.set(value);
    }

    @Override
    public final void setName(String value)
    {
        this.name.set(value);
    }

    @Override
    public final void setSpotSize(double value)
    {
        this.spotSize.set(value);
    }

    @Override
    public final void setSpotTaper(double value)
    {
        this.spotTaper.set(value);
    }

    @Override
    public final void setColor(Color value)
    {
        this.color.setValue(value);
    }

    public final DoubleProperty targetX()
    {
        return targetX;
    }

    public final DoubleProperty targetY()
    {
        return targetY;
    }

    public final DoubleProperty targetZ()
    {
        return targetZ;
    }

    public final DoubleProperty azimuth()
    {
        return azimuth;
    }

    public final DoubleProperty inclination()
    {
        return inclination;
    }

    public final DoubleProperty log10Distance()
    {
        return log10Distance;
    }

    public final DoubleProperty intensity()
    {
        return intensity;
    }

    public final BooleanProperty locked()
    {
        return locked;
    }

    public final StringProperty name()
    {
        return name;
    }

    public final Property<Color> color()
    {
        return color;
    }

    public final DoubleProperty spotSize()
    {
        return spotSize;
    }

    public final DoubleProperty spotTaper()
    {
        return spotTaper;
    }
}

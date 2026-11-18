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

package kintsugi3d.fx.controllers.scene.camera;

import javafx.beans.property.*;
import kintsugi3d.builder.state.project.SerializableCameraSettings;
import kintsugi3d.fx.util.StaticUtilities;

public class ObservableCameraSettings extends SerializableCameraSettings
{
    private final DoubleProperty xCenter = new SimpleDoubleProperty();
    private final DoubleProperty yCenter = new SimpleDoubleProperty();
    private final DoubleProperty zCenter = new SimpleDoubleProperty();
    private final DoubleProperty azimuth = StaticUtilities.wrapAround(-180, 180, new SimpleDoubleProperty());
    private final DoubleProperty inclination = StaticUtilities.clamp(-90, 90, new SimpleDoubleProperty());
    private final DoubleProperty log10Distance = new SimpleDoubleProperty(StrictMath.log10(25.0 / 18.0));
    private final DoubleProperty twist = StaticUtilities.wrapAround(-180.0, 180.0, new SimpleDoubleProperty());
    private final DoubleProperty fov = new SimpleDoubleProperty(
        360 / Math.PI /* convert and multiply by 2) */ * StrictMath.atan(0.36 /* "35 mm" film (actual 36mm horizontal), 50mm lens */));
    private final DoubleProperty focalLength = new SimpleDoubleProperty(50.0);
    private final BooleanProperty locked = new SimpleBooleanProperty();
    private final BooleanProperty orthographic = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty("New Camera");

    public ObservableCameraSettings()
    {
    }

    public ObservableCameraSettings(String name)
    {
        this.name.set(name);
    }

    @Override
    public final String toString()
    {
        if (locked.getValue())
        {
            return String.format("(L) %s", name.getValue());
        }
        else
        {
            return name.getValue();
        }
    }

    public final ObservableCameraSettings duplicate()
    {
        ObservableCameraSettings newCamera = new ObservableCameraSettings();
        newCamera.setXCenter(xCenter.getValue());
        newCamera.setYCenter(yCenter.getValue());
        newCamera.setZCenter(zCenter.getValue());
        newCamera.setAzimuth(azimuth.getValue());
        newCamera.setInclination(inclination.getValue());
        newCamera.setLog10Distance(log10Distance.getValue());
        newCamera.setTwist(twist.getValue());
        newCamera.setFOV(fov.getValue());
        newCamera.setLocked(locked.getValue());
        newCamera.setOrthographic(orthographic.getValue());
        newCamera.setName(String.format("%s copy", name.getValue()));
        return newCamera;
    }

    @Override
    public final double getXCenter()
    {
        return xCenter.get();
    }

    public final DoubleProperty xCenterProperty()
    {
        return xCenter;
    }

    @Override
    public final void setXCenter(double xCenter)
    {
        this.xCenter.set(xCenter);
    }

    @Override
    public final double getYCenter()
    {
        return yCenter.get();
    }

    public final DoubleProperty yCenterProperty()
    {
        return yCenter;
    }

    @Override
    public final void setYCenter(double yCenter)
    {
        this.yCenter.set(yCenter);
    }

    @Override
    public final double getZCenter()
    {
        return zCenter.get();
    }

    public final DoubleProperty zCenterProperty()
    {
        return zCenter;
    }

    @Override
    public final void setZCenter(double zCenter)
    {
        this.zCenter.set(zCenter);
    }

    @Override
    public final double getAzimuth()
    {
        return azimuth.get();
    }

    public final DoubleProperty azimuthProperty()
    {
        return azimuth;
    }

    @Override
    public final void setAzimuth(double azimuth)
    {
        this.azimuth.set(azimuth);
    }

    @Override
    public final double getInclination()
    {
        return inclination.get();
    }

    public final DoubleProperty inclinationProperty()
    {
        return inclination;
    }

    @Override
    public final void setInclination(double inclination)
    {
        this.inclination.set(inclination);
    }

    @Override
    public final double getLog10Distance()
    {
        return log10Distance.get();
    }

    public final DoubleProperty log10DistanceProperty()
    {
        return log10Distance;
    }

    @Override
    public final void setLog10Distance(double log10distance)
    {
        log10Distance.set(log10distance);
    }

    @Override
    public final double getTwist()
    {
        return twist.get();
    }

    public final DoubleProperty twistProperty()
    {
        return twist;
    }

    @Override
    public final void setTwist(double twist)
    {
        this.twist.set(twist);
    }

    @Override
    public final double getFOV()
    {
        return fov.get();
    }

    public final DoubleProperty fovProperty()
    {
        return fov;
    }

    @Override
    public final void setFOV(double fOV)
    {
        fov.set(fOV);
    }

    @Override
    public final double getFocalLength()
    {
        return focalLength.get();
    }

    public final DoubleProperty focalLengthProperty()
    {
        return focalLength;
    }

    @Override
    public final void setFocalLength(double focalLength)
    {
        this.focalLength.set(focalLength);
    }

    @Override
    public final boolean isLocked()
    {
        return locked.get();
    }

    public final BooleanProperty lockedProperty()
    {
        return locked;
    }

    @Override
    public final void setLocked(boolean locked)
    {
        this.locked.set(locked);
    }

    @Override
    public final boolean isOrthographic()
    {
        return orthographic.get();
    }

    public final BooleanProperty orthographicProperty()
    {
        return orthographic;
    }

    @Override
    public final void setOrthographic(boolean orthographic)
    {
        this.orthographic.set(orthographic);
    }

    @Override
    public final String getName()
    {
        return name.get();
    }

    public final StringProperty nameProperty()
    {
        return name;
    }

    @Override
    public final void setName(String name)
    {
        this.name.set(name);
    }
}

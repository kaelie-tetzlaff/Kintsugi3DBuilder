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

package kintsugi3d.fx.controllers.scene.object;

import javafx.beans.property.*;
import kintsugi3d.builder.state.project.SerializableObjectPoseSettings;
import kintsugi3d.fx.util.StaticUtilities;

public class ObservableObjectPoseSettings extends SerializableObjectPoseSettings
{
    private final DoubleProperty centerX = new SimpleDoubleProperty();
    private final DoubleProperty centerY = new SimpleDoubleProperty();
    private final DoubleProperty centerZ = new SimpleDoubleProperty();
    private final DoubleProperty rotateY = StaticUtilities.wrapAround(-180, 180, new SimpleDoubleProperty());
    private final DoubleProperty rotateX = StaticUtilities.clamp(-90, 90, new SimpleDoubleProperty());
    private final DoubleProperty rotateZ = StaticUtilities.wrapAround(-180.0, 180.0, new SimpleDoubleProperty());
    private final BooleanProperty locked = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty("Default Pose");

    private final DoubleProperty scale = new SimpleDoubleProperty(1.0);

    public ObservableObjectPoseSettings()
    {
    }

    public ObservableObjectPoseSettings(String name)
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

    public final ObservableObjectPoseSettings duplicate()
    {
        ObservableObjectPoseSettings copy = new ObservableObjectPoseSettings();

        copy.setCenterX(this.getCenterX());
        copy.setCenterY(this.getCenterY());
        copy.setCenterZ(this.getCenterZ());

        copy.setRotateY(this.getRotateY());
        copy.setRotateX(this.getRotateX());
        copy.setRotateZ(this.getRotateZ());

        copy.setLocked(this.isLocked());
        copy.setScale(this.getScale());
        copy.setName(String.format("%s copy", this.getName()));

        return copy;
    }

    @Override
    public final double getCenterX()
    {
        return centerX.get();
    }

    public final DoubleProperty centerXProperty()
    {
        return centerX;
    }

    @Override
    public final void setCenterX(double centerX)
    {
        this.centerX.set(centerX);
    }

    @Override
    public final double getCenterY()
    {
        return centerY.get();
    }

    public final DoubleProperty centerYProperty()
    {
        return centerY;
    }

    @Override
    public final void setCenterY(double centerY)
    {
        this.centerY.set(centerY);
    }

    @Override
    public final double getCenterZ()
    {
        return centerZ.get();
    }

    public final DoubleProperty centerZProperty()
    {
        return centerZ;
    }

    @Override
    public final void setCenterZ(double centerZ)
    {
        this.centerZ.set(centerZ);
    }

    @Override
    public final double getRotateY()
    {
        return rotateY.get();
    }

    public final DoubleProperty rotateYProperty()
    {
        return rotateY;
    }

    @Override
    public final void setRotateY(double rotateY)
    {
        this.rotateY.set(rotateY);
    }

    @Override
    public final double getRotateX()
    {
        return rotateX.get();
    }

    public final DoubleProperty rotateXProperty()
    {
        return rotateX;
    }

    @Override
    public final void setRotateX(double rotateX)
    {
        this.rotateX.set(rotateX);
    }

    @Override
    public final double getRotateZ()
    {
        return rotateZ.get();
    }

    public final DoubleProperty rotateZProperty()
    {
        return rotateZ;
    }

    @Override
    public final void setRotateZ(double rotateZ)
    {
        this.rotateZ.set(rotateZ);
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

    public final DoubleProperty scaleProperty(){return scale;}

    @Override
    public final double getScale(){return StrictMath.pow(10, scale.get());}
        //scale.get() returns the slider value before it has been adjusted to a logarithmic scale

    @Override
    public final void setScale(double scale){this.scale.set(scale);}

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

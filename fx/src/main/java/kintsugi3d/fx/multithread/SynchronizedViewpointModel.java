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

package kintsugi3d.fx.multithread;

import kintsugi3d.builder.state.scene.ManipulableViewpointModel;
import kintsugi3d.builder.state.scene.ViewpointModelBase;
import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;

public class SynchronizedViewpointModel extends ViewpointModelBase
{
    private final ManipulableViewpointModel baseModel;

    private final @NotNull SynchronizedValue<Vector3> target;
    private final @NotNull SynchronizedValue<Float> azimuth;
    private final @NotNull SynchronizedValue<Float> inclination;
    private final @NotNull SynchronizedValue<Float> log10Distance;
    private final @NotNull SynchronizedValue<Float> twist;
    private final @NotNull SynchronizedValue<Float> horizontalFOV;
    private final @NotNull SynchronizedValue<Float> focalLength;
    private final @NotNull SynchronizedValue<Boolean> orthographic;

    public SynchronizedViewpointModel(@NotNull ManipulableViewpointModel baseModel)
    {
        this.baseModel = baseModel;
        target = SynchronizedValue.createFromFunctions(baseModel::getTarget, baseModel::setTarget);
        azimuth = SynchronizedValue.createFromFunctions(baseModel::getAzimuth, baseModel::setAzimuth);
        inclination = SynchronizedValue.createFromFunctions(baseModel::getInclination, baseModel::setInclination);
        log10Distance = SynchronizedValue.createFromFunctions(baseModel::getLog10Distance, baseModel::setLog10Distance);
        twist = SynchronizedValue.createFromFunctions(baseModel::getTwist, baseModel::setTwist);
        horizontalFOV = SynchronizedValue.createFromFunctions(baseModel::getHorizontalFOV, baseModel::setHorizontalFOV);
        focalLength = SynchronizedValue.createFromFunctions(baseModel::getFocalLength, baseModel::setFocalLength);
        orthographic = SynchronizedValue.createFromFunctions(baseModel::isOrthographic, baseModel::setOrthographic);
    }

    @Override
    public final boolean isLocked()
    {
        return baseModel.isLocked();
    }

    @Override
    public final Vector3 getTarget()
    {
        return target.getValue();
    }

    @Override
    public final void setTarget(Vector3 target)
    {
        this.target.setValue(target);
    }

    @Override
    public final boolean isOrthographic()
    {
        return orthographic.getValue();
    }

    @Override
    public final void setOrthographic(boolean orthographic)
    {
        this.orthographic.setValue(orthographic);
    }

    @Override
    public final float getHorizontalFOV()
    {
        return horizontalFOV.getValue();
    }

    @Override
    public final void setHorizontalFOV(float fov)
    {
        horizontalFOV.setValue(fov);
    }

    @Override
    public final float getLog10Distance()
    {
        return log10Distance.getValue();
    }

    @Override
    public final void setLog10Distance(float log10Distance)
    {
        this.log10Distance.setValue(log10Distance);
    }

    @Override
    public final float getTwist()
    {
        return twist.getValue();
    }

    @Override
    public final void setTwist(float twist)
    {
        this.twist.setValue(twist);
    }

    @Override
    public final float getAzimuth()
    {
        return azimuth.getValue();
    }

    @Override
    public final void setAzimuth(float azimuth)
    {
        this.azimuth.setValue(azimuth);
    }

    @Override
    public final float getInclination()
    {
        return inclination.getValue();
    }

    @Override
    public final void setInclination(float inclination)
    {
        this.inclination.setValue(inclination);
    }

    @Override
    public final float getFocalLength()
    {
        return focalLength.getValue();
    }

    @Override
    public final void setFocalLength(float focalLength)
    {
        this.focalLength.setValue(focalLength);
    }
}

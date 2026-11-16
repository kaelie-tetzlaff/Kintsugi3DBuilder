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

package kintsugi3d.builder.state.scene;

import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;

public abstract class DiscreteLightModelBase implements DiscreteLightModel
{
    private final ManipulableViewpointModel lightCameraModel;

    protected DiscreteLightModelBase(ManipulableViewpointModel lightCameraModel)
    {
        this.lightCameraModel = lightCameraModel;
    }

    @Override
    public final boolean isLocked()
    {
        return lightCameraModel.isLocked();
    }

    @Override
    public final Matrix4 getLookMatrix()
    {
        return lightCameraModel.getLookMatrix();
    }

    @Override
    public final void setLookMatrix(Matrix4 lookMatrix)
    {
        lightCameraModel.setLookMatrix(lookMatrix);
    }

    @Override
    public final Matrix4 getOrbit()
    {
        return lightCameraModel.getOrbit();
    }

    @Override
    public final void setOrbit(Matrix4 orbit)
    {
        lightCameraModel.setOrbit(orbit);
    }

    @Override
    public final float getDistance()
    {
        return lightCameraModel.getDistance();
    }

    @Override
    public final void setDistance(float distance)
    {
        lightCameraModel.setDistance(distance);
    }

    @Override
    public final Vector3 getTarget()
    {
        return lightCameraModel.getTarget();
    }

    @Override
    public final void setTarget(Vector3 target)
    {
        lightCameraModel.setTarget(target);
    }

    @Override
    public final float getAzimuth()
    {
        return lightCameraModel.getAzimuth();
    }

    @Override
    public final void setAzimuth(float azimuth)
    {
        lightCameraModel.setAzimuth(azimuth);
    }

    @Override
    public final float getInclination()
    {
        return lightCameraModel.getInclination();
    }

    @Override
    public final void setInclination(float inclination)
    {
        lightCameraModel.setInclination(inclination);
    }

    @Override
    public final float getLog10Distance()
    {
        return lightCameraModel.getLog10Distance();
    }

    @Override
    public final void setLog10Distance(float log10Distance)
    {
        lightCameraModel.setLog10Distance(log10Distance);
    }

    @Override
    public final float getTwist()
    {
        return lightCameraModel.getTwist();
    }

    @Override
    public final void setTwist(float twist)
    {
        lightCameraModel.setTwist(twist);
    }

    @Override
    public final float getHorizontalFOV()
    {
        return lightCameraModel.getHorizontalFOV();
    }

    @Override
    public final void setHorizontalFOV(float fov)
    {
        lightCameraModel.setHorizontalFOV(fov);
    }

    @Override
    public final float getFocalLength()
    {
        return lightCameraModel.getFocalLength();
    }

    @Override
    public final void setFocalLength(float focalLength)
    {
        lightCameraModel.setFocalLength(focalLength);
    }

    @Override
    public final boolean isOrthographic()
    {
        return lightCameraModel.isOrthographic();
    }

    @Override
    public final void setOrthographic(boolean orthographic)
    {
        lightCameraModel.setOrthographic(orthographic);
    }
}

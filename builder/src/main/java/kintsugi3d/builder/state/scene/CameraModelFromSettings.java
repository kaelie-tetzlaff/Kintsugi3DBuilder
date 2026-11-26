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

package kintsugi3d.builder.state.scene;

import kintsugi3d.builder.state.project.CameraSettings;
import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;

public abstract class CameraModelFromSettings extends ViewpointModelBase implements ManipulableCameraModel
{
    protected abstract CameraSettings getCameraSettings();

    @Override
    public final float getLog10Distance()
    {
        return (float) getCameraSettings().getLog10Distance();
    }

    @Override
    public final void setLog10Distance(float log10Distance)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setLog10Distance(log10Distance);
        }
    }

    @Override
    public final @NotNull Vector3 getTarget()
    {
        return new Vector3((float) getCameraSettings().getXCenter(),
            (float) getCameraSettings().getYCenter(),
            (float) getCameraSettings().getZCenter());
    }

    @Override
    public final void setTarget(@NotNull Vector3 target)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setXCenter(target.x);
            getCameraSettings().setYCenter(target.y);
            getCameraSettings().setZCenter(target.z);
        }
    }

    @Override
    public final float getTwist()
    {
        return (float) getCameraSettings().getTwist();
    }

    @Override
    public final void setTwist(float twist)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setTwist(twist);
        }
    }

    @Override
    public final float getAzimuth()
    {
        return (float) getCameraSettings().getAzimuth();
    }

    @Override
    public final void setAzimuth(float azimuth)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setAzimuth(azimuth);
        }
    }

    @Override
    public final float getInclination()
    {
        return (float) getCameraSettings().getInclination();
    }

    @Override
    public final void setInclination(float inclination)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setInclination(inclination);
        }
    }

    /**
     * this method is intended to return whether or not the selected camera is locked.
     * It is called by the render side of the program, and when it returns true
     * the camera should not be able to be changed using the tools in the render window.
     *
     * @return true for locked
     */
    @Override
    public final boolean isLocked()
    {
        return getCameraSettings().isLocked();
    }

    @Override
    public final float getHorizontalFOV()
    {
        return (float) (getCameraSettings().getFOV() * Math.PI / 180);
    }

    @Override
    public final void setHorizontalFOV(float fov)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setFOV(fov * 180 / Math.PI);
        }
    }

    @Override
    public final float getFocalLength()
    {
        return (float) getCameraSettings().getFocalLength();
    }

    @Override
    public final void setFocalLength(float focalLength)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setFocalLength(focalLength);
        }
    }

    @Override
    public final boolean isOrthographic()
    {
        return getCameraSettings().isOrthographic();
    }

    @Override
    public final void setOrthographic(boolean orthographic)
    {
        if (!getCameraSettings().isLocked())
        {
            getCameraSettings().setOrthographic(orthographic);
        }
    }
}

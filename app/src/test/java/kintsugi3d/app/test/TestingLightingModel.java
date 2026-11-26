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

package kintsugi3d.app.test;

import kintsugi3d.builder.state.scene.BackgroundMode;
import kintsugi3d.builder.state.scene.ReadonlyLightPrototypeModel;
import kintsugi3d.builder.state.scene.ReadonlyLightWidgetModel;
import kintsugi3d.builder.state.scene.ReadonlyLightingEnvironmentModel;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;

public class TestingLightingModel implements ReadonlyLightingEnvironmentModel
{
    @Override
    public final ReadonlyLightWidgetModel getLightWidgetModel(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int getLightCount()
    {
        return 0;
    }

    @Override
    public final int getMaxLightCount()
    {
        return 0;
    }

    @Override
    public final boolean isLightWidgetEnabled(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean areLightWidgetsEthereal()
    {
        return false;
    }

    @Override
    public final float getAmbientLightIntensity()
    {
        return 0;
    }

    @Override
    public final @NotNull Vector3 getAmbientLightColor()
    {
        return Vector3.ZERO;
    }

    @Override
    public final boolean isEnvironmentMappingEnabled()
    {
        return false;
    }

    @Override
    public final @NotNull Matrix4 getEnvironmentMapMatrix()
    {
        return Matrix4.IDENTITY;
    }

    @Override
    public final float getEnvironmentMapFilteringBias()
    {
        return 0;
    }

    @Override
    public final ReadonlyLightPrototypeModel getLightPrototype(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Matrix4 getLightMatrix(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Vector3 getLightCenter(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public final float getBackgroundIntensity()
    {
        return 0;
    }

    @Override
    public final @NotNull Vector3 getBackgroundColor()
    {
        return Vector3.ZERO;
    }

    @Override
    public final @NotNull BackgroundMode getBackgroundMode()
    {
        return BackgroundMode.NONE;
    }

    @Override
    public final @NotNull Vector3 getGroundPlaneColor()
    {
        return Vector3.ZERO;
    }

    @Override
    public final boolean isGroundPlaneEnabled()
    {
        return false;
    }

    @Override
    public final float getGroundPlaneHeight()
    {
        return 0;
    }

    @Override
    public final float getGroundPlaneSize()
    {
        return 0;
    }
}

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

package kintsugi3d.fx.multithread;

import kintsugi3d.builder.state.scene.BackgroundMode;
import kintsugi3d.builder.state.scene.EnvironmentModel;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;

public class SynchronizedEnvironmentModel implements EnvironmentModel
{
    private final EnvironmentModel baseModel;
    private final @NotNull SynchronizedValue<Float> environmentRotation;
    private final @NotNull SynchronizedValue<Float> environmentIntensity;
    private final @NotNull SynchronizedValue<Float> backgroundIntensity;

    public SynchronizedEnvironmentModel(@NotNull EnvironmentModel baseModel)
    {
        this.baseModel = baseModel;
        environmentRotation = SynchronizedValue.createFromFunctions(baseModel::getEnvironmentRotation, baseModel::setEnvironmentRotation);
        environmentIntensity = SynchronizedValue.createFromFunctions(baseModel::getEnvironmentIntensity, baseModel::setEnvironmentIntensity);
        backgroundIntensity = SynchronizedValue.createFromFunctions(baseModel::getBackgroundIntensity, baseModel::setBackgroundIntensity);
    }

    @Override
    public final boolean isEnabled()
    {
        return baseModel.isEnabled();
    }

    @Override
    public final boolean isEnvironmentMappingEnabled()
    {
        return baseModel.isEnvironmentMappingEnabled();
    }

    @Override
    public final Vector3 getEnvironmentColor()
    {
        return baseModel.getEnvironmentColor();
    }

    @Override
    public final Matrix4 getEnvironmentMapMatrix()
    {
        return baseModel.getEnvironmentMapMatrix();
    }

    @Override
    public final float getEnvironmentMapFilteringBias()
    {
        return baseModel.getEnvironmentMapFilteringBias();
    }

    @Override
    public final BackgroundMode getBackgroundMode()
    {
        return baseModel.getBackgroundMode();
    }

    @Override
    public final Vector3 getBackgroundColor()
    {
        return baseModel.getBackgroundColor();
    }

    @Override
    public final float getEnvironmentRotation()
    {
        return environmentRotation.getValue();
    }

    @Override
    public final float getEnvironmentIntensity()
    {
        return environmentIntensity.getValue();
    }

    @Override
    public final float getBackgroundIntensity()
    {
        return backgroundIntensity.getValue();
    }

    @Override
    public final void setEnvironmentRotation(float environmentRotation)
    {
        this.environmentRotation.setValue(environmentRotation);
    }

    @Override
    public final void setEnvironmentIntensity(float environmentIntensity)
    {
        this.environmentIntensity.setValue(environmentIntensity);
    }

    @Override
    public final void setBackgroundIntensity(float backgroundIntensity)
    {
        this.backgroundIntensity.setValue(backgroundIntensity);
    }

    @Override
    public final boolean isGroundPlaneEnabled()
    {
        return baseModel.isGroundPlaneEnabled();
    }

    @Override
    public final Vector3 getGroundPlaneColor()
    {
        return baseModel.getGroundPlaneColor();
    }

    @Override
    public final float getGroundPlaneHeight()
    {
        return baseModel.getGroundPlaneHeight();
    }

    @Override
    public final float getGroundPlaneSize()
    {
        return baseModel.getGroundPlaneSize();
    }
}

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

import kintsugi3d.builder.state.scene.ManipulableObjectPoseModel;
import kintsugi3d.builder.state.scene.ObjectPoseModelBase;
import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;

public class SynchronizedObjectPoseModel extends ObjectPoseModelBase
{
    private final ManipulableObjectPoseModel baseModel;
    private final @NotNull SynchronizedValue<Vector3> center;
    private final @NotNull SynchronizedValue<Float> rotationX;
    private final @NotNull SynchronizedValue<Float> rotationY;
    private final @NotNull SynchronizedValue<Float> rotationZ;

    private final @NotNull SynchronizedValue<Float> scale;

    public SynchronizedObjectPoseModel(@NotNull ManipulableObjectPoseModel baseModel)
    {
        this.baseModel = baseModel;
        center = SynchronizedValue.createFromFunctions(baseModel::getCenter, baseModel::setCenter);
        rotationX = SynchronizedValue.createFromFunctions(baseModel::getRotationX, baseModel::setRotationX);
        rotationY = SynchronizedValue.createFromFunctions(baseModel::getRotationY, baseModel::setRotationY);
        rotationZ = SynchronizedValue.createFromFunctions(baseModel::getRotationZ, baseModel::setRotationZ);
        scale = SynchronizedValue.createFromFunctions(baseModel::getScale, baseModel::setScale);
    }

    @Override
    public final boolean isLocked()
    {
        return baseModel.isLocked();
    }

    @Override
    public final Vector3 getCenter()
    {
        return center.getValue();
    }

    @Override
    public final float getRotationZ()
    {
        return rotationZ.getValue();
    }

    @Override
    public final float getRotationY()
    {
        return rotationY.getValue();
    }

    @Override
    public final float getRotationX()
    {
        return rotationX.getValue();
    }

    @Override
    public final float getScale(){return scale.getValue();}

    @Override
    public final void setCenter(Vector3 center)
    {
        this.center.setValue(center);
    }

    @Override
    public final void setRotationZ(float rotationZ)
    {
        this.rotationZ.setValue(rotationZ);
    }

    @Override
    public final void setRotationY(float rotationY)
    {
        this.rotationY.setValue(rotationY);
    }

    @Override
    public final void setRotationX(float rotationX)
    {
        this.rotationX.setValue(rotationX);
    }

    @Override
    public final void setScale(float scale){this.scale.setValue(scale);}
}

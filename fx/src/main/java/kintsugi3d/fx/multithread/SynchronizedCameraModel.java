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

import kintsugi3d.builder.state.scene.ManipulableCameraModel;

public class SynchronizedCameraModel extends SynchronizedViewpointModel implements ManipulableCameraModel
{
    private final SynchronizedValue<Integer> paddingLeft;
    private final SynchronizedValue<Integer> paddingRight;
    private final SynchronizedValue<Integer> paddingTop;
    private final SynchronizedValue<Integer> paddingBottom;

    public SynchronizedCameraModel(ManipulableCameraModel baseModel)
    {
        super(baseModel);
        this.paddingLeft = SynchronizedValue.createFromFunctions(baseModel::getPaddingLeft, baseModel::setPaddingLeft);
        this.paddingRight = SynchronizedValue.createFromFunctions(baseModel::getPaddingRight, baseModel::setPaddingRight);
        this.paddingTop = SynchronizedValue.createFromFunctions(baseModel::getPaddingTop, baseModel::setPaddingTop);
        this.paddingBottom = SynchronizedValue.createFromFunctions(baseModel::getPaddingBottom, baseModel::setPaddingBottom);
    }

    @Override
    public void setPaddingLeft(int paddingLeft)
    {
        this.paddingLeft.setValue(paddingLeft);
    }

    @Override
    public void setPaddingRight(int paddingRight)
    {
        this.paddingRight.setValue(paddingRight);
    }

    @Override
    public void setPaddingTop(int paddingTop)
    {
        this.paddingTop.setValue(paddingTop);
    }

    @Override
    public void setPaddingBottom(int paddingBottom)
    {
        this.paddingBottom.setValue(paddingBottom);
    }
}

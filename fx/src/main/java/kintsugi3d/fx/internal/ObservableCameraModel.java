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

package kintsugi3d.fx.internal;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import kintsugi3d.builder.state.project.SerializableCameraSettings;
import kintsugi3d.builder.state.scene.CameraModelFromSettings;
import kintsugi3d.fx.controllers.scene.camera.ObservableCameraSettings;

public class ObservableCameraModel extends CameraModelFromSettings
{
    private ObservableValue<ObservableCameraSettings> selectedCameraSetting;
    private final SerializableCameraSettings sentinel = new ObservableCameraSettings("sentinel");

    private final IntegerProperty paddingLeft = new SimpleIntegerProperty(0);
    private final IntegerProperty paddingRight = new SimpleIntegerProperty(0);
    private final IntegerProperty paddingTop = new SimpleIntegerProperty(0);
    private final IntegerProperty paddingBottom = new SimpleIntegerProperty(0);

    public ObservableCameraModel()
    {
        sentinel.setLocked(true);
    }

    public final void setSelectedCameraSetting(ObservableValue<ObservableCameraSettings> selectedCameraSetting)
    {
        this.selectedCameraSetting = selectedCameraSetting;
    }

    @Override
    protected final SerializableCameraSettings getCameraSettings()
    {
        if (selectedCameraSetting == null || selectedCameraSetting.getValue() == null)
        {
            return sentinel;
        }
        else
        {
            return selectedCameraSetting.getValue();
        }
    }

    @Override
    public final int getPaddingLeft()
    {
        return paddingLeft.get();
    }

    public final IntegerProperty paddingLeftProperty()
    {
        return paddingLeft;
    }

    @Override
    public final int getPaddingRight()
    {
        return paddingRight.get();
    }

    public final IntegerProperty paddingRightProperty()
    {
        return paddingRight;
    }

    @Override
    public final int getPaddingTop()
    {
        return paddingTop.get();
    }

    public final IntegerProperty paddingTopProperty()
    {
        return paddingTop;
    }

    @Override
    public final int getPaddingBottom()
    {
        return paddingBottom.get();
    }

    public final IntegerProperty paddingBottomProperty()
    {
        return paddingBottom;
    }

    @Override
    public final void setPaddingLeft(int paddingLeft)
    {
        this.paddingLeft.set(paddingLeft);
    }

    @Override
    public final void setPaddingRight(int paddingRight)
    {
        this.paddingRight.set(paddingRight);
    }

    @Override
    public final void setPaddingTop(int paddingTop)
    {
        this.paddingTop.set(paddingTop);
    }

    @Override
    public final void setPaddingBottom(int paddingBottom)
    {
        this.paddingBottom.set(paddingBottom);
    }
}

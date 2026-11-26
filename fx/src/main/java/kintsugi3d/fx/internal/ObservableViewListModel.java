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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.state.SelectableViewListModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ObservableViewListModel implements SelectableViewListModel
{
    private MultipleSelectionModel<View> selectedCameraViewModel;
    private Property<ObservableList<View>> cameraViewListProperty;
    private BooleanProperty cameraViewSnapEnabledProperty;

    @Override
    public final View getSelectedView()
    {
        return selectedCameraViewModel.getSelectedItem();
    }

    @Override
    public final void setSelectedView(View cameraView)
    {
        selectedCameraViewModel.select(cameraView);
    }

    @Override
    public final @NotNull List<View> getViewList()
    {
        return Collections.unmodifiableList(cameraViewListProperty.getValue());
    }

    @Override
    public final void setViewList(@NotNull List<View> cameraViewList)
    {
        cameraViewListProperty.setValue(FXCollections.observableList(cameraViewList));
    }

    @Override
    public final boolean isViewSnapEnabled()
    {
        return cameraViewSnapEnabledProperty.get();
    }

    @Override
    public final void setViewSnapEnabled(boolean cameraViewSnapEnabled)
    {
        cameraViewSnapEnabledProperty.set(cameraViewSnapEnabled);
    }

    public final BooleanProperty getCameraViewSnapEnabledProperty()
    {
        return cameraViewSnapEnabledProperty;
    }

    public final MultipleSelectionModel<View> getSelectedCameraViewModel()
    {
        return selectedCameraViewModel;
    }

    public final Property<ObservableList<View>> getCameraViewListProperty()
    {
        return cameraViewListProperty;
    }

    public final void setSelectedCameraViewModel(MultipleSelectionModel<View> selectedCameraViewModel)
    {
        this.selectedCameraViewModel = selectedCameraViewModel;
    }

    public final void setCameraViewListProperty(Property<ObservableList<View>> cameraViewListProperty)
    {
        this.cameraViewListProperty = cameraViewListProperty;
    }

    public final void setCameraViewSnapEnabledProperty(BooleanProperty cameraViewSnapEnabledProperty)
    {
        this.cameraViewSnapEnabledProperty = cameraViewSnapEnabledProperty;
    }
}

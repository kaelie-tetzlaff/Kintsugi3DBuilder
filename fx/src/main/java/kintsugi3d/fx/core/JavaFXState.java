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

package kintsugi3d.fx.core;

import kintsugi3d.fx.internal.*;
import org.jetbrains.annotations.NotNull;

public final class JavaFXState
{
    private final @NotNull ObservableCameraModel cameraModel;
    private final @NotNull ObservableEnvironmentModel environmentModel;
    private final @NotNull ObservableLightingEnvironmentModel lightingModel;
    private final @NotNull ObservableObjectPoseModel objectModel;
    private final @NotNull ObservableActiveShaderModel userShaderModel;
    private final @NotNull ObservableViewListModel cameraViewListModel;
    private final @NotNull ObservableLoadOptionsModel loadOptionsModel;
    private final @NotNull ObservableGeneralSettingsModel settingsModel;
    private final @NotNull ObservableProjectModel projectModel;
    private final @NotNull ObservableTabsModel tabModels;
    private final @NotNull ObservableCarouselModel carouselModel;
    private final @NotNull ObservableCacheModel cacheModel;

    private JavaFXState()
    {
        cameraModel = new ObservableCameraModel();
        environmentModel = new ObservableEnvironmentModel();
        objectModel = new ObservableObjectPoseModel();
        lightingModel = new ObservableLightingEnvironmentModel(environmentModel);
        userShaderModel = new ObservableActiveShaderModel();
        cameraViewListModel = new ObservableViewListModel();
        loadOptionsModel = new ObservableLoadOptionsModel();
        settingsModel = new ObservableGeneralSettingsModel();
        projectModel = new ObservableProjectModel();
        carouselModel = new ObservableCarouselModel();
        tabModels = new ObservableTabsModel(carouselModel);
        cacheModel = new ObservableCacheModel();
    }

    public static @NotNull JavaFXState create()
    {
        return new JavaFXState();
    }

    public @NotNull ObservableCameraModel getCameraModel()
    {
        return cameraModel;
    }

    public @NotNull ObservableLightingEnvironmentModel getLightingModel()
    {
        return lightingModel;
    }

    public @NotNull ObservableObjectPoseModel getObjectModel()
    {
        return objectModel;
    }

    public @NotNull ObservableActiveShaderModel getUserShaderModel()
    {
        return userShaderModel;
    }

    public @NotNull ObservableViewListModel getCameraViewListModel()
    {
        return cameraViewListModel;
    }

    public @NotNull ObservableTabsModel getTabModels()
    {
        return tabModels;
    }

    public @NotNull ObservableLoadOptionsModel getLoadOptionsModel()
    {
        return loadOptionsModel;
    }

    public @NotNull ObservableGeneralSettingsModel getSettingsModel()
    {
        return settingsModel;
    }

    public @NotNull ObservableEnvironmentModel getEnvironmentModel()
    {
        return environmentModel;
    }

    public @NotNull ObservableProjectModel getProjectModel()
    {
        return projectModel;
    }

    public @NotNull ObservableCarouselModel getCarouselModel()
    {
        return carouselModel;
    }

    public @NotNull ObservableCacheModel getCacheModel()
    {
        return cacheModel;
    }
}

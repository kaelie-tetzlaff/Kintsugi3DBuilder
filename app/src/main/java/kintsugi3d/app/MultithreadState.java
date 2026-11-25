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

package kintsugi3d.app;

import kintsugi3d.builder.core.Kintsugi3DBuilderState;
import kintsugi3d.builder.io.LoadOptionsModel;
import kintsugi3d.builder.state.CacheModel;
import kintsugi3d.builder.state.CarouselModel;
import kintsugi3d.builder.state.SelectableViewListModel;
import kintsugi3d.builder.state.cards.TabsModel;
import kintsugi3d.builder.state.project.ProjectModel;
import kintsugi3d.builder.state.scene.*;
import kintsugi3d.builder.state.settings.GeneralSettingsModel;
import kintsugi3d.builder.state.shader.ActiveShaderModel;
import kintsugi3d.fx.core.JavaFXState;
import kintsugi3d.fx.multithread.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MultithreadState implements Kintsugi3DBuilderState
{
    private final @NotNull ManipulableCameraModel cameraModel;
    private final @NotNull ManipulableLightingEnvironmentModel lightingModel;
    private final @NotNull ManipulableObjectPoseModel objectModel;
    private final @NotNull ActiveShaderModel activeShaderModel;
    private final @NotNull SelectableViewListModel viewListModel;
    private final @NotNull ProjectModel projectModel;
    private final @NotNull CarouselModel carouselModel;

    private final @NotNull GeneralSettingsModel settingsModel;
    private final @NotNull LoadOptionsModel loadOptionsModel;
    private final @NotNull CacheModel cacheModel;

    private final @NotNull TabsModel tabsModel;

    private static MultithreadState instance;

    // TODO make private
    public static @Nullable MultithreadState getInstance()
    {
        if (instance == null)
        {
            if (isInitialized())
            {
                instance = new MultithreadState(JavaFXApplication.getState());
            }
        }
        //noinspection StaticVariableUsedBeforeInitialization
        return instance;
    }

    public static boolean isInitialized()
    {
        return JavaFXApplication.isInitialized();
    }

    private MultithreadState(@NotNull JavaFXState base)
    {
        cameraModel = new SynchronizedCameraModel(base.getCameraModel());
        objectModel = new SynchronizedObjectPoseModel(base.getObjectModel());
        lightingModel = new SynchronizedLightingEnvironmentModel(base.getLightingModel());
        activeShaderModel = new SynchronizedActiveShaderModel(base.getUserShaderModel());
        viewListModel = new SynchronizedViewListModel(base.getCameraViewListModel());
        projectModel = new SynchronizedProjectModel(base.getProjectModel());
        settingsModel = new SynchronizedGeneralSettingsModel(base.getSettingsModel());
        tabsModel = new SynchronizedTabsModel(base.getTabModels());
        carouselModel = new SynchronizedCarouselModel(base.getCarouselModel());

        // All methods are either read-only or inherently asynchronous, so no multithread wrapping needed.
        // (This might not be 100% true with Java's memory model but it wouldn't be improved with the
        // "SynchronizedValue" framework which only ensures that writes happen on the JavaFX thread.
        // If more than that is needed, than we probably need to rework the synchronization for all models.
        // In practice, this hasn't proven to be necessary.)
        cacheModel = base.getCacheModel();

        loadOptionsModel = new SynchronizedLoadOptionsModel(base.getLoadOptionsModel());
    }

    @Override
    public @NotNull ManipulableCameraModel getCameraModel()
    {
        return cameraModel;
    }

    @Override
    public @NotNull ManipulableLightingEnvironmentModel getLightingModel()
    {
        return lightingModel;
    }

    @Override
    public @NotNull ManipulableObjectPoseModel getObjectModel()
    {
        return objectModel;
    }

    @Override
    public @NotNull ActiveShaderModel getUserShaderModel()
    {
        return activeShaderModel;
    }

    @Override
    public @NotNull SelectableViewListModel getViewListModel()
    {
        return viewListModel;
    }

    @Override
    public @NotNull TabsModel getTabModels() {
        return tabsModel;
    }

    @Override
    public @NotNull GeneralSettingsModel getSettingsModel()
    {
        return settingsModel;
    }

    @Override
    public @NotNull ProjectModel getProjectModel()
    {
        return projectModel;
    }

    @Override
    public @NotNull CarouselModel getCarouselModel()
    {
        return carouselModel;
    }

    @Override
    public @NotNull CacheModel getCacheModel()
    {
        return cacheModel;
    }

    public @NotNull LoadOptionsModel getLoadOptionsModel()
    {
        return loadOptionsModel;
    }
}

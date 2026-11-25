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

package kintsugi3d.fx.controllers.modals.viewselect;

import kintsugi3d.builder.core.Global;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.io.imageset.GenericImageSetInfo;
import kintsugi3d.builder.io.imageset.ImageSetInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class CurrentProjectViewSelectable extends ViewSelectableBase
{
    private final View initialView;
    private final double initialViewRotationDegrees;
    private final @NotNull ImageSetInfo imageSetInfo;

    public CurrentProjectViewSelectable(@NotNull Function<ViewSet, View> getInitialSelection, @Nullable ToDoubleFunction<ViewSet> getInitialRotationDegrees)
    {
        ViewSet currentViewSet = Global.io().getLoadedViewSetOrThrow();
        initialView = getInitialSelection.apply(currentViewSet);

        if (getInitialRotationDegrees != null)
        {
            initialViewRotationDegrees = getInitialRotationDegrees.applyAsDouble(currentViewSet);
        }
        else
        {
            initialViewRotationDegrees = 0.0;
        }

        if (initialView != null)
        {
            String viewName = initialView.getImageFile().getPath();
            selectView(viewName, initialViewRotationDegrees);
        }

        imageSetInfo = new GenericImageSetInfo("Current Project", currentViewSet);
    }

    public CurrentProjectViewSelectable(@NotNull Function<ViewSet, View> getInitialSelection)
    {
        this(getInitialSelection, null);
    }

    public final View getInitialView()
    {
        return initialView;
    }

    public final double getInitialViewRotationDegrees()
    {
        return initialViewRotationDegrees;
    }

    @Override
    public final @Nullable String getAdvanceLabelOverride()
    {
        // No override
        return null;
    }

    @Override
    public final ImageSetInfo getImageSetInfo()
    {
        return imageSetInfo;
    }

    @Override
    public final boolean confirm()
    {
        // Will be handled by the controller itself if a project is already loaded.
        return true;
    }
}

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
import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.fx.util.StaticUtilities;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the PrimaryViewSelector, which is now used as the orientation view selector
 */
public class OrientationViewSelectController extends ViewSelectController
{
    private static final Logger LOG = LoggerFactory.getLogger(OrientationViewSelectController.class);

    @Override
    public final void initCustom()
    {
        setCanConfirm(true);
    }

    private static void noDataWarning()
    {
        LOG.warn("View selection controller has no data.");
    }


    @Override
    public final boolean tryAdvance()
    {
        ViewSelectable data = getData();
        if (data != null)
        {
            data.selectView(getSelectedViewName(), getPrimaryViewRotation());
            return true;
        }
        else
        {
            noDataWarning();
            return false;
        }
    }

    @Override
    public final boolean tryConfirm()
    {
        // If a view set was already loaded, apply changes.
        ViewSet currentViewSet = Global.io().getLoadedViewSetOrNull();
        ViewSelectable data = getData();
        if (data != null)
        {
            if (currentViewSet != null)
            {
                if (data.getViewSelection() == null)
                {
                    currentViewSet.setOrientationView(null);
                }
                else
                {
                    currentViewSet.setOrientationViewByName(data.getViewSelection());
                }

                currentViewSet.setOrientationViewRotationDegrees(data.getViewRotation());
            }

            // The input source will handle loading if a view set wasn't already loaded.
            return data.confirm();
        }
        else
        {
            noDataWarning();
            return false;
        }
    }

    @Override
    public final boolean tryCancel()
    {
        return StaticUtilities.confirmCancel();
    }

    @Override
    protected final @NotNull String getHintText()
    {
        return "Select a photo to use to orient the object:";
    }

    @Override
    protected final boolean canRotateView()
    {
        return true;
    }

    @Override
    protected final boolean canSelectNullView()
    {
        return true;
    }

}
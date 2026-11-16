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

package kintsugi3d.builder.state.scene;

public class LightWidgetModel implements ReadonlyLightWidgetModel
{
    private boolean azimuthWidgetVisible = true;
    private boolean azimuthWidgetSelected = false;
    private boolean inclinationWidgetVisible = true;
    private boolean inclinationWidgetSelected = false;
    private boolean distanceWidgetVisible = true;
    private boolean distanceWidgetSelected = false;
    private boolean centerWidgetVisible = true;
    private boolean centerWidgetSelected = false;

    @Override
    public final boolean isAzimuthWidgetVisible()
    {
        return azimuthWidgetVisible;
    }

    @Override
    public final boolean isAzimuthWidgetSelected()
    {
        return azimuthWidgetSelected;
    }

    @Override
    public final boolean isInclinationWidgetVisible()
    {
        return inclinationWidgetVisible;
    }

    @Override
    public final boolean isInclinationWidgetSelected()
    {
        return inclinationWidgetSelected;
    }

    @Override
    public final boolean isDistanceWidgetVisible()
    {
        return distanceWidgetVisible;
    }

    @Override
    public final boolean isDistanceWidgetSelected()
    {
        return distanceWidgetSelected;
    }

    @Override
    public final boolean isCenterWidgetVisible()
    {
        return centerWidgetVisible;
    }

    @Override
    public final boolean isCenterWidgetSelected()
    {
        return centerWidgetSelected;
    }

    public final void setAzimuthWidgetVisible(boolean azimuthWidgetVisible)
    {
        this.azimuthWidgetVisible = azimuthWidgetVisible;
    }

    public final void setAzimuthWidgetSelected(boolean azimuthWidgetSelected)
    {
        this.azimuthWidgetSelected = azimuthWidgetSelected;
    }

    public final void setInclinationWidgetVisible(boolean inclinationWidgetVisible)
    {
        this.inclinationWidgetVisible = inclinationWidgetVisible;
    }

    public final void setInclinationWidgetSelected(boolean inclinationWidgetSelected)
    {
        this.inclinationWidgetSelected = inclinationWidgetSelected;
    }

    public final void setDistanceWidgetVisible(boolean distanceWidgetVisible)
    {
        this.distanceWidgetVisible = distanceWidgetVisible;
    }

    public final void setDistanceWidgetSelected(boolean distanceWidgetSelected)
    {
        this.distanceWidgetSelected = distanceWidgetSelected;
    }

    public final void setCenterWidgetVisible(boolean centerWidgetVisible)
    {
        this.centerWidgetVisible = centerWidgetVisible;
    }

    public final void setCenterWidgetSelected(boolean centerWidgetSelected)
    {
        this.centerWidgetSelected = centerWidgetSelected;
    }
}

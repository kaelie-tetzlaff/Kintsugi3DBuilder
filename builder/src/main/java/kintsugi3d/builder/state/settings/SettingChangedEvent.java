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

package kintsugi3d.builder.state.settings;

/**
 * An event describing a change to a single setting within a {@link GeneralSettingsModel}.
 */
public class SettingChangedEvent
{
    private final GeneralSettingsModel source;
    private final String settingName;
    private final Object newValue;

    public SettingChangedEvent(GeneralSettingsModel source, String settingName, Object newValue)
    {
        this.source = source;
        this.settingName = settingName;
        this.newValue = newValue;
    }

    /**
     * The settings model in which the setting was changed.
     */
    public GeneralSettingsModel getSource()
    {
        return source;
    }

    public String getSettingName()
    {
        return settingName;
    }

    /**
     * The value that was set. Note that this is not guaranteed to be different from the previous value.
     */
    public Object getNewValue()
    {
        return newValue;
    }
}

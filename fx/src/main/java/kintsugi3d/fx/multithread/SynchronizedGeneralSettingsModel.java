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

import javafx.beans.value.WritableValue;
import kintsugi3d.builder.state.settings.GeneralSettingsModel;
import kintsugi3d.builder.state.settings.GeneralSettingsModelBase;
import kintsugi3d.builder.state.settings.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SynchronizedGeneralSettingsModel extends GeneralSettingsModelBase
{
    private final Map<String, SynchronizedValue<Object>> settings = new HashMap<>(32);
    private final GeneralSettingsModel baseModel;

    public SynchronizedGeneralSettingsModel(GeneralSettingsModel baseModel)
    {
        this.baseModel = baseModel;
    }

    private WritableValue<Object> initSetting(String name)
    {
        SynchronizedValue<Object> synchronizedValue =
            SynchronizedValue.createFromFunctions(() -> baseModel.getObject(name),  value -> baseModel.set(name, value));
        settings.put(name, synchronizedValue);
        return synchronizedValue;
    }

    @Override
    protected final Object getUnchecked(String name)
    {
        if (settings.containsKey(name))
        {
            return settings.get(name).getValue();
        }
        else
        {
            return initSetting(name).getValue();
        }
    }

    @Override
    protected final void setUnchecked(String name, Object value)
    {
        if (settings.containsKey(name))
        {
            settings.get(name).setValue(value);
        }
        else
        {
            initSetting(name).setValue(value);
        }
    }

    @Override
    public final Class<?> getType(String name)
    {
        return baseModel.getType(name);
    }

    @Override
    public final boolean exists(String name)
    {
        return baseModel.exists(name);
    }

    @Override
    public final boolean shouldSerialize(String name)
    {
        if (exists(name))
        {
            return baseModel.shouldSerialize(name);
        }

        return false;
    }

    private Setting getSetting(String settingName)
    {
        return new GeneralSetting(settingName, getType(settingName), getObject(settingName), shouldSerialize(settingName));
    }

    @Override
    public final @NotNull Iterator<Setting> iterator()
    {
        // Before creating iterator, init all settings in the base model
        for (Setting setting : baseModel)
        {
            initSetting(setting.getName());
        }

        return new SynchronizedGeneralSettingsIterator(this);
    }

    @Override
    public final void createSetting(String name, Class<?> type, Object initialValue, boolean serialize)
    {
        baseModel.createSetting(name, type, initialValue, serialize);
        initSetting(name);
    }

    private static class SynchronizedGeneralSettingsIterator implements Iterator<Setting>
    {
        private final SynchronizedGeneralSettingsModel settings;
        private final Iterator<String> base;

        SynchronizedGeneralSettingsIterator(SynchronizedGeneralSettingsModel settingsModel)
        {
            settings = settingsModel;
            base = settingsModel.settings.keySet().iterator();
        }

        @Override
        public final boolean hasNext()
        {
            return base.hasNext();
        }

        @Override
        public final Setting next()
        {
            return settings.getSetting(base.next());
        }
    }
}

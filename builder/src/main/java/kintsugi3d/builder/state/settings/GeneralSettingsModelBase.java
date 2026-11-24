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

import kintsugi3d.builder.util.events.EventDispatcher;

import java.util.Collection;
import java.util.NoSuchElementException;

public abstract class GeneralSettingsModelBase implements GeneralSettingsModel
{
    private final EventDispatcher<SettingChangedListener, SettingChangedEvent> settingChanged
        = new EventDispatcher<>(SettingChangedListener::onSettingChanged);

    protected abstract Object getUnchecked(String name);
    protected abstract void setUnchecked(String name, Object value);

    @Override
    public final EventListeners<SettingChangedListener> settingChangedListeners()
    {
        return settingChanged;
    }

    @Override
    public final Object getObject(String name)
    {
        if (exists(name))
        {
            return getUnchecked(name);
        }
        else
        {
            throw new NoSuchElementException("No setting called \"" + name + "\" exists");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T get(String name, Class<T> settingType)
    {
        if (exists(name))
        {
            Object value = getUnchecked(name);
            if (settingType.isAssignableFrom(value.getClass()))
            {
                return (T) value;
            }
        }

        throw new NoSuchElementException("No setting called \"" + name + "\" exists that can be cast to type " + settingType);
    }

    @Override
    public final void set(String name, Object value)
    {
        if (exists(name))
        {
            if (value == null || getType(name).isAssignableFrom(value.getClass()))
            {
                setUnchecked(name, value);
                settingChanged.notify(new SettingChangedEvent(this, name, value));
            }
            else
            {
                throw new NoSuchElementException("No setting called \"" + name + "\" exists that can be assigned from type " + value.getClass());
            }
        }
        else
        {
            throw new NoSuchElementException("No setting called \"" + name + "\" exists that can be assigned from type " + value.getClass());
        }
    }

    @Override
    public final void copyAllFrom(ReadonlyGeneralSettingsModel other)
    {
        for (Setting s : other)
        {
            if (exists(s.getName()))
            {
                set(s.getName(), s.getValue());
            }
        }
    }

    @Override
    public final void copyFrom(ReadonlyGeneralSettingsModel other, Collection<String> settingNames)
    {
        for (String name : settingNames)
        {
            if (exists(name) && other.exists(name))
            {
                set(name, other.getObject(name));
            }
        }
    }

    @Override
    public final void copyFrom(ReadonlyGeneralSettingsModel other, String... settingNames)
    {
        for (String name : settingNames)
        {
            if (exists(name) && other.exists(name))
            {
                set(name, other.getObject(name));
            }
        }
    }

    protected static final class GeneralSetting implements Setting
    {
        private final String name;
        private final Class<?> type;
        private final Object initialValue;
        private final boolean serialize;

        public GeneralSetting(String name, Class<?> type, Object value, boolean serialize)
        {
            this.name = name;
            this.type = type;
            initialValue = value;
            this.serialize = serialize;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        @Override
        public Object getValue()
        {
            return initialValue;
        }

        @Override
        public boolean shouldSerialize()
        {
            return serialize;
        }
    }
}

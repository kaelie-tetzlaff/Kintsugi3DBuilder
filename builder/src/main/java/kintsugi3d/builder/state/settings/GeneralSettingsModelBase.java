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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.NoSuchElementException;

public abstract class GeneralSettingsModelBase implements GeneralSettingsModel
{
    protected abstract Object getUnchecked(String name);
    protected abstract void setUnchecked(String name, @Nullable Object value);

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
    public final <T> @NotNull T get(String name, @NotNull Class<T> settingType)
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
    public final void set(String name, @Nullable Object value)
    {
        if (exists(name))
        {
            if (value == null || getType(name).isAssignableFrom(value.getClass()))
            {
                setUnchecked(name, value);
            }
            else
            {
                throw new NoSuchElementException(String.format("No setting called \"%s\" exists that can be assigned from type %s",
                    name, value.getClass()));
            }
        }
        else
        {
            throw new NoSuchElementException(String.format("No setting called \"%s\" exists that can be assigned from type %s",
                name, value != null ? value.getClass() : "<null>"));
        }
    }

    @Override
    public final void copyAllFrom(@NotNull ReadonlyGeneralSettingsModel other)
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
    public final void copyFrom(@NotNull ReadonlyGeneralSettingsModel other, @NotNull Collection<String> settingNames)
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
    public final void copyFrom(@NotNull ReadonlyGeneralSettingsModel other, String @NotNull ... settingNames)
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

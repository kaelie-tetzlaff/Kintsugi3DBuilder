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

package kintsugi3d.builder.util.events;

import java.util.Collections;
import java.util.Map;

public class MappedChange<K, V>
{
    public enum Type
    {
        ADDED, REMOVED, MODIFIED
    }

    private final Type type;
    private final Map<K, V> map;

    public MappedChange(Type type, Map<K, V> map)
    {
        this.type = type;
        this.map = Collections.unmodifiableMap(map);
    }

    public MappedChange(Type type, K key, V value)
    {
        this.type = type;
        this.map = Map.of(key, value);
    }
    public Type getType()
    {
        return type;
    }

    public Map<K, V> getMap()
    {
        return map;
    }
}

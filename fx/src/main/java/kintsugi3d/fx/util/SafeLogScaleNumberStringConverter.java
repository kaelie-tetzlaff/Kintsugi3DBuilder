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

package kintsugi3d.fx.util;

import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

public class SafeLogScaleNumberStringConverter extends StringConverter<Number>
{
    private final @NotNull SafeNumberStringConverter base;
    private final @NotNull Number defaultValue;

    public SafeLogScaleNumberStringConverter(@NotNull Number defaultValue)
    {
        this.defaultValue = StrictMath.log10(defaultValue.doubleValue());
        base = new SafeNumberStringConverter(this.defaultValue);
    }

    @Override
    public final String toString(@NotNull Number object)
    {
        Double to10 = StrictMath.pow(10, object.doubleValue());
        return base.toString(to10);
    }

    @Override
    public final Number fromString(String string)
    {
        Number raw = base.fromString(string);
        if (raw.doubleValue() == 0.0)
        {
            return defaultValue;
        }
        return StrictMath.log10(raw.doubleValue());
    }
}

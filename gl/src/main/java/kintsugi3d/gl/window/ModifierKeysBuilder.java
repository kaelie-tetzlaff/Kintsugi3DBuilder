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

package kintsugi3d.gl.window;

public class ModifierKeysBuilder
{
    private boolean shiftModifier;
    private boolean controlModifier;
    private boolean altModifier;
    private boolean superModifier;

    public static ModifierKeysBuilder begin()
    {
        return new ModifierKeysBuilder();
    }

    public ModifierKeysBuilder shift()
    {
        this.shiftModifier = true;
        return this;
    }

    public ModifierKeysBuilder control()
    {
        this.controlModifier = true;
        return this;
    }

    public ModifierKeysBuilder alt()
    {
        this.altModifier = true;
        return this;
    }

    public ModifierKeysBuilder superKey()
    {
        this.superModifier = true;
        return this;
    }

    public ModifierKeys end()
    {
        return new SimpleModifierKeys(shiftModifier, controlModifier, altModifier, superModifier);
    }

    private static final class SimpleModifierKeys extends ModifierKeysBase
    {
        private final boolean shiftModifier;
        private final boolean controlModifier;
        private final boolean altModifier;
        private final boolean superModifier;

        private SimpleModifierKeys(boolean shiftModifier, boolean controlModifier, boolean altModifier, boolean superModifier)
        {
            this.shiftModifier = shiftModifier;
            this.controlModifier = controlModifier;
            this.altModifier = altModifier;
            this.superModifier = superModifier;
        }

        @Override
        public boolean isShiftModifierActive()
        {
            return shiftModifier;
        }

        @Override
        public boolean isControlModifierActive()
        {
            return controlModifier;
        }

        @Override
        public boolean isAltModifierActive()
        {
            return altModifier;
        }

        @Override
        public boolean isSuperModifierActive()
        {
            return superModifier;
        }
    }
}

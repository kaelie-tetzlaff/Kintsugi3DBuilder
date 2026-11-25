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

package kintsugi3d.fx.controllers.paged;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

class DataSelectionPageBuilder<T, FinishType> extends SelectionPageBuilder<T, FinishType>
{
    DataSelectionPageBuilder(SelectionPage<T> page, PageFrameController frameController, Supplier<FinishType> finisher)
    {
        super(page, frameController, finisher);
    }

    @Override
    public final <ControllerType extends SelectionPageController<T>>
    @NotNull SelectionPageBuilder<T, FinishType> choiceSubSelect(String choiceLabel, String subprompt, Supplier<ControllerType> controllerConstructorOverride)
    {
        return choiceSubSelect(choiceLabel, subprompt, SimpleSelectionPage<T, ControllerType>::new, controllerConstructorOverride);
    }

    @Override
    public final @NotNull SelectionPageBuilder<T, FinishType> choiceSubSelect(String choiceLabel, String subprompt)
    {
        return this.<SelectionPageController<T>>choiceSubSelect(choiceLabel, subprompt, null);
    }
}

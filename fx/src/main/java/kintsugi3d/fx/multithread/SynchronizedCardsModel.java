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

import javafx.application.Platform;
import kintsugi3d.builder.state.cards.CardsModel;
import kintsugi3d.builder.state.cards.ProjectDataCard;
import kintsugi3d.builder.state.cards.RefreshFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class SynchronizedCardsModel<T> implements CardsModel<T>
{
    private final @NotNull CardsModel<T> base;

    public SynchronizedCardsModel(@Nullable CardsModel<T> base)
    {
        this.base = Objects.requireNonNull(base, "Base model must not be null");
    }

    @Override
    public final List<? extends Map<String, Runnable>> getGlobalActions()
    {
        return base.getGlobalActions();
    }

    @Override
    public final List<ProjectDataCard> getCardList()
    {
        return base.getCardList();
    }

    @Override
    public final void setCardList(List<ProjectDataCard> cards)
    {
        Platform.runLater(() -> base.setCardList(cards));
    }

    @Override
    public final void refreshCards(RefreshFunction<T> refreshedData)
    {
        Platform.runLater(() -> base.refreshCards(refreshedData));
    }

    @Override
    public final void deleteCards(Predicate<ProjectDataCard> filter)
    {
        Platform.runLater(() -> base.deleteCards(filter));
    }
}

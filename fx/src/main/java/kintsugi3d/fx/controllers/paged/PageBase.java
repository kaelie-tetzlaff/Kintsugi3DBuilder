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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

abstract class PageBase<InType, OutType, ControllerType extends PageController<? super InType>>
    implements Page<InType, OutType>
{
    public static final String SENTINEL_MESSAGE = "Attempt to access a sentinel page.";

    private final @Nullable ControllerType controller;
    private final @Nullable Parent root;

    private final ObjectProperty<Page<? super OutType, ?>> nextPageProperty = new SimpleObjectProperty<>(null);

    private Page<?, ? extends InType> prevPage;

    private final Map<String, Page<? super OutType,?>> fallbackPages = new LinkedHashMap<>(1);

    PageBase(@Nullable FXMLLoader loader)
    {
        if (loader == null)
        {
            controller = null;
            root = null;
        }
        else
        {
            controller = loader.getController();
            root = loader.getRoot();
        }
    }

    @Override
    public final @NotNull ControllerType getController()
    {
        return Objects.requireNonNull(controller, SENTINEL_MESSAGE);
    }

    @Override
    public final @NotNull Parent getRoot()
    {
        return Objects.requireNonNull(root, SENTINEL_MESSAGE);
    }

    @Override
    public final @NotNull ObjectProperty<Page<? super OutType, ?>> getNextPageObservable()
    {
        return nextPageProperty;
    }

    @Override
    public final Page<? super OutType, ?> getNextPage()
    {
        return nextPageProperty.get();
    }

    @Override
    public final boolean hasNextPage()
    {
        return nextPageProperty.isNotNull().get();
    }

    @SuppressWarnings("DesignForExtension")
    @Override
    public void setNextPage(@NotNull Page<? super OutType, ?> page)
    {
        nextPageProperty.set(page);

        if (page.getPrevPage() == null)
        {
            // set default link back (useful for fallback pages which weren't accessed via next button)
            page.setPrevPage(this);
        }
    }

    @Override
    public final @NotNull Map<String, Page<? super OutType, ?>> getFallbackPages()
    {
        return Collections.unmodifiableMap(fallbackPages);
    }

    @Override
    public final boolean hasFallbackPage()
    {
        return !fallbackPages.isEmpty();
    }

    @Override
    public final void addFallbackPage(String fallbackName, Page<? super OutType, ?> page)
    {
        fallbackPages.put(fallbackName, page);
    }

    @Override
    public final Page<?, ? extends InType> getPrevPage()
    {
        return prevPage;
    }

    @Override
    public final boolean hasPrevPage()
    {
        return prevPage != null;
    }

    @Override
    public final void setPrevPage(Page<?, ? extends InType> page)
    {
        prevPage = page;
    }

    @Override
    public final void linkBackFromNextPage()
    {
        nextPageProperty.get().setPrevPage(this);
    }

    final void sendDataToController(InType data)
    {
        getController().receiveData(data);
    }

    @Override
    public final void sendOutData()
    {
        if (hasNextPage())
        {
            getNextPage().receiveData(getOutData());
        }
    }
}

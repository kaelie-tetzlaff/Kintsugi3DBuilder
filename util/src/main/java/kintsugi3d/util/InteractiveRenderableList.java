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

package kintsugi3d.util;

import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.core.Framebuffer;
import kintsugi3d.gl.interactive.InitializationException;
import kintsugi3d.gl.interactive.InteractiveRenderableBase;
import kintsugi3d.gl.interactive.InteractiveRenderableResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A container that can hold many InteractiveRenderable objects with one of them being selected/active. It
 * is also itself a InteractiveRenderable and will keep all its members updated and will draw the currently
 * 'active' InteractiveRenderable in the collection.
 *
 * @param <RenderableType> The type of the objects stored in the container (must implement InteractiveRenderable)
 * @see InteractiveRenderableResource
 */
public class InteractiveRenderableList<ContextType extends Context<ContextType>, RenderableType extends InteractiveRenderableResource<ContextType>>
    extends InteractiveRenderableBase<ContextType> implements SelectableList<RenderableType>
{
    private static final Logger LOG = LoggerFactory.getLogger(InteractiveRenderableList.class);
    private List<RenderableType> renderables;
    private int selectedIndex = -1;

    private List<RenderableType> removedRenderables;
    private List<RenderableType> addedRenderables;

    public InteractiveRenderableList()
    {
        renderables = new ArrayList<>(8);
        removedRenderables = new ArrayList<>(8);
        addedRenderables = new ArrayList<>(8);
    }

    private InteractiveRenderableList(List<RenderableType> renderables)
    {
        this.renderables = renderables;
        removedRenderables = new ArrayList<>(8);
        addedRenderables = new ArrayList<>(renderables);
    }

    @Override
    public final void initialize() throws InitializationException
    {
        for (InteractiveRenderableResource<ContextType> r : renderables)
        {
            r.initialize();
        }
    }

    @Override
    public final void update()
    {
        removedRenderables.forEach(InteractiveRenderableResource::close);

        for (RenderableType r : addedRenderables)
        {
            try
            {
                r.initialize();
            }
            catch (RuntimeException|InitializationException e)
            {
                LOG.error("Error while initializing renderable", e);
                renderables.remove(r);
            }
            catch (Error e)
            {
                LOG.error("Error while initializing renderable", e);
                renderables.remove(r);

                //noinspection ProhibitedExceptionThrown
                throw e;
            }
        }

        removedRenderables = new ArrayList<>(8);
        addedRenderables = new ArrayList<>(8);

        renderables.forEach(InteractiveRenderableResource::update);
    }

    @Override
    public final void draw(Framebuffer<ContextType> framebuffer)
    {
        InteractiveRenderableResource<ContextType> selected = this.getSelectedItem();
        if (selected != null)
        {
            selected.draw(framebuffer);
        }
    }

    @Override
    public final void close()
    {
        for (InteractiveRenderableResource<ContextType> r : renderables)
        {
            r.close();
        }
    }

    @Override
    public final int getSelectedIndex()
    {
        return selectedIndex;
    }

    @Override
    public final @Nullable RenderableType getSelectedItem()
    {
        if (selectedIndex < 0)
        {
            return null;
        }
        else
        {
            return renderables.get(selectedIndex);
        }
    }

    @Override
    public final void setSelectedIndex(int index)
    {
        this.selectedIndex = index;
    }

    @Override
    public final void setSelectedItem(Object item)
    {
        if (item == null)
        {
            this.selectedIndex = -1;
        }
        else
        {
            this.selectedIndex = renderables.indexOf(item);
        }
    }

    @Override
    public final int size()
    {
        return renderables.size();
    }

    @Override
    public final boolean isEmpty()
    {
        return renderables.isEmpty();
    }

    @Override
    public final boolean contains(Object o)
    {
        return renderables.contains(o);
    }

    @Override
    public final @NotNull Iterator<RenderableType> iterator()
    {
        return renderables.iterator();
    }

    @Override
    public final Object @NotNull [] toArray()
    {
        return renderables.toArray();
    }

    @Override
    public final <U> U @NotNull [] toArray(U @NotNull [] a)
    {
        return renderables.toArray(a);
    }

    @Override
    public final boolean add(RenderableType e)
    {
        renderables.add(e);
        addedRenderables.add(e);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean remove(Object o)
    {
        if (renderables.remove(o))
        {
            removedRenderables.add((RenderableType)o);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final boolean containsAll(@NotNull Collection<?> c)
    {
        //noinspection SlowListContainsAll
        return renderables.containsAll(c);
    }

    @Override
    public final boolean addAll(@NotNull Collection<? extends RenderableType> c)
    {
        if (renderables.addAll(c))
        {
            addedRenderables.addAll(c);
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final boolean addAll(int index, @NotNull Collection<? extends RenderableType> c)
    {
        if (renderables.addAll(index, c))
        {
            addedRenderables.addAll(c);
            if (index < selectedIndex)
            {
                selectedIndex += c.size();
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final boolean removeAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException("removeAll() is not supported by InteractiveRenderableList.");
    }

    @Override
    public final boolean retainAll(@NotNull Collection<?> c)
    {
        throw new UnsupportedOperationException("retainAll() is not supported by InteractiveRenderableList.");
    }

    @Override
    public final void clear()
    {
        this.removedRenderables = this.renderables;
        this.renderables = new ArrayList<>(8);
    }

    @Override
    public final RenderableType get(int index)
    {
        return renderables.get(index);
    }

    @Override
    public final RenderableType set(int index, RenderableType element)
    {
        RenderableType removed = renderables.set(index, element);
        if (removed != null)
        {
            removedRenderables.add(removed);
        }
        if (element != null)
        {
            addedRenderables.add(element);
        }
        return removed;
    }

    @Override
    public final void add(int index, RenderableType element)
    {
        renderables.add(index, element);
        if (element != null)
        {
            addedRenderables.add(element);
        }
        if (index < selectedIndex)
        {
            selectedIndex++;
        }
    }

    @Override
    public final RenderableType remove(int index)
    {
        RenderableType removed = renderables.remove(index);
        if (removed != null)
        {
            removedRenderables.add(removed);
        }
        if (index < selectedIndex)
        {
            selectedIndex--;
        }
        return removed;
    }

    @Override
    public final int indexOf(Object o)
    {
        return renderables.indexOf(o);
    }

    @Override
    public final int lastIndexOf(Object o)
    {
        return renderables.lastIndexOf(o);
    }

    @Override
    public final @NotNull ListIterator<RenderableType> listIterator()
    {
        return renderables.listIterator();
    }

    @Override
    public final @NotNull ListIterator<RenderableType> listIterator(int index)
    {
        return renderables.listIterator(index);
    }

    @Override
    public final @NotNull InteractiveRenderableList<ContextType, RenderableType> subList(int fromIndex, int toIndex)
    {
        return new InteractiveRenderableList<>(renderables.subList(fromIndex, toIndex));
    }
}

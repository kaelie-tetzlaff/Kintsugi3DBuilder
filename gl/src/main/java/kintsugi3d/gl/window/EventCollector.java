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

import kintsugi3d.gl.window.listeners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

class EventCollector
{
    private static final Logger LOG = LoggerFactory.getLogger(EventCollector.class);
    
    private final Object lock = new Object();

    private final Queue<Consumer<CanvasPositionListener>> canvasPos = new LinkedList<>();
    private final Queue<Consumer<CanvasSizeListener>> canvasSize = new LinkedList<>();
    private final Queue<Consumer<WindowCloseListener>> windowClose = new LinkedList<>();
    private final Queue<Consumer<CanvasRefreshListener>> canvasRefresh = new LinkedList<>();
    private final Queue<Consumer<WindowFocusLostListener>> windowFocusLost = new LinkedList<>();
    private final Queue<Consumer<WindowFocusGainedListener>> windowFocusGained = new LinkedList<>();
    private final Queue<Consumer<WindowIconifiedListener>> windowIconified = new LinkedList<>();
    private final Queue<Consumer<WindowRestoredListener>> windowRestored = new LinkedList<>();
    private final Queue<Consumer<FramebufferSizeListener>> framebufferSize = new LinkedList<>();
    private final Queue<Consumer<KeyPressListener>> keyPress = new LinkedList<>();
    private final Queue<Consumer<KeyReleaseListener>> keyRelease = new LinkedList<>();
    private final Queue<Consumer<KeyTypeListener>> keyType = new LinkedList<>();
    private final Queue<Consumer<CharacterListener>> character = new LinkedList<>();
    private final Queue<Consumer<CharacterModifiersListener>> charMods = new LinkedList<>();
    private final Queue<Consumer<MouseButtonPressListener>> mouseButtonPress = new LinkedList<>();
    private final Queue<Consumer<MouseButtonReleaseListener>> mouseButtonRelease = new LinkedList<>();
    private final Queue<Consumer<CursorPositionListener>> cursorPos = new LinkedList<>();
    private final Queue<Consumer<CursorEnteredListener>> cursorEnter = new LinkedList<>();
    private final Queue<Consumer<CursorExitedListener>> cursorExit = new LinkedList<>();
    private final Queue<Consumer<ScrollListener>> scroll = new LinkedList<>();

    private final WindowListenerManagerInstance listenerManager = new WindowListenerManagerInstance();

    final WindowListenerManager getListenerManager()
    {
        return listenerManager;
    }

    final void canvasPos(Consumer<CanvasPositionListener> event)
    {
        synchronized (lock)
        {
            this.canvasPos.add(event);
        }
    }

    final void canvasSize(Consumer<CanvasSizeListener> event)
    {
        synchronized (lock)
        {
            this.canvasSize.add(event);
        }
    }

    final void windowClose(Consumer<WindowCloseListener> event)
    {
        synchronized (lock)
        {
            this.windowClose.add(event);
        }
    }

    final void canvasRefresh(Consumer<CanvasRefreshListener> event)
    {
        synchronized (lock)
        {
            this.canvasRefresh.add(event);
        }
    }

    final void windowFocusLost(Consumer<WindowFocusLostListener> event)
    {
        synchronized (lock)
        {
            this.windowFocusLost.add(event);
        }
    }

    final void windowFocusGained(Consumer<WindowFocusGainedListener> event)
    {
        synchronized (lock)
        {
            this.windowFocusGained.add(event);
        }
    }

    final void windowIconified(Consumer<WindowIconifiedListener> event)
    {
        synchronized (lock)
        {
            this.windowIconified.add(event);
        }
    }

    final void windowRestored(Consumer<WindowRestoredListener> event)
    {
        synchronized (lock)
        {
            this.windowRestored.add(event);
        }
    }

    final void framebufferSize(Consumer<FramebufferSizeListener> event)
    {
        synchronized (lock)
        {
            this.framebufferSize.add(event);
        }
    }

    final void keyPress(Consumer<KeyPressListener> event)
    {
        synchronized (lock)
        {
            this.keyPress.add(event);
        }
    }

    final void keyRelease(Consumer<KeyReleaseListener> event)
    {
        synchronized (lock)
        {
            this.keyRelease.add(event);
        }
    }

    final void keyType(Consumer<KeyTypeListener> event)
    {
        synchronized (lock)
        {
            this.keyType.add(event);
        }
    }

    final void character(Consumer<CharacterListener> event)
    {
        synchronized (lock)
        {
            this.character.add(event);
        }
    }

    final void charMods(Consumer<CharacterModifiersListener> event)
    {
        synchronized (lock)
        {
            this.charMods.add(event);
        }
    }

    final void mouseButtonPress(Consumer<MouseButtonPressListener> event)
    {
        synchronized (lock)
        {
            this.mouseButtonPress.add(event);
        }
    }

    final void mouseButtonRelease(Consumer<MouseButtonReleaseListener> event)
    {
        synchronized (lock)
        {
            this.mouseButtonRelease.add(event);
        }
    }

    final void cursorPos(Consumer<CursorPositionListener> event)
    {
        synchronized (lock)
        {
            this.cursorPos.add(event);
        }
    }

    final void cursorEnter(Consumer<CursorEnteredListener> event)
    {
        synchronized (lock)
        {
            this.cursorEnter.add(event);
        }
    }

    final void cursorExit(Consumer<CursorExitedListener> event)
    {
        synchronized (lock)
        {
            this.cursorExit.add(event);
        }
    }

    final void scroll(Consumer<ScrollListener> event)
    {
        synchronized (lock)
        {
            this.scroll.add(event);
        }
    }

    private <L> void pollEvents(Queue<Consumer<L>> eventQueue, Iterable<L> listeners)
    {
        synchronized (lock)
        {
            while (!eventQueue.isEmpty())
            {
                Consumer<L> event = eventQueue.poll();
                for (L l : listeners)
                {
                    if (event == null)
                    {
                        LOG.warn("Event was null", new Throwable());
                    }
                    else
                    {
                        try
                        {
                            event.accept(l);
                        }
                        catch (RuntimeException e)
                        {
                            LOG.error("An error occurred while polling events", e);
                        }
                    }
                }
            }
        }
    }

    final void pollEvents()
    {
        pollEvents(canvasPos, listenerManager.getCanvasPosListeners());
        pollEvents(canvasSize, listenerManager.getCanvasSizeListeners());
        pollEvents(windowClose, listenerManager.getWindowCloseListeners());
        pollEvents(canvasRefresh, listenerManager.getCanvasRefreshListeners());
        pollEvents(windowFocusLost, listenerManager.getWindowFocusLostListeners());
        pollEvents(windowFocusGained, listenerManager.getWindowFocusGainedListeners());
        pollEvents(windowIconified, listenerManager.getWindowIconifiedListeners());
        pollEvents(windowRestored, listenerManager.getWindowRestoredListeners());
        pollEvents(framebufferSize, listenerManager.getFramebufferSizeListeners());
        pollEvents(keyPress, listenerManager.getKeyPressListeners());
        pollEvents(keyRelease, listenerManager.getKeyReleaseListeners());
        pollEvents(keyType, listenerManager.getKeyTypeListeners());
        pollEvents(character, listenerManager.getCharacterListeners());
        pollEvents(charMods, listenerManager.getCharModsListeners());
        pollEvents(mouseButtonPress, listenerManager.getMouseButtonPressListeners());
        pollEvents(mouseButtonRelease, listenerManager.getMouseButtonReleaseListeners());
        pollEvents(cursorPos, listenerManager.getCursorPosListeners());
        pollEvents(cursorEnter, listenerManager.getCursorEnterListeners());
        pollEvents(cursorExit, listenerManager.getCursorExitListeners());
        pollEvents(scroll, listenerManager.getScrollListeners());
    }
}

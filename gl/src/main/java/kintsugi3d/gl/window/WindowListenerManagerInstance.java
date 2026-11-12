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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WindowListenerManagerInstance implements WindowListenerManager
{
    private final List<CanvasPositionListener> canvasPosListeners;
    private final List<CanvasSizeListener> canvasSizeListeners;
    private final List<WindowCloseListener> windowCloseListeners;
    private final List<CanvasRefreshListener> canvasRefreshListeners;
    private final List<WindowFocusLostListener> windowFocusLostListeners;
    private final List<WindowFocusGainedListener> windowFocusGainedListeners;
    private final List<WindowIconifiedListener> windowIconifiedListeners;
    private final List<WindowRestoredListener> windowRestoredListeners;
    private final List<FramebufferSizeListener> framebufferSizeListeners;
    private final List<KeyPressListener> keyPressListeners;
    private final List<KeyReleaseListener> keyReleaseListeners;
    private final List<KeyTypeListener> keyTypeListeners;
    private final List<CharacterListener> characterListeners;
    private final List<CharacterModifiersListener> charModsListeners;
    private final List<MouseButtonPressListener> mouseButtonPressListeners;
    private final List<MouseButtonReleaseListener> mouseButtonReleaseListeners;
    private final List<CursorPositionListener> cursorPosListeners;
    private final List<CursorEnteredListener> cursorEnterListeners;
    private final List<CursorExitedListener> cursorExitListeners;
    private final List<ScrollListener> scrollListeners;

    public WindowListenerManagerInstance()
    {
        windowCloseListeners = new ArrayList<>(1);
        charModsListeners = new ArrayList<>(1);
        cursorExitListeners = new ArrayList<>(1);
        windowFocusGainedListeners = new ArrayList<>(1);
        framebufferSizeListeners = new ArrayList<>(1);
        scrollListeners = new ArrayList<>(1);
        mouseButtonPressListeners = new ArrayList<>(1);
        mouseButtonReleaseListeners = new ArrayList<>(1);
        characterListeners = new ArrayList<>(1);
        canvasPosListeners = new ArrayList<>(1);
        cursorPosListeners = new ArrayList<>(1);
        cursorEnterListeners = new ArrayList<>(1);
        windowIconifiedListeners = new ArrayList<>(1);
        keyReleaseListeners = new ArrayList<>(1);
        keyTypeListeners = new ArrayList<>(1);
        canvasRefreshListeners = new ArrayList<>(1);
        keyPressListeners = new ArrayList<>(1);
        windowRestoredListeners = new ArrayList<>(1);
        canvasSizeListeners = new ArrayList<>(1);
        windowFocusLostListeners = new ArrayList<>(1);
    }

    @Override
    public void addCanvasPositionListener(CanvasPositionListener listener)
    {
        canvasPosListeners.add(listener);
    }

    @Override
    public void addCanvasSizeListener(CanvasSizeListener listener)
    {
        canvasSizeListeners.add(listener);
    }

    @Override
    public void addWindowCloseListener(WindowCloseListener listener)
    {
        windowCloseListeners.add(listener);
    }

    @Override
    public void addCanvasRefreshListener(CanvasRefreshListener listener)
    {
        canvasRefreshListeners.add(listener);
    }

    @Override
    public void addWindowFocusLostListener(WindowFocusLostListener listener)
    {
        windowFocusLostListeners.add(listener);
    }

    @Override
    public void addWindowFocusGainedListener(WindowFocusGainedListener listener)
    {
        windowFocusGainedListeners.add(listener);
    }

    @Override
    public void addWindowIconifiedListener(WindowIconifiedListener listener)
    {
        windowIconifiedListeners.add(listener);
    }

    @Override
    public void addWindowRestoredListener(WindowRestoredListener listener)
    {
        windowRestoredListeners.add(listener);
    }

    @Override
    public void addFramebufferSizeListener(FramebufferSizeListener listener)
    {
        framebufferSizeListeners.add(listener);
    }

    @Override
    public void addKeyPressListener(KeyPressListener listener)
    {
        keyPressListeners.add(listener);
    }

    @Override
    public void addKeyReleaseListener(KeyReleaseListener listener)
    {
        keyReleaseListeners.add(listener);
    }

    @Override
    public void addKeyTypeListener(KeyTypeListener listener)
    {
        keyTypeListeners.add(listener);
    }

    @Override
    public void addCharacterListener(CharacterListener listener)
    {
        characterListeners.add(listener);
    }

    @Override
    public void addCharacterModifiersListener(CharacterModifiersListener listener)
    {
        charModsListeners.add(listener);
    }

    @Override
    public void addMouseButtonPressListener(MouseButtonPressListener listener)
    {
        mouseButtonPressListeners.add(listener);
    }

    @Override
    public void addMouseButtonReleaseListener(MouseButtonReleaseListener listener)
    {
        mouseButtonReleaseListeners.add(listener);
    }

    @Override
    public void addCursorPositionListener(CursorPositionListener listener)
    {
        cursorPosListeners.add(listener);
    }

    @Override
    public void addCursorEnteredListener(CursorEnteredListener listener)
    {
        cursorEnterListeners.add(listener);
    }

    @Override
    public void addCursorExitedListener(CursorExitedListener listener)
    {
        cursorExitListeners.add(listener);
    }

    @Override
    public void addScrollListener(ScrollListener listener)
    {
        scrollListeners.add(listener);
    }

    public Collection<CanvasPositionListener> getCanvasPosListeners()
    {
        return Collections.unmodifiableCollection(canvasPosListeners);
    }

    public Collection<CanvasSizeListener> getCanvasSizeListeners()
    {
        return Collections.unmodifiableCollection(canvasSizeListeners);
    }

    public Collection<WindowCloseListener> getWindowCloseListeners()
    {
        return Collections.unmodifiableCollection(windowCloseListeners);
    }

    public Collection<CanvasRefreshListener> getCanvasRefreshListeners()
    {
        return Collections.unmodifiableCollection(canvasRefreshListeners);
    }

    public Collection<WindowFocusLostListener> getWindowFocusLostListeners()
    {
        return Collections.unmodifiableCollection(windowFocusLostListeners);
    }

    public Collection<WindowFocusGainedListener> getWindowFocusGainedListeners()
    {
        return Collections.unmodifiableCollection(windowFocusGainedListeners);
    }

    public Collection<WindowIconifiedListener> getWindowIconifiedListeners()
    {
        return Collections.unmodifiableCollection(windowIconifiedListeners);
    }

    public Collection<WindowRestoredListener> getWindowRestoredListeners()
    {
        return Collections.unmodifiableCollection(windowRestoredListeners);
    }

    public Collection<FramebufferSizeListener> getFramebufferSizeListeners()
    {
        return Collections.unmodifiableCollection(framebufferSizeListeners);
    }

    public Collection<KeyPressListener> getKeyPressListeners()
    {
        return Collections.unmodifiableCollection(keyPressListeners);
    }

    public Collection<KeyReleaseListener> getKeyReleaseListeners()
    {
        return Collections.unmodifiableCollection(keyReleaseListeners);
    }

    public Collection<KeyTypeListener> getKeyTypeListeners()
    {
        return Collections.unmodifiableCollection(keyTypeListeners);
    }

    public Collection<CharacterListener> getCharacterListeners()
    {
        return Collections.unmodifiableCollection(characterListeners);
    }

    public Collection<CharacterModifiersListener> getCharModsListeners()
    {
        return Collections.unmodifiableCollection(charModsListeners);
    }

    public Collection<MouseButtonPressListener> getMouseButtonPressListeners()
    {
        return Collections.unmodifiableCollection(mouseButtonPressListeners);
    }

    public Collection<MouseButtonReleaseListener> getMouseButtonReleaseListeners()
    {
        return Collections.unmodifiableCollection(mouseButtonReleaseListeners);
    }

    public Collection<CursorPositionListener> getCursorPosListeners()
    {
        return Collections.unmodifiableCollection(cursorPosListeners);
    }

    public Collection<CursorEnteredListener> getCursorEnterListeners()
    {
        return Collections.unmodifiableCollection(cursorEnterListeners);
    }

    public Collection<CursorExitedListener> getCursorExitListeners()
    {
        return Collections.unmodifiableCollection(cursorExitListeners);
    }

    public Collection<ScrollListener> getScrollListeners()
    {
        return Collections.unmodifiableCollection(scrollListeners);
    }
}

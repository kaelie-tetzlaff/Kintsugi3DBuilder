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

package kintsugi3d.gl.window;

import kintsugi3d.gl.window.listeners.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WindowListenerManagerInstance implements WindowListenerManager
{
    private final @NotNull List<CanvasPositionListener> canvasPosListeners;
    private final @NotNull List<CanvasSizeListener> canvasSizeListeners;
    private final @NotNull List<WindowCloseListener> windowCloseListeners;
    private final @NotNull List<CanvasRefreshListener> canvasRefreshListeners;
    private final @NotNull List<WindowFocusLostListener> windowFocusLostListeners;
    private final @NotNull List<WindowFocusGainedListener> windowFocusGainedListeners;
    private final @NotNull List<WindowIconifiedListener> windowIconifiedListeners;
    private final @NotNull List<WindowRestoredListener> windowRestoredListeners;
    private final @NotNull List<FramebufferSizeListener> framebufferSizeListeners;
    private final @NotNull List<KeyPressListener> keyPressListeners;
    private final @NotNull List<KeyReleaseListener> keyReleaseListeners;
    private final @NotNull List<KeyTypeListener> keyTypeListeners;
    private final @NotNull List<CharacterListener> characterListeners;
    private final @NotNull List<CharacterModifiersListener> charModsListeners;
    private final @NotNull List<MouseButtonPressListener> mouseButtonPressListeners;
    private final @NotNull List<MouseButtonReleaseListener> mouseButtonReleaseListeners;
    private final @NotNull List<CursorPositionListener> cursorPosListeners;
    private final @NotNull List<CursorEnteredListener> cursorEnterListeners;
    private final @NotNull List<CursorExitedListener> cursorExitListeners;
    private final @NotNull List<ScrollListener> scrollListeners;

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
    public final void addCanvasPositionListener(CanvasPositionListener listener)
    {
        canvasPosListeners.add(listener);
    }

    @Override
    public final void addCanvasSizeListener(CanvasSizeListener listener)
    {
        canvasSizeListeners.add(listener);
    }

    @Override
    public final void addWindowCloseListener(WindowCloseListener listener)
    {
        windowCloseListeners.add(listener);
    }

    @Override
    public final void addCanvasRefreshListener(CanvasRefreshListener listener)
    {
        canvasRefreshListeners.add(listener);
    }

    @Override
    public final void addWindowFocusLostListener(WindowFocusLostListener listener)
    {
        windowFocusLostListeners.add(listener);
    }

    @Override
    public final void addWindowFocusGainedListener(WindowFocusGainedListener listener)
    {
        windowFocusGainedListeners.add(listener);
    }

    @Override
    public final void addWindowIconifiedListener(WindowIconifiedListener listener)
    {
        windowIconifiedListeners.add(listener);
    }

    @Override
    public final void addWindowRestoredListener(WindowRestoredListener listener)
    {
        windowRestoredListeners.add(listener);
    }

    @Override
    public final void addFramebufferSizeListener(FramebufferSizeListener listener)
    {
        framebufferSizeListeners.add(listener);
    }

    @Override
    public final void addKeyPressListener(KeyPressListener listener)
    {
        keyPressListeners.add(listener);
    }

    @Override
    public final void addKeyReleaseListener(KeyReleaseListener listener)
    {
        keyReleaseListeners.add(listener);
    }

    @Override
    public final void addKeyTypeListener(KeyTypeListener listener)
    {
        keyTypeListeners.add(listener);
    }

    @Override
    public final void addCharacterListener(CharacterListener listener)
    {
        characterListeners.add(listener);
    }

    @Override
    public final void addCharacterModifiersListener(CharacterModifiersListener listener)
    {
        charModsListeners.add(listener);
    }

    @Override
    public final void addMouseButtonPressListener(MouseButtonPressListener listener)
    {
        mouseButtonPressListeners.add(listener);
    }

    @Override
    public final void addMouseButtonReleaseListener(MouseButtonReleaseListener listener)
    {
        mouseButtonReleaseListeners.add(listener);
    }

    @Override
    public final void addCursorPositionListener(CursorPositionListener listener)
    {
        cursorPosListeners.add(listener);
    }

    @Override
    public final void addCursorEnteredListener(CursorEnteredListener listener)
    {
        cursorEnterListeners.add(listener);
    }

    @Override
    public final void addCursorExitedListener(CursorExitedListener listener)
    {
        cursorExitListeners.add(listener);
    }

    @Override
    public final void addScrollListener(ScrollListener listener)
    {
        scrollListeners.add(listener);
    }

    public final @NotNull Collection<CanvasPositionListener> getCanvasPosListeners()
    {
        return Collections.unmodifiableCollection(canvasPosListeners);
    }

    public final @NotNull Collection<CanvasSizeListener> getCanvasSizeListeners()
    {
        return Collections.unmodifiableCollection(canvasSizeListeners);
    }

    public final @NotNull Collection<WindowCloseListener> getWindowCloseListeners()
    {
        return Collections.unmodifiableCollection(windowCloseListeners);
    }

    public final @NotNull Collection<CanvasRefreshListener> getCanvasRefreshListeners()
    {
        return Collections.unmodifiableCollection(canvasRefreshListeners);
    }

    public final @NotNull Collection<WindowFocusLostListener> getWindowFocusLostListeners()
    {
        return Collections.unmodifiableCollection(windowFocusLostListeners);
    }

    public final @NotNull Collection<WindowFocusGainedListener> getWindowFocusGainedListeners()
    {
        return Collections.unmodifiableCollection(windowFocusGainedListeners);
    }

    public final @NotNull Collection<WindowIconifiedListener> getWindowIconifiedListeners()
    {
        return Collections.unmodifiableCollection(windowIconifiedListeners);
    }

    public final @NotNull Collection<WindowRestoredListener> getWindowRestoredListeners()
    {
        return Collections.unmodifiableCollection(windowRestoredListeners);
    }

    public final @NotNull Collection<FramebufferSizeListener> getFramebufferSizeListeners()
    {
        return Collections.unmodifiableCollection(framebufferSizeListeners);
    }

    public final @NotNull Collection<KeyPressListener> getKeyPressListeners()
    {
        return Collections.unmodifiableCollection(keyPressListeners);
    }

    public final @NotNull Collection<KeyReleaseListener> getKeyReleaseListeners()
    {
        return Collections.unmodifiableCollection(keyReleaseListeners);
    }

    public final @NotNull Collection<KeyTypeListener> getKeyTypeListeners()
    {
        return Collections.unmodifiableCollection(keyTypeListeners);
    }

    public final @NotNull Collection<CharacterListener> getCharacterListeners()
    {
        return Collections.unmodifiableCollection(characterListeners);
    }

    public final @NotNull Collection<CharacterModifiersListener> getCharModsListeners()
    {
        return Collections.unmodifiableCollection(charModsListeners);
    }

    public final @NotNull Collection<MouseButtonPressListener> getMouseButtonPressListeners()
    {
        return Collections.unmodifiableCollection(mouseButtonPressListeners);
    }

    public final @NotNull Collection<MouseButtonReleaseListener> getMouseButtonReleaseListeners()
    {
        return Collections.unmodifiableCollection(mouseButtonReleaseListeners);
    }

    public final @NotNull Collection<CursorPositionListener> getCursorPosListeners()
    {
        return Collections.unmodifiableCollection(cursorPosListeners);
    }

    public final @NotNull Collection<CursorEnteredListener> getCursorEnterListeners()
    {
        return Collections.unmodifiableCollection(cursorEnterListeners);
    }

    public final @NotNull Collection<CursorExitedListener> getCursorExitListeners()
    {
        return Collections.unmodifiableCollection(cursorExitListeners);
    }

    public final @NotNull Collection<ScrollListener> getScrollListeners()
    {
        return Collections.unmodifiableCollection(scrollListeners);
    }
}

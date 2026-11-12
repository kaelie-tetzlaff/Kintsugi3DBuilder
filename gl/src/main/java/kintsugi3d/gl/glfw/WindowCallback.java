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

package kintsugi3d.gl.glfw;

import kintsugi3d.gl.window.Key;
import kintsugi3d.gl.window.WindowListenerManagerInstance;
import kintsugi3d.gl.window.listeners.*;
import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Other misc. callbacks<br>
 * Keyboard callbacks<br>
 * Mouse callbacks<br>
 * Window event callbacks<br>
 * Internal classes for wrapping GLFW callbacks
 */
class WindowCallback extends WindowListenerManagerInstance
{
    private final CanvasWindow<?> window;

    WindowCallback(CanvasWindow<?> window)
    {
        this.window = window;

        createCallbacks();
    }

    // A reference to these callbacks must be maintained to prevent Java from garbage collecting them.
    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowPosCallback windowPosCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowSizeCallback windowSizeCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowCloseCallback windowCloseCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowRefreshCallback windowRefreshCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowFocusCallback windowFocusCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWWindowIconifyCallback windowIconifyCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWFramebufferSizeCallback framebufferSizeCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWKeyCallback keyCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWCharCallback charCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWCharModsCallback charModsCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWMouseButtonCallback mouseButtonCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWCursorPosCallback cursorPosCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWCursorEnterCallback cursorEnterCallback;

    @SuppressWarnings({ "unused", "FieldCanBeLocal" })
    private GLFWScrollCallback scrollCallback;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private GLFWDropCallback dropCallback;

    private void createCallbacks()
    {
        windowPosCallback = new WindowPosCallback();
        glfwSetWindowPosCallback(window.getHandle(), windowPosCallback);

        windowSizeCallback = new WindowSizeCallback();
        glfwSetWindowSizeCallback(window.getHandle(), windowSizeCallback);

        windowCloseCallback = new WindowCloseCallback();
        glfwSetWindowCloseCallback(window.getHandle(), windowCloseCallback);

        windowRefreshCallback = new WindowRefreshCallback();
        glfwSetWindowRefreshCallback(window.getHandle(), windowRefreshCallback);

        windowFocusCallback = new WindowFocusCallback();
        glfwSetWindowFocusCallback(window.getHandle(), windowFocusCallback);

        windowIconifyCallback = new WindowIconifyCallback();
        glfwSetWindowIconifyCallback(window.getHandle(), windowIconifyCallback);

        framebufferSizeCallback = new FramebufferSizeCallback();
        glfwSetFramebufferSizeCallback(window.getHandle(), framebufferSizeCallback);

        keyCallback = new KeyCallback();
        glfwSetKeyCallback(window.getHandle(), keyCallback);

        charCallback = new CharCallback();
        glfwSetCharCallback(window.getHandle(), charCallback);

        charModsCallback = new CharModsCallback();
        glfwSetCharModsCallback(window.getHandle(), charModsCallback);

        mouseButtonCallback = new MouseButtonCallback();
        glfwSetMouseButtonCallback(window.getHandle(), mouseButtonCallback);

        cursorPosCallback = new CursorPosCallback();
        glfwSetCursorPosCallback(window.getHandle(), cursorPosCallback);

        cursorEnterCallback = new CursorEnterCallback();
        glfwSetCursorEnterCallback(window.getHandle(), cursorEnterCallback);

        scrollCallback = new ScrollCallback();
        glfwSetScrollCallback(window.getHandle(), scrollCallback);

        dropCallback = new DropCallback();
        glfwSetDropCallback(window.getHandle(), dropCallback);
    }

    private class WindowPosCallback extends GLFWWindowPosCallback
    {
        @Override
        public void invoke(long windowHandle, int xpos, int ypos)
        {
            if (windowHandle == window.getHandle())
            {
                for (CanvasPositionListener listener : getCanvasPosListeners())
                {
                    listener.canvasMoved(window, xpos, ypos);
                }
            }
        }
    }

    private class WindowSizeCallback extends GLFWWindowSizeCallback
    {
        @Override
        public void invoke(long windowHandle, int width, int height)
        {
            if (windowHandle == window.getHandle())
            {
                for (CanvasSizeListener listener : getCanvasSizeListeners())
                {
                    listener.canvasResized(window, width, height);
                }
            }
        }
    }

    private class WindowCloseCallback extends GLFWWindowCloseCallback
    {
        @Override
        public void invoke(long windowHandle)
        {
            if (windowHandle == window.getHandle())
            {
                for (WindowCloseListener listener : getWindowCloseListeners())
                {
                    listener.windowClosing(window);
                }
            }
        }
    }

    private class WindowRefreshCallback extends GLFWWindowRefreshCallback
    {
        @Override
        public void invoke(long windowHandle)
        {
            if (windowHandle == window.getHandle())
            {
                for (CanvasRefreshListener listener : getCanvasRefreshListeners())
                {
                    listener.canvasRefreshed(window);
                }
            }
        }
    }

    private class WindowFocusCallback extends GLFWWindowFocusCallback
    {
        @Override
        public void invoke(long windowHandle, boolean focused)
        {
            if (windowHandle == window.getHandle())
            {
                if (focused)
                {
                    for (WindowFocusGainedListener listener : getWindowFocusGainedListeners())
                    {
                        listener.windowFocusGained(window);
                    }
                }
                else
                {
                    for (WindowFocusLostListener listener : getWindowFocusLostListeners())
                    {
                        listener.windowFocusLost(window);
                    }
                }
            }
        }
    }

    private class WindowIconifyCallback extends GLFWWindowIconifyCallback
    {
        @Override
        public void invoke(long windowHandle, boolean iconified)
        {
            if (windowHandle == window.getHandle())
            {
                if (iconified)
                {
                    for (WindowIconifiedListener listener : getWindowIconifiedListeners())
                    {
                        listener.windowIconified(window);
                    }
                }
                else
                {
                    for (WindowRestoredListener listener : getWindowRestoredListeners())
                    {
                        listener.windowRestored(window);
                    }
                }
            }
        }
    }

    private class FramebufferSizeCallback extends GLFWFramebufferSizeCallback
    {
        @Override
        public void invoke(long windowHandle, int width, int height)
        {
            if (windowHandle == window.getHandle())
            {
                for (FramebufferSizeListener listener : getFramebufferSizeListeners())
                {
                    listener.framebufferResized(window, width, height);
                }
            }
        }
    }

    private class KeyCallback extends GLFWKeyCallback
    {
        @Override
        public void invoke(long windowHandle, int keycode, int scancode, int action, int mods)
        {
            if (windowHandle == window.getHandle())
            {
                Key key = KeyCodeMaps.codeToKey(keycode);

                if (action == GLFW_PRESS)
                {
                    for (KeyPressListener listener : getKeyPressListeners())
                    {
                        listener.keyPressed(window, key, new ModifierKeys(mods));
                    }

                    for (KeyTypeListener listener : getKeyTypeListeners())
                    {
                        listener.keyTyped(window, key, new ModifierKeys(mods));
                    }
                }
                else if (action == GLFW_RELEASE)
                {
                    for (KeyReleaseListener listener : getKeyReleaseListeners())
                    {
                        listener.keyReleased(window, key, new ModifierKeys(mods));
                    }
                }
                else if (action == GLFW_REPEAT)
                {
                    for (KeyTypeListener listener : getKeyTypeListeners())
                    {
                        listener.keyTyped(window, key, new ModifierKeys(mods));
                    }
                }
            }
        }
    }

    private class CharCallback extends GLFWCharCallback
    {
        @Override
        public void invoke(long windowHandle, int codepoint)
        {
            if (windowHandle == window.getHandle())
            {
                for (CharacterListener listener : getCharacterListeners())
                {
                    listener.characterTyped(window, (char)codepoint);
                }
            }
        }
    }

    private class CharModsCallback extends GLFWCharModsCallback
    {
        @Override
        public void invoke(long windowHandle, int codepoint, int mods)
        {
            if (windowHandle == window.getHandle())
            {
                for (CharacterModifiersListener listener : getCharModsListeners())
                {
                    listener.characterTypedWithModifiers(window, (char)codepoint, new ModifierKeys(mods));
                }
            }
        }
    }

    private class MouseButtonCallback extends GLFWMouseButtonCallback
    {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods)
        {
            if (windowHandle == window.getHandle())
            {
                if (action == GLFW_PRESS)
                {
                    for (MouseButtonPressListener listener : getMouseButtonPressListeners())
                    {
                        listener.mouseButtonPressed(window, button, new ModifierKeys(mods));
                    }
                }
                else if (action == GLFW_RELEASE)
                {
                    for (MouseButtonReleaseListener listener : getMouseButtonReleaseListeners())
                    {
                        listener.mouseButtonReleased(window, button, new ModifierKeys(mods));
                    }
                }
            }
        }
    }

    private class CursorPosCallback extends GLFWCursorPosCallback
    {
        @Override
        public void invoke(long windowHandle, double xpos, double ypos)
        {
            if (windowHandle == window.getHandle())
            {
                for (CursorPositionListener listener : getCursorPosListeners())
                {
                    listener.cursorMoved(window, xpos, ypos);
                }
            }
        }
    }

    private class CursorEnterCallback extends GLFWCursorEnterCallback
    {
        @Override
        public void invoke(long windowHandle, boolean entered)
        {
            if (windowHandle == window.getHandle())
            {
                if (entered)
                {
                    for (CursorEnteredListener listener : getCursorEnterListeners())
                    {
                        listener.cursorEntered(window);
                    }
                }
                else
                {
                    for (CursorExitedListener listener : getCursorExitListeners())
                    {
                        listener.cursorExited(window);
                    }
                }
            }
        }
    }

    private class ScrollCallback extends GLFWScrollCallback
    {
        @Override
        public void invoke(long windowHandle, double xoffset, double yoffset)
        {
            if (windowHandle == window.getHandle())
            {
                for (ScrollListener listener : getScrollListeners())
                {
                    listener.scroll(window, xoffset, yoffset);
                }
            }
        }
    }

    private static class DropCallback extends GLFWDropCallback
    {
        @Override
        public void invoke(long windowHandle, int count, long names)
        {
            // Not supported
        }
    }
}

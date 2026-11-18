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

package kintsugi3d.app;

import javafx.stage.Stage;
import javafx.stage.Window;
import kintsugi3d.builder.rendering.RenderingUIBase;
import kintsugi3d.gl.interactive.InteractiveApplication;
import kintsugi3d.gl.opengl.OpenGLContext;
import kintsugi3d.gl.window.FramebufferCanvas;

public class JavaFXRenderingUI extends RenderingUIBase
{
    private final Stage stage;

    public JavaFXRenderingUI(Stage stage)
    {
        this.stage = stage;
    }

    @Override
    public final void onInitialized(InteractiveApplication app)
    {
        if (MultithreadState.isInitialized())
        {
            JavaFXApplication.getState().getProjectModel().registerIOListeners();
            
            // Wake the graphics thread up when the window is un-minimized.
            stage.iconifiedProperty().addListener((observable, wasIconified, isIconified) ->
            {
                if (wasIconified && !isIconified)
                {
                    wakeUp();
                }
            });

            // Wake the graphics thread up when the window is refocused.
            stage.focusedProperty().addListener((observable, wasFocused, isFocused) ->
            {
                var focusedWindows = Window.getWindows().filtered(Window::isFocused);
                if (!wasFocused && isFocused && focusedWindows.size() == 1 && focusedWindows.get(0).equals(stage))
                {
                    wakeUp();
                }
            });
        }
    }

    @Override
    public final void setCanvas(FramebufferCanvas<OpenGLContext> canvas)
    {
        JavaFXApplication.setCanvas(canvas);
    }

    @Override
    protected final boolean isUnfocused()
    {
        return Window.getWindows().filtered(Window::isFocused).isEmpty();
    }

    @Override
    protected final boolean isIconified()
    {
        return stage.isIconified();
    }
}

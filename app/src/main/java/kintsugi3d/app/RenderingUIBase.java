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

package kintsugi3d.app;

import kintsugi3d.builder.rendering.GraphicsRequestQueue;
import kintsugi3d.builder.rendering.Rendering;
import kintsugi3d.gl.interactive.EventPollable;
import kintsugi3d.gl.interactive.InteractiveApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RenderingUIBase implements RenderingUI
{
    private static final Logger LOG = LoggerFactory.getLogger(RenderingUIBase.class);

    private static final int ICONIFIED_TIMEOUT_MILLIS = 5000;
    private static final int UNFOCUSED_TIMEOUT_MILLIS = 1000;

    private final SleepPollable sleepPollable = new SleepPollable();

    @Override
    public void initialize(InteractiveApplication app)
    {
        // Keep the graphics thread paused while the window is minimized.
        app.addPollable(sleepPollable);

        // Wake the graphics thread up when work is added to the request queue.
        getRequestQueue().addRequestAddedListener(sleepPollable::wakeUp);
    }

    protected abstract boolean isUnfocused();

    protected abstract boolean isIconified();

    protected void wakeUp()
    {
        if (getRequestQueue().isEmpty())
        {
            sleepPollable.wakeUp();
        }
    }

    private static GraphicsRequestQueue getRequestQueue()
    {
        return Rendering.getRequestQueue();
    }

    /**
     * Keeps the graphics thread paused while the window is minimized.
     */
    private class SleepPollable implements EventPollable
    {
        /**
         * Used for wait / notify on rendering thread.
         */
        private final Object waitForRenderingWork = new Object();

        void wakeUp()
        {
            synchronized (waitForRenderingWork)
            {
                waitForRenderingWork.notifyAll();
            }
        }

        @Override
        public void pollEvents()
        {
            synchronized (waitForRenderingWork)
            {
                while ((isIconified() || isUnfocused()) && getRequestQueue().isEmpty())
                {
                    try
                    {
                        if (isIconified())
                        {
                            // Longer timeout (5 seconds) while minimized
                            // Just wake it up periodically just in case we missed a condition for notifying the thread.
                            waitForRenderingWork.wait(ICONIFIED_TIMEOUT_MILLIS);
                        }
                        else
                        {
                            // Shorter timeout (1 second) while unfocused
                            // This still reduces GPU load while still rendering regularly as a safeguard for edge cases
                            // (such as refocusing using a window other than the main window).
                            waitForRenderingWork.wait(UNFOCUSED_TIMEOUT_MILLIS);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        LOG.warn("Wait interrupted", e);
                    }
                }
            }
        }

        @Override
        public boolean shouldTerminate()
        {
            return false;
        }
    }
}

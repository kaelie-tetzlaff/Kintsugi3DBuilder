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

package kintsugi3d.builder.rendering;

import kintsugi3d.gl.core.Context;
import kintsugi3d.gl.interactive.DefaultProgressMonitor;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class GraphicsRequestManager<ContextType extends Context<ContextType>> implements GraphicsRequestQueue
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphicsRequestManager.class);

    public static final String CANCELLED_MESSAGE = "The operation was cancelled.";
    public static final String ERROR_MESSAGE = "An error occurred during the operation.";

    private final ContextType context;
    private final @NotNull Queue<Runnable> requestList;
    private final Collection<Runnable> requestAddedListeners = new CopyOnWriteArrayList<>();
    private volatile RenderableManager<ContextType, ImageBasedRenderable<ContextType>> renderableManager;
    private volatile ProgressMonitor progressMonitor;

    public GraphicsRequestManager(ContextType context)
    {
        this.context = context;
        requestList = new LinkedList<>();
    }

    /**
     * Should be called once by Rendering.
     * @param renderableManager
     */
    final void setRenderableManager(RenderableManager<ContextType, ImageBasedRenderable<ContextType>> renderableManager)
    {
        this.renderableManager = renderableManager;
    }

    /**
     * Should be called once by Rendering.
     * @param progressMonitor
     */
    final void setProgressMonitor(ProgressMonitor progressMonitor)
    {
        this.progressMonitor = progressMonitor;
    }

    @Override
    public final boolean isEmpty()
    {
        // Intentionally not synchronized to prevent blocking just for a simple isEmpty check.
        // If isEmpty is called concurrently with a request being added,
        // we'll just catch the next time through the rendering loop.
        return requestList.isEmpty();
    }

    private static void logCancellation(Throwable e)
    {
        LOG.info(CANCELLED_MESSAGE, e);
    }

    private void handleCancellation(UserCancellationException e)
    {
        logCancellation(e);

        if (progressMonitor != null)
        {
            progressMonitor.cancelComplete(e);
        }
    }

    private static void logException(@NotNull Throwable e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof UserCancellationException)
        {
            logCancellation(cause);
        }
        else
        {
            LOG.error(ERROR_MESSAGE, e);
        }
    }

    private void handleException(@NotNull Throwable e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof UserCancellationException)
        {
            handleCancellation((UserCancellationException) cause);
        }
        else
        {
            logException(e);

            if (progressMonitor != null)
            {
                progressMonitor.fail(e);
            }
        }
    }

    @Override
    public final void addRequestAddedListener(Runnable listener)
    {
        requestAddedListeners.add(listener);
    }

    @Override
    public final void addBackgroundImageBasedGraphicsRequest(@NotNull ImageBasedGraphicsRequest request)
    {
        if (renderableManager.getMainRenderable() == null)
        {
            // Instance is currently null, wait for a load and then call this function again (recursive-ish)
            renderableManager.addMainRenderableLoadCallback(instance -> addBackgroundImageBasedGraphicsRequest(request));
        }
        else
        {
            synchronized (requestList)
            {
                requestList.add(() ->
                {
                    // Check again for null, just in case
                    if (renderableManager.getMainRenderable() == null)
                    {
                        // Instance is currently null, wait for a load and then call this function again (recursive-ish)
                        renderableManager.addMainRenderableLoadCallback(instance -> addBackgroundImageBasedGraphicsRequest(request));
                    }
                    else
                    {
                        // Suppress warning about catching and not rethrowing AssertionError.
                        // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                        //noinspection ErrorNotRethrown
                        try
                        {
                            request.executeRequest(renderableManager.getMainRenderable(), new DefaultProgressMonitor());
                        }
                        catch (UserCancellationException e)
                        {
                            logCancellation(e);
                        }
                        catch (Exception | AssertionError e)
                        {
                            logException(e);
                        }
                    }
                });
            }

            // Notify listeners
            for (Runnable r :  requestAddedListeners)
            {
                r.run();
            }
        }
    }

    @Override
    public final void addImageBasedGraphicsRequest(@NotNull ImageBasedGraphicsRequest request)
    {
        if (progressMonitor.isConflictingProcess())
        {
            return;
        }

        if (renderableManager.getMainRenderable() == null)
        {
            // Instance is currently null, wait for a load and then call this function again (recursive-ish)
            renderableManager.addMainRenderableLoadCallback(instance -> addImageBasedGraphicsRequest(request));
        }
        else
        {
            synchronized (requestList)
            {
                requestList.add(() ->
                {
                    if (progressMonitor != null)
                    {
                        progressMonitor.start();
                    }

                    // Check again for null, just in case
                    if (renderableManager.getMainRenderable() == null)
                    {
                        // Instance is currently null, wait for a load and then call this function again (recursive-ish)
                        renderableManager.addMainRenderableLoadCallback(instance -> addImageBasedGraphicsRequest(request));
                    }
                    else
                    {
                        // Suppress warning about catching and not rethrowing AssertionError.
                        // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                        //noinspection ErrorNotRethrown
                        try
                        {
                            request.executeRequest(renderableManager.getMainRenderable(), progressMonitor);

                            if (progressMonitor != null)
                            {
                                progressMonitor.complete();
                            }
                        }
                        catch (UserCancellationException e)
                        {
                            handleCancellation(e);
                        }
                        catch (Exception | AssertionError e)
                        {
                            handleException(e);
                        }
                    }
                });
            }
        }

        // Notify listeners
        for (Runnable r :  requestAddedListeners)
        {
            r.run();
        }
    }

    @Override
    public final void addBackgroundTextureReplacementGraphicsRequest(@NotNull TextureReplacementGraphicsRequest request)
    {
        if (renderableManager.getMainRenderable() == null)
        {
            // Instance is currently null, wait for a load and then call this function again (recursive-ish)
            renderableManager.addMainRenderableLoadCallback(
                instance -> addBackgroundTextureReplacementGraphicsRequest(request));
        }
        else
        {
            synchronized (requestList)
            {
                requestList.add(() ->
                {
                    // Check again for null, just in case
                    ImageBasedRenderable<ContextType> mainRenderable = renderableManager.getMainRenderable();
                    if (mainRenderable == null)
                    {
                        // Instance is currently null, wait for a load and then call this function again (recursive-ish)
                        renderableManager.addMainRenderableLoadCallback(
                            instance -> addBackgroundTextureReplacementGraphicsRequest(request));
                    }
                    else
                    {
                        // Suppress warning about catching and not rethrowing AssertionError.
                        // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                        //noinspection ErrorNotRethrown
                        try
                        {
                            mainRenderable.getResources().replaceTextureResources(
                                request.executeRequest(mainRenderable, new DefaultProgressMonitor()));
                        }
                        catch (UserCancellationException e)
                        {
                            logCancellation(e);
                        }
                        catch (Exception | AssertionError e)
                        {
                            logException(e);
                        }
                    }
                });
            }

            // Notify listeners
            for (Runnable r :  requestAddedListeners)
            {
                r.run();
            }
        }
    }

    @Override
    public final void addTextureReplacementGraphicsRequest(@NotNull TextureReplacementGraphicsRequest request)
    {
        if (progressMonitor.isConflictingProcess())
        {
            return;
        }

        if (renderableManager.getMainRenderable() == null)
        {
            // Instance is currently null, wait for a load and then call this function again (recursive-ish)
            renderableManager.addMainRenderableLoadCallback(
                instance -> addTextureReplacementGraphicsRequest(request));
        }
        else
        {
            synchronized (requestList)
            {
                requestList.add(() ->
                {
                    if (progressMonitor != null)
                    {
                        progressMonitor.start();
                    }

                    // Check again for null, just in case
                    ImageBasedRenderable<ContextType> mainRenderable = renderableManager.getMainRenderable();
                    if (mainRenderable == null)
                    {
                        // Instance is currently null, wait for a load and then call this function again (recursive-ish)
                        renderableManager.addMainRenderableLoadCallback(
                            instance -> addTextureReplacementGraphicsRequest(request));
                    }
                    else
                    {
                        // Suppress warning about catching and not rethrowing AssertionError.
                        // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                        //noinspection ErrorNotRethrown
                        try
                        {

                            mainRenderable.getResources().replaceTextureResources(
                                request.executeRequest(mainRenderable, progressMonitor));

                            if (progressMonitor != null)
                            {
                                progressMonitor.complete();
                            }
                        }
                        catch (UserCancellationException e)
                        {
                            handleCancellation(e);
                        }
                        catch (Exception | AssertionError e)
                        {
                            handleException(e);
                        }
                    }
                });
            }
        }

        // Notify listeners
        for (Runnable r :  requestAddedListeners)
        {
            r.run();
        }
    }

    /**
     * Add a graphics request without a loading monitor
     *
     * @param request
     */
    @Override
    public final void addBackgroundGraphicsRequest(@NotNull GraphicsRequest request)
    {
        synchronized (requestList)
        {
            requestList.add(() ->
            {
                // Suppress warning about catching and not rethrowing AssertionError.
                // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                // noinspection ErrorNotRethrown
                try
                {
                    request.executeRequest(context);
                }
                catch (UserCancellationException e)
                {
                    logCancellation(e);
                }
                catch (Exception | AssertionError e)
                {
                    logException(e);
                }
            });
        }

        // Notify listeners
        for (Runnable r :  requestAddedListeners)
        {
            r.run();
        }
    }

    @Override
    public final void addGraphicsRequest(@NotNull ProgressMonitoredGraphicsRequest request)
    {
        synchronized (requestList)
        {
            requestList.add(() ->
            {
                if (progressMonitor != null)
                {
                    if (progressMonitor.isConflictingProcess())
                    {
                        return;
                    }

                    progressMonitor.start();
                }

                // Suppress warning about catching and not rethrowing AssertionError.
                // The request should effectively be regarded a "sandbox" where a critical logic error should not result in the application terminating.
                // noinspection ErrorNotRethrown
                try
                {
                    request.executeRequest(context, progressMonitor);

                    if (progressMonitor != null)
                    {
                        progressMonitor.complete();
                    }
                }
                catch (UserCancellationException e)
                {
                    handleCancellation(e);
                }
                catch (Exception | AssertionError e)
                {
                    handleException(e);
                }
            });
        }

        // Notify listeners
        for (Runnable r :  requestAddedListeners)
        {
            r.run();
        }
    }

    @Override
    public final void clear()
    {
        synchronized (requestList)
        {
            requestList.clear();
        }
    }

    public final void executeQueue()
    {
        context.makeContextCurrent();

        while (!requestList.isEmpty())
        {
            Runnable nextRequest;

            synchronized (requestList)
            {
                nextRequest = requestList.peek();
            }

            if (nextRequest != null)
            {
                nextRequest.run(); // Peek first to ensure that isEmpty() returns false when called from other threads.
            }

            synchronized (requestList)
            {
                // Need to double-check request identity because the request itself may have cleared the queue
                // and possibly even enqueued another request.
                if (Objects.equals(requestList.peek(), nextRequest))
                {
                    requestList.poll(); // Once the task is done, remove the request from the queue.
                }
            }
        }
    }
}

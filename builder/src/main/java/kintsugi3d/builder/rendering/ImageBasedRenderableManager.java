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

import kintsugi3d.builder.core.Global;
import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.fit.settings.ExportSettings;
import kintsugi3d.builder.io.IOHandler;
import kintsugi3d.builder.io.ReadonlyLoadOptionsModel;
import kintsugi3d.builder.io.ViewSetLoadOptions;
import kintsugi3d.builder.io.ViewSetWriterToVSET;
import kintsugi3d.builder.io.events.ProjectLoadedEvent;
import kintsugi3d.builder.io.events.ProjectLoadedListener;
import kintsugi3d.builder.io.events.ProjectProcessedEvent;
import kintsugi3d.builder.io.events.ProjectProcessedListener;
import kintsugi3d.builder.io.metashape.MetashapeChunk;
import kintsugi3d.builder.io.metashape.MetashapeModel;
import kintsugi3d.builder.rendering.components.RenderingSubject;
import kintsugi3d.builder.resources.project.GraphicsResourcesImageSpace;
import kintsugi3d.builder.resources.project.GraphicsResourcesImageSpace.Builder;
import kintsugi3d.builder.resources.project.MeshImportException;
import kintsugi3d.builder.resources.project.MissingImagesException;
import kintsugi3d.builder.resources.project.texture.TextureResources;
import kintsugi3d.builder.state.SelectableViewListModel;
import kintsugi3d.builder.state.scene.ReadonlyCameraModel;
import kintsugi3d.builder.state.scene.ReadonlyLightingEnvironmentModel;
import kintsugi3d.builder.state.scene.ReadonlyObjectPoseModel;
import kintsugi3d.builder.state.scene.ReadonlyViewpointModel;
import kintsugi3d.builder.state.settings.ReadonlyGeneralSettingsModel;
import kintsugi3d.builder.state.shader.ReadonlyActiveShaderModel;
import kintsugi3d.builder.state.shader.ShaderInfo;
import kintsugi3d.builder.util.events.EventDispatcher;
import kintsugi3d.builder.util.events.EventListeners;
import kintsugi3d.gl.builders.framebuffer.DoubleFramebufferFactory;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.geometry.VertexGeometry;
import kintsugi3d.gl.interactive.*;
import kintsugi3d.gl.vecmath.IntVector2;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.window.FramebufferCanvas;
import kintsugi3d.util.EncodableColorImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ImageBasedRenderableManager<ContextType extends Context<ContextType>>
    extends InteractiveRenderableBase<ContextType>
    implements IOHandler, RenderableManager<ContextType, ImageBasedRenderable<ContextType>>
{
    private static final Logger LOG = LoggerFactory.getLogger(ImageBasedRenderableManager.class);

    private final ContextType context;

    private final RefreshableCollection<RenderRefreshable<ContextType, ImageBasedRenderingEngine<ContextType>>> renderViews
        = new RefreshableCollection<>();
    private final Map<ShaderInfo, RenderRefreshable<ContextType, ImageBasedRenderingEngine<ContextType>>> renderViewMap
        = new HashMap<>(8);

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    private final SceneViewport sceneViewport = new ManagedSceneViewport(this);

    private volatile @Nullable ViewSet loadedViewSet;
    private volatile @Nullable VertexGeometry loadedGeometry;
    private volatile @Nullable ImageBasedRenderingEngine<ContextType> renderableInstance;
    private ProgressMonitor progressMonitor;

    private ReadonlyObjectPoseModel objectModel;
    private ReadonlyViewpointModel cameraModel;
    private ReadonlyLightingEnvironmentModel lightingModel;
    private ReadonlyGeneralSettingsModel settingsModel;
    private SelectableViewListModel viewListModel;

    // These are one-shot callbacks for queuing up graphics requests before a project is loaded
    private final List<Consumer<InteractiveRenderable<? extends Context<?>>>> instanceLoadCallbacks
        = Collections.synchronizedList(new ArrayList<>(4));

    // These are the ongoing listeners for UI synchronization
    private final EventDispatcher<ProjectLoadedListener, ProjectLoadedEvent> projectLoaded
        = new EventDispatcher<>(ProjectLoadedListener::onProjectLoaded);
    private final EventDispatcher<ProjectProcessedListener, ProjectProcessedEvent> projectProcessed
        = new EventDispatcher<>(ProjectProcessedListener::onProjectProcessed);

    public ImageBasedRenderableManager(ContextType context)
    {
        this.context = context;
    }

    private static void logMeshImportException(MeshImportException e)
    {
        LOG.error("An error occurred loading model: ", e);
    }

    private static void logGenericException(Exception e)
    {
        LOG.error("An error occurred loading project: ", e);
    }

    private static void logUserCancellation(UserCancellationException e)
    {
        LOG.info("Loading project was cancelled by user: ", e);
    }

    @Override
    public final @NotNull EventListeners<ProjectLoadedListener> projectLoadedListeners()
    {
        return projectLoaded;
    }

    @Override
    public final @NotNull EventListeners<ProjectProcessedListener> projectProcessedListeners()
    {
        return projectProcessed;
    }

    @Override
    public final boolean isRenderableLoaded()
    {
        return renderableInstance != null;
    }

    @Override
    public final @Nullable ViewSet getLoadedViewSet()
    {
        return loadedViewSet;
    }

    @Override
    public final @Nullable VertexGeometry getLoadedGeometry()
    {
        return loadedGeometry;
    }

    public final @NotNull SceneViewport getSceneViewport()
    {
        return sceneViewport;
    }

    /**
     * Adds callbacks that will be invoked when the instance has finished loading.
     * The callbacks will be cleared after being invoked.
     *
     * @param callback to add
     */
    @Override
    public final void addMainRenderableLoadCallback(Consumer<InteractiveRenderable<? extends Context<?>>> callback)
    {
        synchronized (instanceLoadCallbacks)
        {
            instanceLoadCallbacks.add(callback);
        }
    }

    /**
     * Must NOT be called on the rendering thread or deadlock will result while generating preview images.
     * @param internalName
     * @param builder
     * @throws UserCancellationException
     */
    private void loadInstance(@Nullable File newProjectFile, String internalName, @NotNull Builder<ContextType> builder)
        throws UserCancellationException
    {
        ViewSet viewSet = Objects.requireNonNull(builder.getViewSet(), "Builder must have a view set assigned.");
        loadedViewSet = viewSet;

        VertexGeometry geometry = Objects.requireNonNull(builder.getGeometry(), "Builder must have geometry assigned.");
        loadedGeometry = geometry;

        int gpuBufferSize = viewSet.getGPUBufferSize();
        if (gpuBufferSize > 1024 && progressMonitor != null)
        {
            IOException e = new IOException(String.format("Dataset has %d cameras, which exceeds 1024 and may fail on many graphics cards.", gpuBufferSize));
            progressMonitor.warn(e);
        }
        boolean hasUnsupportedCorrections = viewSet.hasUnsupportedCorrections();
        if (hasUnsupportedCorrections && progressMonitor != null)
        {
            IOException e = new IOException("This project uses 'Fit additional corrections' which are not supported.");
            progressMonitor.warn(e);
        }

        // getViews returns a copy
        // Grab all views (not just enabled) for light calibration
        // TODO this is for the light calibration sidebar and should probably be migrated to a newer system
        // TODO   for managing the list of photos (like the photos tab)
        Global.state().getViewListModel().setViewList(viewSet.getViewsSorted());

        if (newProjectFile != null)
        {
            // Save the project before proceeding.
            // Hypothetically if any textures existed already they would be saved out asynchronously
            // but that shouldn't matter regardless since we just need to have a valid project file path right now.
            try
            {
                Global.io().saveProject(newProjectFile);
            }
            catch (IOException e)
            {
                LOG.error("Error saving project", e);
            }
        }
        // If no project file is specified, don't save the project but attempt to continue loading.
        // This might result in some weird behavior but in theory could be successful.
        // Many directory paths have fallbacks that aren't in a supporting files directory.
        // In theory, this shouldn't ever happen since the UI should ideally prevent this.

        if (progressMonitor != null)
        {
            progressMonitor.setStageCount(2);
            progressMonitor.setStage(0, "Generating preview-resolution images...");
        }

        try
        {
            // Generate preview resolution images and thumbnails
            builder.generateAllPreviewImages();
        }
        catch (IOException e)
        {
            LOG.error("One or more images failed to load", e);
        }

        // Create the instance (will be initialized on the graphics thread)
        ImageBasedRenderingEngine<ContextType> newInstance = new ImageBasedRenderingEngine<>(internalName, context, builder);
        newInstance.setOwningApp(getOwningApp());

        SceneModel newInstanceSceneModel = newInstance.getSceneModel();
        newInstanceSceneModel.setCameraModel(cameraModel);
        initializeSceneModel(newInstanceSceneModel);

        newInstance.setProgressMonitor(new BackendProgressMonitor(newInstance, progressMonitor));

        // Use the runLater system so that the rendering loop knows that an operation that might take longer is queued.
        Rendering.runLater(() ->
        {
            // If a new instance was just loaded, initialize it.
            try
            {
                newInstance.initialize();

                // Check for an old instance just to be safe
                ImageBasedRenderingEngine<ContextType> oldInstance = renderableInstance;
                if (oldInstance != null)
                {
                    oldInstance.close();
                }

                // Use the new instance as the active instance if initialization was successful
                renderableInstance = newInstance;

                synchronized (instanceLoadCallbacks)
                {
                    // Invoke callbacks
                    for (Consumer<InteractiveRenderable<? extends Context<?>>> callback : instanceLoadCallbacks)
                    {
                        callback.accept(renderableInstance);
                    }

                    // Clear the list of callbacks for the next load.
                    instanceLoadCallbacks.clear();
                }

                // Notify listeners that project has loaded
                projectLoaded.notify(new ProjectLoadedEvent(geometry.getBoundingBoxSize()));

                GraphicsResourcesImageSpace<ContextType> resources =
                    Objects.requireNonNull(newInstance.getResources(), "Builder did not yield a valid renderable instance.");
                if (resources.hasProcessedWeightMaps())
                {
                    // Project has been processed previously; notify listeners
                    IntVector2 weightMapResolution = resources.getProcessedWeightMapResolution();
                    projectProcessed.notify(new ProjectProcessedEvent(
                        weightMapResolution.x, weightMapResolution.y));
                }

                // Ensure that the listeners are also notified if the project is processed in the future.
                resources.weightMapsProcessedListeners().register(projectProcessed::notify);

                // Update once before drawing
                newInstance.update();

                // Force GPU resources to be fully loaded / flushed by rendering once to a small throwaway FBO.
                try(FramebufferObject<ContextType> tempFBO = context.buildFramebufferObject(256, 256)
                    .addColorAttachment()
                    .addDepthAttachment()
                    .createFramebufferObject())
                {
                    newInstance.draw(tempFBO);
                }
            }
            catch (InitializationException e)
            {
                if (e.getCause() instanceof UserCancellationException)
                {
                    LOG.info("Cancellation occurred while initializing new instance.", e);
                }
                else
                {
                    LOG.error("Error occurred while initializing new instance.", e);
                }

                newInstance.close();
            }
        });
    }

    /**
     * Does not initialize camera model.
     * @param sceneModel
     */
    private void initializeSceneModel(@NotNull SceneModel sceneModel)
    {
        sceneModel.setObjectModel(objectModel);
        sceneModel.setLightingModel(lightingModel);
        sceneModel.setSettingsModel(settingsModel);
        sceneModel.setCameraViewListModel(viewListModel);
    }

    @Override
    public final void loadFromVSETFile(
        String internalName, @NotNull File vsetFile, File supportingFilesDirectory, ReadonlyLoadOptionsModel loadOptions)
        throws UserCancellationException, MeshImportException, IOException
    {
        if (progressMonitor.isConflictingProcess())
        {
            return;
        }

        progressMonitor.start();
        progressMonitor.setProcessName("Load from File");

        try
        {
            Builder<ContextType> builder;
            try
            {
                builder = GraphicsResourcesImageSpace.getBuilderForContext(context)
                    .setProgressMonitor(progressMonitor)
                    .setImageLoadOptions(loadOptions)
                    .loadVSETFile(vsetFile, supportingFilesDirectory);
            }
            catch (MeshImportException e)
            {
                logMeshImportException(e);
                throw e;
            }
            catch (IOException|RuntimeException e)
            {
                logGenericException(e);
                throw e;
            }

            loadInstance(null, internalName, builder);
        }
        catch (UserCancellationException e)
        {
            logUserCancellation(e);
            throw e;
        }
    }

    @Override
    public final void loadFromMetashapeModel(File newProjectFile, @NotNull MetashapeModel model, ReadonlyLoadOptionsModel loadOptionsModel)
        throws UserCancellationException, MissingImagesException, MeshImportException, IOException, XMLStreamException
    {

        if (progressMonitor.isConflictingProcess())
        {
            return;
        }

        progressMonitor.start();
        progressMonitor.setProcessName("Load from Agisoft Project");

        MetashapeChunk parentChunk = model.getChunk();

        try
        {
            Builder<ContextType> builder;
            try
            {
                String orientationView = model.getLoadPreferences().getOrientationViewName();
                double rotation = model.getLoadPreferences().getOrientationViewRotateDegrees();

                builder = GraphicsResourcesImageSpace.getBuilderForContext(context)
                    .setProgressMonitor(progressMonitor)
                    .setImageLoadOptions(loadOptionsModel)
                    .loadFromMetashapeModel(model)
                    .setOrientationView(orientationView, rotation);
            }
            catch (MeshImportException e)
            {
                logMeshImportException(e);
                throw e;
            }
            catch (IOException | RuntimeException | XMLStreamException | MissingImagesException e)
            {
                logGenericException(e);
                throw e;
            }

            loadInstance(newProjectFile, parentChunk.getFramePath(), builder);
        }
        catch (UserCancellationException e)
        {
            logUserCancellation(e);
            throw e;
        }
    }

    @Override
    public final void loadFromLooseFiles(File newProjectFile, String internalName, @NotNull File xmlFile,
                                         @NotNull ViewSetLoadOptions viewSetLoadOptions, ReadonlyLoadOptionsModel imageLoadOptions)
        throws Exception
    {
        if (progressMonitor.isConflictingProcess())
        {
            return;
        }
        progressMonitor.start();
        progressMonitor.setProcessName("Load from loose files");

        try
        {
            Builder<ContextType> builder;
            try
            {
                builder = GraphicsResourcesImageSpace.getBuilderForContext(context)
                    .setProgressMonitor(progressMonitor)
                    .setImageLoadOptions(imageLoadOptions)
                    .loadLooseFiles(xmlFile, viewSetLoadOptions);
            }
            catch (Exception e)
            {
                logGenericException(e);

                //noinspection ProhibitedExceptionThrown
                throw e; // Re-throw so that calling method can rollback any state changes to account for failure
            }

            // Invoke callbacks now that view set is loaded
            loadInstance(newProjectFile, internalName, builder);
        }
        catch (UserCancellationException e)
        {
            logUserCancellation(e);
            throw e; // Re-throw so that calling method can rollback any state changes to account for cancellation
        }
    }

    public final @NotNull RefreshableCollection<RenderRefreshable<ContextType, ImageBasedRenderingEngine<ContextType>>> getRenderViews()
    {
        return renderViews;
    }

    @Override
    public final void addRenderView(@NotNull ShaderInfo shader, @NotNull FramebufferSize viewSize,
                                    int safeLeftPadding, int safeTopPadding, int safeRightPadding, int safeBottomPadding,
                                    @NotNull Consumer<FramebufferCanvas<?>> framebufferCallback)
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            // Create a new rendering engine instance that references the same resources as the main rendering engine.
            // This can run on any thread, but initialization needs to run on the graphics thread.
            ImageBasedRenderingEngine<ContextType> renderView =
                new ImageBasedRenderingEngine<>(currentInstance.getInternalName(), context, currentInstance.getResources());

            RenderViewCameraModel renderViewCameraModel = new RenderViewCameraModel(cameraModel);
            renderViewCameraModel.setPadding(safeLeftPadding, safeTopPadding, safeRightPadding, safeBottomPadding);

            SceneModel renderViewSceneModel = renderView.getSceneModel();
            renderViewSceneModel.setCameraModel(renderViewCameraModel);
            initializeSceneModel(renderViewSceneModel);

            Rendering.runLater(() ->
            {
                // Create framebuffer
                DoubleFramebufferObject<ContextType> framebuffer =
                    DoubleFramebufferFactory.create(context, viewSize.width, viewSize.height);

                // Create and initialize refreshable, which will manage the framebuffer object
                var refreshable = RenderRefreshable.createWithManagedFrambufferObject(context, renderView, framebuffer);

                try
                {
                    // Pre-initialize here so that we can set the shader right away.
                    // RefreshableCollection will just skip this one when processing its initialize queue
                    // as it sees that it is already initialized.
                    refreshable.initialize();
                }
                catch (InitializationException e)
                {
                    LOG.error("Error initialing render view for shader: {}", shader.getFullName(), e);
                }

                // Set to use the specified shader.
                RenderingSubject<ContextType> subject = renderView.getSubject();
                subject.setExtraFragmentShaderDefinesFactory(shader::getDefines);
                subject.useFragmentShader(shader.getFile());

                // render views will defer actually adding it to its main list until its own refresh call
                // so it can safely be added here in the processing of the runLater queue.
                renderViews.add(refreshable);

                // Also store it in a map so that we can find it by UserShader when it needs to be removed.
                renderViewMap.put(shader, refreshable);

                framebufferCallback.accept(FramebufferCanvas.createUsingExistingFramebuffer(framebuffer));
            });
        }
    }

    @Override
    public final ImageBasedRenderingEngine<ContextType> getRenderableForShader(ShaderInfo shader)
    {
        return renderViewMap.get(shader).getRenderable();
    }

    @Override
    public final void removeRenderView(ShaderInfo shader)
    {
        var renderViewToRemove = renderViewMap.get(shader);

        if (renderViewToRemove != null)
        {
            // This adds the render view to the refreshable collection's terminate queue so it will get cleaned up.
            renderViews.remove(renderViewToRemove);
        }
    }

    private void requestFragmentShader(@NotNull ShaderInfo shaderInfo)
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getDynamicResourceManager().requestFragmentShader(shaderInfo.getFile(), shaderInfo::getDefines);
        }
    }

    @Override
    public final @Nullable ImageBasedRenderable<ContextType> getMainRenderable()
    {
        return renderableInstance;
    }

    @Override
    public final void setProgressMonitor(ProgressMonitor progressMonitor)
    {
        this.progressMonitor = progressMonitor;
    }

    public final void setCameraViewListModel(SelectableViewListModel viewListModel)
    {
        this.viewListModel = viewListModel;
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getSceneModel().setCameraViewListModel(viewListModel);
        }
    }

    public final void setObjectModel(ReadonlyObjectPoseModel objectModel)
    {
        this.objectModel = objectModel;
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getSceneModel().setObjectModel(objectModel);
        }
    }

    public final void setCameraModel(ReadonlyViewpointModel cameraModel)
    {
        this.cameraModel = cameraModel;
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getSceneModel().setCameraModel(cameraModel);
        }
    }

    public final void setLightingModel(ReadonlyLightingEnvironmentModel lightingModel)
    {
        this.lightingModel = lightingModel;
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getSceneModel().setLightingModel(lightingModel);
        }
    }

    public final void setUserShaderModel(@NotNull ReadonlyActiveShaderModel userShaderModel)
    {
        userShaderModel.registerHandler(this::requestFragmentShader);
    }

    public final void setSettingsModel(ReadonlyGeneralSettingsModel settingsModel)
    {
        this.settingsModel = settingsModel;
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getSceneModel().setSettingsModel(settingsModel);
        }
    }

    @Override
    public final @Nullable EncodableColorImage loadEnvironmentMap(File environmentMapFile) throws FileNotFoundException
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            return currentInstance.getDynamicResourceManager().loadEnvironmentMap(environmentMapFile);
        }
        else
        {
            return null;
        }
    }

    @Override
    public final void loadBackplate(File backplateFile) throws FileNotFoundException
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.getDynamicResourceManager().loadBackplate(backplateFile);
        }
    }

    @Override
    public final void saveToVSETFile(@NotNull File vsetFile) throws IOException
    {
        ViewSetWriterToVSET.getInstance().writeToFile(Objects.requireNonNull(loadedViewSet, "No view set is currently loaded."), vsetFile);
    }

    @Override
    public final void saveAllMaterialFiles(@NotNull File materialDirectory, @Nullable Runnable finishedCallback)
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance == null || currentInstance.getResourcesOrNull() == null)
        {
            if (finishedCallback != null)
            {
                finishedCallback.run();
            }
        }
        else
        {
            TextureResources<ContextType> textureResources = currentInstance.getResources().getTextureResources();
            if (textureResources == null)
            {
                if (finishedCallback != null)
                {
                    finishedCallback.run();
                }
            }
            else
            {
                Rendering.runLater(() ->
                {
                    textureResources.saveAll(materialDirectory);

                    if (finishedCallback != null)
                    {
                        finishedCallback.run();
                    }
                });
            }
        }
    }

    @Override
    public final void saveGLTF(@NotNull File outputDirectory, @NotNull ExportSettings settings)
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.saveGLTF(outputDirectory, settings);
        }
    }

    @Override
    public final void unload(@NotNull Runnable onUnloadComplete)
    {
        // Use the runLater system so that the rendering loop knows that an operation that might take longer is queued.
        Rendering.runLater(() ->
        {
            ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
            if (currentInstance != null)
            {
                // Empty sidebar; will be repopulated when another project is opened.
                // TODO rework this; might be a race condition and also isn't the best dependency
                Global.state().getTabModels().clearTabs();

                // Also remove render views which will be tied to the loaded project.
                renderViewMap.clear();
                renderViews.clear();

                currentInstance.close();
                renderableInstance = null;
                loadedViewSet = null;
                loadedGeometry = null;
            }

            onUnloadComplete.run();
        });
    }

    @Override
    public final void initialize() throws InitializationException
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.initialize();
        }
    }

    @Override
    public final void update()
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.update();
        }
    }

    @Override
    public final void draw(Framebuffer<ContextType> framebuffer)
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.draw(framebuffer);
        }
    }

    @Override
    public final void close()
    {
        ImageBasedRenderingEngine<ContextType> currentInstance = renderableInstance;
        if (currentInstance != null)
        {
            currentInstance.close();
        }
    }

    private static final class RenderViewCameraModel implements ReadonlyCameraModel
    {
        private final ReadonlyViewpointModel cameraModel;
        private int paddingLeft = 0;
        private int paddingTop = 0;
        private int paddingRight = 0;
        private int paddingBottom = 0;

        RenderViewCameraModel(ReadonlyViewpointModel cameraModel)
        {
            this.cameraModel = cameraModel;
        }

        @Override
        public Matrix4 getLookMatrix()
        {
            return cameraModel.getLookMatrix();
        }

        @Override
        public float getHorizontalFOV()
        {
            return cameraModel.getHorizontalFOV();
        }

        @Override
        public boolean isOrthographic()
        {
            return cameraModel.isOrthographic();
        }

        @Override
        public int getPaddingLeft()
        {
            return paddingLeft;
        }

        @Override
        public int getPaddingTop()
        {
            return paddingTop;
        }

        @Override
        public int getPaddingRight()
        {
            return paddingRight;
        }

        @Override
        public int getPaddingBottom()
        {
            return paddingBottom;
        }

        void setPadding(int left, int top, int right, int bottom)
        {
            paddingLeft = left;
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
        }
    }
}

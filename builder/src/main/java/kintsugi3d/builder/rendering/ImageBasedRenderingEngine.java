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
import kintsugi3d.builder.core.texture.TextureInfo;
import kintsugi3d.builder.core.viewset.ReadonlyViewSet;
import kintsugi3d.builder.core.viewset.View;
import kintsugi3d.builder.core.viewset.ViewSet;
import kintsugi3d.builder.fit.decomposition.BasisMaterialInfo;
import kintsugi3d.builder.fit.settings.ExportSettings;
import kintsugi3d.builder.io.events.ProjectLoadedEvent;
import kintsugi3d.builder.io.events.ProjectLoadedListener;
import kintsugi3d.builder.io.gltf.ModelExporter;
import kintsugi3d.builder.rendering.components.RenderingSubject;
import kintsugi3d.builder.rendering.components.StandardScene;
import kintsugi3d.builder.rendering.components.lightcalibration.LightCalibration3DScene;
import kintsugi3d.builder.rendering.components.lightcalibration.LightCalibrationRoot;
import kintsugi3d.builder.rendering.components.lit.LitRoot;
import kintsugi3d.builder.rendering.components.snap.ViewSelection;
import kintsugi3d.builder.rendering.components.snap.ViewSelectionImpl;
import kintsugi3d.builder.rendering.components.split.SplitScreenComponent;
import kintsugi3d.builder.resources.DynamicResourceLoader;
import kintsugi3d.builder.resources.DynamicResourceManager;
import kintsugi3d.builder.resources.project.GraphicsResourcesImageSpace;
import kintsugi3d.builder.resources.project.GraphicsResourcesImageSpace.Builder;
import kintsugi3d.builder.resources.project.texture.ReadonlyTextureResources;
import kintsugi3d.builder.state.cards.CardsModel;
import kintsugi3d.builder.state.cards.TabsHelper;
import kintsugi3d.builder.util.events.EventDispatcher;
import kintsugi3d.builder.util.events.EventListeners;
import kintsugi3d.builder.util.events.MappedChange;
import kintsugi3d.builder.util.events.Observer;
import kintsugi3d.gl.builders.framebuffer.ColorAttachmentSpec;
import kintsugi3d.gl.builders.framebuffer.DepthAttachmentSpec;
import kintsugi3d.gl.core.*;
import kintsugi3d.gl.geometry.ReadonlyVertexGeometry;
import kintsugi3d.gl.interactive.InitializationException;
import kintsugi3d.gl.interactive.InteractiveRenderableBase;
import kintsugi3d.gl.interactive.ProgressMonitor;
import kintsugi3d.gl.interactive.UserCancellationException;
import kintsugi3d.gl.vecmath.Matrix3;
import kintsugi3d.gl.vecmath.Matrix4;
import kintsugi3d.gl.vecmath.Vector3;
import kintsugi3d.util.SRGB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class ImageBasedRenderingEngine<ContextType extends Context<ContextType>>
    extends InteractiveRenderableBase<ContextType> implements ImageBasedRenderable<ContextType>
{
    private static final Logger LOG = LoggerFactory.getLogger(ImageBasedRenderingEngine.class);

    private final ContextType context;

    private volatile ProgressMonitor progressMonitor;
    private boolean logErrors = true;

    private final @Nullable Builder<ContextType> resourceBuilder;
    private @Nullable GraphicsResourcesImageSpace<ContextType> resources;
    private final boolean managedResources;

    private VertexBuffer<ContextType> rectangleVertices;

    private final String internalName;

    private final @NotNull SceneModel sceneModel;

    private ProgramObject<ContextType> simpleTexProgram;
    private DrawableResource<ContextType> simpleTexDrawable;

    private SplitScreenComponent<ContextType> lightCalibrationSplitScreen;
    private LightCalibrationRoot<ContextType> lightCalibration;
    private LitRoot<ContextType> litRoot;
    private LitRoot<ContextType> lightCalibration3DRoot;

    private RenderingSubject<ContextType> subject;

    private DynamicResourceLoader<ContextType> dynamicResourceLoader;
    private final @NotNull SceneViewportModel sceneViewportModel;

    private static final int SHADING_FRAMEBUFFER_COUNT = 2;
    private final Collection<FramebufferObject<ContextType>> shadingFramebuffers = new ArrayList<>(SHADING_FRAMEBUFFER_COUNT);

    private final EventDispatcher<ProjectLoadedListener, ProjectLoadedEvent> projectLoaded
        = new EventDispatcher<>(ProjectLoadedListener::onProjectLoaded);

    private boolean loaded = false;

    ImageBasedRenderingEngine(String internalName, ContextType context, @Nullable Builder<ContextType> resourceBuilder)
    {
        this.internalName = internalName;
        this.context = context;
        this.resourceBuilder = resourceBuilder;
        managedResources = true;
        sceneModel = new SceneModel();
        sceneViewportModel = createSceneViewportModel(sceneModel);
    }

    ImageBasedRenderingEngine(String internalName, ContextType context, @Nullable GraphicsResourcesImageSpace<ContextType> resources)
    {
        this.internalName = internalName;
        this.context = context;
        this.resources = resources;
        resourceBuilder = null;
        managedResources = false;
        sceneModel = new SceneModel();
        sceneViewportModel = createSceneViewportModel(sceneModel);
    }

    private static <K, V> @NotNull Observer<MappedChange<K, V>> getMappedChangeObserver(
        @NotNull TabsHelper tabsHelper, String tabName, Class<V> dataClass, @NotNull Function<String, K> keyConstructor)
    {
        return change ->
        {
            CardsModel<V> tab = Global.state().getTabModels().getTab(tabName, dataClass);
            if (tab != null)
            {
                switch (change.getType())
                {
                    case ADDED:
                        tabsHelper.refreshTab(tabName); // TODO implement support for adding individual card without rebuilding
                        break;
                    case REMOVED:
                        tab.deleteCards(card -> change.getMap().get(keyConstructor.apply(card.getInternalName())) != null);
                        break;
                    case MODIFIED:
                        tab.refreshCards(card -> change.getMap().get(keyConstructor.apply(card.getInternalName())));
                        break;
                }
            }
            else
            {
                LOG.warn("Tab not found: {}", tabName);
            }
        };
    }

    private static <V> @NotNull Observer<MappedChange<String, V>> getMappedChangeObserver(
        @NotNull TabsHelper tabsHelper, String tabName, Class<V> dataClass)
    {
        return getMappedChangeObserver(tabsHelper, tabName, dataClass, Function.identity());
    }

    public final boolean isLoaded()
    {
        return loaded;
    }

    private static @NotNull SceneViewportModel createSceneViewportModel(SceneModel sceneModel)
    {
        SceneViewportModel sceneViewportModel = new SceneViewportModel(sceneModel);
        sceneViewportModel.addSceneObjectType("SceneObject");
        return sceneViewportModel;
    }

    @Override
    public final String getInternalName()
    {
        return internalName;
    }

    public final @NotNull EventListeners<ProjectLoadedListener> projectLoadedListeners()
    {
        return projectLoaded;
    }

    public final RenderingSubject<ContextType> getSubject()
    {
        return subject;
    }

    @Override
    public final @NotNull GraphicsResourcesImageSpace<ContextType> getResources()
    {
        return Objects.requireNonNull(resources, "Renderable has not been initialized.");
    }

    public final @Nullable GraphicsResourcesImageSpace<ContextType> getResourcesOrNull()
    {
        return resources;
    }

    @Override
    public final DynamicResourceManager getDynamicResourceManager()
    {
        return dynamicResourceLoader;
    }

    @Override
    public final void initialize() throws InitializationException
    {
        try
        {
            simpleTexProgram = context.getShaderProgramBuilder()
                    .addShader(ShaderType.VERTEX, new File(new File(new File("shaders"), "common"), "texture.vert"))
                    .addShader(ShaderType.FRAGMENT, new File(new File(new File("shaders"), "common"), "texture.frag"))
                    .createProgram();

            rectangleVertices = context.createRectangle();

            if (resourceBuilder != null)
            {
                resources = resourceBuilder
                    .setProgressMonitor(progressMonitor) // Use the progress monitor that offsets the stage count if generating preview images
//                .generateUndistortedPreviewImages()
                    .create();

                context.flush();
            }
            assert resources != null;

            simpleTexDrawable = context.createDrawable(simpleTexProgram);
            simpleTexDrawable.addVertexBuffer("position", rectangleVertices);

            ViewSelection viewSelection = new ViewSelectionImpl(getViewSet(), sceneModel);

            lightCalibration = new LightCalibrationRoot<>(resources, sceneModel, viewSelection, sceneViewportModel);
            lightCalibration.initialize();

            StandardScene<ContextType> scene = new StandardScene<>(resources, sceneModel, sceneViewportModel);
//            scene.setLightVisualsEnabled(true); // Enable light visuals when not in light calibration mode
            litRoot = new LitRoot<>(context, sceneModel, scene);
            litRoot.initialize();
            litRoot.setShadowCaster(resources.getGeometryResources().getPositionBuffer());

            LightCalibration3DScene<ContextType> lightCalibScene =
                new LightCalibration3DScene<>(resources, sceneModel, sceneViewportModel, viewSelection);
            lightCalibration3DRoot = new LitRoot<>(context, sceneModel, lightCalibScene);
            lightCalibration3DRoot.initialize();
            lightCalibration3DRoot.setShadowCaster(resources.getGeometryResources().getPositionBuffer());

            lightCalibrationSplitScreen = new SplitScreenComponent<>(lightCalibration, lightCalibration3DRoot);

            subject = scene.getSubject();
            dynamicResourceLoader = new DynamicResourceLoader<>(progressMonitor,
                context, subject, litRoot.getLightingResources());

            updateWorldSpaceDefinition();

            FramebufferSize framebufferSize = context.getDefaultFramebuffer().getSize();
            FramebufferObject<ContextType> firstShadingFBO =
                context.buildFramebufferObject(framebufferSize.width, framebufferSize.height)
                    .addColorAttachment(
                        ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGB8)
                            .setLinearFilteringEnabled(true))
                    .addDepthAttachment()
                    .createFramebufferObject();

            shadingFramebuffers.add(firstShadingFBO);

            // Render an entire frame to an offscreen framebuffer before announcing that loading is complete.
            // TODO break this into blocks just in case there's a GPU timeout?
//            litRoot.draw(firstShadingFBO, sceneModel.getCurrentViewMatrix(), getProjectionMatrix(framebufferSize));
//
//            // Flush to prevent timeout
//            context.flush();

            ViewSet viewSet = getViewSet();

            // Wait to refresh the tabs manager until we're about to actually initialize the new instance.
            TabsHelper tabsHelper = new TabsHelper(resources);
            tabsHelper.rebuildTabs();

            // Register observer for changes in each tab
            viewSet.viewsChangedListeners().register(
                getMappedChangeObserver(tabsHelper, TabsHelper.PHOTOS, View.class, File::new));
            resources.basisListeners().register(
                getMappedChangeObserver(tabsHelper, TabsHelper.MATERIALS, BasisMaterialInfo.class));
            resources.texturesListeners().register(
                getMappedChangeObserver(tabsHelper, TabsHelper.TEXTURES, TextureInfo.class));
            // Currently the shaders tab is not expected to have changes other than when textures are processed,
            // which is handled separately via an event listener.

            resources.texturesReplacedListeners().register(
                event ->
            {
                // Reload shaders in case preprocessor constants (i.e. number of basis functions) have changed
                reloadShaders();

                new TabsHelper(resources).refreshAllTabs();
            });
        }
        catch (UserCancellationException e)
        {
            LOG.info("User cancelled operation while initializing ProjectRenderingEngine:", e);
            close();
            if (progressMonitor != null)
            {
                progressMonitor.cancelComplete(e);
            }
            throw new InitializationException(e);
        }
        catch (RuntimeException|IOException e)
        {
            LOG.error("Error occurred initializing ProjectRenderingEngine:", e);
            close();
            if (progressMonitor != null)
            {
                progressMonitor.fail(e);
            }
            throw new InitializationException(e);
        }
    }

    @Override
    public final void update()
    {
        try
        {
            dynamicResourceLoader.update();
            litRoot.update();
            lightCalibration.update();
            lightCalibration3DRoot.update();
        }
        catch (RuntimeException e)
        {
            LOG.error("Error occurred during update:", e);
        }

        updateWorldSpaceDefinition();
    }

    private void updateWorldSpaceDefinition()
    {
        ReadonlyVertexGeometry geometry = getResources().getGeometry();
        if (geometry != null)
        {
            ReadonlyViewSet viewSet = getViewSet();

            View orientationView = viewSet.getOrientationView();

            // Inspection suppressed for now; remove suppression once scale handling has been investigated more.
            //noinspection IfStatementWithIdenticalBranches
            if (orientationView == null) // check for override
            {
                // Imported orientation and object center if no override
                // For now, this is all that we're importing from Metashape;
                // everything else should be the same as with a reference image override.
                // This might change in the future.
                sceneModel.setOrientation(Objects.requireNonNullElse(viewSet.getOrientationMatrix(), Matrix3.IDENTITY));

                // COMMENTED OUT: Object translation doesn't seem to be that meaningful coming from Metashape.
//                    sceneModel.setCentroid(sceneModel.getOrientation().transpose()
//                        .times(Objects.requireNonNullElse(viewSet.getObjectTranslation(), Vector3.ZERO).negated()));

                // Just use true centroid instead
                sceneModel.setCentroid(geometry.getCentroid());

                // TODO figure out if we can use imported scale for user interaction without breaking things.
//                    // "Scene scale" is generally taken by the Kintsugi renderer to be world space to model space
//                    // so we need to invert the imported global scale.
//                    sceneModel.setScale(1.0f / viewSet.getObjectScale());
//                    sceneModel.setScale((geometry.getBoundingRadius() + geometry.getCentroid().length()) * 2);
                sceneModel.setScale(geometry.getBoundingRadius() * 2);
            }
            else
            {
                // reference image based override, replaces any imported reference frame
                // use centroid and scale based on geometry assuming the imported scale and center is invalid
                Matrix3 referenceCameraPose = orientationView.getCameraPose().getUpperLeft3x3();
                sceneModel.setOrientation(Matrix3.rotateZ(Math.toRadians(-viewSet.getOrientationViewRotationDegrees()))
                    .times(referenceCameraPose));
                sceneModel.setCentroid(geometry.getCentroid());
                sceneModel.setScale(geometry.getBoundingRadius() * 2);
            }
        }
    }

    private @NotNull Matrix4 getProjectionMatrix(@NotNull FramebufferSize size)
    {
        float scale = sceneModel.getScale();

        return Matrix4.perspective(sceneModel.getVerticalFieldOfView(size),
                (float)size.width / (float)size.height,
                0.01f * scale, 100.0f * scale);
    }

    private @NotNull Vector3 calculateClearColor()
    {
        float maxLuminance = (float) SRGB.fromLinear(getResources().getViewSet().getLuminanceEncoding().getDecodeFunction().applyAsDouble(255.0));
        return new Vector3(
                sceneModel.getLightingModel().getBackgroundColor().x / maxLuminance,
                sceneModel.getLightingModel().getBackgroundColor().y / maxLuminance,
                sceneModel.getLightingModel().getBackgroundColor().z / maxLuminance);
    }

    @Override
    public final void draw(@NotNull Framebuffer<ContextType> framebuffer, @Nullable Matrix4 modelViewOverride, @Nullable Matrix4 projectionOverride,
                           int subdivWidth, int subdivHeight)
    {
        try
        {
            if(sceneModel.getSettingsModel().getBoolean("multisamplingEnabled"))
            {
                context.getState().enableMultisampling();
            }
            else
            {
                context.getState().disableMultisampling();
            }

            context.getState().enableBackFaceCulling();

            FramebufferSize size = framebuffer.getSize();

            int paddingLeft = Global.state().getCameraModel().getPaddingLeft();
            int paddingRight = Global.state().getCameraModel().getPaddingRight();
            int paddingTop = Global.state().getCameraModel().getPaddingTop();
            int paddingBottom = Global.state().getCameraModel().getPaddingBottom();

            Matrix4 projection;
            if (projectionOverride != null)
            {
                // Ignore safe region if the projection was overridden.
                projection = projectionOverride;
            }
            else if (paddingLeft != 0 || paddingTop != 0 || paddingRight != 0 || paddingBottom != 0)
            {
                FramebufferSize safeSize = new FramebufferSize(
                    size.width - paddingLeft - paddingRight,
                    size.height - paddingTop - paddingBottom);

                projection =
                    // After scaling from safe clip space to actual FBO clip space,
                    // translate the origin to the location of the safe clip space center in FBO clip space.
                    // This translation needs to be in normalized device coordinates [-1, 1].
                    // Adding the offsets (+L and -R; +T and -B), negating (-L + +R; -T + +B),
                    // and dividing by the FBO size effectively gives us the center point in NDC
                    // (dividing by two to get the average negative offset would have given the center point in a [0, 1] range,
                    // which is cancelled out by multiplying by 2 before subtracting 1 to get to NDC)
                    Matrix4.translate(
                        new Vector3(
                            (float)(paddingRight - paddingLeft) / (float)size.width,
                            (float)(paddingBottom - paddingTop) / (float)size.height,
                            0))
                    // If the safe region is smaller than the full framebuffer, then scale down in clip space accordingly
                    // so that the content that should be visible is contained within that region.
                    .times(Matrix4.scale(
                        (float)safeSize.width / (float)size.width,
                        (float)safeSize.height / (float)size.height,
                        1.0f))
                    .times(getProjectionMatrix(safeSize));
            }
            else
            {
                projection = getProjectionMatrix(size);
            }

            int fboWidth = size.width;
            int fboHeight = size.height;

            if (sceneModel.getSettingsModel().getBoolean("halfResolutionEnabled"))
            {
                fboWidth /= 2;
                fboHeight /= 2;
            }

            try
            (
                FramebufferObject<ContextType> offscreenFBO = context.buildFramebufferObject(fboWidth, fboHeight)
                        .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.RGB8)
                            .setLinearFilteringEnabled(true))
                        .addColorAttachment(ColorAttachmentSpec.createWithInternalFormat(ColorFormat.R8UI))
                        .addDepthAttachment(DepthAttachmentSpec.createFixedPointWithPrecision(24))
                        .createFramebufferObject()
            )
            {
                offscreenFBO.clearIntegerColorBuffer(1, 0, 0, 0, 0);
                offscreenFBO.clearDepthBuffer();

                // Calculate clear color, clear the offscreen FBO and update the clear color on the scene model
                // for components that reference it (like environment & backplate)
                Vector3 clearColor = calculateClearColor();
                offscreenFBO.clearColorBuffer(0, clearColor.x, clearColor.y, clearColor.z, 1.0f);
                sceneModel.setClearColor(clearColor);

                Matrix4 view = modelViewOverride != null ? sceneModel.getViewFromModelViewMatrix(modelViewOverride)
                        : sceneModel.getCurrentViewMatrix();

                if (sceneModel.getSettingsModel().getBoolean("lightCalibrationMode"))
                {
                    // Split needs to be updated every time as FBO width may have changed.
                    lightCalibrationSplitScreen.setSplit(0.5f, fboWidth);
                    lightCalibrationSplitScreen.drawInSubdivisions(offscreenFBO, subdivWidth, subdivHeight, view, projection);
                }
                else
                {
                    litRoot.drawInSubdivisions(offscreenFBO, subdivWidth, subdivHeight, view, projection);
                }

                // Second pass at full resolution to default framebuffer
                simpleTexDrawable.program().setTexture("tex", offscreenFBO.getColorAttachmentTexture(0));

                framebuffer.clearDepthBuffer();
                simpleTexDrawable.draw(PrimitiveMode.TRIANGLE_FAN, framebuffer);

                context.flush();

                if (!loaded)
                {
                    // First frame drawn successfully.
                    loaded = true;

                    projectLoaded.notify(new ProjectLoadedEvent(getGeometry().getBoundingBoxSize()));

                    // Zero vector signals that the light requires calibration.
                    // TODO support multiple light sources.
                    if (Vector3.ZERO.equals(getViewSet().getLightIntensity(0)))
                    {
                        GraphicsResourcesImageSpace<ContextType> validatedResources = getResources();
                        validatedResources.getViewSet().setAllLightIntensities(validatedResources.determineGlobalLightIntensity());
                    }

                    if (progressMonitor != null)
                    {
                        progressMonitor.complete();
                    }
                }
            }
        }
        catch(RuntimeException e)
        {
            if (logErrors)
            {
                LOG.error("Error during draw call", e);
                logErrors = false; // Suppress recurring errors
            }
        }
        catch (Error e)
        {
            LOG.error("Error during draw call", e);
            //noinspection ProhibitedExceptionThrown
            throw e;
        }
    }

    @Override
    public final void close()
    {
        try
        {
            if (managedResources && resources != null)
            {
                resources.close();
            }

            rectangleVertices.close();
            simpleTexProgram.close();
            simpleTexDrawable.close();
            lightCalibration.close();
            litRoot.close();
            lightCalibration3DRoot.close();

            for (FramebufferObject<ContextType> fbo : shadingFramebuffers)
            {
                fbo.close();
            }

            shadingFramebuffers.clear();
        }
        catch (RuntimeException e)
        {
            LOG.error("Error closing ProjectRenderingEngine:", e);
        }
    }

    final void setProgressMonitor(ProgressMonitor progressMonitor)
    {
        this.progressMonitor = progressMonitor;
    }

    @Override
    public final ReadonlyVertexGeometry getGeometry()
    {
        return getResources().getGeometry();
    }

    @Override
    public final @NotNull ViewSet getViewSet()
    {
        return getResources().getViewSet();
    }

    @Override
    public final @NotNull String toString()
    {
        return internalName.length() > 32
                ? String.format("...%s", internalName.substring(internalName.length() - 31))
                : internalName;
    }

    @Override
    public final void reloadShaders()
    {
        try
        {
            litRoot.reloadShaders();
            lightCalibration.reloadShaders();
            lightCalibration3DRoot.reloadShaders();

            logErrors = true;
        }
        catch (RuntimeException e)
        {
            LOG.error("Error reloading shaders:", e);
        }
    }

    @Override
    public final @NotNull SceneViewport getSceneViewport()
    {
        return sceneViewportModel;
    }


    @Override
    public final @NotNull SceneModel getSceneModel()
    {
        return sceneModel;
    }

    @Override
    public final void saveGLTF(@Nullable File outputDirectory, @NotNull String filename, @NotNull ExportSettings settings, @Nullable Runnable finishedCallback)
    {
        if (outputDirectory != null)
        {
            if (getGeometry() == null)
            {
                throw new IllegalArgumentException("Geometry is null; cannot export GLTF.");
            }

            LOG.info("Starting glTF export...");
            if(progressMonitor != null)
            {
                progressMonitor.setProcessName("Model Export");
            }

            try
            {
                updateWorldSpaceDefinition();

                Matrix4 transform = sceneModel.getFullModelMatrix();

                // Scale to imported scale from the photogrammetry project if that exists, otherwise at the original, raw scale
                // ViewSet should default to scale of 1.0 if nothing was imported.
                transform = Matrix4.scale(getViewSet().getObjectScale()).times(transform);

                ReadonlyTextureResources<ContextType> textureResources = getResources().getTextureResources();

                ModelExporter exporter = ModelExporter.fromVertexGeometry(getGeometry(), transform);
                settings.applyToExporter(exporter, textureResources, filename);

                outputDirectory.mkdirs();
                File modelFile = new File(outputDirectory, filename);

                if (settings.shouldSaveTextures())
                {
                    exporter.exportWithTextures(modelFile, finishedCallback);
                }
                else  // not saving textures
                {
                    exporter.exportModelOnly(modelFile);

                    if (finishedCallback != null)
                    {
                        finishedCallback.run();
                    }
                }

                LOG.info("DONE!");
            }
            catch (IOException e)
            {
                LOG.error("Error occurred during glTF export:", e);
            }
        }
    }
}

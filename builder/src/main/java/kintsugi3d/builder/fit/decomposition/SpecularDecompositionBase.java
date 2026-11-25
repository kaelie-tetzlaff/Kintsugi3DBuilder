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

package kintsugi3d.builder.fit.decomposition;

import kintsugi3d.builder.core.texture.TextureResolution;
import kintsugi3d.builder.io.specular.SpecularFitSerializer;
import kintsugi3d.gl.vecmath.DoubleVector3;
import kintsugi3d.gl.vecmath.DoubleVector4;
import kintsugi3d.util.SRGB;
import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

public abstract class SpecularDecompositionBase implements SpecularDecomposition
{
    private static final Logger LOG = LoggerFactory.getLogger(SpecularDecompositionBase.class);

    private final SimpleMatrix @NotNull [] weightsByTexel;
    private final boolean @NotNull [] weightsValidity;

    private final @NotNull TextureResolution textureResolution;

    protected SpecularDecompositionBase(@NotNull TextureResolution textureResolution, int materialCount)
    {
        weightsByTexel = IntStream.range(0, textureResolution.width * textureResolution.height)
            .mapToObj(p -> new SimpleMatrix(materialCount, 1, DMatrixRMaj.class))
            .toArray(SimpleMatrix[]::new);
        weightsValidity = new boolean[textureResolution.width * textureResolution.height];

        this.textureResolution = textureResolution;
    }

    @Override
    public final @NotNull SpecularBasisWeights getWeights()
    {
        return new SpecularDecompositionWeights(this);
    }

    @Override
    public final TextureResolution getTextureResolution()
    {
        return textureResolution;
    }

    @Override
    public final boolean areWeightsValid(int texelIndex)
    {
        return weightsValidity[texelIndex];
    }

    @Override
    public final double getWeight(int b, int p)
    {
        return weightsByTexel[p].get(b);
    }

    @Override
    public final SimpleMatrix getWeights(int texelIndex)
    {
        return weightsByTexel[texelIndex];
    }

    @Override
    public final void setWeights(int texelIndex, SimpleMatrix weights)
    {
        weightsByTexel[texelIndex] = weights;
    }

    @Override
    public final @NotNull List<SimpleMatrix> getWeightsList()
    {
        return Arrays.asList(weightsByTexel);
    }

    @Override
    public final void invalidateWeights()
    {
        // Quickly invalidate all the weights
        Arrays.fill(weightsValidity, false);
    }

    @Override
    public final void setWeightsValidity(int texelIndex, boolean validity)
    {
        weightsValidity[texelIndex] = validity;
    }

    @Override
    public final void fillHoles()
    {
        // Fill holes
        // TODO Quick hack; should be replaced with something more robust.
        LOG.info("Filling holes...");

        int texelCount = textureResolution.width * textureResolution.height;

        for (int i = 0; i < Math.max(textureResolution.width, textureResolution.height); i++)
        {
            Collection<Integer> filledPositions = new HashSet<>(256);
            for (int p = 0; p < texelCount; p++)
            {
                if (!areWeightsValid(p))
                {
                    int left = (texelCount + p - 1) % texelCount;
                    int right = (p + 1) % texelCount;
                    int up = (texelCount + p - textureResolution.width) % texelCount;
                    int down = (p + textureResolution.width) % texelCount;

                    int count = 0;

                    for (int b = 0; b < getMaterialBasis().getEnabledMaterialCount(); b++)
                    {
                        count = 0;
                        double sum = 0.0;

                        if (areWeightsValid(left))
                        {
                            sum += getWeights(left).get(b);
                            count++;
                        }

                        if (areWeightsValid(right))
                        {
                            sum += getWeights(right).get(b);
                            count++;
                        }

                        if (areWeightsValid(up))
                        {
                            sum += getWeights(up).get(b);
                            count++;
                        }

                        if (areWeightsValid(down))
                        {
                            sum += getWeights(down).get(b);
                            count++;
                        }

                        if (sum > 0.0)
                        {
                            getWeights(p).set(b, sum / count);
                        }
                    }

                    if (count > 0)
                    {
                        filledPositions.add(p);
                    }
                }
            }

            for (int p : filledPositions)
            {
                setWeightsValidity(p, true);
            }
        }

        LOG.info("DONE!");
    }

    @Override
    public final void saveDiffuseMap(File outputDirectory)
    {
        BufferedImage diffuseImg = new BufferedImage(textureResolution.width, textureResolution.height, BufferedImage.TYPE_INT_ARGB);
        int[] diffuseDataPacked = new int[textureResolution.width * textureResolution.height];
        for (int p = 0; p < textureResolution.width * textureResolution.height; p++)
        {
            DoubleVector4 diffuseSum = DoubleVector4.ZERO;

            for (BasisMaterialInfo material : getMaterialBasis().getMaterials())
            {
                if (material.isEnabled())
                {
                    diffuseSum = diffuseSum.plus(material.getDiffuseColor().asVector4(1.0)
                        .times(getWeight(material.getGPUIndex(), p)));
                }
            }

            if (diffuseSum.w > 0)
            {
                DoubleVector3 diffuseAvgGamma = diffuseSum.getXYZ().dividedBy(diffuseSum.w).applyOperator(x -> Math.min(1.0, SRGB.fromLinear(x)));

                // Flip vertically
                int dataBufferIndex = p % textureResolution.width + textureResolution.width * (textureResolution.height - p / textureResolution.width - 1);
                diffuseDataPacked[dataBufferIndex] = new Color((float) diffuseAvgGamma.x, (float) diffuseAvgGamma.y, (float) diffuseAvgGamma.z).getRGB();
            }
        }

        diffuseImg.setRGB(0, 0, diffuseImg.getWidth(), diffuseImg.getHeight(), diffuseDataPacked, 0, diffuseImg.getWidth());

        try
        {
            ImageIO.write(diffuseImg, "PNG", new File(outputDirectory, "diffuse_frombasis.png"));
        }
        catch (IOException e)
        {
            LOG.error("An error occurred while saving diffuse map:", e);
        }
    }

    @Override
    public final void saveBasisFunctions(File outputDirectory)
    {
        getMaterialBasis().save(outputDirectory, null);
    }

    @Override
    public final void saveWeightMaps(File outputDirectory)
    {
        getWeights().save(outputDirectory);
    }

    private static class SpecularDecompositionWeights implements SpecularBasisWeights
    {
        private final SpecularDecompositionBase specularDecomposition;
        private final MaterialBasis basis;

        SpecularDecompositionWeights(@NotNull SpecularDecompositionBase specularDecomposition)
        {
            this.specularDecomposition = specularDecomposition;
            basis = specularDecomposition.getMaterialBasis();
        }

        @Override
        public final double getWeight(int b, int p)
        {
            return specularDecomposition.weightsByTexel[p].get(b);
        }

        @Override
        public final boolean areWeightsValid(int p)
        {
            return specularDecomposition.weightsValidity[p];
        }

        @Override
        public final int getCount()
        {
            return basis.getEnabledMaterialCount();
        }

        @Override
        public final void save(File outputDirectory)
        {
            SpecularFitSerializer.saveWeightImages(
                basis, specularDecomposition.textureResolution.width, specularDecomposition.textureResolution.height, this, outputDirectory);
        }
    }
}

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

import kintsugi3d.builder.io.specular.SpecularFitSerializer;
import kintsugi3d.gl.vecmath.DoubleVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SimpleMaterialBasis implements MutableMaterialBasis
{
    /**
     * Uses LinkedHashMap to preserve insertion order.
     */
    private final @NotNull LinkedHashMap<String, MutableBasisMaterialInfo> basis;

    private final int specularResolution;

    private int enabledMaterialCount;

    SimpleMaterialBasis(DoubleVector3[] diffuseColors, @NotNull List<double[]> redBasis, @NotNull List<double[]> greenBasis, @NotNull List<double[]> blueBasis)
    {
        enabledMaterialCount = redBasis.size();
        specularResolution = redBasis.get(0).length - 1;

        basis = new LinkedHashMap<>(enabledMaterialCount);

        // If we're creating a new basis without disabled materials, just use b as GPU index.
        for (int b = 0; b < enabledMaterialCount; b++)
        {
            String name = String.format("%02d", b);
            String friendlyName = String.format("Material %d", b);
            basis.put(name, MutableBasisMaterialInfo.create(
                name, b, friendlyName, diffuseColors[b], redBasis.get(b), greenBasis.get(b), blueBasis.get(b)));
        }
    }

    private SimpleMaterialBasis(@NotNull LinkedHashMap<String, MutableBasisMaterialInfo> basis)
    {
        this.basis = basis;
        specularResolution = basis.values().stream().findAny().orElseThrow().getResolution();
        enabledMaterialCount = (int)basis.values().stream().filter(BasisMaterialInfo::isEnabled).count();
    }

    /**
     * Enabled basis materials will be automatically assigned a GPU index based on the traversal order of names.
     * @param names
     * @param disabledNames
     * @param diffuseColors
     * @param redBasis
     * @param greenBasis
     * @param blueBasis
     * @param <KeyType>
     */
    public <KeyType> SimpleMaterialBasis(
        @NotNull Map<KeyType, String> names, @NotNull Map<KeyType, String> disabledNames, @NotNull Map<KeyType, DoubleVector3> diffuseColors,
        @NotNull Map<KeyType, double[]> redBasis, @NotNull Map<KeyType, double[]> greenBasis, @NotNull Map<KeyType, double[]> blueBasis)
    {
        enabledMaterialCount = names.size();
        specularResolution = redBasis.values().stream().findAny().orElseThrow().length - 1;

        basis = new LinkedHashMap<>(enabledMaterialCount + disabledNames.size());

        int gpuIndex = 0;
        for (Entry<KeyType, String> entry : names.entrySet())
        {
            KeyType key = entry.getKey();
            String name = entry.getValue();

            basis.put(name, MutableBasisMaterialInfo.create(
                name, gpuIndex, getFriendlyName(name), diffuseColors.get(key),
                redBasis.get(key),  greenBasis.get(key), blueBasis.get(key)));
            gpuIndex++;
        }

        for (Entry<KeyType, String> entry : disabledNames.entrySet())
        {
            KeyType key = entry.getKey();
            String name = entry.getValue();
            basis.put(name, MutableBasisMaterialInfo.create(
                name, gpuIndex, getFriendlyName(name), diffuseColors.get(key),
                redBasis.get(key), greenBasis.get(key), blueBasis.get(key), false));
            gpuIndex++;
        }
    }

    private static @NotNull String getFriendlyName(@NotNull String name)
    {
        try
        {
            // Try to simplify material number presentation and include the prefix "Material"
            return String.format("Material %d", Integer.parseInt(name));
        }
        catch (NumberFormatException e)
        {
            return name;
        }
    }

    @Override
    public final @NotNull Collection<? extends IndexAssignableBasisMaterialInfo> getMaterials()
    {
        return basis.values();
    }

    @Override
    public final @NotNull List<BasisMaterialInfo> getIndexableMaterialList()
    {
        BasisMaterialInfo[] result = new BasisMaterialInfo[basis.size()];
        for (BasisMaterialInfo material : basis.values())
        {
            result[material.getGPUIndex()] = material;
        }

        return List.of(result);
    }

    @Override
    public final BasisMaterialInfo getMaterial(String materialName)
    {
        return basis.get(materialName);
    }

    @Override
    public final int getMaterialCount()
    {
        return basis.size();
    }

    @Override
    public final int getEnabledMaterialCount()
    {
        return enabledMaterialCount;
    }

    @Override
    public final int getSpecularResolution()
    {
        return specularResolution;
    }

    @Override
    public final @Nullable IndexAssignableBasisMaterialInfo deleteMaterial(String name)
    {
        MutableBasisMaterialInfo removed = basis.remove(name);
        if (removed != null)
        {
            // Shift indices of materials to the right of the one that was deleted.
            for (MutableBasisMaterialInfo material : basis.values())
            {
                if (material.getGPUIndex() > removed.getGPUIndex())
                {
                    material.setGPUIndex(material.getGPUIndex() - 1);
                }
            }

            if (removed.isEnabled())
            {
                enabledMaterialCount--;
            }
        }
        return removed;
    }

    @Override
    public final void save(File outputDirectory, String filenameOverride)
    {
        SpecularFitSerializer.serializeBasisFunctions(specularResolution, this, outputDirectory, filenameOverride);
    }

    /**
     * Makes a deep copy in the sense that it copies each individual basis function.
     * @return
     */
    @Override
    public final @NotNull MutableMaterialBasis copy()
    {
        LinkedHashMap<String, MutableBasisMaterialInfo> copy = new LinkedHashMap<>(basis.size());
        for (var entry : basis.entrySet())
        {
            copy.put(entry.getKey(), entry.getValue().copy());
        }

        return new SimpleMaterialBasis(copy);
    }

    @Override
    public final @NotNull BasisMaterialInfo disableMaterial(String name)
    {
        MutableBasisMaterialInfo material = basis.get(name);

        if (material.isEnabled()) // no-op if already enabled
        {
            if (material.getGPUIndex() != enabledMaterialCount - 1)
            {
                // Find the material whose GPU index is the one that will no longer be used after disabling another material.
                MutableBasisMaterialInfo swapMaterial = basis.entrySet().stream()
                    .filter(entry -> entry.getValue().getGPUIndex() == enabledMaterialCount - 1)
                    .findAny().map(Entry::getValue).orElseThrow();

                // Swap locations of the two materials.
                swapMaterial.setGPUIndex(material.getGPUIndex());
                material.setGPUIndex(enabledMaterialCount - 1);
            }

            material.setEnabled(false);
            enabledMaterialCount--;
        }

        return material;
    }

    @Override
    public final @NotNull BasisMaterialInfo enableMaterial(String name)
    {
        MutableBasisMaterialInfo material = basis.get(name);

        if (!material.isEnabled()) // no-op if already disabled
        {
            if (material.getGPUIndex() != enabledMaterialCount)
            {
                // Find the material whose GPU index is the one that will become enabled.
                MutableBasisMaterialInfo swapMaterial = basis.entrySet().stream()
                    .filter(entry -> entry.getValue().getGPUIndex() == enabledMaterialCount)
                    .findAny().map(Entry::getValue).orElseThrow();

                // Swap locations of the two materials.
                swapMaterial.setGPUIndex(material.getGPUIndex());
                material.setGPUIndex(enabledMaterialCount);
            }

            material.setEnabled(true);
            enabledMaterialCount++;
        }

        return material;
    }

    @Override
    public final boolean isMaterialEnabled(String name)
    {
        return basis.get(name).isEnabled();
    }
}

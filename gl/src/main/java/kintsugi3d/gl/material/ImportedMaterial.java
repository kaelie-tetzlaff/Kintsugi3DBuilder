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

package kintsugi3d.gl.material;

import kintsugi3d.gl.vecmath.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ImportedMaterial implements ReadonlyImportedMaterial
{
    private static final Logger LOG = LoggerFactory.getLogger(ImportedMaterial.class);

    private final String name;

    private Vector3 ambient;
    private Vector3 diffuse;
    private Vector3 specular;
    private Vector3 emission;
    private float exponent;
    private float roughness;
    private float opacity;
    private float translucency;
    private float metallic;
    private Vector3 sheen;
    private Vector3 clearcoat;
    private float clearcoatRoughness;

    private @Nullable ReadonlyMaterialColorMap ambientMap;
    private @Nullable ReadonlyMaterialColorMap diffuseMap;
    private @Nullable ReadonlyMaterialColorMap specularMap;
    private @Nullable ReadonlyMaterialColorMap emissionMap;
    private @Nullable ReadonlyMaterialScalarMap exponentMap;
    private @Nullable ReadonlyMaterialScalarMap roughnessMap;
    private @Nullable ReadonlyMaterialScalarMap opacityMap;
    private @Nullable ReadonlyMaterialScalarMap transparencyMap;
    private @Nullable ReadonlyMaterialScalarMap translucencyMap;
    private @Nullable ReadonlyMaterialScalarMap metallicMap;
    private @Nullable ReadonlyMaterialColorMap sheenMap;
    private @Nullable ReadonlyMaterialScalarMap anisotropyMap;
    private @Nullable ReadonlyMaterialScalarMap anisotropyRotationMap;
    private @Nullable ReadonlyMaterialBumpMap bumpMap;
    private @Nullable ReadonlyMaterialTextureMap normalMap;
    private @Nullable ReadonlyMaterialScalarMap displacementMap;
    private @Nullable ReadonlyMaterialScalarMap ambientOcclusionMap;

    private ImportedMaterial(String name)
    {
        this.name = name;

        ambient = new Vector3(0.0f);
        diffuse = new Vector3(0.0f);
        specular = new Vector3(0.0f);
        exponent = 0.0f;
        roughness = 1.0f;
        opacity = 1.0f;
        emission = new Vector3(0.0f);
        metallic = 0.0f;
        sheen = new Vector3(0.0f);
        clearcoat = new Vector3(0.0f);
        clearcoatRoughness = 0.0f;
    }

    public static @NotNull Map<String, ImportedMaterial> loadFromMTLFile(@NotNull File mtlFile) throws IOException
    {
        Map<String, ImportedMaterial> materials = new HashMap<>(1);
        ImportedMaterial currentMaterial = null;
        boolean ambientMissing = true;

        if (mtlFile.exists())
        {
            try(Scanner scanner = new Scanner(mtlFile, StandardCharsets.UTF_8))
            {
                scanner.useLocale(Locale.ROOT);

                while(scanner.hasNext())
                {
                    String id = scanner.next();

                    if ("newmtl".equals(id) && scanner.hasNext())
                    {
                        currentMaterial = new ImportedMaterial(scanner.next());
                        materials.put(currentMaterial.name, currentMaterial);
                    }
                    else if (currentMaterial != null)
                    {
                        try
                        {
                            switch(id.toLowerCase(Locale.ROOT))
                            {
                                case "ka":
                                    currentMaterial.ambient = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    ambientMissing = false;
                                    break;
                                case "kd":
                                    currentMaterial.diffuse = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    break;
                                case "ks":
                                    currentMaterial.specular = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    break;
                                case "ke":
                                    currentMaterial.emission = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    break;
                                case "d":
                                    currentMaterial.opacity = scanner.nextFloat();
                                    break;
                                case "tr":
                                    currentMaterial.opacity = 1.0f - scanner.nextFloat();
                                    break;
                                case "tf":
                                    currentMaterial.translucency = scanner.nextFloat();
                                    break;
                                case "ns":
                                    currentMaterial.setExponent(scanner.nextFloat());
                                    break;
                                case "pr":
                                    currentMaterial.setRoughness(scanner.nextFloat());
                                    break;
                                case "pm":
                                    currentMaterial.metallic = scanner.nextFloat();
                                    break;
                                case "ps":
                                    currentMaterial.sheen = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    break;
                                case "pc":
                                    currentMaterial.clearcoat = new Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
                                    break;
                                case "pcr":
                                    currentMaterial.clearcoatRoughness = scanner.nextFloat();
                                    break;
                                case "map_ka":
                                    currentMaterial.ambientMap = parseColorMapStatement(scanner);
                                    break;
                                case "map_kd":
                                    currentMaterial.diffuseMap = parseColorMapStatement(scanner);
                                    break;
                                case "map_ks":
                                    currentMaterial.specularMap = parseColorMapStatement(scanner);
                                    break;
                                case "map_ke":
                                    currentMaterial.emissionMap = parseColorMapStatement(scanner);
                                    break;
                                case "map_d":
                                    MaterialScalarMap opacityMap = parseScalarMapStatement(scanner);
                                    if (opacityMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        opacityMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.opacityMap = opacityMap;
                                    currentMaterial.transparencyMap = null;
                                    break;
                                case "map_tr":
                                    MaterialScalarMap transparencyMap = parseScalarMapStatement(scanner);
                                    if (transparencyMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        transparencyMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.transparencyMap = transparencyMap;
                                    currentMaterial.opacityMap = null;
                                    break;
                                case "map_tf":
                                    MaterialScalarMap translucencyMap = parseScalarMapStatement(scanner);
                                    if (translucencyMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        translucencyMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.translucencyMap = translucencyMap;
                                    currentMaterial.opacityMap = null;
                                    break;
                                case "map_ns":
                                    MaterialScalarMap exponentMap = parseScalarMapStatement(scanner);
                                    if (exponentMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        exponentMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.exponentMap = exponentMap;
                                    currentMaterial.roughnessMap = null;
                                    break;
                                case "map_pr":
                                    MaterialScalarMap roughnessMap = parseScalarMapStatement(scanner);
                                    if (roughnessMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        roughnessMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.roughnessMap = roughnessMap;
                                    currentMaterial.exponentMap = null;
                                    break;
                                case "map_pm":
                                    MaterialScalarMap metallicMap = parseScalarMapStatement(scanner);
                                    if (metallicMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        metallicMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.metallicMap = metallicMap;
                                    break;
                                case "map_ps":
                                    currentMaterial.sheenMap = parseColorMapStatement(scanner);
                                    break;
                                case "aniso":
                                    MaterialScalarMap anisotropyMap = parseScalarMapStatement(scanner);
                                    if (anisotropyMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        anisotropyMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.anisotropyMap = anisotropyMap;
                                    break;
                                case "anisor":
                                    MaterialScalarMap anisotropyRotationMap = parseScalarMapStatement(scanner);
                                    if (anisotropyRotationMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        anisotropyRotationMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.anisotropyRotationMap = anisotropyRotationMap;
                                    break;
                                case "bump":
                                    MaterialBumpMap bumpMap = parseBumpMapStatement(scanner);
                                    if (bumpMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        bumpMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.bumpMap = bumpMap;
                                    break;
                                case "disp":
                                    MaterialScalarMap displacementMap = parseScalarMapStatement(scanner);
                                    if (displacementMap.getChannel() == MaterialTextureChannel.UNSPECIFIED)
                                    {
                                        displacementMap.setChannel(MaterialTextureChannel.LUMINANCE);
                                    }
                                    currentMaterial.displacementMap = displacementMap;
                                    break;
                                case "normal":
                                case "norm":
                                case "map_bump": // Metashape convention
                                    currentMaterial.normalMap = parseTextureStatement(scanner);
                                    break;
                                case "map_ao": // Metashape convention
                                    currentMaterial.ambientOcclusionMap = parseScalarMapStatement(scanner);
                                    break;
                                default:
                                    LOG.warn("Unrecognized material attribute: {}", id);
                            }
                        }
                        catch(InputMismatchException e)
                        {
                            LOG.error("Error parsing MTL file:", e);
                            scanner.nextLine();
                        }
                        catch(NoSuchElementException e)
                        {
                            LOG.error("Error parsing MTL file:", e);
                        }
                    }

                    if (scanner.hasNextLine())
                    {
                        // Always advance to the next line.
                        scanner.nextLine();
                    }
                }
            }
        }

        if (currentMaterial != null)
        {
            if(ambientMissing)
            {
                currentMaterial.ambient = currentMaterial.diffuse;
            }

            if (currentMaterial.ambientMap == null)
            {
                currentMaterial.ambientMap = currentMaterial.diffuseMap;
            }
        }

        return materials;
    }

    private static void processTextureOption(@NotNull Scanner scanner, @NotNull MaterialTextureMap texture, @NotNull String option)
    {
        try
        {
            // Option statement
            switch(option.toLowerCase(Locale.ROOT))
            {
                case "-mm":
                    texture.setBase(scanner.nextFloat());
                    if (scanner.hasNextFloat())
                    {
                        texture.setGain(scanner.nextFloat());
                    }
                    break;
                case "-o":
                    float x = scanner.nextFloat();
                    float y;
                    float z;
                    if (scanner.hasNextFloat())
                    {
                        y = scanner.nextFloat();
                        if (scanner.hasNextFloat())
                        {
                            z = scanner.nextFloat();
                        }
                        else
                        {
                            z = 0.0f;
                        }
                    }
                    else
                    {
                        y = 0.0f;
                        z = 0.0f;
                    }
                    texture.setOffset(new Vector3(x, y, z));
                    break;
                case "-s":
                    float sx = scanner.nextFloat();
                    float sy;
                    float sz;
                    if (scanner.hasNextFloat())
                    {
                        sy = scanner.nextFloat();
                        if (scanner.hasNextFloat())
                        {
                            sz = scanner.nextFloat();
                        }
                        else
                        {
                            sz = 1.0f;
                        }
                    }
                    else
                    {
                        sy = 1.0f;
                        sz = 1.0f;
                    }
                    texture.setScale(new Vector3(sx, sy, sz));
                    break;
                case "-clamp":
                    if ("on".equals(scanner.next()))
                    {
                        texture.setClampingRequired(true);
                    }
                    break;
                default:
                    // If nothing else, read past the next token.
                    scanner.next();
                    break;
            }
        }
        catch(InputMismatchException e)
        {
            LOG.error("Input error:", e);
            scanner.next();
        }
    }

    private static @NotNull ReadonlyMaterialTextureMap parseTextureStatement(@NotNull Scanner scanner)
    {
        MaterialTextureMap texture = new MaterialTextureMap();

        while(texture.getMapName() == null)
        {
            while(scanner.hasNextFloat())
            {
                // Skip past additional numeric options
                scanner.nextFloat();
            }

            String nextPart = scanner.next();
            if(nextPart.startsWith("-"))
            {
                processTextureOption(scanner, texture, nextPart);
            }
            else
            {
                // The filename
                texture.setMapName(nextPart);
            }
        }

        return texture;
    }

    private static @NotNull ReadonlyMaterialColorMap parseColorMapStatement(@NotNull Scanner scanner)
    {
        MaterialColorMap texture = new MaterialColorMap();

        while(texture.getMapName() == null)
        {
            while(scanner.hasNextFloat())
            {
                // Skip past additional numeric options
                scanner.nextFloat();
            }

            String nextPart = scanner.next();
            if(nextPart.startsWith("-"))
            {
                if ("-cc".equals(nextPart))
                {
                    if ("on".equals(scanner.next()))
                    {
                        texture.setSRGBDecodingRequired(true);
                    }
                }
                else
                {
                    processTextureOption(scanner, texture, nextPart);
                }
            }
            else
            {
                // The filename
                texture.setMapName(nextPart);
            }
        }

        return texture;
    }

    private static @NotNull MaterialScalarMap parseScalarMapStatement(@NotNull Scanner scanner)
    {
        MaterialScalarMap texture = new MaterialScalarMap();

        while(texture.getMapName() == null)
        {
            while(scanner.hasNextFloat())
            {
                // Skip past additional numeric options
                scanner.nextFloat();
            }

            String nextPart = scanner.next();
            if(nextPart.startsWith("-"))
            {
                if ("-imfchan".equals(nextPart))
                {
                    String element = scanner.next();
                    parseChannel(element, texture);
                }
                else
                {
                    processTextureOption(scanner, texture, nextPart);
                }
            }
            else
            {
                // The filename
                texture.setMapName(nextPart);
            }
        }

        return texture;
    }

    private static @NotNull MaterialBumpMap parseBumpMapStatement(@NotNull Scanner scanner)
    {
        MaterialBumpMap texture = new MaterialBumpMap();

        while(texture.getMapName() == null)
        {
            while(scanner.hasNextFloat())
            {
                // Skip past additional numeric options
                scanner.nextFloat();
            }

            String nextPart = scanner.next();
            if(nextPart.startsWith("-"))
            {
                if ("-bm".equals(nextPart))
                {
                    if (scanner.hasNextFloat())
                    {
                        texture.setBumpMultiplier(scanner.nextFloat());
                    }
                    else
                    {
                        scanner.next(); // unexpected format
                    }
                }
                else if ("-imfchan".equals(nextPart))
                {
                    String element = scanner.next();
                    parseChannel(element, texture);
                }
                else
                {
                    processTextureOption(scanner, texture, nextPart);
                }
            }
            else
            {
                // The filename
                texture.setMapName(nextPart);
            }
        }

        return texture;
    }

    private static void parseChannel(@NotNull String channel, @NotNull MaterialScalarMap texture)
    {
        switch(channel.toLowerCase(Locale.ROOT))
        {
            case "r":
                texture.setChannel(MaterialTextureChannel.RED);
                break;
            case "g":
                texture.setChannel(MaterialTextureChannel.GREEN);
                break;
            case "b":
                texture.setChannel(MaterialTextureChannel.BLUE);
                break;
            case "m":
            case "a":
                texture.setChannel(MaterialTextureChannel.ALPHA);
                break;
            case "l":
            case "y":
                texture.setChannel(MaterialTextureChannel.LUMINANCE);
                break;
            case "z":
            case "d":
                texture.setChannel(MaterialTextureChannel.DEPTH);
                break;
            default:
                LOG.warn("Unexpected channel: {}", channel);
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Vector3 getAmbient()
    {
        return ambient;
    }

    public void setAmbient(Vector3 ambient)
    {
        this.ambient = ambient;
    }

    @Override
    public Vector3 getDiffuse()
    {
        return diffuse;
    }

    public void setDiffuse(Vector3 diffuse)
    {
        this.diffuse = diffuse;
    }

    @Override
    public Vector3 getSpecular()
    {
        return specular;
    }

    public void setSpecular(Vector3 specular)
    {
        this.specular = specular;
    }

    @Override
    public float getExponent()
    {
        return exponent;
    }

    public void setExponent(float exponent)
    {
        this.exponent = exponent;
        roughness = (float)Math.sqrt(2.0 / (exponent + 2));
    }

    @Override
    public float getRoughness()
    {
        return roughness;
    }

    public void setRoughness(float roughness)
    {
        this.roughness = roughness;
        exponent = 2.0f / (roughness * roughness) - 2;
    }

    @Override
    public Vector3 getEmission()
    {
        return emission;
    }

    public void setEmission(Vector3 emission)
    {
        this.emission = emission;
    }

    @Override
    public float getMetallic()
    {
        return metallic;
    }

    public void setMetallic(float metallic)
    {
        this.metallic = metallic;
    }

    @Override
    public Vector3 getSheen()
    {
        return sheen;
    }

    public void setSheen(Vector3 sheen)
    {
        this.sheen = sheen;
    }

    @Override
    public Vector3 getClearcoat()
    {
        return clearcoat;
    }

    public void setClearcoat(Vector3 clearcoat)
    {
        this.clearcoat = clearcoat;
    }

    @Override
    public float getClearcoatRoughness()
    {
        return clearcoatRoughness;
    }

    public void setClearcoatRoughness(float clearcoatRoughness)
    {
        this.clearcoatRoughness = clearcoatRoughness;
    }

    @Override
    public float getOpacity()
    {
        return opacity;
    }

    public void setOpacity(float opacity)
    {
        this.opacity = opacity;
    }

    @Override
    public float getTransparency()
    {
        return 1.0f - opacity;
    }

    public void setTransparency(float transparency)
    {
        opacity = 1.0f - opacity;
    }

    @Override
    public float getTranslucency()
    {
        return translucency;
    }

    public void setTranslucency(float translucency)
    {
        this.translucency = translucency;
    }

    @Override
    public @Nullable ReadonlyMaterialColorMap getAmbientMap()
    {
        return ambientMap;
    }

    public void setAmbientMap(@Nullable ReadonlyMaterialColorMap ambientMap)
    {
        this.ambientMap = ambientMap;
    }

    @Override
    public @Nullable ReadonlyMaterialColorMap getDiffuseMap()
    {
        return diffuseMap;
    }

    public void setDiffuseMap(@Nullable ReadonlyMaterialColorMap diffuseMap)
    {
        this.diffuseMap = diffuseMap;
    }

    @Override
    public @Nullable ReadonlyMaterialColorMap getSpecularMap()
    {
        return specularMap;
    }

    public void setSpecularMap(@Nullable ReadonlyMaterialColorMap specularMap)
    {
        this.specularMap = specularMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getExponentMap()
    {
        return exponentMap;
    }

    public void setExponentMap(@Nullable ReadonlyMaterialScalarMap exponentMap)
    {
        this.exponentMap = exponentMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getRoughnessMap()
    {
        return roughnessMap;
    }

    public void setRoughnessMap(@Nullable ReadonlyMaterialScalarMap roughnessMap)
    {
        this.roughnessMap = roughnessMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getOpacityMap()
    {
        return opacityMap;
    }

    public void setOpacityMap(@Nullable ReadonlyMaterialScalarMap opacityMap)
    {
        this.opacityMap = opacityMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getTransparencyMap()
    {
        return transparencyMap;
    }

    public void setTransparencyMap(@Nullable ReadonlyMaterialScalarMap transparencyMap)
    {
        this.transparencyMap = transparencyMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getTranslucencyMap()
    {
        return translucencyMap;
    }

    public void setTranslucencyMap(@Nullable ReadonlyMaterialScalarMap translucencyMap)
    {
        this.translucencyMap = translucencyMap;
    }

    @Override
    public @Nullable ReadonlyMaterialColorMap getEmissionMap()
    {
        return emissionMap;
    }

    public void setEmissionMap(@Nullable ReadonlyMaterialColorMap emissionMap)
    {
        this.emissionMap = emissionMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getMetallicMap()
    {
        return metallicMap;
    }

    public void setMetallicMap(@Nullable ReadonlyMaterialScalarMap metallicMap)
    {
        this.metallicMap = metallicMap;
    }

    @Override
    public @Nullable ReadonlyMaterialColorMap getSheenMap()
    {
        return sheenMap;
    }

    public void setSheenMap(@Nullable ReadonlyMaterialColorMap sheenMap)
    {
        this.sheenMap = sheenMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getAnisotropyMap()
    {
        return anisotropyMap;
    }

    public void setAnisotropyMap(@Nullable ReadonlyMaterialScalarMap anisotropyMap)
    {
        this.anisotropyMap = anisotropyMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getAnisotropyRotationMap()
    {
        return anisotropyRotationMap;
    }

    public void setAnisotropyRotationMap(@Nullable ReadonlyMaterialScalarMap anisotropyRotationMap)
    {
        this.anisotropyRotationMap = anisotropyRotationMap;
    }

    @Override
    public @Nullable ReadonlyMaterialBumpMap getBumpMap()
    {
        return bumpMap;
    }

    public void setBumpMap(@Nullable ReadonlyMaterialBumpMap bumpMap)
    {
        this.bumpMap = bumpMap;
    }

    @Override
    public @Nullable ReadonlyMaterialTextureMap getNormalMap()
    {
        return normalMap;
    }

    public void setNormalMap(@Nullable ReadonlyMaterialTextureMap normalMap)
    {
        this.normalMap = normalMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getDisplacementMap()
    {
        return displacementMap;
    }

    public void setDisplacementMap(@Nullable ReadonlyMaterialScalarMap displacmentMap)
    {
        displacementMap = displacmentMap;
    }

    @Override
    public @Nullable ReadonlyMaterialScalarMap getAmbientOcclusionMap()
    {
        return ambientOcclusionMap;
    }

    public void setAmbientOcclusionMap(@Nullable ReadonlyMaterialScalarMap ambientOcclusionMap)
    {
        this.ambientOcclusionMap = ambientOcclusionMap;
    }
}

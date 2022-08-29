/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AndroidIconResizer
{
    private static Map<String, Map.Entry<Integer, Integer>> dimensions = new LinkedHashMap<String, Map.Entry<Integer, Integer>>()
    {
        { put("xxxhdpi", new AbstractMap.SimpleEntry<>(192, 192)); }
        { put("xxhdpi", new AbstractMap.SimpleEntry<>(144, 144)); }
        { put("xhdpi", new AbstractMap.SimpleEntry<>(96, 96)); }
        { put("hdpi", new AbstractMap.SimpleEntry<>(72, 72)); }
        { put("mdpi", new AbstractMap.SimpleEntry<>(48, 48)); }
    };

    /**
     * @param sourceIconFile - resized icon
     * @param resFolder - path to resource folder
     * @param prefixFolder - mipmap or drawable folder, used for prefixing (mdpi, hdpi...)
     * @return true if all icons were resized and copied
     */
    public static boolean resizeMipmap(File sourceIconFile,
                                    File resFolder,
                                    String prefixFolder,
                                    String launcherIconName) throws IOException
    {
        int patchedCount = 0;

        for(Map.Entry<String, Map.Entry<Integer, Integer>> kvp : dimensions.entrySet())
        {
            String dpi = kvp.getKey();

            File iconFolder = new File(resFolder, String.format("%s-%s", prefixFolder, dpi));

            File outputIconFile = new File(iconFolder, launcherIconName);

            if(!outputIconFile.exists())
                System.out.println("Mising default icon (icon to be replaced) in path: " + outputIconFile + ".");
            else
                patchedCount++;

            Map.Entry<Integer, Integer> size = kvp.getValue();

            int width = size.getKey();
            int height = size.getValue();

            Thumbnails.of(sourceIconFile)
                    .size(width, height)
                    .outputFormat("png")
                    .toFile(outputIconFile);
        }

        return patchedCount == dimensions.size();
    }
}

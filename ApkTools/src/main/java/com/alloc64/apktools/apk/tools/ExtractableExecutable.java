/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import com.alloc64.apktools.res.ClassPathResource;
import com.alloc64.apktools.env.OsType;

import org.apache.commons.io.FileUtils;

import java.io.File;

public abstract class ExtractableExecutable
{
    protected File extractFolder()
    {
        return new File("./tmp");
    }

    private String createPath(OsType platform, String executable)
    {
        return String.format("/executables/%s/%s", platform == null ? "universal" : platform.getValue(), executable);
    }

    protected File getOrExtractFile(OsType platform, String executable)
    {
        try
        {
            String jarPath = createPath(platform, executable);

            ClassPathResource resource = new ClassPathResource(jarPath);

            if (!resource.exists())
                throw new RuntimeException("Unable to extract executable from resources: " + jarPath);

            File extractedFile = new File(extractFolder(), executable);

            if (!extractedFile.exists())
            {
                FileUtils.copyInputStreamToFile(resource.getInputStream(), extractedFile);
                extractedFile.setExecutable(true);
            }

            return extractedFile;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected String getOrExtract(OsType platform, String executable)
    {
        return getOrExtractFile(platform, executable)
                .getAbsolutePath();
    }
}

/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import com.umeng.editor.android.AndroidManifestAXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AndroidManifest
{
    private File file;
    private AndroidManifestAXML manifest;

    public AndroidManifest(File file)
    {
        this.file = file;
        this.manifest = new AndroidManifestAXML();
    }

    public File getFile()
    {
        return file;
    }

    public AndroidManifestAXML getManifest()
    {
        return manifest;
    }

    public void parse() throws Exception
    {
        manifest.parse(new FileInputStream(file));
    }

    public void build(File outputFile) throws Exception
    {
        manifest.build(new FileOutputStream(outputFile));
    }

    public void save() throws Exception
    {
        manifest.build(new FileOutputStream(getFile()));
    }
}

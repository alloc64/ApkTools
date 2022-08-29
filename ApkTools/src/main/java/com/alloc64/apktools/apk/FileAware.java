/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import java.io.File;

public class FileAware
{
    private File file;

    public FileAware(File file)
    {
        this.file = file;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public String getName()
    {
        return file == null ? null : file.getName();
    }

    public String getAbsolutePath()
    {
        return file.getAbsolutePath();
    }

    public boolean exists()
    {
        return file != null && file.exists();
    }

    @Override
    public String toString()
    {
        return file == null ? null : file.toString();
    }
}

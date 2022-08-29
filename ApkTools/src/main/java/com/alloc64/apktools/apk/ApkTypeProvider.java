/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApkTypeProvider
{
    public static class FileInfo
    {
        public enum Type
        {
            Unknown,
            Apk,
            XApk,
        }

        private final Type fileType;
        private List<ZipEntry> zipFiles;

        public FileInfo(Type fileType)
        {
            this.fileType = fileType;
        }

        public FileInfo(Type fileType, List<ZipEntry> zipFiles)
        {
            this(fileType);
            this.zipFiles = zipFiles;
        }

        public Type getFileType()
        {
            return fileType;
        }

        public List<ZipEntry> getZipFiles()
        {
            return zipFiles;
        }
    }

    public static FileInfo provideInfo(File zipOrApk)
    {
        try
        {
            FileInfo.Type fileType = null;
            List<ZipEntry> zipFiles = new ArrayList<>();

            ZipFile zip = new ZipFile(zipOrApk);

            Enumeration entries = zip.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();

                if (!entry.isDirectory() && fileType == null && (name.equals("AndroidManifest.xml") || name.endsWith(".dex")))
                {
                    fileType = FileInfo.Type.Apk;
                }

                if (!entry.isDirectory())
                {
                    try(BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry)))
                    {
                        byte[] header = new byte[4];

                        if(is.read(header, 0, 4) != -1)
                        {
                            if(ZipFileAware.isZip(header))
                            {
                                zipFiles.add(entry);
                            }
                        }
                    }
                }
            }

            if(fileType == null && zipFiles.size() > 0)
                fileType = FileInfo.Type.XApk;

            if(fileType == null)
                fileType = FileInfo.Type.Unknown;

            return new FileInfo(fileType, zipFiles);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new FileInfo(FileInfo.Type.Unknown);
    }


    public static ZipFileAware provide(File tempFolder, File zipOrApk)
    {
        FileInfo info = provideInfo(zipOrApk);

        if(info.getFileType() != null)
        {
            switch (info.getFileType())
            {
                case Apk:
                    return new ApkFile(tempFolder, zipOrApk);

                case XApk:
                    return new XApkFile(tempFolder, zipOrApk);
            }
        }

        throw new IllegalStateException("Invalid ZIP or APK file type.");
    }
}

/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FoldersLister
{
    public interface AllCallback
    {
        boolean onProcess(File file) throws Exception;
    }

    public interface FileCallback
    {
        void onProcessFile(File file) throws Exception;
    }

    public interface ZipCallback
    {
        void onProcessFile(ZipEntry zipFile) throws Exception;
    }

    public interface FolderCallback
    {
        boolean onProcessFolder(File file) throws Exception;
    }

    public static boolean loadFilesForFolder(final File folder, FileCallback callback) throws Exception
    {
        if(folder == null)
            return false;

        for(final File fileEntry : folder.listFiles())
        {
            if(fileEntry.isDirectory())
                loadFilesForFolder(fileEntry, callback);
            else
            if(callback != null)
                callback.onProcessFile(new File(fileEntry.getParent(), fileEntry.getName()));
        }

        return true;
    }

    public static boolean listAll(final File folder, AllCallback callback) throws Exception
    {
        if(folder == null)
            return false;

        for(final File fileEntry : folder.listFiles())
        {
            if(callback != null)
            {
                if(!callback.onProcess(new File(fileEntry.getParent(), fileEntry.getName())))
                    continue;
            }

            if(fileEntry.isDirectory())
                listAll(fileEntry, callback);
        }

        return true;
    }

    public static boolean listFolders(final File folder, FolderCallback callback) throws Exception
    {
        if(folder == null)
            return false;

        for(final File fileEntry : folder.listFiles())
        {
            if(fileEntry.isDirectory())
            {
                if (callback != null)
                {
                    if(callback.onProcessFolder(fileEntry))
                        listFolders(fileEntry, callback);
                }
            }
        }

        return true;
    }


    public static void listZip(File apkFile, ZipCallback callback) throws Exception
    {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null)
        {
            if(callback != null)
                callback.onProcessFile(zipEntry);

            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    public static String relativizePath(File parent, File folder)
    {
        if(parent == null)
            throw new NullPointerException("parent");

        if(folder == null)
            throw new NullPointerException("folder");

        String relativePath = folder.getAbsolutePath().replace(parent.getAbsolutePath(), "");

        if(relativePath.startsWith("/"))
            relativePath = relativePath.substring(1);

        return relativePath;
    }
}

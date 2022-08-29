/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import com.alloc64.apktools.FoldersLister;
import com.alloc64.apktools.apk.tools.ApkTools;
import com.alloc64.apktools.apk.tools.ApkZipper;
import com.alloc64.apktools.keystore.KeystoreInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XApkFile extends ZipFileAware
{
    private File xapkFile;
    private File tempFolder;
    private File baseFolder;

    private File baseApk;
    private List<File> splitList = new ArrayList<>();
    private List<File> obbList = new ArrayList<>();
    private List<File> otherList = new ArrayList<>();

    private String xapkBasename;
    private File xapkWorkingFolder;
    private File xapkUnzippedFolder;

    public XApkFile()
    {

    }

    public XApkFile(File tempFolder, File xapkFile)
    {
        this();

        this.tempFolder = tempFolder;
        this.xapkFile = xapkFile;
        this.xapkBasename = FilenameUtils.getBaseName(xapkFile.getName());
        this.xapkWorkingFolder = new File(tempFolder, xapkBasename);
        this.xapkUnzippedFolder = new File(xapkWorkingFolder, "unzipped");
    }

    public File getXApkWorkingFolder()
    {
        return xapkWorkingFolder;
    }

    public File getXApkUnzippedFolder()
    {
        return xapkUnzippedFolder;
    }

    public void setBaseFolder(File baseFolder)
    {
        this.baseFolder = baseFolder;
    }

    public File getBaseApk()
    {
        return baseApk;
    }

    public void setBaseApk(File baseApk)
    {
        this.baseApk = baseApk;
    }

    public List<File> getSplitList()
    {
        return splitList;
    }

    public void addSplitFile(File file) throws Exception
    {
        addFile(file, splitList);
    }

    public void addSplitFiles(List<File> file) throws Exception
    {
        addFiles(file, splitList);
    }

    public List<File> getObbList()
    {
        return obbList;
    }

    public void addObbFile(File file) throws Exception
    {
        addFile(file, obbList);
    }

    public void addObbFiles(List<File> file) throws Exception
    {
        addFiles(file, obbList);
    }

    public List<File> getOtherList()
    {
        return otherList;
    }

    public void addOtherFile(File file) throws Exception
    {
        addFile(file, otherList);
    }

    public void addOtherFiles(List<File> file) throws Exception
    {
        addFiles(file, otherList);
    }

    public void setReadme(File parentFolder, InputStream stream) throws Exception
    {
        File file = new File(parentFolder, "README.txt");
        FileUtils.copyInputStreamToFile(stream, file);

        addOtherFile(file);
    }

    private void addFile(File file, List<File> targetCollection) throws Exception
    {
        if (file == null || !file.exists())
            throw new FileNotFoundException("File not found: " + file);

        targetCollection.add(file);
    }

    public void addFiles(List<File> file, List<File> targetCollection) throws Exception
    {
        for (File f : file)
            addFile(f, targetCollection);
    }

    public void unzip() throws Exception
    {
        if (xapkFile == null || !xapkFile.exists())
            throw new FileNotFoundException("XAPK ZIP file does not exists: " + xapkFile);

        if (xapkWorkingFolder.exists())
            FileUtils.deleteDirectory(xapkWorkingFolder);

        xapkWorkingFolder.mkdir();

        ApkTools.zipper().unzip(xapkFile, xapkUnzippedFolder);
    }

    public void disassemble() throws Exception
    {
        unzip();

        FoldersLister.listAll(xapkUnzippedFolder, file ->
        {
            String name = file.getName();
            String extension = FilenameUtils.getExtension(name);

            if (isInRoot(file) && "apk".equals(extension) && file.isFile())
            {
                if (baseApk == null && name.contains("base"))
                    setBaseApk(file);
                else
                    addSplitFile(file);
            }
            else if (isInRoot(file) && "obb".equals(extension) && file.isFile())
            {
                addObbFile(file);
            }
            else if (file.isFile())
            {
                addOtherFile(file);
            }

            return true;
        });
    }

    public File assemble() throws Exception
    {
        return zip();
    }

    public File assembleAndResignApks(KeystoreInfo keystore) throws Exception
    {
        for(File file : toApkList())
        {
            ApkFile apkFile = null;

            try
            {
                apkFile = new ApkFile(xapkWorkingFolder, file);

                apkFile.unzip();

                File zipalignedFile = apkFile.assembleZipOnly(keystore); // META-INF gets removed here and all APKs are resigned again. (Because all signatures in XAPK must be the same.)

                zipalignedFile.renameTo(file);
            }
            finally
            {
                if(apkFile != null)
                    apkFile.dispose();
            }
        }

        return zip();
    }

    private File zip() throws IOException
    {
        File zippedFile = new File(tempFolder, String.format("%s.xapk", xapkBasename));

        ApkZipper zipper = new ApkZipper();
        zipper.zip(xapkUnzippedFolder, zippedFile);

        if (!zippedFile.exists())
            throw new IllegalStateException("Failed to zip file " + xapkFile + " to: " + zippedFile);

        return zippedFile;
    }

    private boolean isInRoot(File file)
    {
        if (file == null || file.getParentFile() == null)
            return false;

        File p0 = file.getParentFile();
        File p1 = xapkUnzippedFolder;

        return p0.equals(p1);
    }

    public boolean hasMultipleFiles()
    {
        return splitList.size() > 0 || obbList.size() > 0;
    }

    public void toZip(File outputFile) throws Exception
    {
        if(outputFile.exists())
            outputFile.delete();

        ZipParameters zipParameters = new ZipParameters();

        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setCompressionLevel(CompressionLevel.MAXIMUM);

        if(baseFolder != null)
            zipParameters.setDefaultFolderPath(baseFolder.getAbsolutePath());

        ZipFile zipFile = new ZipFile(outputFile);

        List<File> zippedFiles = new ArrayList<>();

        if(baseApk != null)
        {
            String baseApkName = baseApk.getName();
            if(!baseApkName.contains("base"))
            {
                ZipParameters param = new ZipParameters(zipParameters);
                param.setFileNameInZip(String.format("%s-base.%s", FilenameUtils.getBaseName(baseApkName), FilenameUtils.getExtension(baseApkName)));

                zipFile.addFile(baseApk, param);
            }
            else
            {
                zipFile.addFile(baseApk);
            }
        }

        zippedFiles.addAll(splitList);
        zippedFiles.addAll(obbList);
        zippedFiles.addAll(otherList);

        for(File f : zippedFiles)
        {
            zipFile.addFile(f, zipParameters);
        }
    }

    public List<File> toApkList()
    {
        List<File> list = new ArrayList<>();
        if (this.baseApk != null)
            list.add(this.baseApk);

        list.addAll(this.splitList);
        return list;
    }

    @Override
    public void dispose() throws IOException
    {
        if(xapkWorkingFolder != null && xapkWorkingFolder.exists())
            FileUtils.deleteDirectory(xapkWorkingFolder);

        if(xapkUnzippedFolder != null && xapkUnzippedFolder.exists())
            FileUtils.deleteDirectory(xapkUnzippedFolder);

        if(baseApk != null)
            baseApk.delete();

        if(splitList != null)
            for(File splitFile : splitList)
                if(splitFile != null)
                    splitFile.delete();

        if(obbList != null)
            for(File obbFile : obbList)
                if(obbFile != null)
                    obbFile.delete();

        if(otherList != null)
            for(File otherFile : otherList)
                if(otherFile != null)
                    otherFile.delete();
    }
}

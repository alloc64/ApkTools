/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import com.alloc64.apktools.FoldersLister;
import com.alloc64.apktools.apk.tools.ApkTools;
import com.alloc64.apktools.apk.tools.ApkZipper;
import com.alloc64.apktools.keystore.KeystoreInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toCollection;

public class ApkFile extends ZipFileAware
{
    public static final int allowedMaxDexCount = 20;

    public interface AsyncFunction<Param, Return>
    {
        Return apply(Param p) throws Exception;
    }

    private SmaliReferenceResolver referenceResolver;

    private final File apkFile;
    private final File tempFolder;
    private final String apkBasename;

    private final File apkWorkingFolder;
    private final File apkUnzippedFolder;

    private AndroidManifest androidManifest;
    private File metaInfFolder;
    private List<DexFile> dexFileList = new ArrayList<>();
    private List<File> otherFiles = new ArrayList<>();

    private boolean deleteWorkingFolder;

    private FoldersLister.FileCallback dexListingCallback;

    public ApkFile(File tempFolder, File apkFile)
    {
        this.tempFolder = tempFolder;
        this.apkFile = apkFile;

        this.apkBasename = FilenameUtils.getBaseName(apkFile.getName());

        this.apkWorkingFolder = new File(tempFolder, apkBasename);
        this.apkUnzippedFolder = new File(apkWorkingFolder, "unzipped");

        this.referenceResolver = new SmaliReferenceResolver(this);
    }

    public File getApkFile()
    {
        return apkFile;
    }

    public File getApkWorkingFolder()
    {
        return apkWorkingFolder;
    }

    public File getApkUnzippedFolder()
    {
        return apkUnzippedFolder;
    }

    public File getTempFolder()
    {
        return tempFolder;
    }

    public AndroidManifest getAndroidManifest()
    {
        if (androidManifest == null)
            throw new IllegalStateException("No manifest found in: " + apkFile);

        return androidManifest;
    }

    public File getMetaInfFolder()
    {
        return metaInfFolder;
    }

    public List<DexFile> getDexFileList()
    {
        return dexFileList;
    }

    public DexFile getFirstDex()
    {
        return dexFileList.size() < 1 ? null : dexFileList.get(0);
    }

    public DexFile getLastDex()
    {
        return dexFileList.size() < 1 ? null : dexFileList.get(dexFileList.size() - 1);
    }

    public int getMethodCount()
    {
        return dexFileList.stream()
                .map(DexFile::getTotalMethodCount)
                .reduce(0, (x, y) -> x + y);
    }

    public int getReferencedMethodCount()
    {
        return dexFileList.stream()
                .map(DexFile::getReferencedMethodCount)
                .reduce(0, (x, y) -> x + y);
    }

    public SmaliFile findClass(String className)
    {
        for (DexFile dexFile : dexFileList)
        {
            SmaliFile clazz = dexFile.findClass(className);

            if (clazz != null)
                return clazz;
        }

        return null;
    }

    public List<File> getOtherFiles()
    {
        return otherFiles;
    }

    public void setDexListingCallback(FoldersLister.FileCallback dexListingCallback)
    {
        this.dexListingCallback = dexListingCallback;
    }

    public void setDeleteWorkingFolder(boolean deleteWorkingFolder)
    {
        this.deleteWorkingFolder = deleteWorkingFolder;
    }

    public void disassemble() throws Exception
    {
        unzip();
        disassembleOnly();
    }

    public void disassembleOnly() throws Exception
    {
        dexAsync(dexFileList, dexFile ->
        {
            dexFile.disassemble(dexListingCallback);
            return null;
        });

        referenceResolver.resolve();
    }

    public File assemble(KeystoreInfo keystore) throws Exception
    {
        dexAsync(dexFileList, dexFile ->
        {
            dexFile.assemble(dexFile.getFile());
            return null;
        });

        return assembleZipOnly(keystore);
    }

    public File assembleZipOnly(KeystoreInfo keystore) throws Exception
    {
        deleteMetaInfFolder();

        File zippedApk = zip();

        if (keystore == null)
        {
            deleteWorkingFolder();

            return zippedApk;
        }

        File signedApk = sign(zippedApk, keystore);
        File zipalignedApk = zipalign(signedApk);

        deleteWorkingFolder();

        zippedApk.delete();
        signedApk.delete();

        return zipalignedApk;
    }

    private void deleteMetaInfFolder() throws Exception
    {
        if (metaInfFolder != null && metaInfFolder.exists())
            FileUtils.deleteDirectory(metaInfFolder);
    }

    private void deleteWorkingFolder() throws Exception
    {
        if (deleteWorkingFolder)
            FileUtils.deleteDirectory(apkWorkingFolder);
    }

    private void dexAsync(List<DexFile> dexFiles, ApkFile.AsyncFunction<DexFile, Void> func) throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool(dexFiles.size());

        List<Future<Void>> futureList = new ArrayList<>();

        for (DexFile dex : dexFiles)
        {
            Future<Void> future = executor.submit(() ->
            {
                func.apply(dex);
                return null;
            });

            futureList.add(future);
        }

        for (int i = 0; i < futureList.size(); i++)
        {
            Future<Void> future = futureList.get(i);

            try
            {
                future.get();
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Unable to process DEX: " + dexFiles.get(i).getAbsolutePath(), e);
            }
        }

        executor.shutdown();
    }

    public ArrayList<SmaliFile> getClassesInManifest()
    {
        return androidManifest.getManifest()
                .getAllClassNames()
                .stream()
                .map(clazz -> findClass(ApkTools.toSmali(clazz)))
                .filter(Objects::nonNull)
                .collect(toCollection(ArrayList::new));
    }

    private boolean isInRoot(File file)
    {
        if (file == null || file.getParentFile() == null)
            return false;

        File p0 = file.getParentFile();
        File p1 = apkUnzippedFolder;

        return p0.equals(p1);
    }

    public void unzip() throws Exception
    {
        if (apkWorkingFolder.exists())
            FileUtils.deleteDirectory(apkWorkingFolder);

        apkWorkingFolder.mkdir();

        unzipOnly();

        FoldersLister.listAll(apkUnzippedFolder, file ->
        {
            String name = file.getName();

            if (isInRoot(file) && "AndroidManifest.xml".equals(name) && file.isFile())
            {
                this.androidManifest = new AndroidManifest(file);
            }
            else if (isInRoot(file) && "META-INF".equals(name) && file.isDirectory())
            {
                this.metaInfFolder = file;
            }
            else if (isInRoot(file) && name.startsWith("classes") && name.endsWith(".dex") && file.isFile())
            {
                dexFileList.add(new DexFile(this, file));
            }

            return true;
        });

        dexFileList.sort(Comparator.comparing(DexFile::getAbsolutePath));

        if (androidManifest != null)
            androidManifest.parse();
    }

    public void unzipOnly() throws IOException
    {
        ApkTools.zipper().unzip(apkFile, apkUnzippedFolder);

        if (!apkUnzippedFolder.exists())
            throw new IllegalStateException("Failed to unzip file " + apkFile + " to folder: " + apkUnzippedFolder);
    }

    public File zip() throws IOException
    {
        File zippedFile = new File(tempFolder, String.format("%s_zipped.apk", apkBasename));

        ApkZipper zipper = new ApkZipper();
        zipper.zip(apkUnzippedFolder, zippedFile);

        if (!zippedFile.exists())
            throw new IllegalStateException("Failed to zip file " + apkFile + " to: " + zippedFile);

        return zippedFile;
    }

    public File sign(File outputFile, KeystoreInfo keystore) throws IOException, InterruptedException
    {
        ApkTools.signer().sign(keystore, outputFile);

        if (!outputFile.exists())
            throw new IllegalStateException("Failed to sign file: " + outputFile);

        return outputFile;
    }

    public File zipalign(File inputFile) throws Exception
    {
        File zipalignedFile = new File(tempFolder, String.format("%s_zipaligned.apk", apkBasename));

        ApkTools.zipaligner().zipalign(inputFile, zipalignedFile);

        if (!zipalignedFile.exists())
            throw new IllegalStateException("Failed to zipalign file " + apkFile + " to: " + zipalignedFile);

        return zipalignedFile;
    }

    public DexFile createNewDex()
    {
        String dexName = getLastDex()
                .getDisassembledDexFolder()
                .getName()
                .replace(DexFile.CLASSES_DEX, "");

        if(StringUtils.isEmpty(dexName))
            dexName = "1";

        int dexId = Integer.valueOf(dexName);

        dexId++;

        File newDexFile = new File(getApkUnzippedFolder(), String.format("%s%d.dex", DexFile.CLASSES_DEX, dexId));

        if (newDexFile.exists())
            throw new IllegalStateException("New DEX folder already exists. Inconsistent state detected. " + newDexFile);

        DexFile newDex = new DexFile(this, newDexFile);

        getDexFileList().add(newDex);

        return newDex;
    }

    @Override
    public void dispose() throws IOException
    {
        if(apkWorkingFolder.exists())
            FileUtils.deleteDirectory(apkWorkingFolder);

        if(apkUnzippedFolder.exists())
            FileUtils.deleteDirectory(apkUnzippedFolder);
    }
}

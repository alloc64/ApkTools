/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import com.alloc64.apktools.FoldersLister;
import com.alloc64.apktools.env.OsType;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class DexLib extends ExtractableJarExecutable
{
    private final File apkToolPath;
    private final File baksmaliPath;
    private final File smaliPath;

    public DexLib()
    {
        this.apkToolPath = getOrExtractFile(OsType.Universal, "apktool_2.4.1.jar");
        this.baksmaliPath = getOrExtractFile(OsType.Universal, "baksmali-2.2.6.jar");
        this.smaliPath = getOrExtractFile(OsType.Universal, "smali-2.2.6.jar");
    }

    public void deapktoolAPK(File apkFile, File outputFolder, FoldersLister.FileCallback callback) throws Exception
    {
        apktool(new String[]{"d", apkFile.getPath(), "-o", outputFolder.getPath()});

        if (callback != null)
            FoldersLister.loadFilesForFolder(outputFolder, callback);
    }

    public void apktoolAPK(File apkFile, File outputFolder, FoldersLister.FileCallback callback) throws Exception
    {
        apktool(new String[]{"b", apkFile.getPath(), "-o", outputFolder.getPath()});

        if (callback != null)
            FoldersLister.loadFilesForFolder(outputFolder, callback);
    }

    public void disassembleDEX(String dexFile, File outputFolder, FoldersLister.FileCallback callback) throws Exception
    {
        if (outputFolder.exists())
            FileUtils.deleteDirectory(outputFolder);

        outputFolder.mkdir();

        baksmali(new String[] {"d", dexFile, "-o", outputFolder.getAbsolutePath()});

        if (callback != null)
            FoldersLister.loadFilesForFolder(outputFolder, callback);
    }

    public void assembleDEX(File inputFolder, File outputDexFile) throws Exception
    {
        if (inputFolder == null || !inputFolder.exists() || !inputFolder.isDirectory())
            throw new FileNotFoundException("assembleDEX inputFolder");

        smali(new String[] {"a", inputFolder.getAbsolutePath(), "-o", outputDexFile.getAbsolutePath()});
    }

    public void deapktoolAPKNoSmali(File apkFile, File outputFolder) throws Exception
    {
        apktool(new String[]{"d", apkFile.getPath(), "-o", outputFolder.getPath(), "-s"});
    }

    public void apktoolAPKNoSmali(File apkFile, File outputFolder, boolean useAapt2) throws Exception
    {
        if (useAapt2)
        {
            apktool(new String[]{"--use-aapt2", "b", apkFile.getPath(), "-o", outputFolder.getPath()});
            return;
        }

        apktool(new String[]{"b", apkFile.getPath(), "-o", outputFolder.getPath()});
    }

    // region Classloader jar injection

    private void apktool(String[] args) throws Exception
    {
        executeJar(apkToolPath, "brut.apktool.Main", "main", args);
    }

    private void baksmali(String[] args) throws Exception
    {
        executeJar(baksmaliPath, "org.jf.baksmali.Main", "main", args);
    }

    private void smali(String[] args) throws Exception
    {
        executeJar(smaliPath, "org.jf.smali.Main", "main", args);
    }

    // endregion
}

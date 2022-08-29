/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import com.alloc64.apktools.FoldersLister;
import com.alloc64.apktools.apk.tools.ApkTools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DexFile extends FileAware
{
    public static final String CLASSES_DEX = "classes";

    private String nameWithoutExt;
    private File disassembledDexFolder;

    private Map<String, SmaliFile> smaliFiles = new TreeMap<>();

    public DexFile(ApkFile apkFile, File file)
    {
        super(file);

        this.nameWithoutExt = file.getName().replace(".dex", "");
        this.disassembledDexFolder = new File(apkFile.getApkWorkingFolder(), nameWithoutExt);
    }

    public File getDisassembledDexFolder()
    {
        return disassembledDexFolder;
    }

    public int getTotalMethodCount()
    {
        return getSmaliFileList().stream()
                .map(SmaliFile::getMethodCount)
                .reduce(0, (x, y) -> x + y);
    }

    public int getReferencedMethodCount()
    {
        return getSmaliFileList().stream()
                .map(SmaliFile::getReferencedMethodCount)
                .reduce(0, (x, y) -> x + y);
    }

    public Map<String, SmaliFile> getSmaliFiles()
    {
        return smaliFiles;
    }

    public Collection<SmaliFile> getSmaliFileList()
    {
        return smaliFiles.values();
    }

    public SmaliFile findClass(String clazz)
    {
        return smaliFiles.get(clazz);
    }

    public void disassemble(FoldersLister.FileCallback listingCallback) throws Exception
    {
        ApkTools.dex().disassembleDEX(getFile().getAbsolutePath(), disassembledDexFolder, file ->
        {
            SmaliFile smaliFile = new SmaliFile(file);
            smaliFile.parse();

            smaliFiles.put(smaliFile.getFullClassName(), smaliFile);

            if(listingCallback != null)
                listingCallback.onProcessFile(file);
        });
    }

    public void assemble() throws Exception
    {
        this.assemble(getFile());
    }

    public void assemble(File outputFile) throws Exception
    {
        ApkTools.dex().assembleDEX(disassembledDexFolder, outputFile);
    }


    // TODO: k refaktoru, smerem dolu

    public boolean addFile(SmaliFile clazz) throws IOException
    {
        if(clazz == null || !clazz.exists())
            return false;

        SmaliFile targetSmaliFile = new SmaliFile(
                new File(getDisassembledDexFolder(), String.format("%s/%s", ApkTools.toSmali(clazz.getPackageName()), clazz.getName()))
        );

        File targetFile = targetSmaliFile.getFile();

        if(targetFile.exists())
        {
            System.out.println("Overriding already existing class: " + targetSmaliFile.getAbsolutePath());
            targetFile.delete();
        }

        FileUtils.copyFile(clazz.getFile(), targetFile);

        clazz.setFile(targetFile);

        smaliFiles.put(clazz.getFullClassName(), clazz);

        return true;
    }

    public boolean addFileOnly(SmaliFile clazz)
    {
        if(clazz == null || !clazz.exists())
            return false;

        smaliFiles.put(clazz.getFullClassName(), clazz);

        return true;
    }

    public boolean removeFileReference(SmaliFile clazz)
    {
        if(clazz == null || !clazz.exists())
            return false;

        return smaliFiles.remove(clazz.getFullClassName()) != null;
    }
}

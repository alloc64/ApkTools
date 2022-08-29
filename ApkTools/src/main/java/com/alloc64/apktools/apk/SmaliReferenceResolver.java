/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import java.util.Map;
import java.util.TreeMap;

public class SmaliReferenceResolver
{
    private final ApkFile apkFile;

    public SmaliReferenceResolver(ApkFile apkFile)
    {
        this.apkFile = apkFile;
    }

    public void resolve()
    {
        Map<String, SmaliFile> allSmaliFiles = new TreeMap<>();

        for (DexFile dex : apkFile.getDexFileList())
        {
            allSmaliFiles.putAll(
                    dex.getSmaliFiles()
            );
        }

        for (Map.Entry<String, SmaliFile> kvp : allSmaliFiles.entrySet())
        {
            SmaliFile smaliFile = kvp.getValue();

            if (smaliFile == null)
                continue;

            SmaliFile superClass = allSmaliFiles.get(smaliFile.getFullSuperClassName());

            if (superClass != null)
                smaliFile.setSuperClass(superClass);
        }

        /*
        for (DexFileV2 dex : apkFile.getDexFileList())
        {
            SmaliFileV2 clazz = dex.findClass("a/a/sdk/pub/dx/tasks/UrlTask");

            int refs = clazz.getReferencedMethodCount();

            System.currentTimeMillis();

            int referencedMethodCount = dex.getReferencedMethodCount();
            int totalMethodCount = dex.getTotalMethodCount();

            System.currentTimeMillis();
        }
        */
    }
}

/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import com.alloc64.apktools.ProcessUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Adb
{
    public String installApk(File apkFile, boolean force, String... args) throws Exception
    {
        if(apkFile == null || !apkFile.exists())
            throw new FileNotFoundException("APK file not found: " + apkFile);

        List<String> a = new ArrayList<>();
        a.add("adb");
        a.add("install");

        if(force)
            a.add("-r");

        a.add(apkFile.getAbsolutePath());
        a.addAll(Arrays.asList(args));

        return ProcessUtils.runProcessAndReturnErrors(a.toArray(new String[0]));
    }

    public String uninstallApk(String packageName) throws Exception
    {
        return ProcessUtils.runProcessAndReturnErrors("adb", "uninstall", packageName);
    }
}

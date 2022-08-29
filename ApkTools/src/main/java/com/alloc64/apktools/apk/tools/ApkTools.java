/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

public class ApkTools
{
    private static final ApkZipper apkZipper;
    private static final ApkSigner apkSigner;
    private static final ApkZipalign apkZipalign;
    private static final DexLib dexLib;
    private static final Adb adb;

    static
    {
        apkZipper = new ApkZipper();
        apkSigner = new ApkSigner();
        apkZipalign = new ApkZipalign();
        dexLib = new DexLib();
        adb = new Adb();
    }

    public static ApkZipper zipper()
    {
        return apkZipper;
    }

    public static ApkSigner signer()
    {
        return apkSigner;
    }

    public static ApkZipalign zipaligner()
    {
        return apkZipalign;
    }

    public static DexLib dex()
    {
        return dexLib;
    }

    public static Adb adb()
    {
        return adb;
    }

    public static String fromSmali(String clazz)
    {
        if (clazz == null)
            return null;

        return clazz.replace("/", ".");
    }

    public static String toSmali(String clazz)
    {
        if (clazz == null)
            return null;

        return clazz.replace(".", "/");
    }
}

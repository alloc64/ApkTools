/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.env;

import java.util.Locale;

public enum OsType
{
    Windows("windows"),
    MacOS("macos"),
    Linux("linux"),
    Other("other");

    public static OsType Universal = null;

    private static OsType detectedOS;
    private final String value;

    OsType(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public static OsType get()
    {
        if (detectedOS == null)
        {
            String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
            if ((OS.contains("mac")) || (OS.contains("darwin")))
                detectedOS = OsType.MacOS;
            else if (OS.contains("win"))
                detectedOS = OsType.Windows;
            else if (OS.contains("nux"))
                detectedOS = OsType.Linux;
            else
                detectedOS = OsType.Other;
        }

        return detectedOS;
    }
}
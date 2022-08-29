/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import java.io.IOException;

public abstract class ZipFileAware
{
    public static boolean isZip(byte[] bytes)
    {
        if(bytes == null || bytes.length < 4)
            return false;

        // 50 4B 03 04
        return bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04;
    }

    public abstract void dispose() throws IOException;
}

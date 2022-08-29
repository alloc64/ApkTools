/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import com.alloc64.apktools.ProcessUtils;
import com.alloc64.apktools.keystore.KeystoreInfo;

import java.io.File;
import java.io.IOException;

public class ApkSigner extends ExtractableExecutable
{
    public void sign(KeystoreInfo keystore, File outputApkFile) throws IOException, InterruptedException
    {
        sign(keystore.getKeystoreFile(), outputApkFile, keystore.getAlias(), keystore.getPassphrase());
    }

    public void sign(File keystoreFile, File outputApkFile, String alias, String password) throws IOException, InterruptedException
    {
        int statusCode = ProcessUtils.runProcessNoLogging("jarsigner", "-verbose", "-sigalg", "SHA1withRSA", "-digestalg", "SHA1", "-keystore", keystoreFile.getAbsolutePath(), outputApkFile.getAbsolutePath(), alias, "-storepass", password);

        if(statusCode > 0)
            throw new IllegalStateException("Unable to sign APK: " + outputApkFile + " non-zero status code returned (" + statusCode + ")");

        System.out.println("Signed APK: " + outputApkFile);
    }

    public void signWithAndroidDebugKey(File keystoreFile, File outputApkFile) throws IOException, InterruptedException
    {
        sign(keystoreFile, outputApkFile, "androiddebugkey", "android");
    }
}

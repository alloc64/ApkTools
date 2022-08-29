/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.keystore;

import com.alloc64.apktools.ProcessUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class KeystoreGenerator
{
	public static KeystoreInfo generateKeystore(File outputFile, String alias, String password) throws IOException, InterruptedException
	{
		if(outputFile == null)
			throw new FileNotFoundException("Keystore is null");

		ProcessUtils.runProcess("keytool", "-genkey", "-v", "-keystore", outputFile.getAbsolutePath(), "-storepass", password, "-alias", alias, "-keypass", password, "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000", "-dname", "C=US, O=Android, CN=Android Debug");

		KeystoreInfo ki = null;

		if(outputFile.exists())
		{
			ki = new KeystoreInfo(outputFile, alias, password);
			outputFile.delete();
		}

		return ki;
	}
}

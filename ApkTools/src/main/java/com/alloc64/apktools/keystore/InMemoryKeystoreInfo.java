/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.keystore;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class InMemoryKeystoreInfo extends KeystoreInfo
{
	public InMemoryKeystoreInfo(File tempFolder, byte[] keystoreBytes, String alias, String passphrase) throws IOException
	{
		super(createTemporaryFile(tempFolder, keystoreBytes), alias, passphrase);
	}

	public InMemoryKeystoreInfo(File keystoreFile, String alias, String passphrase)
	{
		super(keystoreFile, alias, passphrase);
	}

	private static File createTemporaryFile(File tempFolder, byte[] bytes) throws IOException
	{
		File file = File.createTempFile("keystore", ".jks", tempFolder);
		FileUtils.writeByteArrayToFile(file, bytes);

		return file;
	}
}

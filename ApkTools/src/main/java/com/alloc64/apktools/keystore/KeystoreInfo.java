/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.keystore;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class KeystoreInfo
{
	private File keystoreFile;
	private String alias;
	private String passphrase;

	public KeystoreInfo(File keystoreFile, String alias, String passphrase)
	{
		this.keystoreFile = keystoreFile;
		this.alias = alias;
		this.passphrase = passphrase;
	}

	public File getKeystoreFile()
	{
		return keystoreFile;
	}

	public byte[] getKeystoreFileBytes() throws IOException
	{
		return FileUtils.readFileToByteArray(keystoreFile);
	}

	public String getAlias()
	{
		return alias;
	}

	public String getPassphrase()
	{
		return passphrase;
	}

	public void dispose()
	{
		keystoreFile.delete();
	}
}

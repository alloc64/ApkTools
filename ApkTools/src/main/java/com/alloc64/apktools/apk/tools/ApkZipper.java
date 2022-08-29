/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ApkZipper
{
    private final static Pattern NO_COMPRESS_PATTERN = Pattern.compile("(" +
            "jpg|jpeg|png|gif|wav|mp2|mp3|ogg|aac|mpg|mpeg|mid|midi|smf|jet|rtttl|imy|xmf|mp4|" +
            "m4a|m4v|3gp|3gpp|3g2|3gpp2|amr|awb|wma|wmv|webm|mkv)$");

    private byte[] buffer = new byte[1024];
    private CRC32 crc = new CRC32();

    public void zip(final File folder, final File zipFile) throws IOException
    {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile)))
        {
            processFolder(folder, zipOutputStream, folder.getPath().length() + 1);
        }
    }

    public List<File> unzip(File zipFile, File outputFolder) throws IOException
    {
        List<File> result = new ArrayList<>();

        int BUFFER = 8 * 1024;
        ZipFile zip = new ZipFile(zipFile);

        Enumeration zipFileEntries = zip.entries();

        while (zipFileEntries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();

            File destFile = new File(outputFolder, currentEntry);
            File destinationParent = destFile.getParentFile();

            destinationParent.mkdirs();

            if (!entry.isDirectory())
            {
                result.add(destFile);

                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));

                int currentByte;
                byte data[] = new byte[BUFFER];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    dest.write(data, 0, currentByte);

                dest.flush();
                dest.close();
                is.close();
            }
        }

        return result;
    }

    private void processFolder(final File folder, final ZipOutputStream zos, final int prefixLength) throws IOException
    {
        for (final File file : folder.listFiles())
        {
            if (file.isDirectory())
            {
                processFolder(file, zos, prefixLength);
                continue;
            }

            String ext = FilenameUtils.getExtension(file.getName());

            ZipEntry entry = new ZipEntry(file.getPath().substring(prefixLength));
            entry.setCompressedSize(-1);

            if (ext.isEmpty() || !NO_COMPRESS_PATTERN.matcher(ext).find())
            {
                entry.setMethod(ZipEntry.DEFLATED);
            }
            else
            {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                crc.reset();

                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1)
                    crc.update(buffer, 0, bytesRead);

                bis.close();

                entry.setMethod(ZipEntry.STORED);
                entry.setCompressedSize(file.length());
                entry.setSize(file.length());
                entry.setCrc(crc.getValue());
            }

            zos.putNextEntry(entry);

            try (FileInputStream is = new FileInputStream(file))
            {
                IOUtils.copy(is, zos);
            }

            zos.closeEntry();
        }
    }

}

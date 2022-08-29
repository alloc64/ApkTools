/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ApkZipalign
{
    private static final String UTF8 = "UTF-8";

    private static final int ZIP_ENTRY_HEADER_LEN = 30;
    private static final int ZIP_ENTRY_VERSION = 20;
    private static final int ZIP_ENTRY_USES_DATA_DESCR = 0x0008;
    private static final int ZIP_ENTRY_DATA_DESCRIPTOR_LEN = 16;
    private static final int DEFAULT_ALIGNMENT = 4;

    private static final int FILE_BUFFER = 32 * 1024;

    private static class XEntry
    {
        final ZipEntry entry;
        final long headerOffset;
        final int flags;
        final int padding;

        /**
         * Creates new instance.
         *
         * @param entry        the entry.
         * @param headerOffset the offset of the header.
         * @param flags        the flags.
         * @param padding      the padding of the "extra" field.
         */
        XEntry(ZipEntry entry, long headerOffset, int flags, int padding)
        {
            this.entry = entry;
            this.headerOffset = headerOffset;
            this.flags = flags;
            this.padding = padding;
        }
    }

    private static class FilterOutputStreamEx extends FilterOutputStream
    {
        private long totalWritten = 0;

        FilterOutputStreamEx(OutputStream out)
        {
            super(out);
        }

        @Override
        public void write(byte[] b) throws IOException
        {
            out.write(b);
            totalWritten += b.length;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            out.write(b, off, len);
            totalWritten += len;
        }

        @Override
        public void write(int b) throws IOException
        {
            out.write(b);
            totalWritten += 1;
        }

        /**
         * Writes a 32-bit int to the output stream in little-endian byte order.
         *
         * @param v the data to write.
         * @throws IOException
         */
        void writeInt(long v) throws IOException
        {
            write((int) ((v >>> 0) & 0xff));
            write((int) ((v >>> 8) & 0xff));
            write((int) ((v >>> 16) & 0xff));
            write((int) ((v >>> 24) & 0xff));
        }// writeInt()

        /**
         * Writes a 16-bit short to the output stream in little-endian byte
         * order.
         *
         * @param v the data to write.
         * @throws IOException
         */
        void writeShort(int v) throws IOException
        {
            write((v >>> 0) & 0xff);
            write((v >>> 8) & 0xff);
        }
    }

    static class ZipAligner
    {
        private final File inputFile;
        private final int alignment;
        private final File outputFile;
        private final List<XEntry> entries = new ArrayList<>();

        private ZipFile zipFile;
        private RandomAccessFile randomAccessFileInput;
        private FilterOutputStreamEx outputStream;
        private long inputFileOffset = 0;
        private int totalPadding = 0;

        ZipAligner(File input, File output)
        {
            this(input, DEFAULT_ALIGNMENT, output);
        }

        ZipAligner(File input, int alignment, File output)
        {
            this.inputFile = input;
            this.alignment = alignment;
            this.outputFile = output;
        }

        void run() throws Exception
        {
            try
            {
                this.zipFile = new ZipFile(inputFile);
                this.randomAccessFileInput = new RandomAccessFile(inputFile, "r");
                this.outputStream = new FilterOutputStreamEx(new BufferedOutputStream(new FileOutputStream(outputFile), FILE_BUFFER));

                copyAllEntries();
                buildCentralDirectory();
            }
            finally
            {
                IOUtils.closeQuietly(zipFile);
                IOUtils.closeQuietly(randomAccessFileInput);
                IOUtils.closeQuietly(outputStream);
            }
        }

        /**
         * Copies all entries, aligning them if needed.
         * <p>
         * This takes 80% of total.
         * </p>
         *
         * @throws IOException
         */
        private void copyAllEntries() throws IOException
        {
            final int entryCount = zipFile.size();

            if (entryCount == 0)
                return;

            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                final ZipEntry entry = entries.nextElement();

                int flags = entry.getMethod() == ZipEntry.STORED ? 0 : 1 << 3;
                flags |= 1 << 11;

                final long outputEntryHeaderOffset = outputStream.totalWritten;

                final int inputEntryHeaderSize = ZIP_ENTRY_HEADER_LEN
                        + (entry.getExtra() != null ? entry.getExtra().length
                        : 0)
                        + entry.getName().getBytes(UTF8).length;
                final long inputEntryDataOffset = inputFileOffset
                        + inputEntryHeaderSize;

                final int padding;

                if (entry.getMethod() != ZipEntry.STORED)
                {
                    /*
                     * The entry is compressed, copy it without padding.
                     */
                    padding = 0;
                }
                else
                {
                    /*
                     * Copy the entry, adjusting as required. We assume that the
                     * file position in the new file will be equal to the file
                     * position in the original.
                     */
                    long newOffset = inputEntryDataOffset + totalPadding;

                    padding = (int) ((alignment - (newOffset % alignment)) % alignment);
                    totalPadding += padding;
                }

                final XEntry xentry = new XEntry(entry, outputEntryHeaderOffset, flags, padding);
                this.entries.add(xentry);

                /*
                 * Modify the original header, add padding to `extra` field and
                 * copy it to output.
                 */
                byte[] extra = entry.getExtra();
                if (extra == null)
                {
                    extra = new byte[padding];
                    Arrays.fill(extra, (byte) 0);
                }
                else
                {
                    byte[] newExtra = new byte[extra.length + padding];
                    System.arraycopy(extra, 0, newExtra, 0, extra.length);
                    Arrays.fill(newExtra, extra.length, newExtra.length,
                            (byte) 0);
                    extra = newExtra;
                }
                entry.setExtra(extra);

                /*
                 * Now write the header to output.
                 */
                outputStream.writeInt(ZipOutputStream.LOCSIG);
                outputStream.writeShort(ZIP_ENTRY_VERSION);
                outputStream.writeShort(flags);
                outputStream.writeShort(entry.getMethod());

                int modDate;
                int time;
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(entry.getTime()));
                int year = cal.get(Calendar.YEAR);
                if (year < 1980)
                {
                    modDate = 0x21;
                    time = 0;
                }
                else
                {
                    modDate = cal.get(Calendar.DATE);
                    modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                    modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                    time = cal.get(Calendar.SECOND) >> 1;
                    time = (cal.get(Calendar.MINUTE) << 5) | time;
                    time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
                }

                outputStream.writeShort(time);
                outputStream.writeShort(modDate);

                outputStream.writeInt(entry.getCrc());
                outputStream.writeInt(entry.getCompressedSize());
                outputStream.writeInt(entry.getSize());

                outputStream
                        .writeShort(entry.getName().getBytes(UTF8).length);
                outputStream.writeShort(entry.getExtra().length);
                outputStream.write(entry.getName().getBytes(UTF8));
                outputStream.write(entry.getExtra(), 0,
                        entry.getExtra().length);

                /*
                 * Copy raw data.
                 */

                inputFileOffset += inputEntryHeaderSize;

                final long sizeToCopy;
                if ((flags & ZIP_ENTRY_USES_DATA_DESCR) != 0)
                    sizeToCopy = (entry.isDirectory() ? 0 : entry
                            .getCompressedSize())
                            + ZIP_ENTRY_DATA_DESCRIPTOR_LEN;
                else
                    sizeToCopy = entry.isDirectory() ? 0 : entry
                            .getCompressedSize();

                if (sizeToCopy > 0)
                {
                    randomAccessFileInput.seek(inputFileOffset);

                    long totalSizeCopied = 0;
                    final byte[] buf = new byte[FILE_BUFFER];
                    while (totalSizeCopied < sizeToCopy)
                    {
                        int read = randomAccessFileInput.read(
                                buf,
                                0,
                                (int) Math.min(FILE_BUFFER, sizeToCopy
                                        - totalSizeCopied));
                        if (read <= 0)
                            break;

                        outputStream.write(buf, 0, read);
                        totalSizeCopied += read;
                    }
                }

                inputFileOffset += sizeToCopy;
            }
        }

        /**
         * Builds central directory.
         * <p>
         * This takes 10% of total.
         * </p>
         *
         * @throws IOException
         */
        private void buildCentralDirectory() throws IOException
        {
            final long centralDirOffset = outputStream.totalWritten;

            for (XEntry xentry : entries)
            {
                /*
                 * Write entry.
                 */
                final ZipEntry entry = xentry.entry;

                int modDate;
                int time;
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(entry.getTime()));
                int year = cal.get(Calendar.YEAR);
                if (year < 1980)
                {
                    modDate = 0x21;
                    time = 0;
                }
                else
                {
                    modDate = cal.get(Calendar.DATE);
                    modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                    modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                    time = cal.get(Calendar.SECOND) >> 1;
                    time = (cal.get(Calendar.MINUTE) << 5) | time;
                    time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
                }

                outputStream.writeInt(ZipFile.CENSIG); // CEN header signature
                outputStream.writeShort(ZIP_ENTRY_VERSION); // version made by
                outputStream.writeShort(ZIP_ENTRY_VERSION); // version needed
                // to
                // extract
                outputStream.writeShort(xentry.flags); // general purpose bit
                // flag
                outputStream.writeShort(entry.getMethod()); // compression
                // method
                outputStream.writeShort(time);
                outputStream.writeShort(modDate);
                outputStream.writeInt(entry.getCrc()); // crc-32
                outputStream.writeInt(entry.getCompressedSize()); // compressed
                // size
                outputStream.writeInt(entry.getSize()); // uncompressed size
                final byte[] nameBytes = entry.getName().getBytes(UTF8);
                outputStream.writeShort(nameBytes.length);
                outputStream.writeShort(entry.getExtra() != null ? entry
                        .getExtra().length - xentry.padding : 0);
                final byte[] commentBytes;
                if (entry.getComment() != null)
                {
                    commentBytes = entry.getComment().getBytes(UTF8);
                    outputStream.writeShort(Math.min(commentBytes.length,
                            0xffff));
                }
                else
                {
                    commentBytes = null;
                    outputStream.writeShort(0);
                }
                outputStream.writeShort(0); // starting disk number
                outputStream.writeShort(0); // internal file attributes
                // (unused)
                outputStream.writeInt(0); // external file attributes (unused)
                outputStream.writeInt(xentry.headerOffset); // relative offset
                // of
                // local
                // header
                outputStream.write(nameBytes);
                if (entry.getExtra() != null)
                    outputStream.write(entry.getExtra(), 0,
                            entry.getExtra().length - xentry.padding);
                if (commentBytes != null)
                    outputStream.write(commentBytes, 0,
                            Math.min(commentBytes.length, 0xffff));
            }// for xentry


            /*
             * Write the end of central directory.
             */
            final long centralDirSize = outputStream.totalWritten - centralDirOffset;

            final int entryCount = entries.size();

            outputStream.writeInt(ZipFile.ENDSIG); // END record signature
            outputStream.writeShort(0); // number of this disk
            outputStream.writeShort(0); // central directory start disk
            outputStream.writeShort(entryCount); // number of directory entries
            // on
            // disk
            outputStream.writeShort(entryCount); // total number of directory
            // entries
            outputStream.writeInt(centralDirSize); // length of central
            // directory
            outputStream.writeInt(centralDirOffset); // offset of central
            // directory
            if (zipFile.getComment() != null)
            {
                final byte[] bytes = zipFile.getComment().getBytes(UTF8);
                outputStream.writeShort(bytes.length);
                outputStream.write(bytes);
            }
            else
            {
                outputStream.writeShort(0);
            }

            outputStream.flush();
        }
    }

    public void zipalign(File unsignedApkFile, File zipalignedFile) throws Exception
    {
        new ZipAligner(unsignedApkFile, zipalignedFile).run();
    }

}
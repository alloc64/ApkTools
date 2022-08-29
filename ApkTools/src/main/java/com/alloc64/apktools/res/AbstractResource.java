/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.res;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public abstract class AbstractResource implements Resource
{
    public boolean exists()
    {
        try
        {
            return getFile().exists();
        }
        catch (IOException ex)
        {
            try
            {
                getInputStream().close();
                return true;
            }
            catch (Throwable isEx)
            {
                return false;
            }
        }
    }

    public boolean isReadable()
    {
        return exists();
    }

    public boolean isOpen()
    {
        return false;
    }

    public boolean isFile()
    {
        return false;
    }

    public URL getURL() throws IOException
    {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    public URI getURI() throws IOException
    {
        URL url = getURL();
        try
        {
            return ResourceUtils.toURI(url);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException("Invalid URI [" + url + "]", ex);
        }
    }

    public File getFile() throws IOException
    {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    public ReadableByteChannel readableChannel() throws IOException
    {
        return Channels.newChannel(getInputStream());
    }

    public long contentLength() throws IOException
    {
        InputStream is = getInputStream();
        try
        {
            long size = 0L;
            byte[] buf = new byte[256];
            int read;
            while ((read = is.read(buf)) != -1)
            {
                size += read;
            }
            return size;
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException iOException)
            {
            }
        }
    }

    public long lastModified() throws IOException
    {
        File fileToCheck = getFileForLastModifiedCheck();
        long lastModified = fileToCheck.lastModified();
        if (lastModified == 0L && !fileToCheck.exists())
        {
            throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for checking its last-modified timestamp");
        }
        return lastModified;
    }

    protected File getFileForLastModifiedCheck() throws IOException
    {
        return getFile();
    }

    public Resource createRelative(String relativePath) throws IOException
    {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    public String getFilename()
    {
        return null;
    }

    public boolean equals(Object other)
    {
        return (this == other || (other instanceof Resource && ((Resource) other)
                .getDescription().equals(getDescription())));
    }

    public int hashCode()
    {
        return getDescription().hashCode();
    }

    public String toString()
    {
        return getDescription();
    }
}


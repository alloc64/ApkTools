package com.alloc64.apktools.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClassPathResource extends AbstractFileResolvingResource
{
    private final String path;
    private ClassLoader classLoader;
    private Class<?> clazz;

    public ClassPathResource(String path)
    {
        this(path, (ClassLoader) null);
    }

    public ClassPathResource(String path, ClassLoader classLoader)
    {
        if (path == null)
            throw new IllegalStateException("Path must not be null");
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/"))
        {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null) ? classLoader : ClassUtils.getDefaultClassLoader();
    }

    public ClassPathResource(String path, Class<?> clazz)
    {
        if (path == null)
            throw new IllegalStateException("Path must not be null");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    @Deprecated
    protected ClassPathResource(String path, ClassLoader classLoader, Class<?> clazz)
    {
        this.path = StringUtils.cleanPath(path);
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    public final String getPath()
    {
        return this.path;
    }

    public final ClassLoader getClassLoader()
    {
        return (this.clazz != null) ? this.clazz.getClassLoader() : this.classLoader;
    }

    public boolean exists()
    {
        return (resolveURL() != null);
    }

    protected URL resolveURL()
    {
        if (this.clazz != null)
        {
            return this.clazz.getResource(this.path);
        }
        if (this.classLoader != null)
        {
            return this.classLoader.getResource(this.path);
        }
        return ClassLoader.getSystemResource(this.path);
    }

    public InputStream getInputStream() throws IOException
    {
        InputStream is;
        if (this.clazz != null)
        {
            is = this.clazz.getResourceAsStream(this.path);
        }
        else if (this.classLoader != null)
        {
            is = this.classLoader.getResourceAsStream(this.path);
        }
        else
        {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null)
        {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    public URL getURL() throws IOException
    {
        URL url = resolveURL();
        if (url == null)
        {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    public Resource createRelative(String relativePath)
    {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return (this.clazz != null) ? new ClassPathResource(pathToUse, this.clazz) : new ClassPathResource(pathToUse, this.classLoader);
    }

    public String getFilename()
    {
        return StringUtils.getFilename(this.path);
    }

    public String getDescription()
    {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = this.path;
        if (this.clazz != null && !pathToUse.startsWith("/"))
        {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/"))
        {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ClassPathResource))
        {
            return false;
        }
        ClassPathResource otherRes = (ClassPathResource) other;
        return (this.path.equals(otherRes.path) &&
                ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader) &&
                ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
    }

    public int hashCode()
    {
        return this.path.hashCode();
    }
}


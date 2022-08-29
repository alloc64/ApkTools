package com.alloc64.apktools.res;

public class ClassUtils
{
    public static ClassLoader getDefaultClassLoader()
    {
        ClassLoader cl = null;
        try
        {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable var3)
        {
        }
        if (cl == null)
        {
            cl = ClassUtils.class.getClassLoader();
            if (cl == null)
            {
                try
                {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable var2)
                {
                }
            }
        }
        return cl;
    }

    public static String classPackageAsResourcePath(Class<?> clazz)
    {
        if (clazz == null)
        {
            return "";
        }
        else
        {
            String className = clazz.getName();
            int packageEndIndex = className.lastIndexOf(46);
            if (packageEndIndex == -1)
            {
                return "";
            }
            else
            {
                String packageName = className.substring(0, packageEndIndex);
                return packageName.replace('.', '/');
            }
        }
    }
}

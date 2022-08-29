/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public abstract class ExtractableJarExecutable extends ExtractableExecutable
{
    protected Object executeJar(File jarFile, String clazz, String method, String[] args) throws Exception
    {
        if(jarFile == null || !jarFile.exists())
            throw new FileNotFoundException("Jar does not exists: " + jarFile);

        ClassLoader cl = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, ClassLoader.getSystemClassLoader());
        final Class<?> c = cl.loadClass(clazz);

        if(c == null)
            throw new IllegalStateException("Jar class not found: " + clazz + " in " + jarFile);

        final Method main = c.getMethod(method, String[].class);

        if(main == null)
            throw new IllegalStateException("Jar executed method not found: " + method + " in " + jarFile);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                try
                {
                    return main.invoke(c, new Object[]{args});
                }
                catch (Throwable e)
                {
                    throw e;
                }
            }
        });


        Object result;

        try
        {
            result = future.get(); // raises ExecutionException for any uncaught exception in child
        }
        catch(Throwable e)
        {
            throw e;
        }
        executor.shutdown();

        return result;
    }
}

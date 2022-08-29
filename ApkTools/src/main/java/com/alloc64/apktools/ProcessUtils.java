/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProcessUtils
{
    public static int runProcess(String... params) throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder(params);
        builder.redirectErrorStream(true);
        builder.inheritIO();

        Process process = builder.start();

        return process.waitFor();
    }

    public static int runProcessNoLogging(String... params) throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder(params);

        Process p = builder.start();

        // consume streams, without consuming deadlocks occur when system buffer is full (DUMB PIECE OF SHIT)
        IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
        IOUtils.toString(p.getErrorStream(), StandardCharsets.UTF_8);

        return p.waitFor();
    }

    public static String runProcessAndReturnErrors(String... params) throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder(params);

        Process process = builder.start();

        return IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
    }
}

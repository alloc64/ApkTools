/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.apk;

import com.alloc64.apktools.smali.Smali;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class SmaliFile extends FileAware
{
    private String packageName;
    private String fullClassName;
    private String className;

    private SmaliFile superClass;
    private String fullSuperClassName;
    private List<String> interfacesList = new ArrayList<>();

    private int methodCount;

    public SmaliFile(File file)
    {
        super(file);
    }

    public static SmaliFile from(File file, String contents) throws Exception
    {
        FileUtils.write(file, contents, StandardCharsets.UTF_8);

        SmaliFile smaliFile = new SmaliFile(file);
        smaliFile.parse();

        return smaliFile;
    }

    public void parse() throws Exception
    {
        File file = getFile();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                if (StringUtils.isEmpty(line))
                    continue;

                if (line.contains(".method"))
                {
                    this.parseMethod(line);
                }
                else if (line.contains(".class") && className == null)
                {
                    this.parseClass(line);
                }
                else if (line.contains(".super") && fullSuperClassName == null)
                {
                    this.parseSuperClass(line);
                }
                else if (line.contains(".implements"))
                {
                    this.parseInterface(line);
                }
            }
        }

        if (packageName == null) // Package Name can be empty, but not null (empty packages are defined as defpackage)
            throw new IllegalStateException("packageName null in class: " + file);

        if (StringUtils.isEmpty(fullClassName))
            throw new IllegalStateException("fullClassName null in class: " + file);

        if (StringUtils.isEmpty(className))
            throw new IllegalStateException("className null in class: " + file);

        if (StringUtils.isEmpty(fullSuperClassName))
            throw new IllegalStateException("fullSuperClassName null in class: " + file);
    }

    private void parseMethod(String line)
    {
        methodCount++;
    }

    private void parseClass(String line)
    {
        Matcher m = Smali.classPattern.matcher(line);

        if (m.find())
        {
            this.fullClassName = m.group(Smali.FULL_CLASS_NAME);

            String[] splits = fullClassName.split("/");

            if (splits.length > 0)
            {
                List<String> a = new ArrayList<>(Arrays.asList(splits));
                a.remove(a.size() - 1);

                this.packageName = String.join(".", a);
                this.className = splits[splits.length - 1];
            }
            else
            {
                this.packageName = ""; // no package name
            }
        }
    }

    private void parseSuperClass(String line)
    {
        Matcher m = Smali.superClassPattern.matcher(line);

        if (m.find())
            this.fullSuperClassName = m.group(Smali.FULL_CLASS_NAME);
    }

    private void parseInterface(String line)
    {
        Matcher m = Smali.interfacePattern.matcher(line);

        if (m.find())
        {
            String interfaceClass = m.group(Smali.FULL_CLASS_NAME);

            if(!StringUtils.isEmpty(interfaceClass))
                interfacesList.add(interfaceClass);
        }
    }

    public String getPackageName()
    {
        return packageName;
    }

    public int getMethodCount()
    {
        return methodCount;
    }

    /**
     * TODO: this method returns REFERENCED method count, which means, we must parse all invokes
     * TODO: and make cross reference checks, which classes calls methods
     * @return
     */
    public int getReferencedMethodCount()
    {
        int methodCount = getMethodCount();

        SmaliFile smali = superClass;
        while(smali != null)
        {
            methodCount += smali.getMethodCount();
            smali = smali.getSuperClass();
        }

        return methodCount;
    }

    public String getClassName()
    {
        return className;
    }

    public String getFullClassName()
    {
        return fullClassName;
    }

    public SmaliFile getSuperClass()
    {
        return superClass;
    }

    public void setSuperClass(SmaliFile superClass)
    {
        this.superClass = superClass;
    }

    public String getFullSuperClassName()
    {
        return fullSuperClassName;
    }

    public String getLowestSuperClass()
    {
        if(superClass == null)
            return null;

        SmaliFile lastSmaliFile = null;
        SmaliFile smali = superClass;
        while(smali != null)
        {
            lastSmaliFile = smali;
            smali = smali.getSuperClass();
        }

        return lastSmaliFile.getFullSuperClassName();
    }

    public List<String> getInterfacesList()
    {
        return interfacesList;
    }
}

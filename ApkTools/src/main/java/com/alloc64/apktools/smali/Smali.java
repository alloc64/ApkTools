/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.smali;

import java.util.regex.Pattern;

@Deprecated
public class Smali
{
    public static final String FULL_CLASS_NAME = "fullClassName";
    public static final String LOCAL_COUNT = "localCount";
    public static final String REGISTER_COUNT = "registerCount";
    public static final String TYPE = "type";
    public static final String GENERIC_TYPE = "genericType";
    public static final String FIELD_NAME = "fieldName";
    public static final String STRING_NAME = "stringName";
    public static final String STRING_VALUE = "stringValue";
    public static final String REGISTER = "register";
    public static final String REGISTER0 = "register0";
    public static final String REGISTER1 = "register1";
    public static final String ACCESS_MODIFIER = "accessModifier";
    public static final String METHOD_NAME = "methodName";
    public static final String METHOD_SIGNATURE = "methodSignature";
    public static final String PARAMTERS = "parameters";
    public static final String RETURN_TYPE = "returnType";
    public static final String FILE_NAME = "fileName";
    public static final String VARIABLE_NAME = "variableName";
    public static final String COMMENT = "comment";
    public static final String TRY_START_LABEL = "tryStartLabel";
    public static final String TRY_END_LABEL = "tryEndLabel";
    public static final String CATCH_LABEL = "catchLabel";
    public static final String PACKED_SWITCH_COUNT = "packedSwitchCount";
    public static final String ARRAY_BYTE_SIZE = "arraySize";

    public static final Pattern classPattern = Pattern.compile(".class\\s?(?<" + ACCESS_MODIFIER + ">...+)?\\sL(?<" + FULL_CLASS_NAME + ">\\S+?);");

    public static final Pattern superClassPattern = Pattern.compile(".super\\sL(?<" + FULL_CLASS_NAME + ">\\S+?);");
    public static final Pattern interfacePattern = Pattern.compile(".implements\\sL(?<" + FULL_CLASS_NAME + ">\\S+?);");

    public static final Pattern localsPattern = Pattern.compile("\\s+\\.locals\\s(?<" + LOCAL_COUNT + ">\\d+)");
    public static final Pattern registersPattern = Pattern.compile("\\s+\\.registers\\s(?<" + REGISTER_COUNT + ">\\d+)");

    public static final Pattern methodPattern = Pattern.compile("\\.method\\s(?<" + ACCESS_MODIFIER + ">...+)\\s(?<" + METHOD_NAME + ">\\S+)\\((?<" + PARAMTERS + ">\\S+)?\\)(?<" + RETURN_TYPE + ">\\S+)");
    public static final Pattern fieldPattern = Pattern.compile(".field\\s(?<" + ACCESS_MODIFIER + ">...+)\\s(?<" + FIELD_NAME + ">[^\\s]+):(?<" + TYPE + ">[^\\s]+)");

    public static final Pattern staticStringPattern = Pattern.compile(".field.+?static.+?(?<" + STRING_NAME + ">\\S+?):Ljava/lang/String;\\s=\\s\"(?<" + STRING_VALUE + ">.+)\"");
    public static final Pattern constStringPattern = Pattern.compile("\\s+const-string(/jumbo)?\\s(?<" + REGISTER + ">[vp0-9]+),\\s\"(?<" + STRING_VALUE + ">.+)\"");

    public static final Pattern instanceGetPattern = Pattern.compile("(iget-?object)\\s(?<" + REGISTER0 + ">\\S+),\\s(?<" + REGISTER1 + ">\\S+),\\s(?<" + FULL_CLASS_NAME + ">...+);->(?<" + FIELD_NAME + ">...+):(?<" + TYPE + ">...+);");
    public static final Pattern sourcePattern = Pattern.compile(".source \"(?<" + FILE_NAME + ">.+)\"");

    public static final Pattern paramPattern = Pattern.compile(".param\\s(?<" + REGISTER + ">.+),\\s\"(?<" + VARIABLE_NAME + ">.+)\"(?<" + COMMENT + ">.+)");

    public static final Pattern localPattern = Pattern.compile(".local\\s(?<" + REGISTER + ">.+),\\s\"(?<" + VARIABLE_NAME + ">.+)\":(?<" + TYPE + ">[^,]+),?(\\s\"(?<" + GENERIC_TYPE + ">...+)\")?");
    public static final Pattern endLocalPattern = Pattern.compile(".end\\slocal\\s(?<" + REGISTER + ">[^\\s]+)(?<" + COMMENT + ">.+)");
    public static final Pattern restartLocalPattern = Pattern.compile(".restart\\slocal\\s(?<" + REGISTER + ">[^\\s]+)(?<" + COMMENT + ">.+)");

    public static final Pattern catchPattern = Pattern.compile(".catch\\s(?<" + TYPE + ">.+)\\s\\{:(?<" + TRY_START_LABEL + ">[^\\s]+)\\s\\.\\.\\s:(?<" + TRY_END_LABEL + ">[^\\s]+)\\}\\s:(?<" + CATCH_LABEL + ">[^\\s]+)");
    public static final Pattern catchAllPattern = Pattern.compile(".catchall\\s\\{:(?<" + TRY_START_LABEL + ">[^\\s]+)\\s\\.\\.\\s:(?<" + TRY_END_LABEL + ">[^\\s]+)\\}\\s:(?<" + CATCH_LABEL + ">[^\\s]+)");

    public static final Pattern packedSwitchPattern = Pattern.compile(".packed-switch\\s(?<" + PACKED_SWITCH_COUNT + ">\\S+)");
    public static final Pattern arrayDataPattern = Pattern.compile(".array-data\\s(?<" + ARRAY_BYTE_SIZE + ">\\d+)");
}

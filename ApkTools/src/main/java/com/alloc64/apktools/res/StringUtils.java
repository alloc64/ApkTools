package com.alloc64.apktools.res;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringUtils
{
    public static boolean hasLength(String str)
    {
        return str != null && !str.isEmpty();
    }

    public static boolean hasText(CharSequence str)
    {
        return str != null && str.length() > 0 && containsText(str);
    }

    public static boolean hasText(String str)
    {
        return str != null && !str.isEmpty() && containsText(str);
    }

    private static boolean containsText(CharSequence str)
    {
        int strLen = str.length();
        for (int i = 0; i < strLen; ++i)
        {
            if (!Character.isWhitespace(str.charAt(i)))
            {
                return true;
            }
        }
        return false;
    }

    public static String replace(String inString, String oldPattern, String newPattern)
    {
        if (hasLength(inString) && hasLength(oldPattern) && newPattern != null)
        {
            int index = inString.indexOf(oldPattern);
            if (index == -1)
            {
                return inString;
            }
            else
            {
                int capacity = inString.length();
                if (newPattern.length() > oldPattern.length())
                {
                    capacity += 16;
                }
                StringBuilder sb = new StringBuilder(capacity);
                int pos = 0;
                for (int patLen = oldPattern.length(); index >= 0; index = inString.indexOf(oldPattern, pos))
                {
                    sb.append(inString.substring(pos, index));
                    sb.append(newPattern);
                    pos = index + patLen;
                }
                sb.append(inString.substring(pos));
                return sb.toString();
            }
        }
        else
        {
            return inString;
        }
    }

    public static String cleanPath(String path)
    {
        if (!hasLength(path))
        {
            return path;
        }
        else
        {
            String pathToUse = replace(path, "\\", "/");
            int prefixIndex = pathToUse.indexOf(58);
            String prefix = "";
            if (prefixIndex != -1)
            {
                prefix = pathToUse.substring(0, prefixIndex + 1);
                if (prefix.contains("/"))
                {
                    prefix = "";
                }
                else
                {
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }
            if (pathToUse.startsWith("/"))
            {
                prefix = prefix + "/";
                pathToUse = pathToUse.substring(1);
            }
            String[] pathArray = delimitedListToStringArray(pathToUse, "/");
            LinkedList<String> pathElements = new LinkedList<>();
            int tops = 0;
            int i;
            for (i = pathArray.length - 1; i >= 0; --i)
            {
                String element = pathArray[i];
                if (!".".equals(element))
                {
                    if ("..".equals(element))
                    {
                        ++tops;
                    }
                    else if (tops > 0)
                    {
                        --tops;
                    }
                    else
                    {
                        pathElements.add(0, element);
                    }
                }
            }
            for (i = 0; i < tops; ++i)
            {
                pathElements.add(0, "..");
            }
            if (pathElements.size() == 1 && "".equals(pathElements.getLast()) && !prefix.endsWith("/"))
            {
                pathElements.add(0, ".");
            }
            return prefix + collectionToDelimitedString(pathElements, "/");
        }
    }

    public static String[] delimitedListToStringArray(String str, String delimiter)
    {
        return delimitedListToStringArray(str, delimiter, (String) null);
    }

    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete)
    {
        if (str == null)
        {
            return new String[0];
        }
        else if (delimiter == null)
        {
            return new String[]{str};
        }
        else
        {
            List<String> result = new ArrayList<>();
            int pos;
            if (delimiter.isEmpty())
            {
                for (pos = 0; pos < str.length(); ++pos)
                {
                    result.add(deleteAny(str.substring(pos, pos + 1), charsToDelete));
                }
            }
            else
            {
                int delPos;
                for (pos = 0; (delPos = str.indexOf(delimiter, pos)) != -1; pos = delPos + delimiter.length())
                {
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }
                if (str.length() > 0 && pos <= str.length())
                {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }
            return toStringArray(result);
        }
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim)
    {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix)
    {
        if (CollectionUtils.isEmpty(coll))
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            Iterator it = coll.iterator();
            while (it.hasNext())
            {
                sb.append(prefix).append(it.next()).append(suffix);
                if (it.hasNext())
                {
                    sb.append(delim);
                }
            }
            return sb.toString();
        }
    }

    public static String deleteAny(String inString, String charsToDelete)
    {
        if (hasLength(inString) && hasLength(charsToDelete))
        {
            StringBuilder sb = new StringBuilder(inString.length());
            for (int i = 0; i < inString.length(); ++i)
            {
                char c = inString.charAt(i);
                if (charsToDelete.indexOf(c) == -1)
                {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        else
        {
            return inString;
        }
    }

    public static String[] toStringArray(Collection<String> collection)
    {
        return (String[]) collection.toArray(new String[0]);
    }

    public static String applyRelativePath(String path, String relativePath)
    {
        int separatorIndex = path.lastIndexOf("/");
        if (separatorIndex != -1)
        {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith("/"))
            {
                newPath = newPath + "/";
            }
            return newPath + relativePath;
        }
        else
        {
            return relativePath;
        }
    }

    public static String getFilename(String path)
    {
        if (path == null)
        {
            return null;
        }
        else
        {
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
        }
    }
}

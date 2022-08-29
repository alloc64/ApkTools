
package com.alloc64.apktools.res;

import java.util.function.Supplier;

public abstract class Assert
{
    public Assert()
    {
    }

    public static void state(boolean expression, String message)
    {
        if (!expression)
        {
            throw new IllegalStateException(message);
        }
    }

    public static void state(boolean expression, Supplier<String> messageSupplier)
    {
        if (!expression)
        {
            throw new IllegalStateException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void state(boolean expression)
    {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    public static void isTrue(boolean expression, String message)
    {
        if (!expression)
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier)
    {
        if (!expression)
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void isTrue(boolean expression)
    {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    public static void isNull(Object object, String message)
    {
        if (object != null)
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isNull(Object object, Supplier<String> messageSupplier)
    {
        if (object != null)
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void isNull(Object object)
    {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    public static void notNull(Object object, String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, Supplier<String> messageSupplier)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void notNull(Object object)
    {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    public static void hasLength(String text, String message)
    {
        if (!StringUtils.hasLength(text))
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasLength(String text, Supplier<String> messageSupplier)
    {
        if (!StringUtils.hasLength(text))
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void hasLength(String text)
    {
        hasLength(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    public static void hasText(String text, String message)
    {
        if (!StringUtils.hasText(text))
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void hasText(String text, Supplier<String> messageSupplier)
    {
        if (!StringUtils.hasText(text))
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    @Deprecated
    public static void hasText(String text)
    {
        hasText(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    public static void doesNotContain(String textToSearch, String substring, String message)
    {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring))
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void doesNotContain(String textToSearch, String substring, Supplier<String> messageSupplier)
    {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring))
        {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public static void noNullElements(Object[] array, String message)
    {
        if (array != null)
        {
            for (Object element : array)
            {
                if (element == null)
                {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    public static void noNullElements(Object[] array, Supplier<String> messageSupplier)
    {
        if (array != null)
        {
            for (Object element : array)
            {
                if (element == null)
                {
                    throw new IllegalArgumentException(nullSafeGet(messageSupplier));
                }
            }
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj, String message)
    {
        notNull(type, (String) "Type to check against must not be null");
        if (!type.isInstance(obj))
        {
            instanceCheckFailed(type, obj, message);
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj, Supplier<String> messageSupplier)
    {
        notNull(type, (String) "Type to check against must not be null");
        if (!type.isInstance(obj))
        {
            instanceCheckFailed(type, obj, nullSafeGet(messageSupplier));
        }
    }

    public static void isInstanceOf(Class<?> type, Object obj)
    {
        isInstanceOf(type, obj, "");
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, String message)
    {
        notNull(superType, (String) "Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType))
        {
            assignableCheckFailed(superType, subType, message);
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType, Supplier<String> messageSupplier)
    {
        notNull(superType, (String) "Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType))
        {
            assignableCheckFailed(superType, subType, nullSafeGet(messageSupplier));
        }
    }

    public static void isAssignable(Class<?> superType, Class<?> subType)
    {
        isAssignable(superType, subType, "");
    }

    private static void instanceCheckFailed(Class<?> type, Object obj, String msg)
    {
        String className = obj != null ? obj.getClass().getName() : "null";
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg))
        {
            if (endsWithSeparator(msg))
            {
                result = msg + " ";
            }
            else
            {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }
        if (defaultMessage)
        {
            result = result + "Object of class [" + className + "] must be an instance of " + type;
        }
        throw new IllegalArgumentException(result);
    }

    private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg)
    {
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg))
        {
            if (endsWithSeparator(msg))
            {
                result = msg + " ";
            }
            else
            {
                result = messageWithTypeName(msg, subType);
                defaultMessage = false;
            }
        }
        if (defaultMessage)
        {
            result = result + subType + " is not assignable to " + superType;
        }
        throw new IllegalArgumentException(result);
    }

    private static boolean endsWithSeparator(String msg)
    {
        return msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith(".");
    }

    private static String messageWithTypeName(String msg, Object typeName)
    {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

    private static String nullSafeGet(Supplier<String> messageSupplier)
    {
        return messageSupplier != null ? (String) messageSupplier.get() : null;
    }
}

package com.alloc64.apktools.res;

import java.util.Arrays;

public class ObjectUtils
{
    public static boolean nullSafeEquals(Object o1, Object o2)
    {
        if (o1 == o2)
        {
            return true;
        }
        else if (o1 != null && o2 != null)
        {
            if (o1.equals(o2))
            {
                return true;
            }
            else
            {
                return (o1.getClass().isArray() && o2.getClass().isArray()) && arrayEquals(o1, o2);
            }
        }
        else
        {
            return false;
        }
    }

    private static boolean arrayEquals(Object o1, Object o2)
    {
        if (o1 instanceof Object[] && o2 instanceof Object[])
        {
            return Arrays.equals((Object[]) ((Object[]) o1), (Object[]) ((Object[]) o2));
        }
        else if (o1 instanceof boolean[] && o2 instanceof boolean[])
        {
            return Arrays.equals((boolean[]) ((boolean[]) o1), (boolean[]) ((boolean[]) o2));
        }
        else if (o1 instanceof byte[] && o2 instanceof byte[])
        {
            return Arrays.equals((byte[]) ((byte[]) o1), (byte[]) ((byte[]) o2));
        }
        else if (o1 instanceof char[] && o2 instanceof char[])
        {
            return Arrays.equals((char[]) ((char[]) o1), (char[]) ((char[]) o2));
        }
        else if (o1 instanceof double[] && o2 instanceof double[])
        {
            return Arrays.equals((double[]) ((double[]) o1), (double[]) ((double[]) o2));
        }
        else if (o1 instanceof float[] && o2 instanceof float[])
        {
            return Arrays.equals((float[]) ((float[]) o1), (float[]) ((float[]) o2));
        }
        else if (o1 instanceof int[] && o2 instanceof int[])
        {
            return Arrays.equals((int[]) ((int[]) o1), (int[]) ((int[]) o2));
        }
        else if (o1 instanceof long[] && o2 instanceof long[])
        {
            return Arrays.equals((long[]) ((long[]) o1), (long[]) ((long[]) o2));
        }
        else
        {
            return (o1 instanceof short[] && o2 instanceof short[]) && Arrays.equals((short[]) ((short[]) o1), (short[]) ((short[]) o2));
        }
    }

    public static int nullSafeHashCode(Object obj)
    {
        if (obj == null)
        {
            return 0;
        }
        else
        {
            if (obj.getClass().isArray())
            {
                if (obj instanceof Object[])
                {
                    return nullSafeHashCode((Object[]) ((Object[]) obj));
                }
                if (obj instanceof boolean[])
                {
                    return nullSafeHashCode((boolean[]) ((boolean[]) obj));
                }
                if (obj instanceof byte[])
                {
                    return nullSafeHashCode((byte[]) ((byte[]) obj));
                }
                if (obj instanceof char[])
                {
                    return nullSafeHashCode((char[]) ((char[]) obj));
                }
                if (obj instanceof double[])
                {
                    return nullSafeHashCode((double[]) ((double[]) obj));
                }
                if (obj instanceof float[])
                {
                    return nullSafeHashCode((float[]) ((float[]) obj));
                }
                if (obj instanceof int[])
                {
                    return nullSafeHashCode((int[]) ((int[]) obj));
                }
                if (obj instanceof long[])
                {
                    return nullSafeHashCode((long[]) ((long[]) obj));
                }
                if (obj instanceof short[])
                {
                    return nullSafeHashCode((short[]) ((short[]) obj));
                }
            }
            return obj.hashCode();
        }
    }

    public static int nullSafeHashCode(Object[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            Object[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                Object element = var2[var4];
                hash = 31 * hash + nullSafeHashCode(element);
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(boolean[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            boolean[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                boolean element = var2[var4];
                hash = 31 * hash + Boolean.hashCode(element);
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(byte[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            byte[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                byte element = var2[var4];
                hash = 31 * hash + element;
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(char[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            char[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                char element = var2[var4];
                hash = 31 * hash + element;
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(double[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            double[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                double element = var2[var4];
                hash = 31 * hash + Double.hashCode(element);
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(float[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            float[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                float element = var2[var4];
                hash = 31 * hash + Float.hashCode(element);
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(int[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            int[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                int element = var2[var4];
                hash = 31 * hash + element;
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(long[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            long[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                long element = var2[var4];
                hash = 31 * hash + Long.hashCode(element);
            }
            return hash;
        }
    }

    public static int nullSafeHashCode(short[] array)
    {
        if (array == null)
        {
            return 0;
        }
        else
        {
            int hash = 7;
            short[] var2 = array;
            int var3 = array.length;
            for (int var4 = 0; var4 < var3; ++var4)
            {
                short element = var2[var4];
                hash = 31 * hash + element;
            }
            return hash;
        }
    }
}

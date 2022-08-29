/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.apktools.smali;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DexUtils
{
	public static final String VOID = "V";

	public static final String MOVE_RESULT = "move-result";

	private static final Pattern registerRangePattern = Pattern.compile("\\{([a-z-0-9]+) .. ([a-z-0-9]+)\\}");

	public static List<String> getRegisters(String registers)
	{
		List<String> list = new ArrayList<>();

		if(registers != null && !registers.equals("{}"))
		{
			List<String> registerRange = getRegisterRange(registers);

			if(registerRange == null)
			{
				registers = registers
						.replace("{", "")
						.replace("}", "");

				list.addAll(Arrays.asList(registers.split(",")));
			}
			else
			{
				list.addAll(registerRange);
			}
		}

		return list;
	}

	private static List<String> getRegisterRange(String registerRange)
	{
		Matcher m = registerRangePattern.matcher(registerRange);

		if(m.find())
		{
			try
			{
				List<String> list = new ArrayList<>();

				int v0 = Integer.parseInt(m.group(1).substring(1));
				int vX = Integer.parseInt(m.group(2).substring(1));

				for(int i = v0; i <= vX; i++)
					list.add(String.format("v%s", i));

				return list;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	// Lnet/twentyonevpn/protoconfid/sdk/rk$a;Lcom/facebook/internal/b;Ljava/lang/String;ZLandroid/content/Context;
	public static List<String> getParameters(String parameters)
	{
		List<String> list = new ArrayList<>();

		if(parameters != null)
		{
			boolean append = false;

			StringBuilder sb = new StringBuilder();

			for(int i = 0; i < parameters.length(); i++)
			{
				char c = parameters.charAt(i);

				if(isFQN(c))
				{
					if(!append)
					{
						sb = new StringBuilder();
						append = true;
					}
				}
				else if(isDescriptor(c))
				{
					if(!append)
					{
						sb = new StringBuilder();
						append = true;
					}
				}
				else if(isTerminator(c))
				{
					append = false;
					list.add(sb.toString());

					sb = new StringBuilder();
				}
				else if(isPrimitiveValue(c))
				{
					if(!append)
					{
						list.add("" + c);
					}
				}

				if(append)
					sb.append(c);
			}
		}

		return list;
	}

	private static boolean isDescriptor(char c) // [descriptor	array of descriptor, usable recursively for arrays-of-arrays, though it is invalid to have more than 255 dimensions.
	{
		return c == '[';
	}

	private static boolean isFQN(char c)  // Lfully/qualified/Name;	the class fully.qualified.Name
	{
		return c == 'L';
	}

	public static boolean isTerminator(char c)
	{
		return c == ';';
	}

	public static boolean isPrimitiveValue(char c)
	{
		return c == 'V' || c == 'Z' || c == 'B' || c == 'S' || c == 'C' || c == 'I' || c == 'J' || c == 'F' || c == 'D';
	}

	public static String serializeParameters(List<String> parameters)
	{
		StringBuilder mod = new StringBuilder();

		for(String param : parameters)
		{
			if(param.length() == 1 && isPrimitiveValue(param.charAt(0)))
			{
				mod.append(param);
			}
			else
			{
				mod.append(param);
				mod.append(";");
			}
		}

		return mod.toString();
	}
}

package com.SkippyoGE;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SkippyoGE
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SkippyoGEPlugin.class);
		RuneLite.main(args);
	}
}
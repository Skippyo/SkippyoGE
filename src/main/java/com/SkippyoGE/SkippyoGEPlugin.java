package com.SkippyoGE;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "SkippyoGE"
)
public class SkippyoGEPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SkippyoGEConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception
    {
        log.debug("SkippyoGE started!");

        // This creates an empty panel for now.
        // You can add UI components to it later.
        final SkippyoGEPanel panel = new SkippyoGEPanel();

        // This loads your icon from the resources folder.
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "SkippyoGE_logo.PNG");

        navButton = NavigationButton.builder()
                .tooltip("Skippyo GE Tracker")
                .icon(icon)
                .priority(5) // Lower number is higher on the sidebar
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.debug("SkippyoGE stopped!");
        clientToolbar.removeNavigation(navButton);
    }

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "SkippyoGE says " + config.greeting(), null);
		}
	}

	@Provides
    SkippyoGEConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkippyoGEConfig.class);
	}
}

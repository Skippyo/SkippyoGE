package com.SkippyoGE;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;


import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.GrandExchangeOfferState;

import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;



@Slf4j
@PluginDescriptor(
        name = "SkippyoGE"
)
public class SkippyoGEPlugin extends Plugin {


    //Item consumed
    private void consumeItems (int itemId, int quantityConsumed){
        //Temp list of consumed items
        List<AcquiredItemStack> consumedStacks = new ArrayList<>();

        // We consume in order of priority. First, GE_BOUGHT items.
        int remainingToConsume = consumeFromCategory(itemId, quantityConsumed, AcquisitionMethod.GE_BOUGHT, consumedStacks);

        // If we still need to consume more, use CRAFTED items
        if(remainingToConsume > 0){
            remainingToConsume = consumeFromCategory(itemId, remainingToConsume, AcquisitionMethod.CRAFTED, consumedStacks);
        }

        // Finally, use GATHERED_LOOTED items
        if(remainingToConsume > 0){
            consumeFromCategory(itemId, remainingToConsume, AcquisitionMethod.GATHERED_LOOTED, consumedStacks);
        }

        // Now, report what was consumed
        for(AcquiredItemStack consumedStack : consumedStacks){
            String itemName = itemManager.getItemComposition(consumedStack.getItemId()).getName();
            long totalValue = (long) consumedStack.getPricePerItem() * consumedStack.getQuantity();

            String message = String.format("Consumed %d x %s (Value: %d gp)",
                    consumedStack.getQuantity(),
                    itemName,
                    totalValue);
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }

    }

    // This is a helper method to handle the consumption for a specific category
    private int consumeFromCategory(int itemId, int quantityToConsume, AcquisitionMethod method, List<AcquiredItemStack> consumedStacks){
        Iterator<AcquiredItemStack> iterator = virtualInventory.iterator();

        while(iterator.hasNext() && quantityToConsume > 0){
            AcquiredItemStack stack = iterator.next();

            // Check if this stack is the item we are looking for and from the correct category
            if(stack.getItemId() == itemId && stack.getAcquisitionMethod() ==method){
                int quantityInStack = stack.getQuantity();
                int amountToTake = Math.min(quantityToConsume, quantityInStack);

                // Add a new stack to our report list
                consumedStacks.add(new AcquiredItemStack(itemId, amountToTake, stack.getPricePerItem(), method));

                // Reduce the quantity in the original stack
                stack.setQuantity(quantityToConsume - amountToTake);
                quantityToConsume -= amountToTake;

                // If the stack is now empty, remove it completely
                if(stack.getQuantity() <= 0){
                    iterator.remove();
                }
            }
        }
        return quantityToConsume; //Return how many are still left to be consumed
    }




    //Item Bought From GE
    @Subscribe
    public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
        var offer = event.getOffer();

        if (offer.getState() == GrandExchangeOfferState.BOUGHT){
            int itemId = offer.getItemId();
            int price = offer.getPrice();
            int quantity = offer.getTotalQuantity();

            log.debug("Bought {}x item {} for {} gp each", quantity, itemId, price);

            // Add to virtual inventory
            virtualInventory.add(new AcquiredItemStack(itemId, quantity, price, AcquisitionMethod.GE_BOUGHT));
        }
    }



    //Adding Custom item stacks
    private final List<AcquiredItemStack> virtualInventory = new ArrayList<>();
    private final List<AcquiredItemStack> virtualBank = new ArrayList<>();
    private final List<AcquiredItemStack> virtualDeathpile = new ArrayList<>();


    @Inject
    private ItemManager itemManager;

    @Inject
	private Client client;

	@Inject
	private SkippyoGEConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        log.debug("SkippyoGE started!");

        // This creates an empty panel for now.
        // You can add UI components to it later.
        final SkippyoGEPanel panel = new SkippyoGEPanel();

        // This loads your icon from the resources folder.
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "SkippyoGE_logo.PNG");

        navButton = NavigationButton.builder()
                .tooltip("Skippyo GE Tracker")
                .icon(icon)
                .priority(2) // Lower number is higher on the sidebar
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("SkippyoGE stopped!");
        clientToolbar.removeNavigation(navButton);
    }

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "SkippyoGE says " + config.greeting(), null);
		}
	}

	@Provides
    SkippyoGEConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(SkippyoGEConfig.class);
	}

    // An array to store the last known inventory state
    private Item[] lastInventoryState = new Item[28];

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        // We only care about the player's main inventory
        if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
            return;
        }

        Item[] currentItems = event.getItemContainer().getItems();

        // Logic to detect consumption (quantity decrease)
        // NOTE: A full implementation also needs to detect additions for looting/gathering
        for (int i = 0; i < 28; i++) {
            Item oldItem = (lastInventoryState.length > i && lastInventoryState[i] != null) ? lastInventoryState[i] : new Item(-1, 0);
            Item newItem = (currentItems.length > i && currentItems[i] != null) ? currentItems[i] : new Item(-1, 0);

            // If an item's quantity has gone down...
            if (oldItem.getId() != -1 && oldItem.getId() == newItem.getId() && oldItem.getQuantity() > newItem.getQuantity()) {
                int quantityConsumed = oldItem.getQuantity() - newItem.getQuantity();
                log.debug("Detected consumption of {}x item {}", quantityConsumed, oldItem.getId());
                consumeItems(oldItem.getId(), quantityConsumed);
            }
        }

        // Update the last known state for the next event
        lastInventoryState = cloneInventory(currentItems);
    }

    // Helper method to safely clone the inventory array
    private Item[] cloneInventory(Item[] items) {
        Item[] clone = new Item[28];
        for (int i = 0; i < items.length; i++) {
            clone[i] = new Item(items[i].getId(), items[i].getQuantity());
        }
        return clone;
    }

}

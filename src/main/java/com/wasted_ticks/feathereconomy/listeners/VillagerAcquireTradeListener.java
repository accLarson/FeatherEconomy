package com.wasted_ticks.feathereconomy.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;

public class VillagerAcquireTradeListener implements Listener {

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        if (event.getRecipe().getResult().isSimilar(new ItemStack(Material.LAPIS_LAZULI))) {
            event.setCancelled(true);
        }
    }
}

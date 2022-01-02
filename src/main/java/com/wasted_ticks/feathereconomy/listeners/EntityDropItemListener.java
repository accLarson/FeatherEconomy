package com.wasted_ticks.feathereconomy.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class EntityDropItemListener implements Listener {

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent event) {
        if (event.getItemDrop().getItemStack().isSimilar(new ItemStack(Material.LAPIS_LAZULI))) {
            event.setCancelled(true);
        }
    }
}

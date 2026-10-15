package com.ervv.hopper16;

import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/** Makes block hoppers and hopper minecarts transfer up to 16 items per transfer operation. */
public final class Hopper16 extends JavaPlugin implements Listener {
    private static final int TRANSFER_AMOUNT = 16;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Hopper16 enabled: block hoppers and hopper minecarts transfer up to 16 items per move.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        // Affect transfers initiated by either a regular block hopper or a hopper minecart.
        // Chest minecarts are handled when a hopper transfers items into/out of them;
        // a chest minecart does not move items by itself.
        Object holder = event.getInitiator().getHolder();
        if (!(holder instanceof Hopper) && !(holder instanceof HopperMinecart)) {
            return;
        }

        ItemStack moving = event.getItem();
        if (moving == null || moving.getAmount() <= 1) {
            return;
        }

        // The event item represents the stack to transfer; destination capacity and
        // available source items are still handled by the server's inventory logic.
        ItemStack largerTransfer = moving.clone();
        largerTransfer.setAmount(Math.min(TRANSFER_AMOUNT, moving.getAmount()));
        event.setItem(largerTransfer);
    }
}

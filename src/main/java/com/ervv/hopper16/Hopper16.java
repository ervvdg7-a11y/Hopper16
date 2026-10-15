package com.ervv.hopper16;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/** Transfers up to 16 items for hopper-driven inventory moves, including hopper minecarts. */
public final class Hopper16 extends JavaPlugin implements Listener {
    private static final int TRANSFER_AMOUNT = 16;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Hopper16 enabled; hopper-driven transfers target up to 16 items.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        // Only alter moves initiated by a hopper block or hopper minecart.
        // A chest minecart is a container, not a transfer initiator.
        Object holder = event.getInitiator().getHolder();
        if (!(holder instanceof org.bukkit.block.Hopper)
                && !(holder instanceof org.bukkit.entity.minecart.HopperMinecart)) {
            return;
        }

        ItemStack eventItem = event.getItem();
        if (eventItem == null || eventItem.getType().isAir()) return;

        Inventory source = event.getSource();
        Inventory destination = event.getDestination();
        if (source == destination) return;

        // Cancel vanilla's one-item move and perform a bounded bulk move ourselves.
        event.setCancelled(true);

        int sourceSlot = findSourceSlot(source, eventItem);
        if (sourceSlot < 0) return;
        ItemStack sourceStack = source.getItem(sourceSlot);
        if (sourceStack == null || sourceStack.getType().isAir()) return;

        int offeredAmount = Math.min(TRANSFER_AMOUNT, sourceStack.getAmount());
        ItemStack offered = sourceStack.clone();
        offered.setAmount(offeredAmount);

        Map<Integer, ItemStack> leftovers = destination.addItem(offered);
        int leftoverAmount = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        int moved = offeredAmount - leftoverAmount;
        if (moved <= 0) return;

        // Remove only the amount the destination actually accepted.
        ItemStack current = source.getItem(sourceSlot);
        if (current == null || current.getType().isAir() || !current.isSimilar(sourceStack)) {
            // Defensive rollback if another plugin changed the source during this event.
            ItemStack rollback = sourceStack.clone();
            rollback.setAmount(moved);
            destination.removeItem(rollback);
            return;
        }
        int remaining = current.getAmount() - moved;
        if (remaining <= 0) {
            source.setItem(sourceSlot, null);
        } else {
            current.setAmount(remaining);
            source.setItem(sourceSlot, current);
        }
    }

    private int findSourceSlot(Inventory source, ItemStack item) {
        for (int slot = 0; slot < source.getSize(); slot++) {
            ItemStack candidate = source.getItem(slot);
            if (candidate != null && !candidate.getType().isAir() && candidate.isSimilar(item)) {
                return slot;
            }
        }
        return -1;
    }
}

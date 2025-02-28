package org.wayggstar.customShopMaker.GUIS

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.wayggstar.customShopMaker.CustomShopMaker
import org.wayggstar.customShopMaker.Shop.ShopManager
import org.wayggstar.customShopMaker.GUIS.ItemAddGUI
import kotlin.collections.mutableListOf as mutableListOf

class ShopGUI(private val shopManager: ShopManager): Listener {

    private val guiTitle = "§6Shop Modify"
    private val saveButton = ItemStack(Material.LIME_WOOL).apply {
        itemMeta = itemMeta?.apply { setDisplayName("§aSave") }
    }
    private val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta?.apply { setDisplayName(" ") }
    }

    fun openShopEditor(player: Player, shopName: String, size: Int = 54) {
        val holder = ShopGUIHolder(shopName)
        val inventory = Bukkit.createInventory(holder, size, guiTitle)
        holder.setInventory(inventory)

        val shop = shopManager.getShop(shopName)
        shop?.items?.toList()?.forEachIndexed { index, item ->
            if (index < size - 9) {
                val itemWithPrice = item.clone()
                var meta = itemWithPrice.itemMeta ?: Bukkit.getItemFactory().getItemMeta(item.type) ?: return@forEachIndexed

                val lore = meta.lore?.toMutableList() ?: mutableListOf()
                lore.removeIf { it.contains("Price:") }
                lore.add("${ChatColor.GREEN}Price: ${ChatColor.YELLOW}${item.itemMeta.persistentDataContainer.get(NamespacedKey(CustomShopMaker.instance, "price"),
                    PersistentDataType.INTEGER )}")

                meta.lore = lore
                itemWithPrice.itemMeta = meta
                inventory.setItem(index, itemWithPrice)
            }
        }

        for (i in 0 until size) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane)
            }
        }
        inventory.setItem(size - 5, saveButton)

        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val holder = event.inventory.holder as? ShopGUIHolder ?: return
        val shopName = holder.name
        val clickedItem = event.currentItem
        val itemAddGUI = ItemAddGUI(shopManager)

        event.isCancelled = true

        if (clickedItem == null || clickedItem.type == Material.AIR) {
            itemAddGUI.openItemAddGUI(player, shopName)
        } else if (!clickedItem.isSimilar(saveButton) && !clickedItem.isSimilar(glassPane)) {
            itemAddGUI.openPriceAnvil(player, clickedItem)
        }
    }
}
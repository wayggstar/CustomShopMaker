package org.wayggstar.customShopMaker.GUIS

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.wayggstar.customShopMaker.Shop.ShopManager

class ItemAddGUI(private val shopManager: ShopManager) : Listener{
    private val guiTitle = "${ChatColor.AQUA}ItemAdd"
    private val saveButton = ItemStack(Material.LIME_WOOL).apply {
        itemMeta = itemMeta?.apply { setDisplayName("${ChatColor.GREEN}Save price") }
    }
    private val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta?.apply { setDisplayName(" ") }
    }

    fun openItemAddGUI(player: Player, name: String){
        val holder = ItemAddHolder(name)
        val inventory = Bukkit.createInventory(holder, 9, guiTitle)
        holder.inventory = inventory

        for (i in 0 until 9){
            if (i != 4) inventory.setItem(i, glassPane)
        }

        inventory.setItem(8, saveButton)
        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent){
        val player = event.whoClicked as? Player ?: return
        val holder = event.inventory.holder as? ItemAddHolder
        event.isCancelled = true

        if (event.slot == 4) {
            event.isCancelled = false
        } else if (event.currentItem != null && event.currentItem!!.isSimilar(saveButton)) {
            val item = event.inventory.getItem(4)
            if (item == null || item.type == Material.AIR) {
                player.sendMessage("${ChatColor.RED}Place item in middle slot of inventory!")
                return
            }
            openPriceAnvil(player ,item)
        }
    }

    fun openPriceAnvil(player: Player, item: ItemStack){
        val anvil = Bukkit.createInventory(null, InventoryType.ANVIL, "§6Price Setting")
        val itemWithMeta = item.clone()
        val meta: ItemMeta? = itemWithMeta.itemMeta
        meta?.setDisplayName("${ChatColor.YELLOW}Type the price of it")
        itemWithMeta.itemMeta = meta

        anvil.setItem(0, itemWithMeta)
        player.openInventory(anvil)
    }

    @EventHandler
    fun onAnvilRename(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val view: InventoryView = event.view

        if (view.title != "${ChatColor.GOLD}Price modify") return

        if (event.slot == 2) {
            val resultItem = event.currentItem ?: return
            val priceString = resultItem.itemMeta?.displayName ?: return

            val price = priceString.replace(Regex("[^0-9]"), "").toIntOrNull()
            if (price == null) {
                player.sendMessage("${ChatColor.RED}Price should be a number")
                event.isCancelled = true
                return
            }

            player.sendMessage("${ChatColor.GREEN}Price is set by ${price}.")
            player.closeInventory()
        }
    }
}
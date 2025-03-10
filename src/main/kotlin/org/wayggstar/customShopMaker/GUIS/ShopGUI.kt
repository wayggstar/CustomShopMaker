package org.wayggstar.customShopMaker.GUIS

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.PlayerInventory
import org.wayggstar.customShopMaker.CustomShopMaker
import org.wayggstar.customShopMaker.Shop.ShopManager

class ShopGUI(private val shopManager: ShopManager) : Listener {

    companion object {
        var slot: Int = 0
    }
    private val guiTitle = "§6Shop Modify"
    private val saveButton = ItemStack(Material.LIME_WOOL).apply {
        itemMeta = itemMeta?.apply { setDisplayName("§aSave") }
    }
    private val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta?.apply { setDisplayName(" ") }
    }

    fun openShopEditor(player: Player, shopName: String, size: Int = 54) {
        if (!player.isOp) return
        val holder = ShopGUIHolder(shopName, ShopGUIHolder.GUItype.EDITOR)
        val inventory = Bukkit.createInventory(holder, size, guiTitle)
        holder.setInventory(inventory)

        val shop = shopManager.getShop(shopName)
        if (shop == null) {
            player.sendMessage("${ChatColor.RED}상점 '$shopName'을(를) 찾을 수 없습니다.")
            return
        }

        shop.items.toList().forEachIndexed { index, item ->
            val meta = item.itemMeta
            if (meta != null) {
                val lore = meta.lore?.toMutableList() ?: mutableListOf()

                val sellPrice = item.persistentDataContainer.get(
                    NamespacedKey(CustomShopMaker.instance, "sell_price"),
                    PersistentDataType.INTEGER
                )
                val buyPrice = item.persistentDataContainer.get(
                    NamespacedKey(CustomShopMaker.instance, "buy_price"),
                    PersistentDataType.INTEGER
                )

                lore.clear()
                if (sellPrice != null) {
                    if (sellPrice == 0) {
                        lore.add("${ChatColor.RED}판매 불가")
                    } else {
                        lore.add("${ChatColor.GREEN}판매 가격: ${ChatColor.YELLOW}$sellPrice")
                    }
                } else {
                    lore.add("${ChatColor.RED}판매 가격 정보 없음")
                }

                if (buyPrice != null) {
                    if (buyPrice == 0) {
                        lore.add("${ChatColor.RED}구매 불가")
                    } else {
                        lore.add("${ChatColor.GREEN}구매 가격: ${ChatColor.YELLOW}$buyPrice")
                    }
                } else {
                    lore.add("${ChatColor.RED}구매 가격 정보 없음")
                }

                meta.lore = lore
                item.itemMeta = meta

                val slot = item.persistentDataContainer.get(
                    NamespacedKey(CustomShopMaker.instance, "slot"),
                    PersistentDataType.INTEGER
                ) ?: -1

                if (slot in 0 until size) {
                    inventory.setItem(slot, item)
                }
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
        val clickedItem = event.currentItem
        val inventory = event.inventory

        val holder = inventory.holder as? ShopGUIHolder ?: return
        val gui = holder.guitype
        if (event.inventory is PlayerInventory) {
            return
        }
        if (gui != ShopGUIHolder.GUItype.EDITOR){return}
        if (clickedItem == null || clickedItem.type == Material.AIR) {
            event.isCancelled = true
            slot = event.slot
            ItemAddGUI(shopManager).openItemAddGUI(player, holder.name)
            return
        }

        if (!clickedItem.isSimilar(saveButton) && !clickedItem.isSimilar(glassPane)) {
            event.isCancelled = true
            ItemAddGUI(shopManager).openPriceAnvil(player, clickedItem, holder.name)
        }

        if (clickedItem.isSimilar(saveButton)) {
            event.isCancelled = true
            player.sendMessage("${ChatColor.GREEN}아이템이 저장되었습니다.")
        }
    }
}
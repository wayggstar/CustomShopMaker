package org.wayggstar.customShopMaker.GUIS

import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.wayggstar.customShopMaker.CustomShopMaker
import org.wayggstar.customShopMaker.Shop.ShopManager
import java.util.*

class ItemAddGUI(private val shopManager: ShopManager) : Listener {

    private val guiTitle = "${ChatColor.AQUA}ItemAdd"
    private val saveButton = ItemStack(Material.LIME_WOOL).apply {
        itemMeta = itemMeta?.apply { setDisplayName("${ChatColor.GREEN}Save price") }
    }
    private val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta?.apply { setDisplayName(" ") }
    }

    fun openItemAddGUI(player: Player, name: String) {
        val holder = ItemAddHolder(name)
        val inventory = Bukkit.createInventory(holder, 9, guiTitle)
        holder.setInventory(inventory)

        for (i in 0 until 9) {
            if (i != 4) inventory.setItem(i, glassPane)
        }

        inventory.setItem(8, saveButton)
        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val holder = event.inventory.holder as? ItemAddHolder ?: return
        event.isCancelled = true

        if (event.inventory is PlayerInventory) {
            return
        }

        if (event.slot == 4) {
            event.isCancelled = false
        } else if (event.currentItem != null && !event.currentItem!!.isSimilar(glassPane) && !event.currentItem!!.isSimilar(
                saveButton
            )
        ) {
            val clickedItem = event.currentItem!!.clone()
            event.inventory.setItem(4, clickedItem)
            event.currentItem = null
            player.sendMessage("${ChatColor.GREEN}아이템이 추가되었습니다. 가격을 설정하려면 저장 버튼을 클릭하세요!")
        } else if (event.currentItem != null && event.currentItem!!.isSimilar(saveButton)) {
            val item = event.inventory.getItem(4)
            if (item == null || item.type == Material.AIR) {
                player.sendMessage("${ChatColor.RED}중앙 슬롯에 아이템을 배치하세요!")
                return
            }
            openPriceAnvil(player, item, holder.name)
        }
    }

    fun openPriceAnvil(player: Player, item: ItemStack, shopname: String) {
        AnvilGUI.Builder()
            .plugin(CustomShopMaker.instance)
            .title("§aType the sell price")
            .text("100")
            .itemLeft(ItemStack(Material.PAPER))
            .onClick { slot, stateSnapshot ->
                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return@onClick Collections.emptyList()
                }
                val priceText = stateSnapshot.text
                val price = priceText.toIntOrNull()

                if (price == null) {
                    stateSnapshot.player.sendMessage("§cThe price must be the number type.")
                    return@onClick listOf(AnvilGUI.ResponseAction.replaceInputText("retry!"))
                }

                openSecondPriceAnvil(player, item, shopname, price)
                return@onClick listOf(AnvilGUI.ResponseAction.close())
            }.open(player)

    }

    fun openSecondPriceAnvil(player: Player, item: ItemStack, shopname: String, sell_price: Int) {
        AnvilGUI.Builder()
            .plugin(CustomShopMaker.instance)
            .title("§aType the buy price")
            .text("100")
            .itemLeft(ItemStack(Material.PAPER))
            .onClick { slot, stateSnapshot ->
                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return@onClick Collections.emptyList()
                }
                val priceText = stateSnapshot.text
                val price = priceText.toIntOrNull()

                if (price == null) {
                    stateSnapshot.player.sendMessage("§cThe price must be the number type.")
                    return@onClick listOf(AnvilGUI.ResponseAction.replaceInputText("retry!"))
                }

                shopManager.registerItemWithData(shopname, item, price, sell_price)
                stateSnapshot.player.sendMessage("${ChatColor.GREEN}Item price is set.")
                stateSnapshot.player.closeInventory()
                return@onClick listOf(AnvilGUI.ResponseAction.close())
            }.open(player)
    }
}
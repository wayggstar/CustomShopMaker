package org.wayggstar.customShopMaker.GUIS

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.NamespacedKey
import org.bukkit.inventory.PlayerInventory
import org.wayggstar.customShopMaker.CustomShopMaker
import org.wayggstar.customShopMaker.VaultAPI.VaultSetUp
import org.wayggstar.customShopMaker.Shop.ShopManager

class ShopTransactionGUI(private val shopManager: ShopManager) : Listener {

    private val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
        itemMeta = itemMeta?.apply { setDisplayName(" ") }
    }

    fun openTransactionGUI(player: Player, shopName: String, size: Int = 54) {
        val holder = ShopGUIHolder(shopName, ShopGUIHolder.GUItype.TRANSACTION)
        val inventory = Bukkit.createInventory(holder, size, "§6Transaction - $shopName")
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
            for (i in 0 until size) {
                if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, glassPane)
                }
            }

            player.openInventory(inventory)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val holder = event.inventory.holder as? ShopGUIHolder ?: return
        val shopName = holder.name
        val clickedItem = event.currentItem
        val gui = holder.guitype
        if (gui != ShopGUIHolder.GUItype.TRANSACTION){return}
        event.isCancelled = true

        if (event.inventory is PlayerInventory) {
            return
        }

        if (clickedItem == null || clickedItem.type == Material.AIR) {
            return
        }

        val sellPrice = clickedItem.persistentDataContainer.get(NamespacedKey(CustomShopMaker.instance, "sell_price"), PersistentDataType.INTEGER)
        val buyPrice = clickedItem.persistentDataContainer.get(NamespacedKey(CustomShopMaker.instance, "buy_price"), PersistentDataType.INTEGER)

        if (sellPrice == 0 && buyPrice == 0) {
            player.sendMessage("${ChatColor.RED}이 아이템은 구매와 판매 모두 불가합니다!")
            return
        }

        if (event.isLeftClick && buyPrice != null && buyPrice > 0) {
            handleBuy(player, clickedItem, buyPrice, shopName)
        }

        if (event.isRightClick && sellPrice != null && sellPrice > 0) {
            handleSell(player, clickedItem, sellPrice, shopName)
        }
    }

    private fun handleBuy(player: Player, item: ItemStack, buyPrice: Int, shopName: String) {
        if (VaultSetUp.getBalance(player.uniqueId) < buyPrice) {
            player.sendMessage("${ChatColor.RED}잔액이 부족합니다! 구매할 수 없습니다.")
            return
        }
        val itemData = shopManager.getItemWithData(shopName, item) ?: return
        VaultSetUp.removeMoney(player.uniqueId, buyPrice.toDouble())
        player.inventory.addItem(itemData)

        player.sendMessage("${ChatColor.GREEN}아이템을 구매하여 ${ChatColor.YELLOW}$buyPrice ${ChatColor.GREEN}지불했습니다.")
    }

    private fun handleSell(player: Player, item: ItemStack, sellPrice: Int, shopName: String) {
        val itemtosell = shopManager.getItemWithData(shopName, item) ?: return
        val itemCount = player.inventory.containsAtLeast(itemtosell, 1)

        if (!itemCount) {
            player.sendMessage("${ChatColor.RED}아이템이 부족합니다! 판매할 수 없습니다.")
            return
        }
        player.inventory.removeItem(itemtosell)
        VaultSetUp.addMoney(player.uniqueId, sellPrice.toDouble())

        player.sendMessage("${ChatColor.GREEN}아이템을 판매하여 ${ChatColor.YELLOW}$sellPrice ${ChatColor.GREEN}받았습니다.")
    }
}
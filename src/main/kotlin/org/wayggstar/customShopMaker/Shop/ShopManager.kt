package org.wayggstar.customShopMaker.Shop

import net.md_5.bungee.api.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.wayggstar.customShopMaker.CustomShopMaker
import org.wayggstar.customShopMaker.CustomShopMaker.Companion.itemDataMap
import org.wayggstar.customShopMaker.GUIS.ShopGUI
import java.io.File
import java.util.*

class ShopManager {

    private val shops: MutableMap<String, Shop> = mutableMapOf()

    private val shopdata: File
        get() {
            val file = File(CustomShopMaker.instance.dataFolder, "shopdata.yml")
            if (!file.exists()) file.createNewFile()
            return file
        }

    fun saveShops() {

        val logger=CustomShopMaker.instance.logger
        val buyKey=NamespacedKey(CustomShopMaker.instance, "buy_price")
        val sellKey=NamespacedKey(CustomShopMaker.instance, "sell_price")
        val idKey=NamespacedKey(CustomShopMaker.instance, "id")
        val config=YamlConfiguration.loadConfiguration(shopdata)

        logger.info("Saving shops...")

        if (!config.contains("shop")) config.createSection("shop")
        for ((name, shop) in shops) {
            val shopItems = shop.items.mapNotNull { item ->
                val meta = item.itemMeta ?: return@mapNotNull null
                val pdc = meta.persistentDataContainer

                val buyPrice = pdc.get(buyKey, PersistentDataType.INTEGER)?: 0
                val sellPrice = pdc.get(sellKey, PersistentDataType.INTEGER) ?: 0
                val id = pdc.get(idKey, PersistentDataType.STRING) ?: ""

                val itemData = mapOf(
                    "item" to item.serialize(),
                    "buy_price" to buyPrice,
                    "sell_price" to sellPrice,
                    "id" to id
                )

                itemData
            }

            logger.info("Shop '$name' Items: $shopItems")

            if (shopItems.isNotEmpty()) config.set("shop.$name.items", shopItems)
            else logger.warning("No items found for shop '$name'")
        }
        logger.info("Saving to: ${shopdata.absolutePath}")
        try {
            config.save(shopdata)
            logger.info("Saved content to shopdata.yml")
        } catch (e: Exception) {
            logger.severe("Error saving shopdata.yml: ${e.message}")
        } finally {
            logger.info("Shop Data: ${config.saveToString()}")
            logger.info("Shops successfully saved!")
        }

    }

    fun loadShops() {

        val config=YamlConfiguration.loadConfiguration(shopdata)
        if (config.contains("shop")) {
            for (key in config.getConfigurationSection("shop")!!.getKeys(false)) {
                val shop = Shop(name = key, items = mutableListOf())

                val itemList = config.getList("shop.$key.items") as? List<Map<String, Any>>
                itemList?.forEach { itemDataMap ->
                    val itemStack = ItemStack.deserialize(itemDataMap["item"] as Map<String, Any>)
                    val buyPrice = itemDataMap["buy_price"] as? Int ?: 0
                    val sellPrice = itemDataMap["sell_price"] as? Int ?: 0

                    val meta = itemStack.itemMeta ?: return@forEach
                    val buyKey = NamespacedKey(CustomShopMaker.instance, "buy_price")
                    val sellKey = NamespacedKey(CustomShopMaker.instance, "sell_price")
                    meta.persistentDataContainer.set(buyKey, PersistentDataType.INTEGER, buyPrice)
                    meta.persistentDataContainer.set(sellKey, PersistentDataType.INTEGER, sellPrice)
                    itemStack.itemMeta = meta

                    shop.items.add(itemStack)
                }

                shops[key] = shop
            }
        }
    }

    fun createShop(name: String): Boolean {
        if (shops.containsKey(name)) return false
        shops[name] = Shop(name)
        return true
    }

    fun getShop(name: String): Shop? {
        return shops[name]
    }

    fun registerItemWithData(shopName: String, item: ItemStack, buyPrice: Int, sellPrice: Int) {
        val itemClone = item.clone()
        val itemtogive = item.clone()

        val meta = itemClone.itemMeta ?: return
        val persistentDataContainer = meta.persistentDataContainer

        val uniqueId = UUID.randomUUID().toString()
        val slotkey = NamespacedKey(CustomShopMaker.instance, "slot")

        persistentDataContainer.set(NamespacedKey(CustomShopMaker.instance, "id"), PersistentDataType.STRING, uniqueId)
        persistentDataContainer.set(NamespacedKey(CustomShopMaker.instance, "buy_price"), PersistentDataType.INTEGER, buyPrice)
        persistentDataContainer.set(NamespacedKey(CustomShopMaker.instance, "sell_price"), PersistentDataType.INTEGER, sellPrice)
        meta.persistentDataContainer.set(slotkey, PersistentDataType.INTEGER, ShopGUI.slot)

        itemClone.itemMeta = meta

        val giveMeta = itemtogive.itemMeta
        if (giveMeta != null) {
            val giveDataContainer = giveMeta.persistentDataContainer
            giveDataContainer.remove(NamespacedKey(CustomShopMaker.instance, "buy_price"))
            giveDataContainer.remove(NamespacedKey(CustomShopMaker.instance, "sell_price"))
            giveDataContainer.remove(NamespacedKey(CustomShopMaker.instance, "slot"))
            itemtogive.itemMeta = giveMeta
        }

        val itemKey = NamespacedKey(CustomShopMaker.instance, "item_data_${shopName}_$uniqueId")

        CustomShopMaker.itemDataMap[itemKey] = itemtogive
        shops[shopName]?.items?.add(itemClone)

        CustomShopMaker.instance.logger.info("Registering item with id: $uniqueId, key: $itemKey")
    }

    fun getItemWithData(shopName: String, item: ItemStack): ItemStack? {
        val meta = item.itemMeta ?: return null
        val persistentDataContainer = meta.persistentDataContainer
        val uniqueId = persistentDataContainer.get(NamespacedKey(CustomShopMaker.instance, "id"), PersistentDataType.STRING) ?: return null

        CustomShopMaker.instance.logger.info("Retrieving item with id: $uniqueId")
        val itemKey = NamespacedKey(CustomShopMaker.instance, "item_data_${shopName}_$uniqueId")
        val itemEntry = CustomShopMaker.itemDataMap[itemKey]?.clone()

        if (itemEntry == null) {
            CustomShopMaker.instance.logger.warning("Item with id $uniqueId not found for shop: $shopName")
        } else {
            CustomShopMaker.instance.logger.info("Successfully retrieved item with id: $uniqueId for shop: $shopName")
        }

        return itemEntry
    }
}
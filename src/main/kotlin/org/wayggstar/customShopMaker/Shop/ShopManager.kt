package org.wayggstar.customShopMaker.Shop

import net.md_5.bungee.api.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.wayggstar.customShopMaker.CustomShopMaker

class ShopManager(private val shopdata: FileConfiguration) {

    private val shops: MutableMap<String, Shop> = mutableMapOf()

    fun loadShops(){
        if (shopdata.contains("shop")){
            for (key in shopdata.getConfigurationSection("shop")!!.getKeys(false)){
                val shop = Shop(name = key, items = mutableListOf())

                val itemList = shopdata.getList("shop.$key.items") as? List<Map<String, Any>>
                itemList?.forEach{shop.items.add(ItemStack.deserialize(it))}

                shops[key] = shop
            }
        }
    }

    fun saveShops(){
        for ((name, shop) in shops){
            shopdata.set("shop.$name.items", shop.items.map { it.serialize() })
        }
    }

    fun createShop(name: String): Boolean{
        if (shops.containsKey(name)) return false
        shops[name] = Shop(name)
        return true
    }

    fun getShop(name: String): Shop? {
        return shops[name]
    }

    fun addItemToShop(name: String, item: ItemStack){
        shops[name]?.items?.add(item)
    }

    fun setItemPrice(item: ItemStack, price: Int): ItemStack {
        val meta = item.itemMeta ?: return item
        val key = NamespacedKey(CustomShopMaker.instance, "price")
        meta.persistentDataContainer.set(key, PersistentDataType.INTEGER, price)
        val loretoadd = meta.lore ?: mutableListOf()
        loretoadd.removeIf { it.contains("Price:") }
        loretoadd.add("${ChatColor.GREEN}Price: ${ChatColor.YELLOW}${price}$")
        meta.lore = loretoadd
        item.itemMeta = meta
        return item
    }

    fun getItemPrice(item: ItemStack): Int {
        val meta = item.itemMeta ?: return 0
        val key = NamespacedKey(CustomShopMaker.instance, "price")
        return meta.persistentDataContainer.get(key, PersistentDataType.INTEGER) ?: 0
    }

    fun updateItemPrice(item: ItemStack, newPrice: Int): ItemStack {
        return setItemPrice(item, newPrice)
    }
}
package org.wayggstar.customShopMaker.GUIS

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class ShopGUIHolder(val name: String, val guitype: GUItype): InventoryHolder {

    private lateinit var shopGUI: Inventory

    override fun getInventory(): Inventory {
        return shopGUI
    }

    fun setInventory(inventory: Inventory){
        this.shopGUI = inventory
    }

    enum class GUItype{
        TRANSACTION,
        EDITOR,
        PLACER
    }
}
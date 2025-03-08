package org.wayggstar.customShopMaker.GUIS

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class ItemAddHolder(val name: String): InventoryHolder {

    private lateinit var iainventory: Inventory

    override fun getInventory(): Inventory {
        return iainventory
    }

    fun setInventory(inventory: Inventory){
        this.iainventory = inventory
    }
}
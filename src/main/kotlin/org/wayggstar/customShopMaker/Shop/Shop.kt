package org.wayggstar.customShopMaker.Shop

import org.bukkit.inventory.ItemStack

data class Shop(
    val name: String,
    val items: MutableList<ItemStack> = mutableListOf()
)

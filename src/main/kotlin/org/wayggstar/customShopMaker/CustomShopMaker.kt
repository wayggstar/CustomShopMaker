package org.wayggstar.customShopMaker

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.wayggstar.customShopMaker.Commands.CSMcmd
import org.wayggstar.customShopMaker.GUIS.ItemAddGUI
import org.wayggstar.customShopMaker.GUIS.ShopGUI
import org.wayggstar.customShopMaker.GUIS.ShopTransactionGUI
import org.wayggstar.customShopMaker.Shop.ShopManager
import org.wayggstar.customShopMaker.VaultAPI.VaultSetUp

class CustomShopMaker : JavaPlugin() {

    companion object{
        lateinit var instance: JavaPlugin
        val itemDataMap: MutableMap<NamespacedKey, ItemStack> = mutableMapOf()
    }

    private lateinit var shopManager: ShopManager

    override fun onEnable() {
        instance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        saveDefaultConfig()

        shopManager = ShopManager()

        if (!VaultSetUp.setupEconomy()) {
            logger.severe("No Economy plugin to use VaultAPI!!!!!")
            logger.severe("Pls download economy support plugin")
            logger.severe("ex) EssentialX, Economy, and so on")
            server.pluginManager.disablePlugin(this)
            return
        }

        try {
            shopManager.loadShops()
            logger.info("Shops successfully loaded.")
        } catch (e: Exception) {
            logger.severe("Error loading shops: ${e.message}")
        }

        server.pluginManager.registerEvents(ShopGUI(shopManager), this)
        server.pluginManager.registerEvents(ItemAddGUI(shopManager), this)
        server.pluginManager.registerEvents(ShopTransactionGUI(shopManager), this)
        getCommand("csm")?.setExecutor(CSMcmd(this, shopManager))

        logger.info("§aCustomShop Plugin Enabled")
    }

    override fun onDisable() {
        try {
            shopManager.saveShops()
            logger.info("Shops successfully saved.")
        } catch (e: Exception) {
            logger.severe("Error saving shops: ${e.message}")
        }

        logger.info("§aCustomShop Plugin Disabled")
    }
}
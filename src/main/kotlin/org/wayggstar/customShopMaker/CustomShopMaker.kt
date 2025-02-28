package org.wayggstar.customShopMaker

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.wayggstar.customShopMaker.VaultAPI.VaultSetUp
import kotlin.time.Instant

class CustomShopMaker : JavaPlugin() {

    companion object{
        lateinit var instance: JavaPlugin
    }
    override fun onEnable() {
        instance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        saveDefaultConfig()

        if (!VaultSetUp.setupEconomy()) {
            logger.severe("No Economy plugin to use VaultAPI!!!!!")
            logger.severe("Pls download economy support plugin")
            logger.severe("ex) EssentialX, Economy, and so on")
            server.pluginManager.disablePlugin(this)
            return
        }
        logger.info("§aCustomShop Plugin Enabled")
    }

    override fun onDisable() {
        logger.info("§aCustomShop Plugin Disabled")
    }
}

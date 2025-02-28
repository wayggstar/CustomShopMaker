package org.wayggstar.customShopMaker.Commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.wayggstar.customShopMaker.GUIS.ShopGUI
import org.wayggstar.customShopMaker.Shop.Shop
import org.wayggstar.customShopMaker.Shop.ShopManager

class CSMcmd(private val plugin: JavaPlugin, private val shopManager: ShopManager): CommandExecutor {

    private val shopdata: FileConfiguration = plugin.config

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player){
            val player = sender
            val commandname = args.getOrNull(0) ?: ""

            when (commandname){
                "create" -> CreateShop(player, args)
                "modify" -> ShopGUI(shopManager).openShopEditor(player, args[1])
            }

        }
        return true
    }

    fun CreateShop(player: Player, args: Array<out String>){
        val name = args[1]
        if (name.equals(null)){
            player.sendMessage("§cPls type the name of your shop to create.")
        }
        if(!shopManager.createShop(name)){
            player.sendMessage("§cThe name $name already exist in your shop list.")
        }
        Shop(name)
        player.sendMessage("§aThe shop $name is created!!")
    }
}
package org.wayggstar.customShopMaker.Commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.wayggstar.customShopMaker.GUIS.ShopGUI
import org.wayggstar.customShopMaker.GUIS.ShopTransactionGUI
import org.wayggstar.customShopMaker.Shop.Shop
import org.wayggstar.customShopMaker.Shop.ShopManager

class CSMcmd(private val plugin: JavaPlugin, private val shopManager: ShopManager): CommandExecutor {

    private val shopdata: FileConfiguration = plugin.config

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            val player = sender
            val commandname = args.getOrNull(0) ?: ""

            when (commandname) {
                "create" -> CreateShop(player, args)
                "open" -> openShop(player, args)
                "modify" -> {
                    if (args.getOrNull(1) == "price") {
                        modifyPrice(player, args)
                    } else if (args.getOrNull(1) == "place") {
                        modifyPlace(player, args)
                    }
                }
                else -> player.sendMessage("§cUnknown command.")
            }
        }
        return true
    }

    fun CreateShop(player: Player, args: Array<out String>) {
        if (player.isOp) {
            val name = args[1]
            if (name.equals(null)) {
                player.sendMessage("§cPls type the name of your shop to create.")
                return
            }
            if (!shopManager.createShop(name)) {
                player.sendMessage("§cThe name $name already exist in your shop list.")
                return
            }
            Shop(name)
            player.sendMessage("§aThe shop $name is created!!")
        }
    }

    fun openShop(player: Player, args: Array<out String>) {
        val shopName = args.getOrNull(1)
        val targetPlayer = args.getOrNull(2) ?: player.name  // 플레이어 이름이 없다면 본인에게 연다

        if (shopName.isNullOrEmpty()) {
            player.sendMessage("§cPlease provide a shop name.")
            return
        }

        val target = Bukkit.getPlayer(targetPlayer)
        if (target == null) {
            player.sendMessage("§cPlayer not found.")
            return
        }

        ShopTransactionGUI(shopManager).openTransactionGUI(target, shopName)
        player.sendMessage("§aOpening shop '$shopName' for $targetPlayer.")
    }

    fun modifyPrice(player: Player, args: Array<out String>) {
        val shopName = args.getOrNull(2)
        if (shopName.isNullOrEmpty()) {
            player.sendMessage("§cPlease provide a shop name to modify prices.")
            return
        }
        ShopGUI(shopManager).openShopEditor(player, shopName)
        player.sendMessage("§aOpening price modification for shop '$shopName'.")
    }

    fun modifyPlace(player: Player, args: Array<out String>) {
        val shopName = args.getOrNull(2)
        if (shopName.isNullOrEmpty()) {
            player.sendMessage("§cPlease provide a shop name to modify placement.")
            return
        }

        player.sendMessage("§aOpening item placement modification for shop '$shopName'.")
    }
}
package org.wayggstar.customShopMaker.VaultAPI

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.RegisteredServiceProvider
import java.util.*

class VaultSetUp {
    companion object {
        private var economy: Economy? = null

        fun setupEconomy(): Boolean {
            val rsp: RegisteredServiceProvider<Economy>? = Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)
            economy = rsp?.provider
            return economy != null
        }

        fun getBalance(player: UUID): Double {
            val playerName = Bukkit.getOfflinePlayer(player).name ?: return 0.0
            return economy?.getBalance(playerName) ?: 0.0
        }

        fun addMoney(player: UUID, amount: Double) {
            val playerName = Bukkit.getOfflinePlayer(player).name ?: return
            economy?.depositPlayer(playerName, amount)
        }

        fun removeMoney(player: UUID, amount: Double): Boolean {
            val playerName = Bukkit.getOfflinePlayer(player).name ?: return false
            return economy?.withdrawPlayer(playerName, amount)?.transactionSuccess() ?: false
        }
    }
}
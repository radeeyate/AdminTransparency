package org.radi8.adminTransparency;

import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

public final class AdminTransparency extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String command = event.getMessage();

        if (player.isOp()) {
            String message = ChatColor.GREEN + "[AdminTransparency] " + playerName + " executed a command: " + command;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (p.getName().equals(playerName)) {
                    continue;
                } else {
                    p.sendMessage(message);
                }
            }
        }
    }
}
package org.radi8.adminTransparency;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class AdminTransparency extends JavaPlugin implements Listener, CommandExecutor {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("toggle-exclusion").setExecutor(new ToggleExclusion());

        this.saveDefaultConfig();

        List<String> excludedPlayersDefault = List.of("example-user");
        getConfig().addDefault("broadcast-to-command-sender", false);
        getConfig().addDefault("excluded-players", excludedPlayersDefault);

        getConfig().options().copyDefaults();

    }

    public List<String> getExcludedPlayers() {
        @SuppressWarnings("unchecked") // idk if this is safe
        List<String> excludedPlayers = (List<String>) getConfig().getList("excluded-players");

        System.out.println(excludedPlayers);
        return excludedPlayers;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String command = event.getMessage();

        if (player.isOp()) {
            String message = ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + playerName + " executed a command: " + command;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (p.getName().equals(playerName) || getExcludedPlayers().contains(p.getName())) {
                    if (getConfig().getBoolean("broadcast-to-command-sender")) {
                        p.sendMessage(message);
                    } else {
                        continue;
                    }
                } else {
                    p.sendMessage(message);
                }
            }
        }
    }

    public class ToggleExclusion implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player player) {
                if (player.isOp()) {
                    if (args.length > 0) {
                        String targetPlayer = args[0];
                        if (getExcludedPlayers().contains(targetPlayer)) {
                            getConfig().getStringList("excluded-players").remove(targetPlayer);
                            saveConfig();

                            player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + targetPlayer + " removed from the exclusion list.");
                        } else {
                            getConfig().getStringList("excluded-players").add(targetPlayer);
                            saveConfig();

                            player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + targetPlayer + " added to the exclusion list.");
                        }
                    } else {
                        player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + "You must specify a user.");
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + "You must be an operator to execute this command.");
                }
            }

            return true;
        }
    }
}
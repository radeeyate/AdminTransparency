package org.radi8.adminTransparency;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class AdminTransparency extends JavaPlugin implements Listener, CommandExecutor {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("toggle-exclusion")).setExecutor(new ToggleExclusion());
        Objects.requireNonNull(this.getCommand("list-exclusions")).setExecutor(new ListExclusions());

        this.saveDefaultConfig();

        List<String> excludedPlayersDefault = List.of("example-user");
        getConfig().addDefault("broadcast-to-command-sender", false);
        getConfig().addDefault("excluded-players", excludedPlayersDefault);

        getConfig().options().copyDefaults();

    }

    public List<String> getExcludedPlayers() {
        @SuppressWarnings("unchecked") // I don't know if this is safe
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
            String message = ChatColor.GREEN + "[AdminTransparency] " + ChatColor.YELLOW + playerName + ChatColor.RESET + " executed a command: " + ChatColor.YELLOW + command;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (command.startsWith("/toggle-exclusion") && Objects.equals(command.split(" ")[1], p.getName())) {
                    continue;
                }

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
                if (args.length > 0) {
                    String targetPlayer = args[0];
                    if (getExcludedPlayers().contains(targetPlayer)) {
                        List<String> excludedPlayers = getExcludedPlayers();
                        excludedPlayers.remove(targetPlayer);
                        getConfig().set("excluded-players", excludedPlayers);
                        saveConfig();

                        player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.YELLOW + targetPlayer + ChatColor.RESET + " removed from the exclusion list.");
                    } else {
                        List<String> excludedPlayers = getExcludedPlayers();
                        excludedPlayers.add(targetPlayer);
                        getConfig().set("excluded-players", excludedPlayers);
                        saveConfig();

                        player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.YELLOW + targetPlayer + ChatColor.RESET + " added to the exclusion list.");
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + "You must specify a user.");
                }
            }

            return true;
        }
    }

    public class ListExclusions implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            List<String> excludedPlayers = getExcludedPlayers();
            String message;

            if (excludedPlayers.size() == 1) {
                message = ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + "There is " + ChatColor.YELLOW + excludedPlayers.size() + ChatColor.RESET + " excluded player: " + ChatColor.YELLOW + excludedPlayers.getFirst() + ChatColor.RESET + ".";
            } else if (excludedPlayers.isEmpty()) {
                message = ChatColor.GREEN + "[AdminTransparency] " + ChatColor.RESET + "There are no excluded players.";
            } else {
                message = ChatColor.GREEN + "[AdminTransparency]" + ChatColor.RESET + "There are " + ChatColor.YELLOW + excludedPlayers.size() + ChatColor.RESET + " excluded players: " + ChatColor.YELLOW + String.join(", ", excludedPlayers) + ChatColor.RESET + ".";
            }

            sender.sendMessage(message);

            return true;
        }
    }
}
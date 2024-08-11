package org.radi8.adminTransparency;

import com.google.common.reflect.TypeToken;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public final class AdminTransparency extends JavaPlugin implements Listener, CommandExecutor {

    private static List<String> excludedPlayers;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("toggle-exclusion").setExecutor(new ToggleExclusion());

        try {
            File jsonFile = new File("./transparency-exclude.json");
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            }

            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            excludedPlayers = gson.fromJson(new FileReader(jsonFile), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ToggleExclusion implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player player) {
                if (player.isOp()) {
                    if (args.length > 0) {
                        String targetPlayer = args[0];
                        if (excludedPlayers.contains(targetPlayer)) {
                            excludedPlayers.remove(targetPlayer);
                            player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + targetPlayer + " removed from the exclusion list.");
                        } else {
                            excludedPlayers.add(targetPlayer);
                            player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + targetPlayer + " added to the exclusion list.");
                        }

                        try {
                            Gson gson = new Gson();
                            String json = gson.toJson(excludedPlayers);

                            File jsonFile = new File("./transparency-exclude.json");
                            java.io.FileWriter writer = new FileWriter(jsonFile);
                            writer.write(json);
                            writer.close();
                        } catch (IOException e) {
                            player.sendMessage(ChatColor.RED + "[AdminTransparency] Error saving exclusion list!");
                            try {
                                e.printStackTrace();
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + "You must specify a user.");
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "[AdminTransparency] " + "You must be an operator to execute this command.");
                }
            }

            return true;
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String command = event.getMessage();

        if (player.isOp()) {
            String message = ChatColor.GREEN + "[AdminTransparency] " + playerName + " executed a command: " + command;
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (p.getName().equals(playerName) || excludedPlayers.contains(p.getName())) {
                    continue;
                } else {
                    p.sendMessage(message);
                }
            }
        }
    }
}
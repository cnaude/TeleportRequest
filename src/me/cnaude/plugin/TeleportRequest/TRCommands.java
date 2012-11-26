/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.TeleportRequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class TRCommands implements CommandExecutor {

    private final TR plugin;

    public TRCommands(TR instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (player.isDead()) {
                sender.sendMessage(ChatColor.RED + "You are dead!!");
                return true;
            }
            
            if (sender.hasPermission("teleportrequest.rtp")
                    || sender.hasPermission("rtp.use")
                    || sender.hasPermission("rtp.admin")
                    || sender.hasPermission("teleportrequest.admin")
                    || !plugin.enablePerms()) {
            
                if (args.length == 1) {
                    String arg = args[0];
                    if (arg.equalsIgnoreCase("yes")) {
                        // accept request
                        plugin.acceptRequest(player, "all");
                        return true;
                    }
                    if (arg.equalsIgnoreCase("no")) {
                        // deny request
                        plugin.denyRequest(player, "all");
                        return true;
                    }
                    Player dstPlayer = Bukkit.getPlayerExact(arg);
                    if (dstPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Player " + ChatColor.AQUA 
                                + arg + ChatColor.RED + " is not online!");
                        return true;
                    } else if (player.equals(dstPlayer)) {
                        sender.sendMessage(ChatColor.RED + "Why would you do that!? ");
                        return true;
                    } else {
                        if (sender.hasPermission("teleportrequest.admin") || sender.hasPermission("rtp.admin")) {
                            player.sendMessage(ChatColor.YELLOW + "Teleporting you to " + ChatColor.AQUA
                                + dstPlayer.getName() + ChatColor.YELLOW + "!");
                            player.teleport(dstPlayer);
                        } else {
                            // send tp request to dstPlayer from player
                            plugin.sendRequest(player, dstPlayer);
                        }
                        return true;
                    }
                } else if (args.length > 1) {
                    String arg = args[0];
                    if (arg.equalsIgnoreCase("yes")) {
                        // accept request
                        for (int x = 1; x < args.length; x++) {
                            plugin.acceptRequest(player, args[x]);
                        }
                        return true;
                    } else if (arg.equalsIgnoreCase("no")) {
                        // deny request
                        for (int x = 1; x < args.length; x++) {
                            plugin.denyRequest(player, args[x]);
                        }
                        return true;
                    } else {
                        return false;
                    }                
                } else {
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return true;
            }
        } else {
            if (sender != null) {
                sender.sendMessage("Only a player can send teleport requests.");
            }
            return true;
        }
    }
}

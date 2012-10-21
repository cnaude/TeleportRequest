/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.TeleportRequest;

import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author cnaude
 */
public class TR extends JavaPlugin {

    public static final String PLUGIN_NAME = "TeleportRequest";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    static final Logger log = Logger.getLogger("Minecraft");
    // destination player, requestor
    public static HashMap<String, String> tpRequests = new HashMap<String, String>();

    @Override
    public void onEnable() {
        getCommand("rtp").setExecutor(new TRCommands(this));
    }

    public void sendRequest(Player requestor, Player dstPlayer) {
        String sName = requestor.getName();
        String dstName = dstPlayer.getName();
        Timer timer = new Timer();
        if (tpRequests.containsKey(dstName)) {
            requestor.sendMessage(ChatColor.RED + "A teleport request to "
                    + ChatColor.AQUA + dstName + ChatColor.RED + " already exists!");
        } else {
            requestor.sendMessage(ChatColor.YELLOW + "A teleport request has been sent to "
                    + ChatColor.AQUA + dstName + ChatColor.YELLOW + "!");
            dstPlayer.sendMessage(ChatColor.AQUA + sName + ChatColor.YELLOW
                    + " has sent you a teleport request. Type "
                    + ChatColor.GREEN + "/rtp yes" + ChatColor.YELLOW + " to accept.");
            tpRequests.put(dstName, sName);
            // timeout the request after time
            timer.schedule(new TRTimeOut(this, dstPlayer), 120000); 
        }
    }

    public void acceptRequest(Player dstPlayer) {
        String dstName = dstPlayer.getName();
        
        if (tpRequests.containsKey(dstName)) {
            String sName = tpRequests.get(dstName);
            Player requestor = Bukkit.getPlayerExact(sName);
            if (requestor != null) {
                dstPlayer.sendMessage(ChatColor.YELLOW + "Teleporting " + ChatColor.AQUA
                        + sName + ChatColor.YELLOW + " to your location!");
                requestor.sendMessage(ChatColor.YELLOW + "Teleporting you to " + ChatColor.AQUA
                        + dstName + ChatColor.YELLOW + "!");
                requestor.teleport((Entity) dstPlayer);      
            } else {
                dstPlayer.sendMessage(ChatColor.YELLOW + "Teleport requestor, " + ChatColor.AQUA
                        + sName + ChatColor.YELLOW + ", is not online!");
            }
            tpRequests.remove(dstName);
        }
    }
    
    public void denyRequest(Player dstPlayer) {
        String dstName = dstPlayer.getName();
        if (tpRequests.containsKey(dstName)) {
            String sName = tpRequests.get(dstName);
            Player requestor = Bukkit.getPlayerExact(sName);
            dstPlayer.sendMessage(ChatColor.YELLOW + "Denying teleportation request from " + ChatColor.AQUA
                        + sName + ChatColor.YELLOW + "!");
            if (requestor != null) {
                requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                        + dstName + ChatColor.YELLOW + " was denied!");
            } 
            tpRequests.remove(dstName);
        }
    }
    
    public void timeOutRequest(Player dstPlayer) {
        String dstName = dstPlayer.getName();
        if (tpRequests.containsKey(dstName)) {
            String sName = tpRequests.get(dstName);
            Player requestor = Bukkit.getPlayerExact(sName);
            dstPlayer.sendMessage(ChatColor.YELLOW + "Teleportation request from " + ChatColor.AQUA
                        + sName + ChatColor.YELLOW + " has timed out!");
            if (requestor != null) {
                requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                        + dstName + ChatColor.YELLOW + " has timed out!");
            } 
            tpRequests.remove(dstName);
        }
    }
}

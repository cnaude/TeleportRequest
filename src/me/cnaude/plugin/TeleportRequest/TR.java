/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.TeleportRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    // destination player, requestor(s)
    public static HashMap<String, List<String>> tpRequests = new HashMap<String, List<String>>();

    @Override
    public void onEnable() {
        getCommand("rtp").setExecutor(new TRCommands(this));
    }

    public void sendRequest(Player requestor, Player dstPlayer) {
        String sName = requestor.getName();
        String dstName = dstPlayer.getName();
        Timer timer = new Timer();

        if (tpRequests.containsKey(dstName)) {
            if (tpRequests.get(dstName).size() >= 10) {
                requestor.sendMessage(ChatColor.RED + "Player " + ChatColor.AQUA
                        + dstName + ChatColor.RED + " has too many teleportation requests!");
                return;
            }
        } else {
            tpRequests.put(dstName, new ArrayList<String>());
        }

        for (String s : tpRequests.get(dstName)) {
            if (s.equals(sName)) {
                requestor.sendMessage(ChatColor.RED + "You have already sent a teleportation request to " 
                        + ChatColor.AQUA + dstName + ChatColor.RED + "!");
                return;
            }
        }
        requestor.sendMessage(ChatColor.YELLOW + "A teleport request has been sent to "
                + ChatColor.AQUA + dstName + ChatColor.YELLOW + "!");
        dstPlayer.sendMessage(ChatColor.AQUA + sName + ChatColor.YELLOW
                + " has sent you a teleport request. Type "
                + ChatColor.GREEN + "/rtp yes" + ChatColor.YELLOW + " to accept.");
        tpRequests.get(dstName).add(sName);
        // timeout the request after time
        timer.schedule(new TRTimeOut(this, dstPlayer, sName), 120000);           
    }

    public void acceptRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        ArrayList<String> sNames = new ArrayList<String>();

        if (sName.equalsIgnoreCase("all")) {
            if (tpRequests.containsKey(dstName)) {
                for (String s : tpRequests.get(dstName)) {
                    sNames.add(s);
                }
            } else {
                return;
            }
        } else {
            sNames.add(sName);
        }
        
        if (tpRequests.containsKey(dstName)) {
            for (int x = sNames.size(); x > 0; x--) {
                String s = sNames.get(x);
                Player requestor = Bukkit.getPlayerExact(s);
                if (requestor != null) {
                    dstPlayer.sendMessage(ChatColor.YELLOW + "Teleporting " + ChatColor.AQUA
                            + s + ChatColor.YELLOW + " to your location!");
                    requestor.sendMessage(ChatColor.YELLOW + "Teleporting you to " + ChatColor.AQUA
                            + dstName + ChatColor.YELLOW + "!");
                    requestor.teleport((Entity) dstPlayer);
                } else {
                    dstPlayer.sendMessage(ChatColor.YELLOW + "Teleport requestor, " + ChatColor.AQUA
                            + s + ChatColor.YELLOW + ", is not online!");
                }
                sNames.remove(x);
            }
            // remove all requests
            tpRequests.remove(dstName);
            // replace with new list, even if empty
            tpRequests.put(dstName, sNames);
        }
    }

    public void denyRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        ArrayList<String> sNames = new ArrayList<String>();

        if (sName.equalsIgnoreCase("all")) {
            if (tpRequests.containsKey(dstName)) {
                for (String s : tpRequests.get(dstName)) {
                    sNames.add(s);
                }
            } else {
                return;
            }
        } else {
            sNames.add(sName);
        }
        
        if (tpRequests.containsKey(dstName)) {
            for (int x = sNames.size(); x > 0; x--) {
                String s = sNames.get(x);
                Player requestor = Bukkit.getPlayerExact(s);
                dstPlayer.sendMessage(ChatColor.YELLOW + "Denying teleportation request from " + ChatColor.AQUA
                        + s + ChatColor.YELLOW + "!");
                if (requestor != null) {
                    requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                            + dstName + ChatColor.YELLOW + " was denied!");
                }
                sNames.remove(x);
            }
            // remove all requests
            tpRequests.remove(dstName);
            // replace with new list, even if empty
            tpRequests.put(dstName, sNames);
        }
    }

    public void timeOutRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        ArrayList<String> sNames = new ArrayList<String>();
        
        if (tpRequests.containsKey(dstName)) {
            for (int x = sNames.size(); x > 0; x--) {
                String s = sNames.get(x);
                if (s.equals(sName)) {
                    Player requestor = Bukkit.getPlayerExact(s);
                    dstPlayer.sendMessage(ChatColor.YELLOW + "Teleportation request from " + ChatColor.AQUA
                            + s + ChatColor.YELLOW + " has timed out!");
                    if (requestor != null) {
                        requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                                + dstName + ChatColor.YELLOW + " has timed out!");
                    }
                    sNames.remove(x);
                    // remove all requests
                    tpRequests.remove(dstName);
                    // replace with new list, even if empty
                    tpRequests.put(dstName, sNames);
                }
            }
        }
    }
}

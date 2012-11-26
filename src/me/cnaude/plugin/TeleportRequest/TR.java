/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.TeleportRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
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
    public static HashMap<String, List<String>> ignoreRequests = new HashMap<String, List<String>>();
        
    private File pluginFolder;
    private File configFile;
    
    private static int reqTimeOut = 120;
    private static int reqMax = 10;
    private static boolean enablePermissions = true;

    @Override
    public void onEnable() {
        pluginFolder = getDataFolder();
        configFile = new File(pluginFolder, "config.yml");
        createConfig();
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getCommand("rtp").setExecutor(new TRCommands(this));
    }
    
    public boolean enablePerms() {
        return enablePermissions;
    }

    public void sendRequest(Player requestor, Player dstPlayer) {
        String sName = requestor.getName();
        String dstName = dstPlayer.getName();
        Timer timer = new Timer();

        if (tpRequests.containsKey(dstName)) {
            if (tpRequests.get(dstName).size() >= reqMax) {
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
                + ChatColor.GREEN + "/rtp yes ([player|all])" + ChatColor.YELLOW + " to accept.");
        tpRequests.get(dstName).add(sName);
        // timeout the request after time
        timer.schedule(new TRTimeOut(this, dstPlayer, sName), (reqTimeOut * 1000));           
    }
    
    public void ignoreRequest(Player player, Player badPlayer) {
        String sName = player.getName();
        String badName = badPlayer.getName();

        if (!ignoreRequests.containsKey(sName)) {
            ignoreRequests.put(sName, new ArrayList<String>());
        }

        for (String s : ignoreRequests.get(sName)) {
            if (s.equalsIgnoreCase(badName)) {
                player.sendMessage(ChatColor.RED + "You are already ignoring teleportation requests from " 
                        + ChatColor.AQUA + badName + ChatColor.RED + "!");
                return;
            }
        }
        player.sendMessage(ChatColor.YELLOW + "Now ignoring teleportation requests from "
                + ChatColor.AQUA + badName + ChatColor.YELLOW + "!");
        ignoreRequests.get(sName).add(badName);     
    }
    
    public boolean isIgnored(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        if (!ignoreRequests.containsKey(dstName)) {
            return false;
        }
        for (String s : ignoreRequests.get(dstName)) {
            if (s.equalsIgnoreCase(sName)) {
                return true;
            }
        }
        return false;
    }

    public void acceptRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        ArrayList<String> remainingNames = new ArrayList<String>();
                        
        if (tpRequests.containsKey(dstName)) {
            for (String s : tpRequests.get(dstName)) {
                if (s.equalsIgnoreCase(sName) || sName.equalsIgnoreCase("all")) {
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
                } else {
                    remainingNames.add(s);
                }
            }
            // remove all requests
            tpRequests.remove(dstName);
            // replace with new list, even if empty
            tpRequests.put(dstName, remainingNames);
        }
    }

    public void denyRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();
        ArrayList<String> remainingNames = new ArrayList<String>();
       
        if (tpRequests.containsKey(dstName)) {
            for (String s : tpRequests.get(dstName)) {
                if (s.equalsIgnoreCase(sName) || sName.equalsIgnoreCase("all")) {
                    Player requestor = Bukkit.getPlayerExact(s);
                    dstPlayer.sendMessage(ChatColor.YELLOW + "Denying teleportation request from " + ChatColor.AQUA
                            + s + ChatColor.YELLOW + "!");
                    if (requestor != null) {
                        requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                                + dstName + ChatColor.YELLOW + " was denied!");
                    }
                } else {
                    remainingNames.add(s);
                }
            }
            // remove all requests
            tpRequests.remove(dstName);
            // replace with new list, even if empty
            tpRequests.put(dstName, remainingNames);
        }
    }

    public void timeOutRequest(Player dstPlayer, String sName) {
        String dstName = dstPlayer.getName();       
        ArrayList<String> remainingNames = new ArrayList<String>();
       
        if (tpRequests.containsKey(dstName)) {
            for (String s : tpRequests.get(dstName)) {
                if (s.equalsIgnoreCase(sName) || sName.equalsIgnoreCase("all")) {
                    Player requestor = Bukkit.getPlayerExact(s);
                    dstPlayer.sendMessage(ChatColor.YELLOW + "Teleportation request from " + ChatColor.AQUA
                            + s + ChatColor.YELLOW + " has timed out!");
                    if (requestor != null) {
                        requestor.sendMessage(ChatColor.YELLOW + "Your teleportation request to " + ChatColor.AQUA
                                + dstName + ChatColor.YELLOW + " has timed out!");
                    }
                } else {
                    remainingNames.add(s);
                }
            }
            // remove all requests
            tpRequests.remove(dstName);
            // replace with new list, even if empty
            tpRequests.put(dstName, remainingNames);                                
        }
    }
    
    private void createConfig() {
        if (!pluginFolder.exists()) {
            try {
                pluginFolder.mkdir();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());                
            }
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());
            }
        }
    }
        
    private void loadConfig() {        
        reqTimeOut = getConfig().getInt("request-timeout");
        reqMax = getConfig().getInt("request-max");
        enablePermissions = getConfig().getBoolean("permissions");
    }
            
    public void logInfo(String _message) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }
}

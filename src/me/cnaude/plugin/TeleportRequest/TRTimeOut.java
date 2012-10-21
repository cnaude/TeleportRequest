/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.TeleportRequest;

import java.util.TimerTask;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class TRTimeOut extends TimerTask {
    private Player dstPlayer;
    TR plugin;

    public TRTimeOut(TR instance, Player player) {
        dstPlayer = player;
        plugin = instance;
    }

    @Override
    public void run() {
        plugin.timeOutRequest(dstPlayer);
    }
}

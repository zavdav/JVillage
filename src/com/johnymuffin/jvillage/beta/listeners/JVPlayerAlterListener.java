package com.johnymuffin.jvillage.beta.listeners;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.models.Village;
import com.johnymuffin.jvillage.beta.player.VPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

import static com.johnymuffin.jvillage.beta.JVUtility.isAuthorized;

public class JVPlayerAlterListener implements Listener {
    private JVillage plugin;

    public JVPlayerAlterListener(JVillage plugin) {
        this.plugin = plugin;
    }

    //Lowest priority so that other plugins can cancel this event
    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onBlockPlaceEvent(final BlockPlaceEvent event) {
        if (isAuthorizedToAlter(event.getPlayer())) {
            return;
        }

        Village village = plugin.getVillageAtLocation(event.getBlock().getLocation());
        String message = plugin.getLanguage().getMessage("build_denied").replace("%village%", village.getTownName());
        event.getPlayer().sendMessage(message);

        event.setCancelled(true);
    }

    //Lowest priority so that other plugins can cancel this event
    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onBlockBreakEvent(final BlockBreakEvent event) {
        if (isAuthorizedToAlter(event.getPlayer())) {
            return;
        }

        Village village = plugin.getVillageAtLocation(event.getBlock().getLocation());
        String message = plugin.getLanguage().getMessage("destroy_denied").replace("%village%", village.getTownName());
        event.getPlayer().sendMessage(message);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onBlockIgniteEvent(final BlockIgniteEvent event) {
        //This event isn't only triggered by a player, so we need to check if the igniter is a player
        if (event.getPlayer() == null) {
            return;
        }

        if (isAuthorizedToAlter(event.getPlayer())) {
            return;
        }

        Village village = plugin.getVillageAtLocation(event.getBlock().getLocation());
        String message = plugin.getLanguage().getMessage("ignite_denied").replace("%village%", village.getTownName());
        event.getPlayer().sendMessage(message);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onPaintingPlace(final PaintingPlaceEvent event) {
        //TODO: Is this null check needed? is this fired when paintings are broken by non-players
        if (event.getPlayer() == null) {
            return;
        }

        Player player = event.getPlayer();

        if (isAuthorizedToAlter(player)) {
            return;
        }

        Village village = plugin.getVillageAtLocation(event.getBlock().getLocation());
        String message = plugin.getLanguage().getMessage("build_denied").replace("%village%", village.getTownName());
        event.getPlayer().sendMessage(message);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = Event.Priority.Lowest)
    public void onPaintingDestruction(final PaintingBreakEvent preEvent) {
        if (!(preEvent instanceof PaintingBreakByEntityEvent)) {
            return;
        }

        PaintingBreakByEntityEvent event = (PaintingBreakByEntityEvent) preEvent;

        if (event.getRemover() == null || !(event.getRemover() instanceof CraftPlayer)) {
            return;
        }

        Player player = (Player) event.getRemover();

        if (isAuthorizedToAlter(player)) {
            return;
        }

        Village village = plugin.getVillageAtLocation(event.getPainting().getLocation());
        String message = plugin.getLanguage().getMessage("destroy_denied").replace("%village%", village.getTownName());
        player.sendMessage(message);
        event.setCancelled(true);

    }


    //Method utilized to determine if a player is authorized to alter a block based on their permissions and the village they are in
    private boolean isAuthorizedToAlter(Player player) {
        if (isAuthorized(player, "jvillage.admin.bypass")) {
            return true;
        }

        VPlayer vPlayer = plugin.getPlayerMap().getPlayer(player.getUniqueId());
        if (!vPlayer.isLocatedInVillage()) {
            return true;
        }
        Village locatedVillage = vPlayer.getCurrentlyLocatedIn();
        if (locatedVillage.canPlayerAlter(player)) {
            return true;
        }

        return false;
    }

}

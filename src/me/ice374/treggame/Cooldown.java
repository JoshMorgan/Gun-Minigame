package me.ice374.treggame;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Cooldown extends BukkitRunnable{

    private Player player;

    public Cooldown(Player argPlayer) {
        player = argPlayer;
    }
    
    //TODO: Add Different reload times for different ranks.
    public void run() {
        if(player.getExp() < 1F) {
            player.setExp(player.getExp() + (1F/(3*20)));
        }
    }
}
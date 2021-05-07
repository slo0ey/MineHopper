package com.acceler8tion.minehopper;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public final class CorePlugin extends JavaPlugin implements Listener {
    private static final Logger LOGGER = Bukkit.getLogger();
    private static HashMap<UUID, Movement> MOVEMENTS;

    private static final class Movement {
        private final Queue<Integer> queue;
        private boolean isFirstHop;
        private double acceleration;
        private int doubleGroundCatcher;

        private Movement() {
            this.queue = new Queue<>();
            this.isFirstHop = true;
            this.acceleration = 0.3d;
            this.doubleGroundCatcher = 0;
        }

        private void clear() {
            queue.clear();
            isFirstHop = true;
            acceleration = 0.3d;
            doubleGroundCatcher = 0;
        }
    }

    @Override
    public void onEnable() {
        MOVEMENTS = new HashMap<>();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        MOVEMENTS.clear();
    }

    private static final int NONE           = 0b000000;
    private static final int ON_AIR_UP      = 0b000001; //0
    private static final int ON_AIR_DOWN    = 0b000010; //1
    private static final int SPRINT_MODE    = 0b000100; //2
    private static final int SNEAKING_MODE  = 0b001000; //3
    private static final int FIRST_HOP      = 0b010000; //4
    private static final int ON_GROUND      = 0b100000; //5

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        MOVEMENTS.putIfAbsent(uuid, new Movement());

        Movement move = MOVEMENTS.get(uuid);
        if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        int action = NONE;
        if(event.getFrom().getY() < event.getTo().getY())
            action = action | ON_AIR_UP;
        if(event.getFrom().getY() > event.getTo().getY())
            action = action | ON_AIR_DOWN;
        if(player.isSprinting())
            action = action | SPRINT_MODE;
        if(player.isSneaking())
            action = action | SNEAKING_MODE;
        if(move.isFirstHop)
            action = action | FIRST_HOP;
        if(onGround(player.getLocation()))
            action = action | ON_GROUND;

        if(!compare(action, 2)) {
            move.clear();
        } else {
            move.queue.enqueue(action);
            if(!compare(action, 5)) { //NotOnGround
                move.doubleGroundCatcher = 0;
                if(!move.isFirstHop) {
                    Vector dir = player.getLocation().getDirection();
                    Vector vel = player.getVelocity().clone();
                    player.setVelocity(vel.setX(dir.getX() * move.acceleration).setZ(dir.getZ() * move.acceleration));
                }
            } else { //OnGround
                if(move.doubleGroundCatcher++ == 1) {
                    move.queue.clear();
                    return; //skip
                }
                int airTime = 0;
                int sneakTime = 0;
                int lastSneak = 0;
                int count = 0;
                boolean perfectHop = false;
                boolean onTakeOff = false;
                boolean onLanding = false;
                while(!move.queue.isEmpty()) {
                    int a = move.queue.dequeue();
                    count++;
                    if(compare(a, 5)) {
                        if(compare(a, 3)) {
                            sneakTime++;
                            perfectHop = true;
                        }
                        break;
                    } else {
                        airTime++;
                        if(compare(a, 3)) {
                            lastSneak = count;
                            sneakTime++;
                        }
                        if(compare(a, 0)) {
                            onTakeOff = true;
                        }
                        if(compare(a, 1)) {
                            onLanding = true;
                        }
                    }
                }
                move.queue.clear();
                if(count < 2)
                    return;
                if(count - lastSneak < 3)
                    perfectHop = true;

                if(airTime < 6) {
                    //player.sendMessage(String.format("Not enough airTime: %d", airTime));
                    move.clear();
                }
                else if(sneakTime < 1 || sneakTime > 3) {
                    //player.sendMessage(String.format("Insufficient sneakTime: %d", sneakTime));
                    move.clear();
                }
                else if(!perfectHop) {
                    //player.sendMessage("No perfectHop");
                    move.clear();
                }
                else if(!(onTakeOff && onLanding)) {
                    //player.sendMessage("Need Up & Down");
                    move.clear();
                }
                else {
                    //player.sendMessage("GreatJob!");
                    if(move.acceleration < 1.51) {
                        move.acceleration += 0.06d;
                    }
                    move.isFirstHop = false;
                }
            }
        }
    }

    private boolean onGround(Location location) {
        double y = location.getY();
        double err = y - ((int) y);
        return (err >= 0 && err <= 0.01) && !location.add(0, -1, 0).getBlock().isPassable();
    }

    private boolean compare(int action, int pos) {
        return (action >> pos) % 2 == 1;
    }
}

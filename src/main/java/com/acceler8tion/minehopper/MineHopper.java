package com.acceler8tion.minehopper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public final class MineHopper extends JavaPlugin implements Listener {
    private static final Logger LOGGER = Bukkit.getLogger();
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Map<UUID, Integer> taskMap = new HashMap<>();
    private final Map<UUID, Vector> preVectorMap = new HashMap<>();
    private ScoreboardManager sbManager;

    @Override
    public void onEnable() {
        sbManager = Bukkit.getScoreboardManager();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event) {
        initDefaultScoreboard(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent event) {
    }

    @SuppressWarnings("all")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Scoreboard sb = player.getScoreboard();
        final UUID uuid = player.getUniqueId();
        final Vector v1 = preVectorMap.get(uuid);
        final Vector v2 = player.getVelocity();
        sb.getTeam("spx").setSuffix(String.format("%.3f", v2.getX()));
        sb.getTeam("spy").setSuffix(String.format("%.3f", v2.getY()));
        sb.getTeam("spz").setSuffix(String.format("%.3f", v2.getZ()));
        sb.getTeam("dx").setSuffix(String.format("%.3f", v2.getX() - (v1 == null ? 0d : v1.getX())));
        sb.getTeam("dy").setSuffix(String.format("%.3f", v2.getY() - (v1 == null ? 0d : v1.getY())));
        sb.getTeam("dz").setSuffix(String.format("%.3f", v2.getZ() - (v1 == null ? 0d : v1.getZ())));
        sb.getTeam("sp").setSuffix(String.format("%.3f", Math.sqrt(Math.pow(v2.getX(), 2) + Math.pow(v2.getY(), 2) + Math.pow(v2.getZ(), 2))));
        sb.getTeam("vsp").setSuffix(String.format("%.3f", Math.sqrt(Math.pow(v2.getX(), 2) + Math.pow(v2.getZ(), 2))));
        sb.getTeam("hsp").setSuffix(String.format("%.3f", v2.getY()));
    }

    private void initDefaultScoreboard(final Player player) {
        final Scoreboard sb = sbManager.getNewScoreboard();
        final Objective obj = sb.registerNewObjective("SpeedMeter", "dummy", "SpeedMeter");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        final Team t1 = sb.registerNewTeam("spx");
        final Team t2 = sb.registerNewTeam("spy");
        final Team t3 = sb.registerNewTeam("spz");
        final Team t4 = sb.registerNewTeam("dx");
        final Team t5 = sb.registerNewTeam("dy");
        final Team t6 = sb.registerNewTeam("dz");
        final Team t7 = sb.registerNewTeam("sp");
        final Team t8 = sb.registerNewTeam("vsp");
        final Team t9 = sb.registerNewTeam("hsp");
        t1.addEntry("x_speed=");
        t2.addEntry("y_speed=");
        t3.addEntry("z_speed=");
        t4.addEntry("dx=");
        t5.addEntry("dy=");
        t6.addEntry("dz=");
        t7.addEntry("speed=");
        t8.addEntry("v-speed=");
        t9.addEntry("h-speed=");
        obj.getScore("x_speed=").setScore(9);
        obj.getScore("y_speed=").setScore(8);
        obj.getScore("z_speed=").setScore(7);
        obj.getScore("dx=").setScore(6);
        obj.getScore("dy=").setScore(5);
        obj.getScore("dz=").setScore(4);
        obj.getScore("speed=").setScore(3);
        obj.getScore("v-speed=").setScore(2);
        obj.getScore("h-speed=").setScore(1);
        player.setScoreboard(sb);
    }
}

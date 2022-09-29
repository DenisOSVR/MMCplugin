package ga.denis.mmcplugin;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public final class MMCplugin extends JavaPlugin implements Listener, CommandExecutor {

    boolean zoneOn = false;
    int zoneSize = 100;
    byte tickCount = 1;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("The plugin has started!");

        this.getCommand("skywars").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("skywars")) {
            if (args[0].equals("zone")) {
                if (Integer.parseInt(args[1]) == 0) {
                    zoneOn = false;
                    sender.sendPlainMessage("Zone has been disabled!");
                    return true;
                } else {
                    zoneSize = Integer.parseInt(args[1]);
                    sender.sendPlainMessage("Zone size has been set to " + zoneSize);
                    zoneOn = true;

                    Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
                    for (Player hrac : hraci) {
                        hit(hrac);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    public void zone() {
        if (zoneOn) {
//            ParticleBuilder particle = new ParticleBuilder(Particle.REDSTONE);
//            particle.color(Color.RED);
//            World world = Bukkit.createWorld(new WorldCreator("skywars"));
//            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
//            world.spawnParticle(Particle.REDSTONE, new Location(world,zoneSize,50,0), 50, dustOptions);
//            particle.location(world, zoneSize, 50, 0);
//            particle.allPlayers();
            if (tickCount == 20) {
                tickCount = 1;
                World world = Bukkit.getWorld("skywars");
                for (int j = 43; j <= 73 ; j += 5) {
                    for (double i = 0; i < 2; i += (Math.max(0.01 * (5 - (0.05 * zoneSize)),0.01))) {
                        double a = i * 2.0 * Math.PI;
                        double x = 0.5 + zoneSize * Math.cos(a);
                        double y = 0.5 + zoneSize * Math.sin(a);

                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.5F);
                        world.spawnParticle(Particle.REDSTONE, new Location(world,x,j,y), 50, dustOptions);
                    }
                }
            } else {
                tickCount++;
            }
        }
    }

    @EventHandler
    public void onLeavingZone(PlayerMoveEvent event) {
        hit(event.getPlayer());
//            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
//            Scoreboard scoreboard = event.getPlayer().getScoreboard();
//            Objective objective = scoreboard.getObjective("zone");
//            Score score = objective.getScore("skywars");
    }

    public void hit(Player hrac) {
        if (hrac.getWorld().getName().equals("skywars")) {
            if (hrac.getLocation().distance(new Location(Bukkit.getWorld("skywars"), 0.5, hrac.getLocation().getY(), 0.5)) > zoneSize) {
                hrac.addScoreboardTag("hit");
            } else if (hrac.getScoreboardTags().contains("hit")) {
                hrac.removeScoreboardTag("hit");
            }
        }
    }

    @EventHandler
    public void onTickStart(ServerTickStartEvent event) {
        zone();
        Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
        for (Player hrac : hraci) {
            if (hrac.getScoreboardTags().contains("hit")) {
                if (!hrac.getWorld().getName().equals("skywars")) {
                    hrac.removeScoreboardTag("hit");
                } else {
                    hrac.damage(3);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event) {
        if (event.getPlayer().getScoreboardTags().contains("hit")) {
            event.getPlayer().removeScoreboardTag("hit");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

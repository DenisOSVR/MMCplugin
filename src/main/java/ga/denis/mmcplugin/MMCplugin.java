package ga.denis.mmcplugin;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public final class MMCplugin extends JavaPlugin implements Listener, CommandExecutor {

    boolean zoneOn = false;
    int zoneSize = 100;
    byte tickCountSkywars = 1;
    byte tickCountParkour = 1;
    FileConfiguration config;
    Location[] checkpoints;
    boolean showCheckpoints;
    List cordsList;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("The plugin has started!");
        config = this.getConfig();
        config.addDefault("zoneSize", 100);
        config.addDefault("zoneOn", false);
        config.addDefault("checkpoints", new List[1]);
        config.addDefault("showCheckpoints", false);
        config.options().copyDefaults(true);
        saveDefaultConfig();
        zoneOn = config.getBoolean("zoneOn");
        zoneSize = config.getInt("zoneSize");
        cordsList = config.getStringList("checkpoints");
        checkpoints = new Location[cordsList.size()];
        for (int i = 0; i < cordsList.size(); i++) {
            String[] xyz = ((String) cordsList.get(i)).split(";");
            checkpoints[i] = new Location(Bukkit.getWorld("parkour"),Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));
        }
        showCheckpoints = config.getBoolean("showCheckpoints");

        this.getCommand("skywars").setExecutor(this);
        this.getCommand("parkour").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("skywars")) {
            if (args[0].equals("zone")) {
                if (Integer.parseInt(args[1]) == 0) {
                    zoneOn = false;
                    config.set("zoneOn", false);
                    sender.sendPlainMessage("Zone has been disabled!");
                    return true;
                } else {
                    zoneSize = Integer.parseInt(args[1]);
                    sender.sendPlainMessage("Zone size has been set to " + zoneSize);
                    zoneOn = true;
                    config.set("zoneOn", true);
                    config.set("zoneSize", zoneSize);
                    Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
                    for (Player hrac : hraci) {
                        hit(hrac);
                    }

                    return true;
                }
            }
        } else if (command.getName().equals("parkour") && sender instanceof Player) {
            if (args[0].equals("add")) {
                Player hrac = (Player) sender;
                config.set("checkpoints", config.getStringList("checkpoints").add(hrac.getLocation().getX() + ";" + hrac.getLocation().getY() + ";" + hrac.getLocation().getZ()));
                saveConfig();
                cordsList = config.getStringList("checkpoints");
                checkpoints = new Location[cordsList.size()];
                for (int i = 0; i < cordsList.size(); i++) {
                    String[] xyz = ((String) cordsList.get(i)).split(";");
                    checkpoints[i] = new Location(Bukkit.getWorld("parkour"),Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));
                }

                sender.sendPlainMessage("Checkpoint added!");
                return true;
            }

            if (args[0].equals("list")) {
                for (int i = 0; i < checkpoints.length; i++) {
                    sender.sendPlainMessage("ID: " + i + " X: " + checkpoints[i].getX() + " Y: " + checkpoints[i].getY() + " Z: " + checkpoints[i].getZ());
                }
                return true;
            }

            if (args[0].equals("show")) {
                if (showCheckpoints) {
                    showCheckpoints = false;
                } else {
                    showCheckpoints = true;
                }
                return true;
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
            if (tickCountSkywars == 20) {
                tickCountSkywars = 1;
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
                tickCountSkywars++;
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
        showCheck();
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

    public void showCheck() {
        if (showCheckpoints) {
            if (tickCountParkour == 20) {
                tickCountParkour = 1;
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 0, 255), 0.5F);
                World world = Bukkit.getWorld("parkour");
                for (Location location : checkpoints) {
                    Location cords = location;
                    cords.add(0,1.5,0);
                    world.spawnParticle(Particle.REDSTONE, cords, 50, 0.5, 0, 0.5, dustOptions);
                }
            } else {
                tickCountParkour++;
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
        saveConfig();
    }
}

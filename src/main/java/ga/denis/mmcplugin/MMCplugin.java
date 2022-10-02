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

public final class MMCplugin extends JavaPlugin implements Listener, CommandExecutor {

    boolean zoneOn = false;
    int zoneSize = 100;
    byte tickCountSkywars = 1;
    byte tickCountParkour = 1;
    FileConfiguration config;
    Location[] checkpoints;
    boolean showCheckpoints;
    String cordsString;

    @Override
    public void onEnable() {
        // Plugin startup logic
        config = this.getConfig();
        config.addDefault("zoneSize", 100);
        config.addDefault("zoneOn", false);
        config.addDefault("checkpoints", "");
        config.addDefault("showCheckpoints", false);
        config.options().copyDefaults(true);
        saveDefaultConfig();
        zoneOn = config.getBoolean("zoneOn");
        zoneSize = config.getInt("zoneSize");
        cordsString = config.getString("checkpoints");
        if (!cordsString.equals("")) {
            String[] cordsArray = cordsString.split(";");
            checkpoints = new Location[cordsArray.length];
            for (int i = 0; i < cordsArray.length; i++) {
                String[] xyz = cordsArray[i].split(",");
                checkpoints[i] = new Location(Bukkit.getWorld("parkour"),Double.parseDouble(xyz[0]),Double.parseDouble(xyz[1]),Double.parseDouble(xyz[2]));
            }
        }

        showCheckpoints = config.getBoolean("showCheckpoints");

        this.getCommand("skywars").setExecutor(this);
        this.getCommand("parkour").setExecutor(this);
        this.getCommand("ghostblock").setExecutor(this);
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
                /*if (cordsString.equals("")) {
                    config.set("checkpoints", hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ());
                } else {
                    config.set("checkpoints", config.getString("checkpoints") + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ());
                }*/
                Location[] cordsArray = new Location[checkpoints.length + 1];
                for (int i = 0; i < checkpoints.length; i++) {
                    cordsArray[i] = checkpoints[i];
                }
                checkpoints = cordsArray;
                checkpoints[checkpoints.length - 1] = new Location(Bukkit.getWorld("parkour"),hrac.getLocation().getX(),hrac.getLocation().getY(),hrac.getLocation().getZ());
                cordsString = checkpoints[0].getX() + "," + checkpoints[0].getY() + "," + checkpoints[0].getZ();
                for (int i = 1; i < checkpoints.length; i++) {
                    cordsString = cordsString + ";" + checkpoints[i].getX() + "," + checkpoints[i].getY() + "," + checkpoints[i].getZ();
                }
                //cordsString = cordsString + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ();
                config.set("checkpoints", cordsString);
                saveConfig();
                /*cordsString = config.getString("checkpoints");
                String[] cordsArray = cordsString.split(";");
                checkpoints = new Location[cordsArray.length];
                for (int i = 0; i < cordsArray.length; i++) {
                    String[] xyz = cordsArray[i].split(",");
                    checkpoints[i] = new Location(Bukkit.getWorld("parkour"),Double.parseDouble(xyz[0]),Double.parseDouble(xyz[1]),Double.parseDouble(xyz[2]));
                }*/

                sender.sendPlainMessage("Checkpoint added!");
                return true;
            }

            else if (args[0].equals("list")) {
                for (int i = 0; i < checkpoints.length; i++) {
                    sender.sendPlainMessage("ID: " + i + " X: " + checkpoints[i].getX() + " Y: " + checkpoints[i].getY() + " Z: " + checkpoints[i].getZ());
                }
                return true;
            }

            else if (args[0].equals("show")) {
                if (showCheckpoints) {
                    showCheckpoints = false;
                    sender.sendPlainMessage("Checkpoints hidden!");
                } else {
                    showCheckpoints = true;
                    sender.sendPlainMessage("Checkpoints shown!");
                }
                return true;
            }

            else if (args[0].equals("remove")) {
                if (args.length < 2) {
                    Player hrac = (Player) sender;
                    int closest = 0;
                    byte skip = 0;
                    for (int i = 1; i < checkpoints.length; i++) {
                        if (hrac.getLocation().distance(new Location(Bukkit.getWorld("parkour"),checkpoints[closest].getX(),checkpoints[closest].getY(),checkpoints[closest].getZ())) > hrac.getLocation().distance(new Location(Bukkit.getWorld("parkour"),checkpoints[i].getX(),checkpoints[i].getY(),checkpoints[i].getZ()))) {
                            closest = i;
                        }
                    }
                    Location[] cordsArray = new Location[checkpoints.length - 1];
                    for (int i = 0; i < cordsArray.length; i++) {
                        if (i == closest) {
                            skip = 1;
                        }
                        cordsArray[i] = checkpoints[i + skip];
                    }
                    checkpoints = cordsArray;
                    cordsString = checkpoints[0].getX() + "," + checkpoints[0].getY() + "," + checkpoints[0].getZ();
                    for (int i = 1; i < checkpoints.length; i++) {
                        cordsString = cordsString + ";" + checkpoints[i].getX() + "," + checkpoints[i].getY() + "," + checkpoints[i].getZ();
                    }
                    cordsString = cordsString + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ();
                    config.set("checkpoints", cordsString);
                    saveConfig();
                    sender.sendPlainMessage("Removed closest checkpoint!");
                    return true;
                }

                else {
                    Player hrac = (Player) sender;
                    byte skip = 0;
                    int id = Integer.parseInt(args[1]);
                    Location[] cordsArray = new Location[checkpoints.length - 1];
                    for (int i = 0; i < cordsArray.length; i++) {
                        if (i == id) {
                            skip = 1;
                        }
                        cordsArray[i] = checkpoints[i + skip];
                    }
                    checkpoints = cordsArray;
                    cordsString = checkpoints[0].getX() + "," + checkpoints[0].getY() + "," + checkpoints[0].getZ();
                    for (int i = 1; i < checkpoints.length; i++) {
                        cordsString = cordsString + ";" + checkpoints[i].getX() + "," + checkpoints[i].getY() + "," + checkpoints[i].getZ();
                    }
                    cordsString = cordsString + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ();
                    config.set("checkpoints", cordsString);
                    saveConfig();
                    sender.sendPlainMessage("Removed checkpoint number " + id +"!");
                    return true;
                }
            }
        } else if (command.getName().equals("ghostblock") && sender instanceof Player) {
            Player hrac = (Player) sender;
            Location lokace = hrac.getLocation();
            lokace.setY(lokace.getY() - 1);
            hrac.sendBlockChange(hrac.getLocation(), lokace.getBlock().getBlockData());
            sender.sendPlainMessage("Ghostblock placed :D");
            return true;
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
    public void onPlayerMove(PlayerMoveEvent event) {
        hit(event.getPlayer());
        checkpointReached(event.getPlayer());
//            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
//            Scoreboard scoreboard = event.getPlayer().getScoreboard();
//            Objective objective = scoreboard.getObjective("zone");
//            Score score = objective.getScore("skywars");
    }

    public void checkpointReached(Player hrac) {
        if (hrac.getWorld().getName().equals("skywars")) {

        }
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
                    Location cords = new Location(Bukkit.getWorld("parkour"), location.getX(), location.getY() + 1, location.getZ());
                    world.spawnParticle(Particle.REDSTONE, cords, 50, 0.5, 0, 0.5, dustOptions);
                    world.spawnParticle(Particle.REDSTONE, cords, 50, 0, 1, 0, dustOptions);
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

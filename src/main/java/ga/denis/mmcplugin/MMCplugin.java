package ga.denis.mmcplugin;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;

public final class MMCplugin extends JavaPlugin implements Listener, CommandExecutor {

    boolean zoneOn = false;
    int zoneSize = 100;
    byte tickCountSkywars = 1;
    byte tickCountParkour = 1;
    FileConfiguration config;
    Location[] checkpoints;
    boolean showCheckpoints;
    String cordsString;
    HashMap<String,boolean[]> checkMap;
    HashMap<String,Location> parkourRespawnMap;
    int parkourTPBvalue;
    boolean parkourTPBallowed;

    @Override
    public void onEnable() {
        // Plugin startup logic
        config = this.getConfig();
        config.addDefault("zoneSize", 100);
        config.addDefault("zoneOn", false);
        config.addDefault("checkpoints", "");
        config.addDefault("showCheckpoints", false);
        config.addDefault("parkourTPBvalue", 0);
        config.addDefault("parkourTPBallowed", false);
        //config.options().copyDefaults(true);
        saveDefaultConfig();
        zoneOn = config.getBoolean("zoneOn");
        zoneSize = config.getInt("zoneSize");
        cordsString = config.getString("checkpoints");
        parkourTPBvalue = config.getInt("parkourTPBvalue");
        parkourTPBallowed = config.getBoolean("parkourTPBallowed");
        checkMap = new HashMap<>();
        parkourRespawnMap = new HashMap<>();
        if (!cordsString.equals("")) {
            String[] cordsArray = cordsString.split(";");
            checkpoints = new Location[cordsArray.length];
            for (int i = 0; i < cordsArray.length; i++) {
                String[] xyz = cordsArray[i].split(",");
                checkpoints[i] = new Location(Bukkit.getWorld("parkour"),Double.parseDouble(xyz[0]),Double.parseDouble(xyz[1]),Double.parseDouble(xyz[2]), Float.parseFloat(xyz[3]), Float.parseFloat(xyz[4]));
            }
        } else {
            checkpoints = new Location[0];
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

                Location lokace = new Location(Bukkit.getWorld("parkour"),hrac.getLocation().getBlockX() + 0.5,hrac.getLocation().getBlockY(),hrac.getLocation().getBlockZ() + 0.5, hrac.getLocation().getYaw(), hrac.getLocation().getPitch());
                checkpoints[checkpoints.length - 1] = lokace;

//                lokace.getBlock().setType(Material.SEA_PICKLE);
//                SeaPickle blockData = (SeaPickle) lokace.getBlock().getBlockData();
//                blockData.setPickles(3);
//                lokace.getBlock().setBlockData(blockData);

                //lokace.getBlock().setBlockData(lokace.getBlock().getBlockData());
                cordsString = checkpoints[0].getX() + "," + checkpoints[0].getY() + "," + checkpoints[0].getZ() + "," + checkpoints[0].getYaw() + "," + checkpoints[0].getPitch();
                for (int i = 1; i < checkpoints.length; i++) {
                    cordsString = cordsString + ";" + checkpoints[i].getX() + "," + checkpoints[i].getY() + "," + checkpoints[i].getZ() + "," + checkpoints[i].getYaw() + "," + checkpoints[i].getPitch();
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
                Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
                for (Player player : hraci) {
                    generateHashmap(player);
                }
                return true;
            }

            else if (args[0].equals("list")) {
                for (int i = 0; i < checkpoints.length; i++) {
                    sender.sendPlainMessage("ID: " + i + " X: " + checkpoints[i].getX() + " Y: " + checkpoints[i].getY() + " Z: " + checkpoints[i].getZ());
                }
                generateHashmap((Player) sender);
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
                    if (checkpoints.length > 0) {
                        cordsString = checkpoints[0].getX() + "," + checkpoints[0].getY() + "," + checkpoints[0].getZ();
                    }
                    for (int i = 1; i < checkpoints.length; i++) {
                        cordsString = cordsString + ";" + checkpoints[i].getX() + "," + checkpoints[i].getY() + "," + checkpoints[i].getZ();
                    }
                    //xxxcordsString = cordsString + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ();
                    config.set("checkpoints", cordsString);
                    saveConfig();
                    sender.sendPlainMessage("Removed closest checkpoint!");
                    Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
                    for (Player player : hraci) {
                        generateHashmap(player);
                    }
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
                    //xxxcordsString = cordsString + ";" + hrac.getLocation().getX() + "," + hrac.getLocation().getY() + "," + hrac.getLocation().getZ();
                    config.set("checkpoints", cordsString);
                    saveConfig();
                    sender.sendPlainMessage("Removed checkpoint number " + id +"!");
                    return true;
                }
            } else if (args[0].equals("tp")) {
                if (args.length < 2) {
                    if (parkourTPBallowed) {
                        parkourTPBallowed = false;
                        config.set("parkourTPBallowed", false);
                        sender.sendPlainMessage("Parkour teleport back disabled!");
                    }
                    else {
                        parkourTPBallowed = true;
                        config.set("parkourTPBallowed", true);
                        sender.sendPlainMessage("Parkour teleport back enabled!");
                    }
                    return true;
                } else {
                    parkourTPBvalue = Integer.parseInt(args[1]);
                    sender.sendPlainMessage("Parkour teleport back value set to: " + parkourTPBvalue);
                    config.set("parkourTPBvalue", parkourTPBvalue);
                    return true;
                }
            } else if (args[0].equals("reset")) {
                boolean[] pointArray = new boolean[checkpoints.length];
                for (int i = 0; i < checkpoints.length; i++) {
                    pointArray[i] = true;
                }
                checkMap.put(sender.getName() + "Checkpoints", pointArray);
                parkourRespawnMap.put(sender.getName() + "ParkourRespawn", new Location(Bukkit.getWorld("parkour"),0,52,0));
                sender.sendPlainMessage("Checkpoints reset!");
                return true;
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

    @EventHandler
    public void join(PlayerJoinEvent event) {
        generateHashmap(event.getPlayer());
        generateRespawnMap(event.getPlayer());
    }

    @EventHandler
    public void changeWorld(PlayerChangedWorldEvent event) {
        generateHashmap(event.getPlayer());
        generateRespawnMap(event.getPlayer());
    }

    public void generateRespawnMap(Player player) {
        if (player.getWorld().getName().equals("parkour") && !parkourRespawnMap.containsKey(player.getName() + "ParkourRespawn")) {
            parkourRespawnMap.put(player.getName() + "ParkourRespawn", new Location(Bukkit.getWorld("parkour"),0,52,0));
        }
    }

    public void generateHashmap(Player player) {
        if (!checkMap.containsKey(player.getName() + "Checkpoints")) {
            boolean[] pointArray = new boolean[checkpoints.length];
            for (int i = 0; i < checkpoints.length; i++) {
                pointArray[i] = true;
            }
            checkMap.put(player.getName() + "Checkpoints", pointArray);
        }
        if (player.getWorld().getName().equals("parkour")) {
            boolean[] pointArray = checkMap.get(player.getName() + "Checkpoints");
            if (!(pointArray.length == checkpoints.length)) {
                pointArray = new boolean[checkpoints.length];
                for (int i = 0; i < checkpoints.length; i++) {
                    pointArray[i] = true;
                }
                checkMap.put(player.getName() + "Checkpoints", pointArray);
            }
            BlockData blockData = new Location(Bukkit.getWorld("parkour"),0,0 ,0).getBlock().getBlockData();
            for (int i = 0; i < checkpoints.length; i++) {
                if (pointArray[i]) {
                    //player.sendPlainMessage("setting blocks");
                    player.sendBlockChange(new Location(Bukkit.getWorld("parkour"), checkpoints[i].getX(), checkpoints[i].getY(), checkpoints[i].getZ()), blockData);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        BlockData blockData = new Location(Bukkit.getWorld("parkour"),0,0 ,0).getBlock().getBlockData();
        for (int i = 0; i < checkMap.get(player.getName() + "Checkpoints").length; i++) {
            if (checkMap.get(player.getName() + "Checkpoints")[i]) {
                //player.sendPlainMessage("setting blocks");
                player.sendBlockChange(new Location(Bukkit.getWorld("parkour"), checkpoints[i].getX(), checkpoints[i].getY(), checkpoints[i].getZ()), blockData);
            }
        }
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
        parkourTPB(event.getPlayer());
//            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
//            Scoreboard scoreboard = event.getPlayer().getScoreboard();
//            Objective objective = scoreboard.getObjective("zone");
//            Score score = objective.getScore("skywars");
    }

    public void parkourTPB(Player player) {
        if (parkourTPBallowed && player.getLocation().getBlockY() < parkourTPBvalue) {
            //player.setVelocity(new Vector(0,0,0));
            player.teleport(parkourRespawnMap.get(player.getName() + "ParkourRespawn"));
        }
    }

    public void checkpointReached(Player hrac) {
        if (hrac.getWorld().getName().equals("parkour")) {
            for (int i = 0; i < checkpoints.length; i++) {
                Location lokace = new Location(Bukkit.getWorld("parkour"), checkpoints[i].getX(), checkpoints[i].getY(), checkpoints[i].getZ(), checkpoints[i].getYaw(), checkpoints[i].getPitch());
                boolean[] cords = checkMap.get(hrac.getName() + "Checkpoints");
                if (lokace.distance(hrac.getLocation()) < 1.5) {
                    if (cords[i]) {
                        hrac.sendPlainMessage("checkpoint");
                        parkourRespawnMap.put(hrac.getName() + "ParkourRespawn", lokace);
                        cords[i] = false;
                        checkMap.put(hrac.getName() + "Checkpoints", cords);
                        hrac.playSound(hrac.getLocation(), Sound.BLOCK_GLASS_FALL, 1f, 1f);
                        Firework fw = hrac.getWorld().spawn(lokace, Firework.class);
                        FireworkMeta fm = fw.getFireworkMeta();
                        fm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).build());
                        fw.setFireworkMeta(fm);
                        fw.detonate();
                        hrac.sendBlockChange(lokace, hrac.getWorld().getBlockData(new Location(hrac.getWorld(), lokace.getX(), lokace.getY() + 1,lokace.getZ())));
                    }
                }
            }
        }
    }

    @EventHandler
    public void noFireworkDmg(EntityDamageByEntityEvent event) {
        if (event.getEntity().getWorld().getName().equals("parkour") && event.getDamager() instanceof Firework) {
            event.setCancelled(true);
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
    public void parkourFallDamage(EntityDamageEvent event) {
        if (event.getEntity().getWorld().getName().equals("parkour") && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
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
    public void onPlayerDeath(PlayerDeathEvent event) {
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

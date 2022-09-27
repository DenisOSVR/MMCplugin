package ga.denis.myfirstplugin;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Collection;

public final class MyFirstPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("The plugin has started!");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println("A player joined the server - yay! His name is: " + event.getPlayer().getName());
        event.joinMessage(Component.text("Bonjour " + event.getPlayer().getName()));
    }

    @EventHandler
    public void onLeavingArea(PlayerMoveEvent event) {
        if (event.getPlayer().getWorld().getName().equals("skywars")) {
            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = event.getPlayer().getScoreboard();
            Objective objective = scoreboard.getObjective("zone");
            Score score = objective.getScore("skywars");
            if (event.getPlayer().getLocation().getX() >= score.getScore()) {
                //event.getPlayer().damage(0);
                event.getPlayer().addScoreboardTag("hit");
            } else if (event.getPlayer().getScoreboardTags().contains("hit")) {
                event.getPlayer().removeScoreboardTag("hit");
            }
        }
    }

    @EventHandler
    public void onTickStart(ServerTickStartEvent event) {
        Collection<Player> hraci = (Collection<Player>) Bukkit.getOnlinePlayers();
        for (Player hrac : hraci) {
            if (hrac.getScoreboardTags().contains("hit")) {
                hrac.damage(3);
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

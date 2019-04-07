package me.ggamer55.scoreboard;

import com.google.common.base.Preconditions;
import me.ggamer55.scoreboard.entry.Entry;
import me.ggamer55.scoreboard.placeholder.JoinedPlaceholderResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ScoreboardApi extends JoinedPlaceholderResolver {

    private JavaPlugin plugin;

    private Map<UUID, ScoreboardObjective> scoreboardObjectives;
    private AtomicBoolean initialized;

    private BukkitTask updateScoreboardTask;
    private BukkitTask updateTitleTask;

    private List<Entry> defaultContent;

    private Entry defaultTitle;

    public ScoreboardApi(JavaPlugin plugin) {
        this.plugin = plugin;
        scoreboardObjectives = new ConcurrentHashMap<>();

        initialized = new AtomicBoolean();
        defaultContent = new CopyOnWriteArrayList<>();
    }

    public void init() {
        Preconditions.checkState(initialized.compareAndSet(false, true), "Scorebord API already initialized!");

        updateScoreboardTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllScoreboards, 1, 1);
        updateTitleTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllScoreboardsTitles, 1, 1);

        plugin.getServer().getPluginManager().registerEvents(new ScoreboardListener(), plugin);
    }

    public void disable() {
        Preconditions.checkState(initialized.compareAndSet(true, false), "Scorebord API not initialized!");
        updateScoreboardTask.cancel();
        updateTitleTask.cancel();

        scoreboardObjectives.forEach((uuid, scoreboardObjective) -> {
            scoreboardObjective.deleteScoreboard();
        });
        scoreboardObjectives.clear();
    }

    public Optional<ScoreboardObjective> getScoreboardObjective(Player player) {
        return Optional.ofNullable(scoreboardObjectives.get(player.getUniqueId()));
    }

    public ScoreboardObjective createScoreboardObjective(Player player) {
        Preconditions.checkState(!scoreboardObjectives.containsKey(player.getUniqueId()));

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);

        ScoreboardObjective objective = new ScoreboardObjective(player, scoreboard, this);

        scoreboardObjectives.put(player.getUniqueId(), objective);

        return objective;
    }

    public void removeScoreboardObjective(OfflinePlayer player) {
        Preconditions.checkState(scoreboardObjectives.containsKey(player.getUniqueId()));

        scoreboardObjectives.remove(player.getUniqueId()).deleteScoreboard();
    }

    public void setDefaultContent(List<Entry> defaultContent) {
        this.defaultContent = new CopyOnWriteArrayList<>(defaultContent);
    }

    public void setDefaultTitle(Entry title) {
        if (Objects.isNull(title) || title.equals(defaultTitle)) {
            return;
        }

        this.defaultTitle = title;
    }

    void updateAllScoreboards() {
        scoreboardObjectives.values().forEach(ScoreboardObjective::updateScoreboard);
    }

    void updateAllScoreboardsTitles() {
        scoreboardObjectives.values().forEach(ScoreboardObjective::updateTitle);
    }

    class ScoreboardListener implements Listener {

        @EventHandler
        public void scoreboardAdd(PlayerJoinEvent event) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                ScoreboardObjective objective = createScoreboardObjective(event.getPlayer());

                if (defaultTitle != null) {
                    objective.setTitle(defaultTitle.clone());
                }

                if (defaultContent != null) {
                    objective.setAllContent(defaultContent.stream().map(Entry::clone).collect(Collectors.toList()));
                }
            }, 1);


        }

        @EventHandler
        public void scoreboardRemove(PlayerQuitEvent event) {
            removeScoreboardObjective(event.getPlayer());
        }
    }

}

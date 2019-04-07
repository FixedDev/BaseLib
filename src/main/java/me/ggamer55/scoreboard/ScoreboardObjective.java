package me.ggamer55.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import gnu.trove.map.hash.TIntObjectHashMap;
import me.ggamer55.scoreboard.entry.Entry;
import me.ggamer55.scoreboard.entry.Frame;
import me.ggamer55.scoreboard.placeholder.JoinedPlaceholderResolver;
import me.ggamer55.scoreboard.placeholder.ListPlaceholderResolver;
import me.ggamer55.scoreboard.placeholder.PlaceholderResolver;
import me.ggamer55.scoreboard.placeholder.implementations.PlayerPlaceholderResolver;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.*;

public class ScoreboardObjective {
    private final Scoreboard scoreboard;

    private Set<Entry> entriesToBeRemoved = new HashSet<>();
    private TIntObjectHashMap<Entry> scoreboardContent = new TIntObjectHashMap<>();
    private Map<String, String> placeholderLastValues = new HashMap<>();

    private org.bukkit.scoreboard.Objective current;
    private DisplaySlot currentDisplaySlot;

    private Player player;

    private Entry title;

    private PlaceholderResolver placeholderResolver;

    ScoreboardObjective(Player player, Scoreboard scoreboard, @Nullable PlaceholderResolver placeholderResolver) {
        Preconditions.checkArgument(player.getScoreboard().equals(scoreboard), "The provided scoreboard must be the same scoreboard used by the provided player!");

        this.scoreboard = scoreboard;
        this.player = player;

        this.placeholderResolver = placeholderResolver;
        this.current = scoreboard.registerNewObjective("SbObj", "dummy");

        current.setDisplaySlot(DisplaySlot.SIDEBAR);
        currentDisplaySlot = DisplaySlot.SIDEBAR;

        String randomTitle = RandomStringUtils.random(5);

        title = Entry.of(randomTitle, 1, Collections.singletonList(randomTitle));

        if (placeholderResolver == null) {
            this.placeholderResolver = new JoinedPlaceholderResolver(Sets.newHashSet(new PlayerPlaceholderResolver()));
        } else {
            this.placeholderResolver = new JoinedPlaceholderResolver(Sets.newHashSet(placeholderResolver, new PlayerPlaceholderResolver()));
        }

    }

    public void setDisplaySlot(DisplaySlot displaySlot) {
        Preconditions.checkNotNull(displaySlot);
        if (currentDisplaySlot == null || displaySlot != currentDisplaySlot) {
            currentDisplaySlot = displaySlot;
            current.setDisplaySlot(displaySlot);
        }
    }

    public void setAllContent(List<Entry> content) {
        Validate.notNull(content);

        int i;

        if (content.size() <= scoreboardContent.size()) {
            for (i = content.size(); i < scoreboardContent.size(); i++) {
                entriesToBeRemoved.add(scoreboardContent.get(i));
                scoreboardContent.remove(i);
            }
        }

        i = 0;

        this.scoreboardContent.clear();

        for (Entry entry : content) {
            this.setLine(content.size() - i++, entry);
        }

    }

    public void setTitle(Entry title) {
        if (title == null || title.equals(this.title)) return;

        this.title = title;
    }

    public void setLine(int line, Entry entry) {
        Validate.notNull(entry);

        Entry value = scoreboardContent.get(line);

        if (!entry.equals(value) && value != null) {
            entriesToBeRemoved.add(entry);
            scoreboardContent.put(line, entry);
        } else if (value == null) {
            this.scoreboardContent.put(line, entry);
        }

    }

    public void updateTitle() {
        if (title == null || title.isEmpty()) {
            return;
        }

        title.tickEntry();

        if (title.getCurrentTick() % title.getUpdateTicks() > 0) {
            return;
        }

        Frame oldFrame = Frame.of(title.getCurrentFrame());
        Frame frame = Frame.of(title.getNextFrame());

        if (oldFrame.equals(frame) && title.isDisplayed()) {
            return;
        }

        title.setDisplayed(true);
        this.current.setDisplayName(frame.getPrefix() + frame.getName() + frame.getSuffix());
    }

    public void deleteScoreboard() {
        PlaceholderResolver lastValuesPlaceholderResolver = new ListPlaceholderResolver(placeholderLastValues);

        removeEntriesFromScoreboard(lastValuesPlaceholderResolver);

        placeholderLastValues.clear();

        current.unregister();

        entriesToBeRemoved.clear();
        scoreboardContent.clear();

        title = null;
    }

    public void updateScoreboard() {
        if (current == null || scoreboardContent.isEmpty()) {
            return;
        }

        Map<String, String> placeholderCurrentValues = placeholderResolver.getPlaceholdersValues(player);

        PlaceholderResolver lastValuesPlaceholderResolver = new ListPlaceholderResolver(placeholderLastValues);

        scoreboardContent.forEachEntry((index, entry) -> {
            if (entry == null) {
                return true;
            }

            entry.tickEntry();

            if (entry.getCurrentTick() % entry.getUpdateTicks() > 0 && entry.isDisplayed()) {
                return true;
            }

            String oldFrameString = entry.getCurrentFrame();
            String frameString = entry.getNextFrame();

            oldFrameString = lastValuesPlaceholderResolver.replacePlaceholders(player, oldFrameString);
            frameString = placeholderResolver.replacePlaceholders(player, frameString);

            Frame oldFrame = Frame.of(oldFrameString);
            Frame frame = Frame.of(frameString);

            if (oldFrame.equals(frame) && entry.isDisplayed()) {
                return true;
            }

            Team team = scoreboard.getTeam(entry.getEntryName());

            if (team == null) {
                team = scoreboard.registerNewTeam(entry.getEntryName());
            }

            // Team entry adding
            if (!team.hasEntry(frame.getName())) {
                team.addEntry(frame.getName());
            }

            team.setDisplayName(frame.getName());

            // Team Suffix and prefix adding
            team.setPrefix(frame.getPrefix());
            team.setSuffix(frame.getSuffix());

            // Team set score
            current.getScore(frame.getName()).setScore(index + 1);

            entry.setDisplayed(true);

            return true;
        });

        removeEntriesFromScoreboard(lastValuesPlaceholderResolver);
        placeholderLastValues = new HashMap<>(placeholderCurrentValues);
    }

    private void removeEntriesFromScoreboard(PlaceholderResolver lastValuesPlaceholderResolver){
        List<Entry> entriesBeingDeleted = new ArrayList<>(entriesToBeRemoved);
        entriesBeingDeleted.addAll(scoreboardContent.valueCollection());

        entriesBeingDeleted.forEach(entry -> {
            if (entry == null) {
                return;
            }

            String lastFrameString = entry.getLastFrame();
            String currentFrameString = entry.getCurrentFrame();

            lastFrameString = lastValuesPlaceholderResolver.replacePlaceholders(player, lastFrameString);
            currentFrameString = placeholderResolver.replacePlaceholders(player, currentFrameString);

            Frame lastFrame = Frame.of(lastFrameString);
            Frame currentFrame = Frame.of(currentFrameString);

            if (!entriesToBeRemoved.contains(entry)) {
                if (entry.getCurrentTick() % entry.getUpdateTicks() > 0 && entry.isDisplayed()) {
                    return;
                }

                if (lastFrame.equals(currentFrame)) {
                    return;
                }
            }

            Team team = scoreboard.getTeam(entry.getEntryName());

            if (team != null) {
                team.removeEntry(lastFrame.getName());
            }

            scoreboard.resetScores(lastFrame.getName());
        });
    }
}

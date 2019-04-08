

package me.fixeddev.scoreboard.entry;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.beans.ConstructorProperties;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Getter
@EqualsAndHashCode
public class Entry {
    private String entryName;

    @Setter
    private int updateTicks;
    private Deque<String> frames;

    @Getter
    private volatile long currentTick;

    @Getter
    @Setter
    private volatile boolean displayed;

    private Entry(String entryName, int updateTicks, String[] entries) {
        this.entryName = entryName;
        this.updateTicks = updateTicks;
        this.frames = new LinkedList<>();

        for (String entry : entries) {
            if (entry == null) {
                frames.offer("");
                continue;
            }

            frames.offer(entry);
        }

    }

    private Entry(String entryName, int updateTicks, List<String> entries) {
        this.entryName = entryName;
        this.updateTicks = updateTicks;
        this.frames = new LinkedList<>(entries);
    }

    @ConstructorProperties({"entryName", "updateTicks", "frames"})
    private Entry(String entryName, int updateTicks, Deque<String> entries) {
        this.entryName = entryName;
        this.updateTicks = updateTicks;
        if(entries == null){
            this.frames = new LinkedList<>();
        } else {
            this.frames = new LinkedList<>(entries);
        }
    }

    public static Entry of(String entryName, int updateTicks, List<String> entries) {
        return new Entry(entryName, updateTicks, entries);
    }

    public static Entry of(String entryName, int updateTicks, Deque<String> entries) {
        return new Entry(entryName, updateTicks, entries);
    }

    public static Entry of(String entryName, int updateTicks, String[] entries) {
        return new Entry(entryName, updateTicks, entries);
    }

    public String getLastFrame() {
        return frames.getLast();
    }

    public String getNextFrame() {
        frames.offer(frames.poll());
        return frames.getFirst();
    }

    public String getCurrentFrame() {
        return frames.getFirst();
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public long tickEntry() {
        long actualTick = currentTick;
        actualTick++;
        currentTick = actualTick;

        return currentTick;
    }

    @Override
    public Entry clone() {
        return Entry.of(entryName, updateTicks, frames);
    }
}

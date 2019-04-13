package me.fixeddev.nametag;

import org.jetbrains.annotations.NotNull;

public class SimpleTag implements Tag {
    private @NotNull String prefix;
    private @NotNull String name;
    private @NotNull String suffix;

    public SimpleTag(@NotNull String prefix, @NotNull String name, @NotNull String suffix) {
        this.prefix = prefix;
        this.name = name;
        this.suffix = suffix;
    }

    public SimpleTag(@NotNull String name) {
        prefix = "";
        this.name = name;
        suffix = "";
    }


    @NotNull
    @Override
    public String getPrefix() {
        return prefix;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getSuffix() {
        return suffix;
    }
}

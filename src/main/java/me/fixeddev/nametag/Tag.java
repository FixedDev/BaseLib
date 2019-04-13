package me.fixeddev.nametag;

import org.jetbrains.annotations.NotNull;

public interface Tag {
    @NotNull
    String getPrefix();

    @NotNull
    String getName();

    @NotNull
    String getSuffix();

    default boolean hasPrefix() {
        return !getPrefix().isEmpty();
    }

    default boolean hasSuffix() {
        return !getSuffix().isEmpty();
    }
}


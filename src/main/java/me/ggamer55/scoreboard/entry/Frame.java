package me.ggamer55.scoreboard.entry;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
@Getter
public class Frame {
    private String prefix;
    private String name;
    private String suffix;

    public static Frame of(String string) {
        Preconditions.checkArgument(Objects.nonNull(string), "The frame content can't be null!");

        if (string.length() < 16) {
            return of("", string, "");
        } else if (string.length() >= 16 && string.length() < 32) {
            return of(string.substring(0, 15), string.substring(15), "");
        } else if (string.length() >= 32 && string.length() < 48) {
            return of(string.substring(0, 15), string.substring(15, 31), string.substring(31));
        }

        return of(string.substring(47));
    }
}

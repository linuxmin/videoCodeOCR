package at.javaprofi.ocr.parsing.api.enums;

import java.util.Arrays;

public enum ColorForTime
{
    WHITE(0L, 0L),
    YELLOW(1L, 29999L),
    ORANGE(30000L, 59999L),
    RED(60000L, 89999L),
    GREEN(90000L, 119999L),
    PINK(120000L, 149999L),
    PURPLE(150000L, 179999L),
    BLUE(180000L, Long.MAX_VALUE);

    final long startDuration;
    final long endDuration;

    ColorForTime(long startDuration, long endDuration)
    {
        this.startDuration = startDuration;
        this.endDuration = endDuration;
    }

    public static ColorForTime getColorForCurrentDuration(long currentDuration)
    {
        return Arrays.stream(values())
            .filter(color -> currentDuration >= color.startDuration && currentDuration <= color.endDuration)
            .findFirst()
            .orElse(ColorForTime.WHITE);
    }
}

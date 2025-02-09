package dk.martinersej.plugin.utils;

public class TimeUtils {

    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, secs);
        } else {
            return formatTimeSeconds(secs);
        }
    }

    public static String formatTimeSeconds(int seconds) {
        return String.format("%02ds", seconds);
    }
}

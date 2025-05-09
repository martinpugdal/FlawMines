package dk.martinersej.plugin.utils;

import dk.martinersej.plugin.config.Configs;
import dk.martinersej.plugin.config.Messages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeUtils {

    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String dateFormat;
        if (hours == 0 && minutes == 0) {
            dateFormat = Configs.TIME_FORMAT_WITHOUT_MINUTES.get();
        } else if (hours == 0) {
            dateFormat = Configs.TIME_FORMAT_WITHOUT_HOURS.get();
        } else {
            dateFormat = Configs.TIME_FORMAT.get();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, secs);

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        String timeText = sdf.format(calendar.getTime());
        if (hours == 0 && minutes == 0) {
            String secondText = seconds == 1 ? Messages.SECOND.get() : Messages.SECONDS.get();
            String newText = Messages.TIME_FORMAT_FOR_SECONDS.getWithPlaceholders(timeText,secondText);
            if (!newText.trim().isEmpty())
                timeText = newText;
        }
        return timeText;
    }
}

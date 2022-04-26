package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A cool ASCII progress bar for ffmpeg
 *
 * [=======>         ] XX% UUU/VVVmib/s ETA
 */
public class ProgressBar {

    private static final ASDLLogger LOGGER = Loggers.getLogger(ProgressBar.class);

    private int progressBarLength = 20; // doesn't count '[' and ']'

    private int duration = - 1;

    private int lastTime = -1;
    private int lastSize = -1;

    private int currTime;
    private int currSize;

    public ProgressBar() {

    }

    public void update(String ffmpegOutput) {
        if (ffmpegOutput.startsWith("  Duration")) {
            duration = getDuration(ffmpegOutput);
        } else if (ffmpegOutput.startsWith("frame")) {
            parseFrame(ffmpegOutput);

            LOGGER.info(createProgressBar());
            lastSize = currSize;
            lastTime = currTime;
        } else if (ffmpegOutput.startsWith("video")) {
            currTime = duration;

            LOGGER.info(createProgressBar());
        }
    }

    protected int getDuration(String ffmpegOutput) {
        Pattern pattern = Pattern.compile("(\\d\\d):(\\d\\d):(\\d\\d).(\\d\\d)");
        Matcher matcher = pattern.matcher(ffmpegOutput);

        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            int second = Integer.parseInt(matcher.group(3));
            int centi = Integer.parseInt(matcher.group(4));

            return toMillis(hour, minute, second, centi);
        } else {
            return 0;
        }
    }

    protected void parseFrame(String ffmpegOutput) {
        Pattern pattern = Pattern.compile("size=\\W*(\\d*)(\\w*) time=(\\d\\d):(\\d\\d):(\\d\\d).(\\d\\d)");
        Matcher matcher = pattern.matcher(ffmpegOutput);

        if (matcher.find()) {
            int size = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            int hour = Integer.parseInt(matcher.group(3));
            int minute = Integer.parseInt(matcher.group(4));
            int second = Integer.parseInt(matcher.group(5));
            int centi = Integer.parseInt(matcher.group(6));

            currTime = centi * 10 + 1000 * (second + 60 * (minute + 60 * hour));

            switch (unit) {
                case "kB" -> currSize = size * 1024;
                case "mB" -> currSize = size * 1024 * 1024;
                default -> currSize = size;
            }
        }
    }

    protected int toMillis(int hour, int minute, int second, int centi) {
        return centi * 10 + 1000 * (second + 60 * (minute + 60 * hour));
    }

    protected String createProgressBar() {
        StringBuilder b = new StringBuilder();
        b.append('\r');
        addProgressBar(b);

        // in b/s
        int speed = currSize - lastSize;
        b.append(' ').append(Utils.bytesHumanReadable(speed, 2)).append("/s");

        // in s
        long eta = calculateETA(speed);
        b.append(" ETA ").append(Utils.timeHumanReadable(eta, 2));

        return b.toString();
    }


    private void addProgressBar(StringBuilder b) {
        double ratio = (double) currTime / duration;
        int length = (int) (Math.round(progressBarLength * ratio));

        b.append(Utils.colorStart(Utils.ANSI_PURPLE));
        b.append('[');
        if (length - 1 > 0) {
            b.append("=".repeat(length - 1));
        }
        if (length < progressBarLength) {
            b.append(">");

            if (length + 1 < progressBarLength) {
                b.append(" ".repeat(Math.max(0, progressBarLength - (length + 1))));
            }
        }

        b.append(']');
        b.append(Utils.colorEnd());

        int percent = (int) (Math.round(100 * ratio));
        b.append(" ").append(percent).append("%");
    }

    private long calculateETA(int speed) {
        if (speed == 0) {
            if (duration == currTime) {
                return 0;
            } else {
                return Long.MAX_VALUE;
            }
        } else {
            return (duration - currTime) / (currTime - lastTime);
        }
    }
}

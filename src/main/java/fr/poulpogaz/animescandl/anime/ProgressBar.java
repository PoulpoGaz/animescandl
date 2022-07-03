package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.utils.CircularQueue;
import fr.poulpogaz.animescandl.utils.Utils;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * A cool ASCII progress bar for ffmpeg
 *
 * [=======>         ] XX% YYY/ZZZmb/s ETA WWWs
 */
public class ProgressBar {

    private static final ASDLLogger LOGGER = Loggers.getLogger(ProgressBar.class);

    private int progressBarLength = 20; // doesn't count '[' and ']'

    private CircularQueue<Integer> sizes = new CircularQueue<>(10);

    // The two variables are relative to the video
    private int videoDuration = - 1;
    private CircularQueue<Integer> times = new CircularQueue<>(10);

    public ProgressBar() {

    }

    public void update(String ffmpegOutput) {
        if (ffmpegOutput.startsWith("  Duration")) {
            videoDuration = getDuration(ffmpegOutput);
        } else if (ffmpegOutput.startsWith("frame")) {
            parseFrame(ffmpegOutput);

            LOGGER.info(createProgressBar());
        } else if (ffmpegOutput.startsWith("video")) {
            times.offer(videoDuration);
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

            int t = centi * 10 + 1000 * (second + 60 * (minute + 60 * hour));
            times.offer(t);

            int currSize = switch (unit) {
                case "kB" -> size * 1024;
                case "mB" -> size * 1024 * 1024;
                default -> size;
            };
            sizes.offer(currSize);
        }
    }

    protected int toMillis(int hour, int minute, int second, int centi) {
        return centi * 10 + 1000 * (second + 60 * (minute + 60 * hour));
    }

    protected String createProgressBar() {
        if (times.size() == 0 || sizes.size() == 0) {
            return "Unknown status";
        }

        StringBuilder b = new StringBuilder();
        b.append('\r');
        addProgressBar(b);

        // in b/s
        int speed = approxSpeed();
        b.append(' ').append(Utils.bytesHumanReadable(speed, 2)).append("/s");

        // in s
        long eta = approxETA();
        b.append(" ETA ").append(Utils.timeHumanReadable(eta, 2));

        return b.toString();
    }


    private void addProgressBar(StringBuilder b) {
        double ratio = (double) times.tail() / videoDuration;
        int length = (int) (Math.round(progressBarLength * ratio));

        b.append(ansi().fg(MAGENTA));
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
        b.append(ansi().reset());

        int percent = (int) (Math.round(100 * ratio));
        b.append(" ").append(percent).append("%");
    }

    private long approxETA() {
        int s = times.size();

        if (s == 0) {
            return Long.MAX_VALUE;
        } else if (s == 1) {
            return (videoDuration - times.peek()) / times.peek();
        } else {
            return s * (videoDuration - times.tail()) / (times.tail() - times.peek());
        }
    }

    private int approxSpeed() {
        int s = sizes.size();

        if (s == 0) {
            return 0;
        } else if (s == 1) {
            return sizes.peek();
        } else {
            return (sizes.tail() - sizes.peek()) / s;
        }
    }

    public int getProgressBarLength() {
        return progressBarLength;
    }

    public void setProgressBarLength(int progressBarLength) {
        this.progressBarLength = progressBarLength;
    }
}

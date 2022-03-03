package fr.poulpogaz.animescandl.utils;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.Video;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class M3U8Downloader {

    private static final Logger LOGGER = LogManager.getLogger(M3U8Downloader.class);

    private static final String FFMPEG_HEADER =
            """
            $'User-Agent: %s\r
            authority: %s\r
            scheme: %s\r
            path: %s\r
            accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r
            accept-encoding: gzip, deflate, br\r
            accept-language: fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7
            """;

    public static void download(Video video, String dst) throws IOException {
        ProcessBuilder builder;
        if (video.subtile() == null) {
            builder = buildFFMPEGProcess(video, dst);
        } else {
            builder = buildFFMPEGProcessWithSubtitle(video, dst);
        }

        Process process = builder.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = br.readLine()) != null) {
            if (Main.verbose.isPresent()) {
                if (line.startsWith("frame=")) {
                    LOGGER.info(line);
                } else {
                    LOGGER.debug(line);
                }
            } else {
                LOGGER.debug(line);
                if (line.startsWith("frame")) {
                    System.out.print("\r" + line);
                }
            }
        }

        if (Main.verbose.isNotPresent()) {
            System.out.println();
        }
    }

    private static ProcessBuilder buildFFMPEGProcess(Video video, String dst) {
        ProcessBuilder builder = new ProcessBuilder();
        command(builder,
                getFFMPEGPath(),
                "-protocol_whitelist", "file,http,https,tcp,tls",
                "-http_multiple", "0",
                "-headers", getHeader(video.file()),
                "-i", video.file(),
                "-c", "copy",
                dst);

        return builder;
    }

    private static ProcessBuilder buildFFMPEGProcessWithSubtitle(Video video, String dst) {
        ProcessBuilder builder = new ProcessBuilder();
        command(builder,
                getFFMPEGPath(),
                "-protocol_whitelist", "file,http,https,tcp,tls",
                "-http_multiple", "0",
                "-headers", getHeader(video.file()),
                "-i", video.file(),
                "-headers", getHeader(video.subtile()),
                "-i", video.subtile(),
                "-c", "copy",
                "-c:s", "mov_text",
                dst);

        return builder;
    }

    private static String getHeader(String url) {
        URI uri = URI.create(url);
        return FFMPEG_HEADER.formatted(FakeUserAgent.getUserAgent(), uri.getRawAuthority(), uri.getScheme(), uri.getRawPath());
    }

    private static void command(ProcessBuilder builder, String... args) {
        List<String> commands = builder.command();
        commands.addAll(Arrays.asList(args));
    }

    private static String getFFMPEGPath() {
        return Main.ffmpeg.getArgument(0).orElse("ffmpeg");
    }
}
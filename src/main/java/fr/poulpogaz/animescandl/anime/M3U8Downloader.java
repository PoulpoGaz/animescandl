package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.Main;
import fr.poulpogaz.animescandl.model.Source;
import fr.poulpogaz.animescandl.utils.FakeUserAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class M3U8Downloader {

    public static final String FORMAT = "m3u8";
    private static final Logger LOGGER = LogManager.getLogger(M3U8Downloader.class);

    // authority: %s
    // scheme: %s
    // path: %s

    private static final String FFMPEG_HEADER =
            """
            $'User-Agent: %s\r
            accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r
            accept-encoding: gzip, deflate, br\r
            accept-language: fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7
            """;

    public static void download(Source source, String dst) throws IOException {
        ProcessBuilder builder;
        if (source.getSubtitleURL().isPresent()) {
            builder = buildFFMPEGProcessWithSubtitle(source, dst);
        } else {
            builder = buildFFMPEGProcess(source, dst);
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

    private static ProcessBuilder buildFFMPEGProcess(Source source, String dst) {
        ProcessBuilder builder = new ProcessBuilder();
        command(builder,
                getFFMPEGPath(),
                "-protocol_whitelist", "file,http,https,tcp,tls",
                "-http_multiple", "0",
                "-headers", getHeader(source.getUrl()),
                "-i", source.getUrl(),
                "-c", "copy",
                dst);

        return builder;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static ProcessBuilder buildFFMPEGProcessWithSubtitle(Source source, String dst) {
        ProcessBuilder builder = new ProcessBuilder();
        command(builder,
                getFFMPEGPath(),
                "-protocol_whitelist", "file,http,https,tcp,tls",
                "-http_multiple", "0",
                "-headers", getHeader(source.getUrl()),
                "-i", source.getUrl(),
                "-headers", getHeader(source.getSubtitleURL().get()),
                "-i", source.getSubtitleURL().get(),
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
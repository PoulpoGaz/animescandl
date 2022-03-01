package fr.poulpogaz.animescandl.extractors;

import fr.poulpogaz.animescandl.Video;
import fr.poulpogaz.animescandl.utils.ExtractorException;
import fr.poulpogaz.animescandl.utils.IRequestSender;

import java.io.IOException;
import java.util.List;

public interface IExtractor {

    String url();

    List<Video> extract(IRequestSender s, String url) throws ExtractorException, IOException, InterruptedException;
}

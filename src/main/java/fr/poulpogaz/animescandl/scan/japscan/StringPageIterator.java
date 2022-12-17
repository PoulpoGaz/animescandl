package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;

import java.io.IOException;

public class StringPageIterator extends BaseIterator<String> {

    public StringPageIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException, JsonException {
        super(japscan, chapter);
    }

    @Override
    public String next() throws IOException, InterruptedException, WebsiteException {
        String url = nextURL();

        index++;
        return url;
    }
}

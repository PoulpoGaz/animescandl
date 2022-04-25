package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;

public class StringPageIterator extends BaseIterator<String> {

    public StringPageIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException, WebsiteException {
        super(japscan, chapter);
    }

    @Override
    public String next() throws IOException, InterruptedException, WebsiteException {
        String decrypted = nextURL();

        index++;
        return decrypted;
    }
}

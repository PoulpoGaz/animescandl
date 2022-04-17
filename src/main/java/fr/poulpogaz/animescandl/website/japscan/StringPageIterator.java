package fr.poulpogaz.animescandl.website.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;

public class StringPageIterator extends BaseIterator<String> {

    public StringPageIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException {
        super(japscan, chapter);
    }

    @Override
    public String next() throws IOException, InterruptedException, WebsiteException {
        String decrypted = nextURL();

        if (!HttpUtils.exists(japscan, decrypted)) {
            loadPages();

            Pair<String, String> pair = pages.get(index);
            decrypted = decrypt(pair.right());

            if (!HttpUtils.exists(japscan, decrypted)) {
                throw new WebsiteException("Can't decrypt url");
            }
        }

        index++;
        return decrypted;
    }
}
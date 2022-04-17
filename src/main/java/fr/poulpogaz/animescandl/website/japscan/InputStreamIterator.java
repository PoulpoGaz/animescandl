package fr.poulpogaz.animescandl.website.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.utils.HttpResponseDecoded;
import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class InputStreamIterator extends BaseIterator<InputStream> {

    public InputStreamIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException {
        super(japscan, chapter);
    }

    @Override
    public InputStream next() throws IOException, InterruptedException, WebsiteException {
        String decrypted = nextURL();

        HttpResponseDecoded response = japscan.GET(decrypted);

        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            loadPages();

            Pair<String, String> pair = pages.get(index);
            decrypted = decrypt(pair.right());

            return japscan.getInputStream(decrypted);
        }

        index++;
        return response.body();
    }
}

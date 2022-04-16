package fr.poulpogaz.animescandl.website.iterators;

import fr.poulpogaz.animescandl.utils.HttpUtils;
import fr.poulpogaz.animescandl.utils.IRequestSender;
import fr.poulpogaz.animescandl.website.WebsiteException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public class InputStreamPageIterator implements PageIterator<InputStream> {

    private final PageIterator<String> urlIterator;
    private final IRequestSender sender;

    public InputStreamPageIterator(PageIterator<String> urlIterator, IRequestSender sender) {
        this.urlIterator = Objects.requireNonNull(urlIterator);
        this.sender = Objects.requireNonNullElse(sender, HttpUtils.STANDARD);
    }

    @Override
    public boolean hasNext() {
        return urlIterator.hasNext();
    }

    @Override
    public InputStream next() throws IOException, InterruptedException, WebsiteException {
        String next = urlIterator.next();

        return sender.getInputStream(next);
    }

    @Override
    public Optional<Integer> nPages() {
        return urlIterator.nPages();
    }
}

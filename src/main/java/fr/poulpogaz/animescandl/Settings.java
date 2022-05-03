package fr.poulpogaz.animescandl;

import fr.poulpogaz.animescandl.utils.math.Interval;
import fr.poulpogaz.animescandl.utils.math.Set;

import java.nio.file.Path;

public record Settings(Set range, boolean concatenateAll, String language, Path out) {

    public boolean rangeContains(float n) {
        return range.contains(n);
    }

    public static class Builder {
        private Set range = Interval.all();
        private boolean concatenateAll = false;
        private String language = null;
        private Path out = Path.of("");

        public Settings build() {
            return new Settings(range, concatenateAll, language, out);
        }

        public Set getRange() {
            return range;
        }

        public Builder setRange(Set range) {
            this.range = range;
            return this;
        }

        public boolean isConcatenateAll() {
            return concatenateAll;
        }

        public Builder setConcatenateAll(boolean concatenateAll) {
            this.concatenateAll = concatenateAll;
            return this;
        }

        public String getLanguage() {
            return language;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Path getOut() {
            return out;
        }

        public Builder setOut(Path out) {
            this.out = out;
            return this;
        }
    }
}
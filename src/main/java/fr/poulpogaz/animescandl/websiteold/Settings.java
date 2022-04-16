package fr.poulpogaz.animescandl.websiteold;

import java.nio.file.Path;

public record Settings(String name, int[] range, boolean concatenateAll, String language, Path out) {

    public boolean rangeContains(int n) {
        if (range == null) {
            return true;
        }

        for (int j : range) {
            if (j == n) {
                return true;
            }
        }
        return false;
    }

    public static class Builder {
        private String name = null;
        private int[] range = null;
        private boolean concatenateAll = false;
        private String language = null;
        private Path out = null;

        public Settings build() {
            if (name == null) {
                return null;
            }

            return new Settings(name, range, concatenateAll, language, out);
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public int[] getRange() {
            return range;
        }

        public Builder setRange(int[] range) {
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
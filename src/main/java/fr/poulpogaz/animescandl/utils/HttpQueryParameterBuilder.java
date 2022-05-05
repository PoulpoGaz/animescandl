package fr.poulpogaz.animescandl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpQueryParameterBuilder {

    private static final Appender DEFAULT = new Appender() {
        @Override
        public void append(StringBuilder out, String paramName, String b, int index, int size) {
            out.append(paramName).append("=").append(b);

            if (index + 1 < size) {
                out.append("&");
            }
        }
    };

    private final HashMap<String, List<String>> parameters = new HashMap<>();
    private Appender appender = DEFAULT;

    public String build() {
        StringBuilder sb = new StringBuilder();
        append(sb);

        return sb.toString();
    }

    public void append(StringBuilder sb) {
        int i = 0;
        for (Map.Entry<String, List<String>> param : parameters.entrySet()) {
            String name = param.getKey();
            List<String> values = param.getValue();

            appender.before(sb, name);
            for (int j = 0; j < values.size(); j++) {
                appender.append(sb, name, values.get(j), j, values.size());
            }
            appender.after(sb, name);

            if (i + 1 < parameters.size()) {
                sb.append("&");
            }

            i++;
        }
    }

    public void add(String parameter, String value) {
        List<String> values = Utils.getOrCreate(parameters, parameter, ArrayList::new);
        values.add(value);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    public Appender getAppender() {
        return appender;
    }

    public HttpQueryParameterBuilder setAppender(Appender appender) {
        if (appender == null) {
            this.appender = DEFAULT;
        } else {
            this.appender = appender;
        }

        return this;
    }

    @FunctionalInterface
    public interface Appender {

        default void before(StringBuilder out, String paramName) {}

        void append(StringBuilder out, String paramName, String b, int index, int size);

        default void after(StringBuilder out, String paramName) {}
    }
}

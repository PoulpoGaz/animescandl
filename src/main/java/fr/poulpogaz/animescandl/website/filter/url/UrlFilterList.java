package fr.poulpogaz.animescandl.website.filter.url;

import fr.poulpogaz.animescandl.utils.HttpQueryParameterBuilder;
import fr.poulpogaz.animescandl.website.filter.*;

import java.util.ArrayList;
import java.util.List;

public class UrlFilterList extends FilterList {

    public UrlFilterList() {
    }

    public UrlFilterList(List<Filter<?>> filters) {
        super(filters);

        for (Filter<?> f : filters) {
            if (!(f instanceof UrlFilter)) {
                throw new IllegalArgumentException("Not an UrlFilter");
            }
        }
    }

    public String createRequest(String baseURL, HttpQueryParameterBuilder b) {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL);

        for (Filter<?> f : getFilters()) {
            if (f instanceof UrlGroup group) {
                addGroup(b, group);
            } else {
                UrlFilter urlFilter = (UrlFilter) f;
                add(b, urlFilter, null);
            }
        }

        if (!b.isEmpty()) {
            sb.append("?");
            b.append(sb);
        }

        return sb.toString();
    }

    private void addGroup(HttpQueryParameterBuilder b, UrlGroup group) {
        for (Filter<?> f : getFilters()) {
            UrlFilter urlFilter = (UrlFilter) f;
            add(b, urlFilter, group.getQueryName());
        }
    }

    protected void add(HttpQueryParameterBuilder b, UrlFilter filter, String groupQueryName) {
        String name = filter.getQueryName();
        String arg = filter.getQueryArgument();

        if (arg != null) {
            if (name != null) {
                b.add(name, arg);
            } else if (groupQueryName != null) {
                b.add(groupQueryName, arg);
            }
        }
    }


    public static class Builder extends BaseBuilder<Builder> {

        public FilterList build() {
            return new FilterList(filters);
        }

        public GroupBuilder group(String name) {
            return new GroupBuilder(this, name);
        }

        public GroupBuilder group(String name, String queryName) {
            return new GroupBuilder(this, name, queryName);
        }

        @Override
        protected Builder getTHIS() {
            return this;
        }
    }

    public static class GroupBuilder extends BaseBuilder<GroupBuilder> {

        private final Builder ancestor;
        private String name;
        private String queryName;

        public GroupBuilder(Builder ancestor, String name) {
            this.ancestor = ancestor;
            this.name = name;
        }

        public GroupBuilder(Builder ancestor, String name, String queryName) {
            this.ancestor = ancestor;
            this.name = name;
            this.queryName = queryName;
        }

        public Builder build() {
            return ancestor.addFilter(new UrlGroup(name, filters, queryName));
        }

        public String getName() {
            return name;
        }

        public GroupBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public String getQueryName() {
            return queryName;
        }

        public GroupBuilder setQueryName(String queryName) {
            this.queryName = queryName;
            return this;
        }

        @Override
        protected GroupBuilder getTHIS() {
            return this;
        }
    }

    private static abstract class BaseBuilder<THIS extends BaseBuilder<THIS>> {

        protected final List<Filter<?>> filters = new ArrayList<>();

        protected abstract THIS getTHIS();

        public THIS triStateCheckBox(String name) {
            filters.add(new TriStateCheckBox(name));
            return getTHIS();
        }

        public THIS triStateCheckBox(String name, String selected, String excluded) {
            return triStateCheckBox(name, null, null, selected, excluded);
        }

        public THIS triStateCheckBox(String name, String queryName, String selected, String excluded) {
            return triStateCheckBox(name, queryName, null, selected, excluded);
        }

        public THIS triStateCheckBox(String name, String queryName, String unselected, String selected, String excluded) {
            filters.add(new UrlTriStateCheckBox(name, queryName, unselected, selected, excluded));
            return getTHIS();
        }

        public THIS checkBox(String name) {
            return checkBox(name, null, null, null);
        }

        public THIS checkBox(String name, String selected) {
            return checkBox(name, null, null, selected);
        }

        public THIS checkBox(String name, String queryName, String selected) {
            return checkBox(name, queryName, null, selected);
        }

        public THIS checkBox(String name, String queryName, String unselected, String selected) {
            filters.add(new UrlCheckBox(name, queryName, unselected, selected));
            return getTHIS();
        }

        public THIS text(String name) {
            return text(name, null);
        }

        public THIS text(String name, String queryName) {
            filters.add(new UrlText(name, queryName));
            return getTHIS();
        }

        public <T2> SelectBuilder<T2, THIS> select(String name) {
            return select(name, null);
        }

        public <T2> SelectBuilder<T2, THIS> select(String name, String queryName) {
            return new SelectBuilder<>(getTHIS(), name, queryName);
        }

        public THIS addFilter(Filter<?> filter) {
            filters.add(filter);
            return getTHIS();
        }
    }


    public static class SelectBuilder<T, B extends BaseBuilder<B>> {

        private final B ancestor;
        private String name;
        private String queryName;
        private List<T> values;
        private List<String> arguments;

        private SelectBuilder(B ancestor, String name) {
            this.ancestor = ancestor;
            this.name = name;
            values = new ArrayList<>();
            arguments = new ArrayList<>();
        }

        public SelectBuilder(B ancestor, String name, String queryName) {
            this.ancestor = ancestor;
            this.name = name;
            this.queryName = queryName;
        }

        public SelectBuilder<T, B> addArg(String arg) {
            arguments.add(arg);
            return this;
        }

        public SelectBuilder<T, B> addVal(T val) {
            values.add(val);
            return this;
        }

        public SelectBuilder<T, B> add(T val, String arg) {
            values.add(val);
            arguments.add(arg);
            return this;
        }

        public List<T> getValues() {
            return values;
        }

        public SelectBuilder<T, B> setValues(List<T> values) {
            this.values = values;
            return this;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public SelectBuilder<T, B> setArguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public String getName() {
            return name;
        }

        public SelectBuilder<T, B> setName(String name) {
            this.name = name;
            return this;
        }

        public String getQueryName() {
            return queryName;
        }

        public SelectBuilder<T, B> setQueryName(String queryName) {
            this.queryName = queryName;
            return this;
        }

        public B build() {
            return ancestor.addFilter(new Select<>(name, values));
        }
    }
}

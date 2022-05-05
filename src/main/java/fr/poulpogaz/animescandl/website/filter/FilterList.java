package fr.poulpogaz.animescandl.website.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterList {

    protected final List<Filter<?>> filters;
    protected int limit = 10;

    public FilterList() {
        this(List.of());
    }

    public FilterList(List<Filter<?>> filters) {
        this.filters = Collections.unmodifiableList(filters);
    }

    public Filter<?> get(int index) {
        return filters.get(index);
    }

    public int getLimit() {
        return limit;
    }

    public FilterList setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public List<Filter<?>> getFilters() {
        return filters;
    }

    public static class Builder extends BaseBuilder<Builder> {

        public FilterList build() {
            return new FilterList(filters);
        }

        public GroupBuilder group(String name) {
            return new GroupBuilder(this, name);
        }

        @Override
        protected Builder getTHIS() {
            return this;
        }
    }

    public static class GroupBuilder extends BaseBuilder<GroupBuilder> {

        private final Builder ancestor;
        private String name;

        public GroupBuilder(Builder ancestor, String name) {
            this.ancestor = ancestor;
            this.name = name;
        }

        public Builder build() {
            return ancestor.addFilter(new Group(name, filters));
        }

        public String getName() {
            return name;
        }

        public GroupBuilder setName(String name) {
            this.name = name;
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

        public THIS checkBox(String name) {
            filters.add(new CheckBox(name));
            return getTHIS();
        }

        public THIS text(String name) {
            filters.add(new Text(name));
            return getTHIS();
        }

        public <T2> SelectBuilder<T2, THIS> select(String name) {
            return new SelectBuilder<>(getTHIS(), name);
        }

        public THIS addFilter(Filter<?> filter) {
            filters.add(filter);
            return getTHIS();
        }
    }


    public static class SelectBuilder<T, B extends BaseBuilder<B>> {

        private final B ancestor;
        private String name;
        private List<T> values;

        private SelectBuilder(B ancestor, String name) {
            this.ancestor = ancestor;
            this.name = name;
            values = new ArrayList<>();
        }

        public SelectBuilder<T, B> add(T val) {
            values.add(val);
            return this;
        }

        public List<T> getValues() {
            return values;
        }

        public SelectBuilder<T, B> setValues(List<T> values) {
            this.values = values;
            return this;
        }

        public String getName() {
            return name;
        }

        public SelectBuilder<T, B> setName(String name) {
            this.name = name;
            return this;
        }

        public B build() {
            return ancestor.addFilter(new Select<>(name, values));
        }
    }
}

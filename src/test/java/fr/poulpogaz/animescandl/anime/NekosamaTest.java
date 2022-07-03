package fr.poulpogaz.animescandl.anime;

import fr.poulpogaz.animescandl.model.Anime;
import fr.poulpogaz.animescandl.website.filter.CheckBox;
import fr.poulpogaz.animescandl.website.filter.FilterList;
import fr.poulpogaz.animescandl.website.filter.Select;
import fr.poulpogaz.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class NekosamaTest {

    @Test
    void sort() throws JsonException, IOException, InterruptedException {
        Nekosama ns = new Nekosama();

        FilterList filterList = ns.getSearchFilter();
        filterList.setOffset(5);
        ((Select<?>) filterList.getFilter("Ordre")).setValue(4);
        ((CheckBox) filterList.getFilter("Action")).select();
        ((CheckBox) filterList.getFilter("Ecchi")).select();
        ((Select<?>) filterList.getFilter("Status")).setValue(1);
        ((Select<?>) filterList.getFilter("Format")).setValue(4);

        List<Anime> animes = ns.search(null, filterList);
        animes.forEach(System.out::println);
        System.out.println(animes.size());
    }
}

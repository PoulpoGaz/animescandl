package fr.poulpogaz.animescandl.model;

public enum Language {

    ARABIC("Arabic", "ar"),
    BENGALI("Bengali", "bn"),
    BULGARIAN("Bulgarian", "bg"),
    BURMESE("Burmese", "my"),
    CATALAN("Catalan", "ca"),
    CHINESEROMANIZED("Chinese (Romanized)", "zh-ro"),
    CHINESESIMPLIFIED("Chinese (Simplified)", "zh"),
    CHINESETRADITIONAL("Chinese (Traditional)", "zh-hk"),
    CZECH("Czech", "cs"),
    DANISH("Danish", "da"),
    DUTCH("Dutch", "nl"),
    ENGLISH("English", "en"),
    FILIPINO("Filipino", "tl"),
    FINNISH("Finnish", "fi"),
    FRENCH("French", "fr"),
    GERMAN("German", "de"),
    GREEK("Greek", "el"),
    HEBREW("Hebrew", "he"),
    HINDI("Hindi", "hi"),
    HUNGARIAN("Hungarian", "hu"),
    INDONESIAN("Indonesian", "id"),
    ITALIAN("Italian", "it"),
    JAPANESE("Japanese", "ja"),
    JAPANESEROMANIZED("Japanese (romanized)", "ja-ro"),
    KOREAN("Korean", "ko"),
    KOREANROMANIZED("Korean (romanized)", "ko-ro"),
    LITHUANIAN("Lithuanian", "lt"),
    MALAY("Malay", "ms"),
    MONGOLIAN("Mongolian", "mn"),
    NEPALI("Nepali", "ne"),
    NILOSAHARAN("Nilo saharan", "kr"),
    NORWEGIAN("Norwegian", "no"),
    PERSIAN("Persian", "fa"),
    POLISH("Polish", "pl"),
    PORTUGUESEBRAZILIAN("Portuguese (Br)", "pt-br"),
    PORTUGUESEPORTUGAL("Portuguese", "pt"),
    ROMANIAN("Romanian", "rm"),
    RUSSIAN("Russian", "ru"),
    SERBOCROATIAN("Serbian", "sr"),
    SPANISHCASTILIAN("Spanish", "es"),
    SPANISHLATINAMERICAN("Spanish (LATAM)", "es-la"),
    SWEDISH("Swedish", "sv"),
    THAI("Thai", "th"),
    TURKISH("Turkish", "tr"),
    UKRAINIAN("Ukrainian", "uk"),
    VIETNAMESE("Vietnamese", "vi");

    private final String name;
    private final String code;

    Language(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}

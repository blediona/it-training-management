package services;

import java.util.Locale;

public class I18nService {
    private static Locale currentLocale = Locale.forLanguageTag("sq");

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void setLanguage(String languageCode) {
        currentLocale = Locale.forLanguageTag(languageCode);
    }
}
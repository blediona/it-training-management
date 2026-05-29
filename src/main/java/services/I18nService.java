package services;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18nService {
    private static Locale currentLocale = Locale.forLanguageTag("sq");

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static void setLanguage(String languageCode) {
        currentLocale = Locale.forLanguageTag(languageCode);
    }

    public static String getCurrentLanguageLabel() {
        String language = currentLocale.getLanguage();
        if ("en".equalsIgnoreCase(language)) {
            return get("menu.language") + ": English";
        }
        if ("de".equalsIgnoreCase(language)) {
            return get("menu.language") + ": Deutsch";
        }
        return get("menu.language") + ": Shqip";
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("i18n.messages", currentLocale);
    }

    public static String get(String key) {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
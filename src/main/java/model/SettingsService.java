package model;

import javafx.beans.property.*;

public class SettingsService {

    // Singleton class (design pattern)
    private static SettingsService instance;
    // private constructor to prevent instantiation
    private SettingsService() {
    }
    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    public enum ThemeType { DARK, LIGHT }

    // initialising all property values
    private final ObjectProperty<ThemeType> theme = new SimpleObjectProperty<>(ThemeType.DARK);
    private final BooleanProperty largeText = new SimpleBooleanProperty(false);
    private final BooleanProperty dysFont = new SimpleBooleanProperty(false);
    private final BooleanProperty CVDMode = new SimpleBooleanProperty(false);

    public ObjectProperty<ThemeType> themeProperty() {
        return theme;
    }
    public BooleanProperty largeTextProperty() {
        return largeText;
    }
    public BooleanProperty dysFontProperty() {
        return dysFont;
    }
    public BooleanProperty CVDModeProperty() {
        return CVDMode;
    }

    // getters and setters for all settings features
    // setters are not necessary here as UI changes are bound bidirectionally (automatically updating) instead of manually updating
    public ThemeType getTheme() {
        return theme.get();
    }

    public void setTheme(ThemeType t) {
        theme.set(t);
    }

    public boolean isLargeText() {
        return largeText.get();
    }

    public void setLargeText(boolean lt) {
        largeText.set(lt);
    }

    public boolean isDysFont() {
        return dysFont.get();
    }

    public void setDysFont(boolean df) {
        dysFont.set(df);
    }

    public boolean isCVDMode() {
        return CVDMode.get();
    }

    public void setCVDMode(boolean cm) {
        CVDMode.set(cm);
    }
}
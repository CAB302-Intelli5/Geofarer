package utils;

import javafx.scene.Scene;
import model.SettingsService;

import java.util.ArrayList;
import java.util.List;

public class StyleManager {

    private static final StyleManager instance = new StyleManager();

    public static StyleManager getInstance() {
        return instance;
    }

    // keeping track of active scenes in Array
    private final List<Scene> activeScenes = new ArrayList<>();

    // Observer pattern --> tracking settings changes and re-applying styles
    private StyleManager() {
        SettingsService settings = SettingsService.getInstance();
        settings.themeProperty().addListener((obs, o, n) -> updateAllScenes());
        settings.largeTextProperty().addListener((obs, o, n) -> updateAllScenes());
        settings.dysFontProperty().addListener((obs, o, n) -> updateAllScenes());
        settings.CVDModeProperty().addListener((obs, o, n) -> updateAllScenes());
    }

    // adding scene to list of active scene and then applying styles to it (via Function)
    public void activateScene(Scene scene) {
        if (!activeScenes.contains(scene)) {
            activeScenes.add(scene);
            applyGlobalStyles(scene);
            System.out.println("StyleManager: applying styles to active scene function called.");
        } else {
            System.err.println("StyleManager: active scene is null!");
        }
    }

    // updating styles to all scenes
    private void updateAllScenes() {
        activeScenes.forEach(this::applyGlobalStyles);
        System.out.println("StyleManager: update to all scenes function called.");
    }

    // function to apply all enabled styles via Instance
    public void applyGlobalStyles(Scene scene) {
        if (scene != null) {
            SettingsService settings = SettingsService.getInstance();

            scene.getStylesheets().clear();
            scene.getStylesheets().add("@../styles/base.css");

            switch (settings.getTheme()) {
                case DARK -> scene.getStylesheets().add("@../styles/dark-theme.css");
                case LIGHT -> scene.getStylesheets().add("@../styles/light-theme.css");
            }

            if (settings.isLargeText()) {
                scene.getStylesheets().add("@../styles/large-text.css");
                System.out.println("StyleManager: large text stylesheet called.");
            }

            if (settings.isDysFont()) {
                scene.getStylesheets().add("@../styles/dys-font.css");
                System.out.println("StyleManager: dyslexia-friendly font stylesheet called.");
            }

            if (settings.isCVDMode()) {
                scene.getStylesheets().add("@../styles/cvd-mode.css");
                System.out.println("StyleManager: colour vision mode stylesheet called.");
            }
        } else {
            System.err.println("StyleManager: scene is null!");
        }
    }
}
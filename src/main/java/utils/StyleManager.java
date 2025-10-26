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
            // Use resource URLs for CSS so they are found both in IDE and packaged jars
            String baseCss = resourceToExternalForm("/styles/base.css");
            if (baseCss != null) scene.getStylesheets().add(baseCss);

            switch (settings.getTheme()) {
                case DARK -> {
                    String dark = resourceToExternalForm("/styles/dark-theme.css");
                    if (dark != null) scene.getStylesheets().add(dark);
                }
                case LIGHT -> {
                    String light = resourceToExternalForm("/styles/light-theme.css");
                    if (light != null) scene.getStylesheets().add(light);
                }
            }

            if (settings.isLargeText()) {
                String large = resourceToExternalForm("/styles/large-text.css");
                if (large != null) scene.getStylesheets().add(large);
                System.out.println("StyleManager: large text stylesheet called.");
            }

            if (settings.isDysFont()) {
                String dys = resourceToExternalForm("/styles/dys-font.css");
                if (dys != null) scene.getStylesheets().add(dys);
                System.out.println("StyleManager: dyslexia-friendly font stylesheet called.");
            }

            if (settings.isCVDMode()) {
                String cvd = resourceToExternalForm("/styles/cvd-mode.css");
                if (cvd != null) scene.getStylesheets().add(cvd);
                System.out.println("StyleManager: colour vision mode stylesheet called.");
            }
        } else {
            System.err.println("StyleManager: scene is null!");
        }
    }

    // Helper to convert a resource path (starting with '/') to an external form usable by Scene.getStylesheets().add
    private String resourceToExternalForm(String resourcePath) {
        try {
            var url = StyleManager.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("StyleManager: resource not found: " + resourcePath);
                return null;
            }
            return url.toExternalForm();
        } catch (Exception e) {
            System.err.println("StyleManager: error loading resource " + resourcePath + " -> " + e);
            return null;
        }
    }
}
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

            try {
                var root = scene.getRoot();
                if (root != null) {
                    if (!root.getStyleClass().contains("theme")) {
                        root.getStyleClass().add("theme");
                        System.out.println("StyleManager: added 'theme' styleClass to scene root.");
                    }
                    System.out.println("StyleManager: root style classes = " + root.getStyleClass());
                }
            } catch (Exception e) {
                System.err.println("StyleManager: error adding theme styleClass to root -> " + e);
            }

            System.out.println("StyleManager: applied stylesheets: " + scene.getStylesheets());
            // If light theme is active, ensure the hints area is forced to a white background.
            try {
                if (settings.getTheme() == model.SettingsService.ThemeType.LIGHT) {
                    var ta = scene.lookup("#hintsTextArea");
                    if (ta != null) {
                        javafx.scene.Parent p = ta.getParent();
                        javafx.scene.layout.Pane targetPane = null;
                        while (p != null) {
                            if (p instanceof javafx.scene.layout.Pane pane) {
                                if (pane.getStyleClass().contains("hints-box")) {
                                    targetPane = pane;
                                    break;
                                }
                                targetPane = pane;
                            }
                            p = p.getParent();
                        }
                        if (targetPane != null) {
                            // Remove inline background declarations
                            javafx.scene.Parent p2 = ta.getParent();
                            while (p2 != null) {
                                if (p2 instanceof javafx.scene.Node) {
                                    javafx.scene.Node node = (javafx.scene.Node) p2;
                                    String old = node.getStyle();
                                    if (old != null && (old.contains("-fx-background-color") || old.contains("background-color"))) {
                                        String cleaned = old.replaceAll("(?i)(-fx-background-color\\\s*:[^;]+;?)|(background-color\\\s*:[^;]+;?)", "");
                                        node.setStyle(cleaned);
                                        System.out.println("StyleManager: cleared inline background on ancestor " + p2.getClass().getSimpleName() + " oldStyle='" + old + "' newStyle='" + cleaned + "'");
                                    }
                                }
                                p2 = p2.getParent();
                            }

                            //set explicit white background
                            String append = "background-color: white; -fx-background-color: white;";
                            String prev = targetPane.getStyle();
                            if (prev == null) prev = "";
                            targetPane.setStyle(prev + append);
                            System.out.println("StyleManager: forced hints-box (or nearest Pane) background to white on light theme (inline set). Target classes=" + targetPane.getStyleClass());
                        } else {
                            System.out.println("StyleManager: hintsTextArea found but no Pane ancestor to set style on.");
                        }
                    } else {
                        System.out.println("StyleManager: hintsTextArea not found in scene lookup.");
                    }
                } else {
                    // on dark theme, remove any inline override we added earlier
                    var ta = scene.lookup("#hintsTextArea");
                    if (ta != null) {
                        javafx.scene.Parent p = ta.getParent();
                        while (p != null && !(p instanceof javafx.scene.layout.Pane)) {
                            p = p.getParent();
                        }
                        if (p instanceof javafx.scene.layout.Pane pane) {
                            // clear inline style so stylesheet can control appearance
                            pane.setStyle("");
                            System.out.println("StyleManager: cleared inline hints container style for dark theme.");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("StyleManager: error forcing hints background -> " + e);
            }

            try {
                var node = scene.lookup("#hintsTextArea");
                if (node != null) {
                    System.out.println("StyleManager DEBUG: Inspecting ancestors of #hintsTextArea");
                    javafx.scene.Parent p = node.getParent();
                    int level = 0;
                    while (p != null && level < 10) {
                        String classes = p.getStyleClass().toString();
                        String inline = (p instanceof javafx.scene.Node) ? ((javafx.scene.Node) p).getStyle() : "";
                        String info = String.format("  ancestor[%d] type=%s classes=%s inlineStyle=%s", level, p.getClass().getSimpleName(), classes, inline);
                        System.out.println(info);
                        if (p instanceof javafx.scene.layout.Region r) {
                            var bg = r.getBackground();
                            if (bg != null && bg.getFills() != null && !bg.getFills().isEmpty()) {
                                System.out.println("    -> Region background fills: " + bg.getFills());
                            }
                        }
                        p = p.getParent();
                        level++;
                    }
                } else {
                    System.out.println("StyleManager DEBUG: #hintsTextArea not present for ancestor inspection.");
                }
            } catch (Exception e) {
                System.err.println("StyleManager DEBUG: error inspecting ancestors -> " + e);
            }
        } else {
            System.err.println("StyleManager: scene is null!");
        }
    }


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
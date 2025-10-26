package model;

import utils.Constants;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.shape.Shape;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods for loading map raster images and shapefile vector data from natural earth
 * This uses caching for better performance and supports the loading of the raster image,
 * the extraction of necesasry data from the shape files
 */
public class MapService {
    private static Image cachedRasterImage = null;
    private static List<FeatureInfo> cachedFeatures = null;

    public static class FeatureInfo {
        public final Geometry geom;
        public final String name;
        public final String fips10;
        public final String continent;
        public final List<Shape> shapes = new ArrayList<>();

        /**
         *  Stores the metadata for the feature on given country
         * @param geom this is the geometry data of the shapefile
         * @param name This is the name extracted from the shapefile
         * @param fips10 This is the fips10 / GEC code from shape file
         * @param continent This is the continent of the shapefile country
         */
        public FeatureInfo(Geometry geom, String name, String fips10, String continent) {
            this.geom = geom;
            this.name = name;
            this.continent = continent;
            this.fips10 = fips10;
        }
    }


    /**
     * Loads the raster image from the world map. Requires git lfs as the file is quite large.
     * The file uses a tif format and is cached for loading the image
     * @return a JavaFX image of the world map
     */
    public Image loadRasterImage() {
        if (cachedRasterImage != null) {
            return cachedRasterImage;
        }

        Image raster = null;

        // Try JavaFX Image first
        try (InputStream is = new FileInputStream(Constants.RASTER_PATH)) {
            raster = new Image(is);
            if (!raster.isError() && raster.getWidth() > 0 && raster.getHeight() > 0) {
                System.out.println("JavaFX Image load successful: " +
                        raster.getWidth() + "x" + raster.getHeight());
                cachedRasterImage = raster;
                return raster;
            }
        } catch (Exception e) {
            System.err.println("JavaFX Image load failed: " + e.getMessage());
        }

        // Fallback to ImageIO
        if (raster == null || raster.isError() || raster.getWidth() <= 0 || raster.getHeight() <= 0) {
            try {
                BufferedImage bi = ImageIO.read(new File(Constants.RASTER_PATH));
                if (bi != null) {
                    raster = SwingFXUtils.toFXImage(bi, null);
                    System.out.println("ImageIO load successful: " +
                            bi.getWidth() + "x" + bi.getHeight());
                    cachedRasterImage = raster;
                    return raster;
                } else {
                    throw new RuntimeException("ImageIO returned null for raster file.");
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to load raster. Convert to PNG/JPG for testing or add TIFF ImageIO plugin.", ex);
            }
        }

        return raster;
    }

    /**
     * Loads the country features from a shapefile this simplifies the geometries and extracts teh metadata allowing
     * for each country to be determined on click. Thus then linking the natural earth to the hints API for
     * future use
     * @return a list of {@link FeatureInfo} representing countries
     */
    public List<FeatureInfo> loadShapefileData() {
        if (cachedFeatures != null && !cachedFeatures.isEmpty()) {
            return new ArrayList<>(cachedFeatures);
        }

        List<FeatureInfo> featureInfos = new ArrayList<>();

        try {
            File shpFile = new File(Constants.SHAPEFILE_PATH);
            if (!shpFile.exists()) {
                throw new RuntimeException("Missing shapefile: " + shpFile.getAbsolutePath());
            }

            FileDataStore store = FileDataStoreFinder.getDataStore(shpFile);
            if (store == null) {
                throw new RuntimeException("Could not open shapefile store: " + shpFile.getAbsolutePath());
            }

            SimpleFeatureSource featureSource = store.getFeatureSource();

            // Check coordinate reference system
            CoordinateReferenceSystem crs = featureSource.getSchema().getCoordinateReferenceSystem();
            if (crs != null) {
                System.out.println("Shapefile CRS: " + CRS.toSRS(crs));
            } else {
                System.out.println("No CRS found in shapefile, assuming WGS84");
            }

            SimpleFeatureCollection collection = featureSource.getFeatures();
            int featureCount = 0;
            try (SimpleFeatureIterator it = collection.features()) {
                while (it.hasNext()) {
                    SimpleFeature f = it.next();
                    Object geomObj = f.getDefaultGeometry();
                    if (!(geomObj instanceof Geometry)) continue;

                    Geometry g = (Geometry) geomObj;

                    // Validate geometry bounds (should be in geographic coordinates)
                    if (g.getEnvelopeInternal().getMinX() < -180 || g.getEnvelopeInternal().getMaxX() > 180 ||
                            g.getEnvelopeInternal().getMinY() < -90 || g.getEnvelopeInternal().getMaxY() > 90) {
                        System.err.println("Warning: Geometry bounds outside expected geographic range for feature: " +
                                extractName(f));
                    }

                    // Simplify complex geometries for better performance
                    Geometry simplified = g;
                    if (g.getNumPoints() > 1000) {
                        // Use a smaller tolerance for better precision
                        simplified = TopologyPreservingSimplifier.simplify(g, 0.01);
                    }

                    String name = extractName(f);

                    String continent = "Unknown";
                    Object continentAttr = f.getAttribute("CONTINENT");
                    if (continentAttr != null) {
                        continent = continentAttr.toString();
                    }

                    String fips10 = "XX"; // default unknown
                    Object adm0Attr = f.getAttribute("FIPS_10");
                    if (adm0Attr != null) {
                        fips10 = adm0Attr.toString();
                    }

                    // If the FIPS code is the invalid placeholder, skip this feature entirely.
                    if ("-99".equals(fips10)) {
                        System.out.println("Skipping feature with invalid FIPS code: " + extractName(f));
                        continue; // Jumps to the next iteration of the loop
                    }

                    // Create FeatureInfo with ADM0_A3
                    FeatureInfo fi = new FeatureInfo(simplified, name, fips10, continent);
                    featureInfos.add(fi);
                    featureCount++;
                }
            } finally {
                store.dispose();
            }

            System.out.println("Loaded " + featureCount + " countries from shapefile");

            // Cache results
            cachedFeatures = new ArrayList<>(featureInfos);

            // Print some sample coordinates for debugging
            if (!featureInfos.isEmpty()) {
                FeatureInfo sample = featureInfos.get(0);
                if (sample.geom != null) {
                    System.out.println("Sample geometry bounds: " + sample.geom.getEnvelopeInternal());
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading shapefile: " + e.getMessage());
            e.printStackTrace();
        }

        return featureInfos;
    }

    private String extractName(SimpleFeature feature) {
        // Try various common name attributes in order of preference
        String[] keys = {"NAME", "NAME_LONG", "ADMIN", "SOVEREIGNT", "NAME_EN", "name", "admin"};
        for (String k : keys) {
            Object attr = feature.getAttribute(k);
            if (attr != null && !attr.toString().trim().isEmpty()) {
                return attr.toString().trim();
            }
        }

        // Fall back to any non-geometry attribute
        for (Object attrObj : feature.getAttributes()) {
            if (attrObj instanceof Geometry) continue;
            if (attrObj != null && !attrObj.toString().trim().isEmpty()) {
                return attrObj.toString().trim();
            }
        }

        return "Unknown";
    }
}
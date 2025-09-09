package com.example.geofarer.services;

import com.example.geofarer.utils.Constants;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.shape.Polyline;
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

public class MapService {
    private static Image cachedRasterImage = null;
    private static List<FeatureInfo> cachedFeatures = null;

    public static class FeatureInfo {
        public final Geometry geom;
        public final String name;
        public final List<Polyline> shapes = new ArrayList<>();

        public FeatureInfo(Geometry geom, String name) {
            this.geom = geom;
            this.name = name;
        }
    }

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
                    FeatureInfo fi = new FeatureInfo(simplified, name);
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
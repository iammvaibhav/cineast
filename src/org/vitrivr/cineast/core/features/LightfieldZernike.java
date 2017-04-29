package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.complex.Complex;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.images.ZernikeHelper;
import org.vitrivr.cineast.core.util.math.MathConstants;
import org.vitrivr.cineast.core.util.math.ZernikeMoments;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 17.03.17
 */
public class LightfieldZernike extends Lightfield {
    /** Size of the feature vector. */
    private static final int SIZE = 36 + 1; /* Number of Coefficients + Pose Idx */

    /**
     * Default constructor for LightfieldZernike class.
     */
    public LightfieldZernike() {
        super("features_lightfieldzernike", 2.0f, MathConstants.VERTICES_3D_DODECAHEDRON);
    }

    /**
     * Weights used for kNN retrieval based on images / sketches. Higher frequency components (standing for finer details)
     * have less weight towards the final result.
     *
     * Also, the first entry (pose-idx) does count less towards the final distance, if known, and not at all if is unknown.
     */
    private static final float[] WEIGHTS_POSE = new float[SIZE];
    private static final float[] WEIGHTS_NOPOSE = new float[SIZE];
    static {
        WEIGHTS_POSE[0] = 1.50f;
        WEIGHTS_NOPOSE[0] = 0.0f;
        for (int i = 1; i< SIZE; i++) {
            WEIGHTS_POSE[i] = 1.0f - (i-2)*(1.0f/(2*SIZE));
            WEIGHTS_NOPOSE[i] = 1.0f - (i-2)*(1.0f/(2*SIZE));
        }
    }

    /**
     * Returns the modified QueryConfig for the provided feature vector. Creates a weighted
     * version of the original configuration.
     *
     * @param qc Original query config
     * @param feature Feature for which a weight-vector is required.
     * @return
     */
    protected ReadableQueryConfig queryConfigForFeature(QueryConfig qc, float[] feature) {
        if (feature[0] == POSEIDX_UNKNOWN) {
            return qc.clone().setDistanceWeights(WEIGHTS_NOPOSE);
        } else {
            return qc.clone().setDistanceWeights(WEIGHTS_POSE);
        }
    }

    /**
     * Extracts the Lightfield Fourier descriptors from a provided BufferedImage. The returned list contains
     * elements for each identified contour of adequate size.
     *
     * @param image Image for which to extract the Lightfield Fourier descriptors.
     * @param poseidx Poseidx of the extracted image.
     * @return List of descriptors for image.
     */
    protected List<float[]> featureVectorsFromImage(BufferedImage image, int poseidx) {
        final List<ZernikeMoments> moments = ZernikeHelper.zernikeMomentsForShapes(image, RENDERING_SIZE /2, 10);
        final List<float[]> features = new ArrayList<>(moments.size());
        for (ZernikeMoments moment : moments) {
            float[] feature = new float[SIZE];
            int i = 0;
            for (Complex m : moment.getMoments()) {
                feature[i] = (float)m.abs();
                i++;
            }
            feature = MathHelper.normalizeL2InPlace(feature);
            feature[0] = poseidx;
            features.add(feature);
        }
        return features;
    }
}

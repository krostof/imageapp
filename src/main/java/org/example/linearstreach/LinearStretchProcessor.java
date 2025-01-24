package org.example.linearstreach;

import lombok.extern.log4j.Log4j2;
import org.example.histogram.LUTGenerator;

import java.awt.image.BufferedImage;

/**
 Liniowego rozciąganie histogramu w wersjach z i bez
 przesycenia (max przesycenie powinno dotyczyć 5% pikseli)
 */
@Log4j2
public class LinearStretchProcessor {

    private final LUTGenerator lutGenerator;

    public LinearStretchProcessor() {
        this.lutGenerator = new LUTGenerator();
    }

    /**
     * Liniowe rozciąganie histogramu w trybie automatycznym (bez clippingu)
     * lub z clippingiem. Pozostawione jako wcześniej omawiane metody.
     */
    public void applyLinearStretch(BufferedImage image, boolean withClipping, double clippingPercentage) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }
        if (clippingPercentage < 0 || clippingPercentage > 1) {
            throw new IllegalArgumentException("Clipping percentage must be between 0 and 1.");
        }

        if (withClipping) {
            log.info("Applying linear stretch WITH clipping. Clipping = {}%", clippingPercentage * 100);
            applyLinearStretchWithClipping(image, clippingPercentage);
        } else {
            log.info("Applying linear stretch WITHOUT clipping.");
            applyLinearStretchWithoutClipping(image);
        }
    }

    /**
     * Metoda do manualnego rozciągania histogramu w zadanym przez użytkownika zakresie [p1..p2]
     * do zakresu [q3..q4].
     */
    public void applyManualRangeStretch(BufferedImage image, int p1, int p2, int q3, int q4) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null.");
        }
        if (p1 < 0 || p2 > 255 || p1 >= p2) {
            throw new IllegalArgumentException("Invalid source range [p1..p2]. Must be within [0..255], p1 < p2.");
        }
        if (q3 < 0 || q4 > 255 || q3 >= q4) {
            throw new IllegalArgumentException("Invalid target range [q3..q4]. Must be within [0..255], q3 < q4.");
        }

        log.info("Applying manual range stretch: src=[{}..{}], dst=[{}..{}]", p1, p2, q3, q4);

        // Budujemy LUT: [p1..p2] -> [q3..q4]
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i <= p1) {
                lut[i] = q3;
            } else if (i >= p2) {
                lut[i] = q4;
            } else {
                double fraction = (double)(i - p1) / (p2 - p1);
                lut[i] = (int)(q3 + fraction * (q4 - q3));
            }
        }

        // Zastosowanie LUT do obrazu (kanał 0)
        transformImageWithLUT(image, lut);
    }

    /**
     * Liniowe rozciąganie histogramu bez obcinania wartości (auto [min..max] -> [0..255]).
     */
    private void applyLinearStretchWithoutClipping(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] histogram = lutGenerator.generateHistogramLUT(image);

        // Znalezienie minIntensity i maxIntensity
        int minIntensity = 0;
        while (minIntensity < 256 && histogram[minIntensity] == 0) {
            minIntensity++;
        }
        int maxIntensity = 255;
        while (maxIntensity >= 0 && histogram[maxIntensity] == 0) {
            maxIntensity--;
        }

        log.info("Linear stretch WITHOUT clipping -> minIntensity={}, maxIntensity={}", minIntensity, maxIntensity);

        if (minIntensity >= maxIntensity) {
            log.warn("Cannot stretch - minIntensity >= maxIntensity. Possibly uniform image?");
            return;
        }

        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i <= minIntensity) {
                lut[i] = 0;
            } else if (i >= maxIntensity) {
                lut[i] = 255;
            } else {
                lut[i] = (int) (((i - minIntensity) * 255.0) / (maxIntensity - minIntensity));
            }
        }

        transformImageWithLUT(image, lut);
    }

    /**
     * Liniowe rozciąganie histogramu z obcinaniem (auto find lowerBound, upperBound).
     */
    private void applyLinearStretchWithClipping(BufferedImage image, double clippingPercentage) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        int[] histogram = lutGenerator.generateHistogramLUT(image);

        // Ile pikseli obcinamy z dołu i z góry
        int clipPixels = (int) (totalPixels * clippingPercentage);

        int lowerBound = findClippingBound(histogram, clipPixels, true);
        int upperBound = findClippingBound(histogram, clipPixels, false);

        log.info("Linear stretch WITH clipping -> lowerBound={}, upperBound={}", lowerBound, upperBound);

        if (lowerBound >= upperBound) {
            log.warn("Invalid clipping bounds, nothing done. (lowerBound >= upperBound)");
            return;
        }

        int[] lut = generateClippedLUT(lowerBound, upperBound);
        transformImageWithLUT(image, lut);
    }

    private int findClippingBound(int[] histogram, int clipPixels, boolean isLower) {
        int sum = 0;
        if (isLower) {
            for (int i = 0; i < 256; i++) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
            return 0;
        } else {
            for (int i = 255; i >= 0; i--) {
                sum += histogram[i];
                if (sum >= clipPixels) {
                    return i;
                }
            }
            return 255;
        }
    }

    private int[] generateClippedLUT(int lowerBound, int upperBound) {
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i < lowerBound) {
                lut[i] = 0;
            } else if (i > upperBound) {
                lut[i] = 255;
            } else {
                lut[i] = (int) (((i - lowerBound) * 255.0) / (upperBound - lowerBound));
            }
        }
        return lut;
    }

    /**
     * Zastosowanie LUT do obrazu (TYPE_BYTE_GRAY).
     */
    private void transformImageWithLUT(BufferedImage image, int[] lut) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRaster().getSample(x, y, 0);
                int newPixel = lut[pixel];
                image.getRaster().setSample(x, y, 0, newPixel);
            }
        }
        log.info("LUT transform applied. (width={}, height={})", width, height);
    }
}

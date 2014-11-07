package net.rwchess.utils;

import java.io.IOException;

public class ImageMagickImageScaler implements ImageScaler {
    @Override
    public void scale(String srcPath, String destPath, int scaleSize) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("convert "+srcPath
                    +" -resize "+scaleSize+"x "+destPath);
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

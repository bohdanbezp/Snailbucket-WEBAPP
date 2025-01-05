package net.rwchess.controller;

import net.rwchess.utils.ImageScaler;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bodia on 10/15/14.
 */
@Controller
@RequestMapping("/wikiImg")
public class WikiImageController {

    private final ImageScaler imageScaler;

    public WikiImageController(ImageScaler imageScaler) {
        this.imageScaler = imageScaler;
    }

    Pattern scalePatt = Pattern.compile("([0-9]+)px-(.*)");

    @RequestMapping(value = "/{imgName:.*}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] wikiImgProcessJpeg(@PathVariable String imgName, HttpServletRequest request, HttpServletResponse response) {
        Matcher m = scalePatt.matcher(imgName);

        ServletContext context = request.getSession().getServletContext();
        String absPath = "/home/bodia/snail/wikiImg/";
        String filePath = absPath + imgName;

        try {
            if (m.matches()) {
                int size = Integer.parseInt(m.group(1));
                File res = new File(absPath + imgName);
                if (!res.exists() && !res.isDirectory()) {
                    imageScaler.scale(absPath + m.group(2), absPath + imgName, size);
                }
            }

            File file = new File(filePath);
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }

            // Set cache headers
            response.setHeader("Cache-Control", "public, max-age=31536000"); // Cache for 1 year
            response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L); // Expires in 1 year
            response.setHeader("Last-Modified", String.valueOf(file.lastModified()));
            response.setHeader("ETag", String.valueOf(file.hashCode()));

            FileInputStream is = new FileInputStream(file);
            byte[] array = IOUtils.toByteArray(is);
            IOUtils.closeQuietly(is);
            return array;

        } catch (Exception e) {
            try {
                response.sendError(404);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
    }
}

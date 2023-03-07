package com.italia.municipality.lakesebu.utils;

import java.awt.AlphaComposite;
import java.awt.BasicStroke; 
import java.awt.Color;
import java.awt.Graphics2D; 
import java.awt.RenderingHints;
import java.awt.image.BufferedImage; 
import java.io.ByteArrayOutputStream;
import java.io.IOException; 
import java.io.OutputStream;
import java.util.ArrayList; 
import java.util.List;
import java.util.regex.Matcher; 
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
/**
 * 
 * @author Mark Italia
 * @apiNote this source code is from https://primefaces.github.io/primefaces/8_0/#/components/signature?id=convert-to-binary
 *
 */
public class SignatureImageConverter {

	//private static final String IMAGE_FORMAT = "png";
    //private static final int SIGNATURE_HEIGHT = 200; 
    //private static final int SIGNATURE_WIDTH = 400;
    /* A point along a line within a signature. */
    private static class Point {
        private int x; private int y;
        public Point(float x, float y) {
            this.x = Math.round(x); this.y = Math.round(y);
        } 
    }


    /** 
     * Extract a signature from its JSON encoding and redraw it as an image.
     * @param jsonEncoding the JSON representation of the signature
     * @param output the destination stream for the image * @throws IOException if a problem writing the signature
     */ 
    public static void generateSignature(String jsonEncoding, OutputStream output, int sig_width, int sig_height, String img_format) throws IOException { 
        output.write(redrawSignature(extractSignature(jsonEncoding), sig_width, sig_height,img_format));
        output.close(); 
    }

    /**
     * Extract the signature lines and points from the JSON encoding.
     * @param jsonEncoding the JSON representation of the signature 
     * @return the retrieved lines and points
     */
    private static List<List<Point>> extractSignature(String jsonEncoding) {
        List<List<Point>> lines = new ArrayList<List<Point>>(); 
        Matcher lineMatcher = Pattern.compile("(\\[(?:,?\\[-?[\\d\\.]+,-?[\\d\\.]+\\])+\\])").matcher(jsonEncoding);
        while (lineMatcher.find()) { 
            Matcher pointMatcher = Pattern.compile("\\[(-?[\\d\\.]+),(-?[\\d\\.]+)\\]").matcher(lineMatcher.group(1));
            List<Point> line = new ArrayList<Point>(); 
            lines.add(line);
            while (pointMatcher.find()) { 
                line.add(new Point(Float.parseFloat(pointMatcher.group(1)),
                Float.parseFloat(pointMatcher.group(2)))); 
            }
        } 
        return lines;
    }
    /** 
     * Redraw the signature from its lines definition.
     * @param lines the individual lines in the signature
     * @return the corresponding signature image * @throws IOException if a problem generating the signature
     */ 
    private static byte[] redrawSignature(List<List<Point>> lines, int sig_width, int sig_height, String img_format) throws IOException {
        BufferedImage signature = new BufferedImage( sig_width, sig_height, BufferedImage.TYPE_INT_ARGB);//BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = (Graphics2D)signature.getGraphics(); 
        
        g.setComposite(AlphaComposite.Clear);
        
        //g.setColor(Color.WHITE);
        
        g.fillRect(0, 0, signature.getWidth(), signature.getHeight()); 
        
        g.setComposite(AlphaComposite.Src);
        int alpha = 127;//50% transparent
        
        //g.setColor(Color.BLACK);
        g.setColor(new Color(0,0,0,alpha));
        //g.fillRect(100, 100, 123, 123);
        
        //g.setColor(new Color(0,0,0));
        //g.fillRect(30, 30, 60, 60);
        
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        Point lastPoint = null;
        for (List<Point> line : lines) { 
            for (Point point : line) {
                if (lastPoint != null) { 
                    g.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                } 
                lastPoint = point;
            } 
            lastPoint = null;
        } 
        g.dispose();
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(signature, img_format, output); 
        ImageIO.write(signature, img_format, output);
        return output.toByteArray(); 
    }

}
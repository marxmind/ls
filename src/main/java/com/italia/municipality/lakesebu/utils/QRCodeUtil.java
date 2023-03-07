package com.italia.municipality.lakesebu.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.NoArgsConstructor;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;

@NoArgsConstructor
public class QRCodeUtil {
	
 	/**
 	 * 
 	 * @param content recommended character is less than 300 for better reading
 	 * @param width
 	 * @param height
 	 * @param locationToSave
 	 * @param qrname
 	 * @return File
 	 * @throws IOException
 	 */
 public static File createQRCode(String content, int width, int height, String locationToSave, String qrname) throws IOException{
	 	File file = null;
	 	BufferedImage qrCode = getQRCode(content, width, height);
        file = new File(locationToSave + qrname);
        ImageIO.write(qrCode, "png", file);
        return file;
 }
 
 
    private static BufferedImage getQRCode(String targetUrl, int width, int height) {
        try {
            Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();

            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(targetUrl, BarcodeFormat.QR_CODE, width, height, hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();

            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_ARGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            
            graphics.setComposite(AlphaComposite.Clear);
            
            //graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            
            
            graphics.setComposite(AlphaComposite.Src);
            int alpha = 127;//50% transparent
            graphics.setColor(new Color(0,0,0,alpha));
            
            //graphics.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return image;
        } catch (WriterException e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting QR Code");
        }

    }

}
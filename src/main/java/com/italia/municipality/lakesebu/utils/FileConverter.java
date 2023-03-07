package com.italia.municipality.lakesebu.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileConverter {

	/**
	 * 
	 * @param pdfFile
	 * @param fileDestination
	 * @param imageExt jpg|png
	 */
	public static void convert(String folderLocation, String pdfFileName,String fileDestination, String imageExt) {
		File pdfFile = new File(folderLocation + pdfFileName + ".pdf");
		try {
			PDDocument document = PDDocument.load(pdfFile);
			PDFRenderer renderer = new PDFRenderer(document);
			BufferedImage image = renderer.renderImage(0);
			if("jpg".equalsIgnoreCase(imageExt)) {
				ImageIO.write(image, "JPEG", new File(folderLocation + pdfFileName + "." + imageExt));
			}else if("png".equalsIgnoreCase(imageExt)) {
				ImageIO.write(image, "PNG", new File(folderLocation + pdfFileName + "." + imageExt));
			}
			 System.out.println("Image created");
		       
		      //Closing the document
		      document.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}


package org.dainst.gazetteer.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ImageController {

	private Font fontBig = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	private Font fontMedium = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
	private Font fontSmall = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
	
	@ResponseBody
	@RequestMapping(value = "/markerImage/{number}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] numberedMarkerImage(@PathVariable int number) throws Exception {
		BufferedImage image = null;
		image = ImageIO.read(new URL("http://www.google.com/intl/en_us/mapfiles/ms/micons/red.png"));
		
		if (image != null) {			
			if (number < 1000) {
				int xPos, yPos;
				Font font;
				if (number < 10) {
					xPos = 12;
					yPos = 14;
					font = fontBig;
				} else if (number < 100) {
					xPos = 9;
					yPos = 13;
					font = fontMedium;
				} else {
					xPos = 8;
					yPos = 13;
					font = fontSmall;
				}
			
				Graphics graphics = image.getGraphics();
				graphics.setFont(font);
				graphics.setColor(Color.BLACK);
				graphics.drawString(String.valueOf(number), xPos, yPos);
				graphics.dispose();
			}
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();			
			ImageIO.write(image, "png", outputStream);			
			return outputStream.toByteArray();
		}
		return null;
	}

}
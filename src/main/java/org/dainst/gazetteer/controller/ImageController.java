package org.dainst.gazetteer.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
	private ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	
	@ResponseBody
	@RequestMapping(value = "/markerImage/{color}/{number}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] numberedMarkerImage(@PathVariable String color, @PathVariable int number) throws Exception {
		String fileName;
		switch (color) {
		case "blue":
			fileName = "marker_blue.png";
			break;
		case "lightRed":
			fileName = "marker_light_red.png";
			break;
		default:
			fileName = "marker_red.png";
			break;
		}

		InputStream inputStream = classloader.getResourceAsStream("images/" + fileName);
		BufferedImage image = null;
		image = ImageIO.read(inputStream);
		
		if (image != null) {			
			if (number >= 0 && number < 1000) {
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
			
				Graphics2D graphics = (Graphics2D) image.getGraphics();
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
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
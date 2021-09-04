/*
 * The Nheengatu Project : a free Java library for HTML  abstraction.
 *
 * Project Info:  http://www.aryjr.com/nheengatu/
 * Project Lead:  Ary Rodrigues Ferreira Junior
 *
 * (C) Copyright 2005, 2006 by Ary Junior
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.aryjr.nheengatu.pdf;

import java.io.IOException;
import java.net.URL;

import com.aryjr.nheengatu.html.Tag;
import com.aryjr.nheengatu.util.TagsManager;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 
 * A HTML image in a PDF document.
 * 
 * @version $Id: PDFImage.java,v 1.1 2007/12/26 15:57:41 tah Exp $
 * @author <a href="mailto:junior@aryjr.com">Ary Junior</a>
 * 
 */
public class PDFImage {

	public static Image createImage(final Tag htmlImage) throws BadElementException, IOException {
		final TagsManager gm = TagsManager.getInstance();
		// TODO the image path can't be static
		Image img;
		if (htmlImage.getPropertyValue("src").indexOf("http://") >= 0) {
			img = Image.getInstance(new URL(htmlImage.getPropertyValue("src")));
		} else {
			img = Image.getInstance(htmlImage.getPropertyValue("src"));
		}
		img.setWidthPercentage(0);// TODO without it, the image dimensions
		img.setAlignment(gm.getAlign());

		String heightStr, widthStr;
		Integer height, width;

		heightStr = htmlImage.getPropertyValue("height");
		widthStr = htmlImage.getPropertyValue("width");

		height = NumberUtils.isParsable(heightStr) ? Integer.parseInt(heightStr) : null;
		width = NumberUtils.isParsable(widthStr) ? Integer.parseInt(widthStr) : null;

		if(height != null | width != null) {
			if(height == null & width != null) {
				height = Math.round(img.getHeight() * (width /img.getWidth()));
			}
			if(width == null & height != null) {
				width = Math.round(img.getWidth() * (height /img.getHeight()));
			}

			img.scaleAbsolute(height, width);
		}
		return img;
	}

}


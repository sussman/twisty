/*
 * $Id: CanvasImpl.java,v 1.8 2006/05/16 18:35:23 weiju Exp $
 * 
 * Created on 2006/01/23
 * Copyright 2005-2006 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.swingui;

import com.google.twisty.zplet.BufferedImage;
import com.google.twisty.zplet.Color;
import com.google.twisty.zplet.Font;
import com.google.twisty.zplet.ImageObserver;
import com.google.twisty.zplet.ZGraphics;


/**
 * The implementation of the Canvas interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class CanvasImpl implements Canvas {

  private BufferedImage image;
  private ZGraphics graphics;
  private ImageObserver observer;
  
  public CanvasImpl(BufferedImage image, ImageObserver observer,
      boolean antialias) {
    
    this.image = image;
    this.graphics = image.getGraphics();
    
    // activate antialiasing if set
    if (antialias) {
      // TODO: antialiasing?
    	/*
      ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
          */
    }
    this.observer = observer;
  }
  
  public int getWidth() {
    
    return image.getWidth();
  }
  
  public int getHeight() {
    
    return image.getHeight();
  }
  
  public void setFont(Font font) {
    
    image.getGraphics().setFont(font);
  }
  
  public int getFontHeight(Font font) {
    
    return graphics.getFontMetrics(font).getHeight();
  }
  
  public int getFontAscent(Font font) {
    
    return graphics.getFontMetrics(font).getMaxAscent();
  }
  
  public int getCharWidth(Font font, char c) {
    
    return graphics.getFontMetrics(font).charWidth(c);
  }
  
  public int getFontDescent(Font font) {
    
    return graphics.getFontMetrics(font).getMaxDescent();
  }
  
  public int getStringWidth(Font font, String str) {
    
    return graphics.getFontMetrics(font).stringWidth(str);
  }
  
  public void fillRect(Color color, int left, int top, int width,
      int height) {
     
    graphics.fillRect(left, top, width, height, color);
  }
  
  public void drawString(Color color, Font font, int x, int y, String str) {
   
    graphics.setFont(font);
    graphics.drawString(str, x, y, color);
  }

  /**
   * {@inheritDoc}
   */
  public void scrollUp(Color backColor, Font font, int top, int height) {
    
    int fontHeight = getFontHeight(font);
    graphics.copyArea(0, top + fontHeight, getWidth(),
                      height - fontHeight, 0, -fontHeight);
    graphics.fillRect(0, top + height - fontHeight,
                      getWidth(), fontHeight + 1, backColor);
  }

  /**
   * {@inheritDoc}
   */
  public void scroll(Color backColor, int left, int top,
      int width, int height, int numPixels) {
    
    if (numPixels >= 0) {
      graphics.copyArea(left, top + numPixels, width,
                        height - numPixels, 0, -numPixels);
      graphics.fillRect(left, top + height - numPixels,
                        width, numPixels + 1, backColor);
    } else {
      
      graphics.copyArea(left, top, width, height - numPixels, 0, numPixels);
      graphics.fillRect(left, top, width, numPixels + 1, backColor);
    }
  }
  
  public void setClip(int left, int top, int width, int height) {
    
    graphics.setClip(left, top, width, height);
  }
  
  public void drawImage(BufferedImage image, int x, int y, int width,
      int height) {
    
    graphics.drawImage(image, x, y, width, height, observer);
  }
  
  public Color getColorAtPixel(int x, int y) {
 
    return new Color(image.getRGB(x, y));
  }
}

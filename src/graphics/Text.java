/**
 * @Author: RogerDTZ
 * @FileName: Text.java
 */

package graphics;

import graphics.Sprite;
import object.GameObject;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Text extends GameObject {

    private String text;
    private Font font;
    private Color color;
    private String fontName;
    private int fontStyle;
    private int fontSize;


    public Text(String id, String text, Font font) {
        super(id);
        this.text = text;
        this.color = Color.black;
        this.setFont(font);
    }

    public void setFont(Font font) {
        this.font = font;
        this.fontName = this.font.getFontName();
        this.fontStyle = this.font.getStyle();
        this.fontSize = this.font.getSize();
        this.updateSprite();
    }

    public Font getFont(){
        return this.font;
    }

    public void setColor(Color color) {
        this.color = color;
        this.updateSprite();
    }

    public void setFontName(String fontName) {
        this.setFont(new Font(fontName, this.fontStyle, this.fontSize));
    }

    public void setFontStyle(int fontStyle) {
        this.setFont(new Font(this.fontName, fontStyle, this.fontSize));
    }

    public void setFontSize(int fontSize) {
        this.setFont(new Font(this.fontName, this.fontStyle, fontSize));
    }

    public void setText(String text) {
        this.text = text;
        this.updateSprite();
    }

    public String getText() {
        return this.text;
    }

    private void updateSprite() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tmpG = img.createGraphics();
        tmpG.setFont(this.font);
        FontMetrics fm = tmpG.getFontMetrics();
        int r_width = Math.max(1, fm.stringWidth(this.text) + 10);
        int r_height = fm.getHeight();
        Font big = new Font(this.font.getFontName(), this.font.getStyle(), this.font.getSize() * 2);
        tmpG.setFont(big);
        fm = tmpG.getFontMetrics();
        int width = Math.max(1, fm.stringWidth(this.text) + 20);
        int height = fm.getHeight();
        tmpG.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        tmpG = img.createGraphics();
        tmpG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        tmpG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        tmpG.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        tmpG.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        tmpG.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        tmpG.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        tmpG.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        tmpG.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        tmpG.setFont(big);
        fm = tmpG.getFontMetrics();
        tmpG.setColor(this.color);
        tmpG.drawString(this.text, 5, fm.getAscent());
        tmpG.dispose();

        if (this.sprite != null)
            this.sprite.setImage(img);
        else
            this.sprite = new Sprite(img);
        this.resizeTo(r_width, r_height);
    }

}

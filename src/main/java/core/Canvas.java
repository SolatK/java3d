package core;

import core.graphics.Polygon;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class Canvas extends java.awt.Canvas {

    private BufferStrategy bs;

    private Graphics g;

    public Canvas(int width, int height) {
        setBackground(Color.BLACK);
        setSize(new Dimension(width, height));
    }

    public void setBs(BufferStrategy bs) {
        this.bs = bs;
    }

    public void newFrame() {
        bs.dispose();
        g = bs.getDrawGraphics();
        //g.translate(originX, originY);
        clear();
    }
    public void render() {
        do {
            bs.show();
        } while (bs.contentsLost());
    }

    public void drawFPS(long frameTime) {
        int fps = (int) (1000 / (frameTime + 1));
        g.setColor(Color.white);
        g.drawString(Long.toString(fps), 10, 10);
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x3, y3, x1, y1);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
        g.setColor(color);
        g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

    public void fillTriangle(Polygon polygon) {
        g.setColor(polygon.color);
        g.fillPolygon(
                new int[]{(int) polygon.p[0].x, (int) polygon.p[1].x, (int) polygon.p[2].x},
                new int[]{(int) polygon.p[0].y, (int) polygon.p[1].y, (int) polygon.p[2].y},
                3
        );
    }

    public void clear() {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

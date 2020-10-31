package com.company;

import com.company.algorithms.BresenhamDrawer;
import com.company.algorithms.BufferedImagePixelDrawer;
import com.company.interfaces.LineDrawer;
import com.company.interfaces.OvalDrawer;
import com.company.interfaces.PixelDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class DrawPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {

    private final ScreenConverter screenConverter = new ScreenConverter(-2, 2, 4, 4, 800, 600);
    private final List<Line> allLines = new ArrayList<>();
    private final List<Circle> allCircle = new ArrayList<>();
    private final List<BrokenLine> brokenLines = new ArrayList<>();
    private final Line xAxis = new Line(new RealPoint(-1, 0), new RealPoint(1, 0));
    private final Line yAxis = new Line(new RealPoint(0, -1), new RealPoint(0, 1));

    public DrawPanel() { //alt + enter - реализуем самостоятельно listener
        this.addMouseMotionListener(this); //на движение мышки
        this.addMouseListener(this); //
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
    }

    @Override
    public void paint(Graphics g) {
        screenConverter.setScreenWidth(getWidth());
        screenConverter.setScreenHeight(getHeight());
        Graphics2D g2d = (Graphics2D) g;
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g_bufferImage = bufferedImage.createGraphics();
        g_bufferImage.setColor(Color.WHITE);
        g_bufferImage.fillRect(0, 0, getWidth(), getHeight());
        g_bufferImage.setColor(Color.BLACK);

        PixelDrawer pixelDrawer = new BufferedImagePixelDrawer(bufferedImage);
        LineDrawer lineDrawer = new BresenhamDrawer(pixelDrawer);
        OvalDrawer ovalDrawer = new BresenhamDrawer(pixelDrawer);

        //drawLine(lineDrawer, xAxis);
        //drawLine(lineDrawer, yAxis);

        for (Line line : allLines) {
            drawLine(lineDrawer, line);
            if (currentLine != null) {
                drawLine(lineDrawer, currentLine);
            }
        }

        for (Circle circle : allCircle){
            drawCircle(ovalDrawer, circle);
            if(currentCircle != null){
                drawCircle(ovalDrawer, currentCircle);
            }
        }

        g2d.drawImage(bufferedImage, 0, 0, null);
        g_bufferImage.dispose();
    }

    private void drawLine(LineDrawer lineDrawer, Line line) {
        lineDrawer.drawLine(screenConverter.R2S(line.getP1()), screenConverter.R2S(line.getP2()), Color.BLACK);
    }

    private void drawCircle(OvalDrawer ovalDrawer, Circle circle) {
        ovalDrawer.drawOval(screenConverter.R2S(circle.getCenter()), (int) circle.getSize(), (int) circle.getSize(), circle.getColor());
    }

    public void clearField(){
        allLines.clear();
        allCircle.clear();
        repaint();
    }

    private ScreenPoint lastPosition = null;
    private Line currentLine = null;
    private Circle currentCircle = null;
    private boolean createBrokenLine = true;
    private boolean redaction = false;

    @Override
    public void mouseDragged(MouseEvent e) { //движение с зажатой кнопкой мыши
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());

        if (lastPosition != null) {
            ScreenPoint deltaScreen = new ScreenPoint(
                    currentPosition.getX() - lastPosition.getX(),
                    currentPosition.getY() - lastPosition.getY());
            RealPoint deltaReal = screenConverter.S2R(deltaScreen);
            RealPoint zeroReal = screenConverter.S2R(new ScreenPoint(0, 0));
            RealPoint vector = new RealPoint(deltaReal.getX() - zeroReal.getX(), deltaReal.getY() - zeroReal.getY());
            screenConverter.setCornerX(screenConverter.getCornerX() - vector.getX());
            screenConverter.setCornerY(screenConverter.getCornerY() - vector.getY());
            lastPosition = currentPosition;
        }

        if (currentLine != null) {
            currentLine.setP2(screenConverter.S2R(currentPosition));
            currentCircle.setCenter(screenConverter.S2R(currentPosition));
            //System.out.print("drag");
        }

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());

        if (currentLine != null && createBrokenLine) {
            currentLine.setP2(screenConverter.S2R(currentPosition));
            currentCircle.setCenter(screenConverter.S2R(currentPosition));
            //System.out.print("move");
        }

        if(currentLine != null && redaction){

        }

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());

        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
            allLines.add(currentLine);
            allCircle.add(currentCircle);
            createBrokenLine = true;
        }

        if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
            allLines.add(currentLine);
            allCircle.add(currentCircle);
            createBrokenLine = false;
        }

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            lastPosition = new ScreenPoint(e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            currentLine = new Line(
                    screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())),
                    screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())));
            currentCircle = new Circle(screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())), 5);
            allCircle.add(currentCircle);
        }
        repaint();
    } //нажимаем

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            lastPosition = null;
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            currentCircle = new Circle(screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())), 5);
            allCircle.add(currentCircle);
            allLines.add(currentLine);
            currentLine = new Line(
                    screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())),
                    screenConverter.S2R(new ScreenPoint(e.getX(), e.getY())));

        }
        repaint();
    } //отпускаем

    @Override
    public void mouseEntered(MouseEvent e) {

    } //вход

    @Override
    public void mouseExited(MouseEvent e) {

    } //выход из элемента управления

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int clicks = e.getWheelRotation();
        double scale = 1;
        double coefficient = clicks < 0 ? 1.1 : 0.9;

        for (int i = 0; i < Math.abs(clicks); i++) {
            scale *= coefficient;
        }

        //radius = (int) (radius * scale);
//        for (Circle circle : allCircle){
//            circle.setSize(radius * scale);
//        }

        screenConverter.setRealWidth(screenConverter.getRealWidth() * scale);
        screenConverter.setRealHeight(screenConverter.getRealHeight() * scale);
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}

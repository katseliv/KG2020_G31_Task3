package com.company;

import com.company.algorithms.BresenhamDrawer;
import com.company.algorithms.BufferedImagePixelDrawer;
import com.company.algorithms.DDALineDrawer;
import com.company.interfaces.LineDrawer;
import com.company.interfaces.OvalDrawer;
import com.company.interfaces.PixelDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class DrawPanel extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {

    public Timer timer = new Timer("Timer");
    private Color color = Color.BLACK;
    public final List<Circle> allCircle = new ArrayList<>();
    public final List<BrokenLine> brokenLines = new ArrayList<>();
    private final ScreenConverter screenConverter = new ScreenConverter(-2, 2, 4, 4, 800, 600);

    public DrawPanel() { //alt + enter - реализуем самостоятельно listener
        this.addMouseMotionListener(this); //на движение мышки
        this.addMouseListener(this); //
        this.addMouseWheelListener(this);
        this.setFocusable(true);
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

        RealPoint firstPoint = null;
        for (RealPoint point : realPoints) {
            if (startPoint != null) {
                drawLine(lineDrawer, startPoint, currentPoint);
            }
            if (firstPoint == null) {
                firstPoint = point;
                continue;
            }
            drawLine(lineDrawer, firstPoint, point);
            firstPoint = point;
        }

        for (Circle circle : circles) {
            drawCircle(ovalDrawer, circle);
            if (currentCircle != null) {
                drawCircle(ovalDrawer, currentCircle);
            }
        }

        for (BrokenLine brokenLine : brokenLines) {
            firstPoint = null;
            for (int i = 0; i < brokenLine.getRealPoints().size(); i++) {
                drawCircle(ovalDrawer, brokenLine.getCircles().get(i));
                if (firstPoint == null) {
                    firstPoint = brokenLine.getRealPoints().get(i);
                    continue;
                }
                drawLine(lineDrawer, firstPoint, brokenLine.getRealPoints().get(i));
                firstPoint = brokenLine.getRealPoints().get(i);
            }
        }

        g2d.drawImage(bufferedImage, 0, 0, null);
        g_bufferImage.dispose();
    }

    private void drawLine(LineDrawer lineDrawer, RealPoint p1, RealPoint p2) {
        lineDrawer.drawLine(screenConverter.R2S(p1), screenConverter.R2S(p2), color);
    }

    private void drawCircle(OvalDrawer ovalDrawer, Circle circle) {
        ovalDrawer.drawOval(screenConverter.R2S(circle.getCenter()), (int) circle.getSize(), (int) circle.getSize(), color);
    }

    public void setCreateBrokenLine(boolean createBrokenLine) {
        this.createBrokenLine = createBrokenLine;
        editBrokenLine = false;
    }

    public void setEditBrokenLine(boolean editBrokenLine) {
        this.editBrokenLine = editBrokenLine;
        createBrokenLine = false;
    }

    public void setColor(String c) {
        switch (c) {
            case "Black":
                color = new Color(0, 0, 0);
                break;
            case "Green":
                color = new Color(30, 165, 3);
                break;
            case "Violet":
                color = new Color(90, 0, 186);
                break;
            case "Turquoise":
                color = new Color(0, 137, 161);
                break;

        }
        repaint();
    }

    public void clearField() {
        realPoints.clear();
        brokenLines.clear();
        allCircle.clear();
        repaint();
    }

    private ScreenPoint lastPosition = null;

    private RealPoint startPoint = null;
    private RealPoint currentPoint = null;
    private Circle currentCircle = null;
    private boolean createBrokenLine = true;

    private int indexOfEditPoint = -1;
    private int indexOfLine = -1;
    private int indexOfCircle = -1;
    private boolean editBrokenLine = false;

    private BrokenLine brokenLine = null;
    private final List<RealPoint> realPoints = new ArrayList<>();
    private final List<Circle> circles = new ArrayList<>();

    @Override
    public void mouseDragged(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());
        RealPoint realPosition = screenConverter.S2R(currentPosition);

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

        if (editBrokenLine && indexOfLine != -1 && indexOfCircle != -1 && indexOfEditPoint != -1) {
            brokenLines.get(indexOfLine).getCircles().get(indexOfCircle).setCenter(new RealPoint(realPosition.getX(), realPosition.getY()));
            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setX(realPosition.getX());
            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setY(realPosition.getY());
        }

        repaint();
    } //движение с зажатой кнопкой мыши

    @Override
    public void mouseMoved(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());

        if (currentPoint != null && startPoint != null && createBrokenLine) {
            if (currentCircle == null) {
                currentCircle = new Circle(screenConverter.S2R(currentPosition), 10);
            } else {
                currentCircle.setCenter(screenConverter.S2R(currentPosition));
            }

            currentPoint.setX(screenConverter.S2R(currentPosition).getX());
            currentPoint.setY(screenConverter.S2R(currentPosition).getY());
        }

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());

        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && brokenLines.size() < 2 && !editBrokenLine) {
            brokenLine = new BrokenLine();
            startPoint = new RealPoint(screenConverter.S2R(currentPosition).getX(), screenConverter.S2R(currentPosition).getY());
            currentPoint = new RealPoint(screenConverter.S2R(currentPosition).getX(), screenConverter.S2R(currentPosition).getY());

            if (currentCircle == null) {
                currentCircle = new Circle(screenConverter.S2R(currentPosition), 10);
            } else {
                currentCircle.setCenter(screenConverter.S2R(currentPosition));
            }

            circles.add(currentCircle);
            currentCircle = null;

            realPoints.add(startPoint);
            createBrokenLine = true;
        }

        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && brokenLines.size() < 2 && createBrokenLine && !editBrokenLine) {
            realPoints.add(currentPoint);
            circles.add(currentCircle);
            brokenLine.getRealPoints().addAll(realPoints);
            brokenLine.getCircles().addAll(circles);
            brokenLines.add(brokenLine);
            System.out.println("Length Points " + realPoints.size() + " Length Circle : " + circles.size());
            brokenLine = null;
            startPoint = null;
            realPoints.clear();
            circles.clear();
            currentCircle = null;
            createBrokenLine = false;
        }

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());
        RealPoint realPosition = screenConverter.S2R(currentPosition);

        if (e.getButton() == MouseEvent.BUTTON3 && createBrokenLine) {
            lastPosition = new ScreenPoint(e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON3 && editBrokenLine) {
            whatNearestPoint(new ScreenPoint(e.getX(), e.getY()));
            //allCircle.get(indexOfCircle).setCenter(new RealPoint(realPosition.getX(), realPosition.getY()));
            //allCircle.get(indexOfCircle).getCenter().setX(realPosition.getX());
            //allCircle.get(indexOfCircle).getCenter().setY(realPosition.getY());

            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setX(realPosition.getX());
            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setY(realPosition.getY());
        }

        repaint();
    } //нажимаем

    public void whatNearestPoint(ScreenPoint screenPoint) {
        double min = Integer.MAX_VALUE;

        for (BrokenLine brokenLine : brokenLines) {
            for (int i = 0; i < brokenLine.getRealPoints().size(); i++) {
                double distance = countDistance(brokenLine.getRealPoints().get(i), screenConverter.S2R(screenPoint));
                if (distance < min) {
                    min = distance;
                    indexOfEditPoint = brokenLine.getRealPoints().indexOf(brokenLine.getRealPoints().get(i));
                    indexOfCircle = brokenLine.getCircles().indexOf(brokenLine.getCircles().get(i));
                    indexOfLine = brokenLines.indexOf(brokenLine);
                }
            }
        }
//
//        for (BrokenLine brokenLine : brokenLines) {
//            for (RealPoint realPoint : brokenLine.getRealPoints()) {
//                double distance = countDistance(realPoint, screenConverter.S2R(screenPoint));
//                if (distance < min) {
//                    min = distance;
//                    indexOfEditPoint = brokenLine.getRealPoints().indexOf(realPoint);
//                    indexOfCircle = brokenLine.getCircles().indexOf();
//                    indexOfLine = brokenLines.indexOf(brokenLine);
//                }
//            }
//        }
    }

    private double countDistance(RealPoint p1, RealPoint p2) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();
        double xSquare = Math.pow(x2 - x1, 2);
        double ySquare = Math.pow(y2 - y1, 2);
        return Math.pow(xSquare + ySquare, 0.5);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ScreenPoint currentPosition = new ScreenPoint(e.getX(), e.getY());
        RealPoint realPosition = screenConverter.S2R(currentPosition);

        if (e.getButton() == MouseEvent.BUTTON3 && createBrokenLine) {
            lastPosition = null;
        } else if (e.getButton() == MouseEvent.BUTTON3 && editBrokenLine) {
            //allCircle.get(indexOfCircle).setCenter(new RealPoint(realPosition.getX(), realPosition.getY()));

            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setX(realPosition.getX());
            brokenLines.get(indexOfLine).getRealPoints().get(indexOfEditPoint).setY(realPosition.getY());
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

        screenConverter.setRealWidth(screenConverter.getRealWidth() * scale);
        screenConverter.setRealHeight(screenConverter.getRealHeight() * scale);
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            System.out.println("press");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

}

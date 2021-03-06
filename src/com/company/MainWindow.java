package com.company;

import com.company.figures.Curve;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

public class MainWindow extends JFrame {
    private Timer timer;
    private DrawPanel drawPanel;
    private JPanel buttonsPanel;
    private double coefficient = 0;
    private double step;
    private final double goal = 1 + step;
    private int timeCounter = 0;
    private final Label clockLabel = new Label("00 : 00");
    private final Label conditions = new Label("Condition: ");
    private final TextField textFieldTime = new TextField();
    private final JButton action = new JButton("Complete");
    private final JButton delete = new JButton("Clear");
    private static final Font FONT = new Font(Font.SERIF, Font.ITALIC, 20);

    public MainWindow() throws HeadlessException {
        super("Animation line");

        panel();
        this.add(drawPanel);
        this.add(buttonsPanel, BorderLayout.NORTH);
        this.addKeyListener(drawPanel);
        this.setFocusable(true);
    }

    private void panel() {
        drawPanel = new DrawPanel();
        buttonsPanel = new JPanel();

        clockLabel.setPreferredSize(new Dimension(100, 25));
        clockLabel.setFont(FONT);
        buttonsPanel.add(clockLabel);

        conditions.setPreferredSize(new Dimension(200, 25));
        conditions.setFont(FONT);
        buttonsPanel.add(conditions);

        Label textTime = new Label("Time");
        textTime.setFont(FONT);

        textFieldTime.setFont(new Font(Font.SERIF, Font.PLAIN, 25));
        textFieldTime.setPreferredSize(new Dimension(100, 30));
        buttonsPanel.add(textTime);
        buttonsPanel.add(textFieldTime);

        action.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                try {
                    double time = Double.parseDouble(textFieldTime.getText());
                    step = 1 / (time * 25);

                    timer = new Timer(40, new ActionListener() {
                        final List<Curve> bezierCurves = drawPanel.getBezierCurves();

                        final Curve bezierCurve1 = bezierCurves.get(0);
                        final Curve bezierCurve2 = bezierCurves.get(1);

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            timeCounter++;
                            setClockLabel(timeCounter);
                            Curve curve = function(bezierCurve1, bezierCurve2);
                            drawPanel.setAnimateCurve(curve);
                            repaint();
                        }
                    });
                    timer.start();

                } catch (Exception e) {
                    conditions.setText("Condition: " + e.getMessage());
                    timer.stop();
                }
            }
        });

        action.setFont(FONT);
        buttonsPanel.add(action);

        Label labelConditions = new Label("Choose action:");
        labelConditions.setFont(FONT);
        buttonsPanel.add(labelConditions);

        String[] conditions = {
                "Create",
                "Edit"
        };

        JComboBox<String> comboBoxConditions = new JComboBox<>(conditions);
        comboBoxConditions.setFont(FONT);
        comboBoxConditions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cb = (JComboBox<String>) e.getSource();
                String conditionName = (String) cb.getSelectedItem();
                switch (Objects.requireNonNull(conditionName)) {
                    case "Create":
                        drawPanel.setCreateBrokenLine(true);
                        break;
                    case "Edit":
                        drawPanel.setEditBrokenLine(true);
                        break;
                }
            }
        });
        buttonsPanel.add(comboBoxConditions);

        Label labelColors = new Label("Choose color:");
        labelColors.setFont(FONT);
        buttonsPanel.add(labelColors);

        String[] colors = {
                "Black",
                "Green",
                "Violet",
                "Turquoise"
        };

        JComboBox<String> comboBoxColors = new JComboBox<>(colors);
        comboBoxColors.setFont(FONT);
        comboBoxColors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cb = (JComboBox<String>) e.getSource();
                String colorName = (String) cb.getSelectedItem();
                drawPanel.setColor(colorName);
            }
        });
        buttonsPanel.add(comboBoxColors);

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawPanel.clearField();
                if (action.isSelected()) {
                    timer.stop();
                }
            }
        });
        delete.setFont(FONT);
        buttonsPanel.add(delete);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 15));
    }

    public Curve function(Curve a, Curve b) {
        final List<RealPoint> realPointsA = a.getRealPoints();
        final List<RealPoint> realPointsB = b.getRealPoints();

        Curve currentCurve = new Curve();
        List<RealPoint> curvePoints = currentCurve.getRealPoints();

        try {
            for (int i = 0; i < realPointsA.size(); i++) {
                double vectorX = realPointsB.get(i).getX() - realPointsA.get(i).getX();
                double vectorY = realPointsB.get(i).getY() - realPointsA.get(i).getY();
                curvePoints.add(new RealPoint(
                        realPointsA.get(i).getX() + vectorX * coefficient,
                        realPointsA.get(i).getY() + vectorY * coefficient));
            }

            coefficient += step;

            if (coefficient > goal) {
                timeCounter = 0;
                timer.stop();
                conditions.setText("Condition: " + "Finished!");
                currentCurve = null;
                coefficient = 0;
            }

        } catch (IndexOutOfBoundsException e) {
            conditions.setText("Condition: " + e.getMessage());
        }

        return currentCurve;
    }

    private void setClockLabel(int time) {
        if (time / 25 < 10) {
            clockLabel.setText("00 : 0" + time / 25);
        } else {
            clockLabel.setText("00 : " + time / 25);
        }

    }
}

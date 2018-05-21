package grorbits;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;


import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Application: GRorbits2
 * Author: Slavomir Tuleja
 * Work started: August 18, 2002 - August 31, 2002
 * First major revision: Febuary and March 2003
 * Last major revision: May 2006 
 * Last change: November 15, 2015
 * Contributors: Wolfgang Christian, Tomas Jezo, Jozef Hanc. 
 * Description: The application simulates motion of an orbiter or light moving 
 * freely in equatorial plane of a Kerr black hole
 * in Boyer-Lindquist coordinates and in rain coordinates
 */
public class GRorbits extends JFrame implements Runnable, ActionListener, PropertyChangeListener {

    Container cp;
    Thread animationThread;
    ImageIcon iconPlay, iconStop, iconStepForward, iconStepBack;
    JButton btnStartStop, btnStepForward, btnStepBack, btnReset, btnPlotOrbit, btnResetExplore, btnShowOrbitData;
    JPanel pnlSouth, pnlButtons, pnlEffPot, pnlOrbit, pnlExplore;
    JSplitPane splitPane, splitPaneOrbitAndPotential;
    JSplitPane splitPaneProperties;
    JLabel lblComment;
    OrbitSliderControls sliderControls;
    InitialConditionsInspector icInspector;
    OrbitDataInspector odInspector;
    Orbit orbit;
    JMenuBar mainMenu;
    JMenu menuFile, menuProgramMode, menuMetric, menuOrbits, menuDisplay, menuInitial, menuZoom, menuAdvanced, menuAbout, menuSetCircularOrbit;
    JMenu menuOrbitalTrajectorySettings;
    JMenuItem menuItemRead, menuItemSave, menuItemSetToMaxLPlusInside, menuItemSetToMinLMinusInside, menuItemSetToMaxLPlusOutside, menuItemSetToMinLPlusOutside, menuItemSetToMaxLMinusOutside, menuItemSetToMinLMinusOutside, menuItemSetToMinLOutside;
    JMenuItem menuItemDT, menuItemNumberOfPoints, menuItemReset;
    ButtonGroup bgMode, bgOrbits;
    JRadioButtonMenuItem menuItemNewton, menuItemBoyerLindquistM,
            menuItemBoyerLindquistL, menuItemRainM, menuItemRainL;
    JCheckBoxMenuItem menuItemGrid, menuItemTrail, menuItemScale, menuItemRing;
    JRadioButtonMenuItem menuItemTimePlot, menuItemFullOrbitPlot;
    ButtonGroup bgInitial;
    JRadioButtonMenuItem menuItemInward, menuItemOutward;
    JCheckBoxMenuItem menuItemAutoZoom;
    JMenuItem menuItemAbout;
    DecimalFormat format = new DecimalFormat("0.000");
    double rMaxOld;
    GridBagConstraints c;
    OrbitDrawingPanel orbDrawingPanel;
    PotentialDrawingPanel potDrawingPanel;
    //Create a file chooser
    JFileChooser fileChooser;
    Calendar calendar = Calendar.getInstance(new Locale("en", "US"));
    String sVersionDate;
    DecimalFormat format9, format0;
    javax.swing.Timer timerStepForward, timerStepBack;
    int timerSFSteps=0, timerSBSteps=0;

    public GRorbits() {
        //  look and feel
        try {
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't set the desired look and feel...");
        }

        setLocale(Locale.getDefault());

    }

    public void initialize() {

        File f = new File(".");
        try {
            String path = f.getCanonicalPath();
            fileChooser = new JFileChooser(path);
        } catch (IOException e) {
            fileChooser = new JFileChooser();
        }

        //Timers for btnStepForward and btnStepBack
        timerStepForward = new javax.swing.Timer(500, this);
        timerStepForward.setInitialDelay(500);
        timerStepForward.addActionListener(this);
        timerStepForward.setActionCommand("stepForward");

        timerStepBack = new javax.swing.Timer(500, this);
        timerStepBack.setInitialDelay(500);
        timerStepBack.addActionListener(this);
        timerStepBack.setActionCommand("stepBack");

        fileChooser.setFileFilter(new ScenarioFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);


        //version date
        calendar.set(2015, Calendar.NOVEMBER, 15);
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("en", "US"));
        sVersionDate = dateFormatter.format(calendar.getTime());

        //formats
        format9 = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "US"));
        format9.applyPattern("###.################");
        format0 = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "US"));
        format0.applyPattern("0");


        //definition of Menu
        mainMenu = new JMenuBar();
        menuFile = new JMenu("File");
        menuProgramMode = new JMenu("Program mode");

        menuMetric = new JMenu("Motion");
        menuOrbits = new JMenu("Orbit");
        menuDisplay = new JMenu("Display");
        menuInitial = new JMenu("Initial");
        menuSetCircularOrbit = new JMenu("Set circular orbit . . .");
        menuZoom = new JMenu("Zoom");
        menuAdvanced = new JMenu("Advanced");
        menuAbout = new JMenu("About");

        menuItemRead = new JMenuItem("Read");
        menuItemSave = new JMenuItem("Save");

        menuItemDT = new JMenuItem("Set the increment dt*");
        menuItemDT.setActionCommand("menuItemDT");
        menuItemNumberOfPoints = new JMenuItem("Set the number of data points");
        menuItemNumberOfPoints.setActionCommand("menuItemNumberOfPoints");
        menuItemReset = new JMenuItem("Reset to defaults");
        menuItemReset.setActionCommand("menuItemReset");


        menuOrbitalTrajectorySettings = new JMenu("Orbital trajectory settings");
        menuAdvanced.add(menuOrbitalTrajectorySettings);
        menuOrbitalTrajectorySettings.add(menuItemDT);
        menuOrbitalTrajectorySettings.add(menuItemNumberOfPoints);
        menuOrbitalTrajectorySettings.add(menuItemReset);

        bgOrbits = new ButtonGroup();
        menuItemTimePlot = new JRadioButtonMenuItem("Time plot", true);
        menuItemTimePlot.setEnabled(true);
        menuItemFullOrbitPlot = new JRadioButtonMenuItem("Full orbit plot", false);
        menuItemFullOrbitPlot.setEnabled(true);
        menuOrbits.add(menuItemTimePlot);
        menuOrbits.add(menuItemFullOrbitPlot);
        bgOrbits.add(menuItemTimePlot);
        bgOrbits.add(menuItemFullOrbitPlot);

        bgMode = new ButtonGroup();

        menuItemBoyerLindquistM = new JRadioButtonMenuItem("Schwarzschild/Kerr map m>0", true);
        menuItemBoyerLindquistL = new JRadioButtonMenuItem("Schwarzschild/Kerr map m=0", false);
        menuItemRainM = new JRadioButtonMenuItem("Rain map m>0", false);
        menuItemRainL = new JRadioButtonMenuItem("Rain map m=0", false);

        menuItemNewton = new JRadioButtonMenuItem("Newton map m>0", false);

        //for the following no button group necessary...
        menuItemGrid = new JCheckBoxMenuItem("Show grid", true);
        menuItemTrail = new JCheckBoxMenuItem("Show trail", true);
        menuItemScale = new JCheckBoxMenuItem("Show scale", true);
        menuItemRing = new JCheckBoxMenuItem("Show ring", true);

        bgInitial = new ButtonGroup();
        menuItemInward = new JRadioButtonMenuItem("Inward", true);
        menuItemOutward = new JRadioButtonMenuItem("Outward", false);
        menuItemSetToMinLMinusInside = new JMenuItem("at the minimum of lower effective potential inside the black hole");
        menuItemSetToMaxLPlusInside = new JMenuItem("at the maximum of upper effective potential inside the black hole");
        menuItemSetToMaxLPlusOutside = new JMenuItem("at the maximum of upper effective potential outside the black hole");
        menuItemSetToMinLPlusOutside = new JMenuItem("at the minimum of upper effective potential outside the black hole");
        menuItemSetToMaxLMinusOutside = new JMenuItem("at the maximum of lower effective potential outside the black hole");
        menuItemSetToMinLMinusOutside = new JMenuItem("at the minimum of lower effective potential outside the black hole");
        menuItemSetToMinLOutside = new JMenuItem("at the minimum of effective potential");
        
        
        menuItemAutoZoom = new JCheckBoxMenuItem("Auto adjust", false);

        menuItemAbout = new JMenuItem("About this program");

        menuFile.add(menuItemRead);
        menuFile.add(menuItemSave);

        menuMetric.add(menuItemBoyerLindquistM);
        bgMode.add(menuItemBoyerLindquistM);
        menuMetric.add(menuItemBoyerLindquistL);
        bgMode.add(menuItemBoyerLindquistL);
        menuMetric.addSeparator();
        menuMetric.add(menuItemRainM);
        bgMode.add(menuItemRainM);
        menuMetric.add(menuItemRainL);
        bgMode.add(menuItemRainL);
        menuMetric.addSeparator();
        menuMetric.add(menuItemNewton);
        bgMode.add(menuItemNewton);

        menuDisplay.add(menuItemGrid);
        menuDisplay.add(menuItemTrail);
        menuDisplay.add(menuItemScale);
        menuDisplay.add(menuItemRing);


        bgInitial.add(menuItemInward);
        bgInitial.add(menuItemOutward);




        menuZoom.add(menuItemAutoZoom);


        menuAbout.add(menuItemAbout);

        menuProgramMode.add(menuOrbits);
        menuProgramMode.add(menuMetric);
        mainMenu.add(menuFile);
        mainMenu.add(menuProgramMode);
        mainMenu.add(menuDisplay);
        mainMenu.add(menuInitial);
        mainMenu.add(menuZoom);
        mainMenu.add(menuAdvanced);
        mainMenu.add(menuAbout);
        setJMenuBar(mainMenu);


        //Menu action listeners
        menuItemRead.addActionListener(this);
        menuItemSave.addActionListener(this);
        menuItemTimePlot.addActionListener(this);
        menuItemFullOrbitPlot.addActionListener(this);
        menuItemNewton.addActionListener(this);
        menuItemBoyerLindquistM.addActionListener(this);
        menuItemBoyerLindquistL.addActionListener(this);
        menuItemRainM.addActionListener(this);
        menuItemRainL.addActionListener(this);
        menuItemGrid.addActionListener(this);
        menuItemTrail.addActionListener(this);
        menuItemScale.addActionListener(this);
        menuItemRing.addActionListener(this);
        menuItemInward.addActionListener(this);
        menuItemOutward.addActionListener(this);
        
        menuItemSetToMinLPlusOutside.addActionListener(this);
        menuItemSetToMaxLPlusOutside.addActionListener(this);
        menuItemSetToMinLMinusOutside.addActionListener(this);
        menuItemSetToMaxLMinusOutside.addActionListener(this);
        menuItemSetToMinLOutside.addActionListener(this);
        menuItemSetToMaxLPlusInside.addActionListener(this);
        menuItemSetToMinLMinusInside.addActionListener(this);
        
        menuItemAutoZoom.addActionListener(this);
        menuItemAbout.addActionListener(this);
        menuItemDT.addActionListener(this);
        menuItemNumberOfPoints.addActionListener(this);
        menuItemReset.addActionListener(this);


        //Buttons...
        //loading images
        String imagePath = "/org/opensourcephysics/tuleja/images/";
        iconPlay = createImageIcon(imagePath + "Play.gif");
        iconStop = createImageIcon(imagePath + "Stop.gif");
        iconStepForward = createImageIcon(imagePath + "StepForward.gif");
        iconStepBack = createImageIcon(imagePath + "StepBack.gif");

        btnStartStop = new JButton("Start", iconPlay);
        btnStartStop.addActionListener(this);

        btnStepForward = new JButton("Step", iconStepForward);
        btnStepForward.addActionListener(this);
        btnStepForward.setActionCommand("stepForward");
        btnStepForward.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                timerStepForward.start();
            }

            public void mouseReleased(MouseEvent e) {
                timerStepForward.stop();
                timerSFSteps=0;
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        btnStepBack = new JButton("Step", iconStepBack);
        btnStepBack.addActionListener(this);
        btnStepBack.setActionCommand("stepBack");
        btnStepBack.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                timerStepBack.start();
            }

            public void mouseReleased(MouseEvent e) {
                timerStepBack.stop();
                timerSBSteps=0;
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        btnReset = new JButton("Reset");
        btnReset.addActionListener(this);

        btnPlotOrbit = new JButton("Plot the orbit");
        btnPlotOrbit.addActionListener(this);
        btnResetExplore = new JButton("Reset");
        btnResetExplore.addActionListener(this);
        btnShowOrbitData = new JButton("Show orbit data");
        btnShowOrbitData.addActionListener(this);


        //  Orbit
        orbit = new OrbitBoyerLindquistM();
        prepareLayout(orbit);

        //remember initial rMax
        rMaxOld = potDrawingPanel.getRMax();
    }

    public void prepareLayout(Orbit orbit) {

        //menu Initial
        menuInitial.removeAll();
        menuInitial.add(menuItemInward);
        menuInitial.add(menuItemOutward);
        menuInitial.addSeparator();
        menuInitial.add(menuSetCircularOrbit);

        if (menuItemBoyerLindquistM.isSelected() || menuItemRainM.isSelected()) {
            menuSetCircularOrbit.removeAll();
            menuSetCircularOrbit.add(menuItemSetToMaxLPlusOutside);
            menuSetCircularOrbit.add(menuItemSetToMinLPlusOutside);
            menuSetCircularOrbit.add(menuItemSetToMaxLMinusOutside);
            menuSetCircularOrbit.add(menuItemSetToMinLMinusOutside);
            menuSetCircularOrbit.addSeparator();
            menuSetCircularOrbit.add(menuItemSetToMaxLPlusInside);
            menuSetCircularOrbit.add(menuItemSetToMinLMinusInside);
            
            
        } else if (menuItemNewton.isSelected()) {
            menuSetCircularOrbit.removeAll();
            menuSetCircularOrbit.add(menuItemSetToMinLOutside);
            
        } else {
            menuSetCircularOrbit.removeAll();
            menuSetCircularOrbit.add(menuItemSetToMaxLPlusOutside);
            menuSetCircularOrbit.add(menuItemSetToMinLMinusOutside);
            menuSetCircularOrbit.addSeparator();
            menuSetCircularOrbit.add(menuItemSetToMaxLPlusInside);
            menuSetCircularOrbit.add(menuItemSetToMinLMinusInside);
        }


        pnlButtons = new JPanel();
        pnlButtons.setLayout(new GridLayout(1, 4));
        pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
        pnlButtons.add(btnStartStop);
        pnlButtons.add(btnStepBack);
        pnlButtons.add(btnStepForward);
        pnlButtons.add(btnReset);

        lblComment = new JLabel(" ");
        lblComment.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblComment.setForeground(Color.red);
        lblComment.setBorder(BorderFactory.createTitledBorder(""));

        pnlExplore = new JPanel();
        pnlExplore.setLayout(new GridLayout(1, 3));
        pnlExplore.setBorder(BorderFactory.createTitledBorder(""));
        pnlExplore.add(btnPlotOrbit);
        pnlExplore.add(btnResetExplore);
        pnlExplore.add(btnShowOrbitData);

        pnlSouth = new JPanel();
        pnlSouth.setBackground(new Color(250, 250, 250));
        pnlSouth.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 0;
        pnlSouth.add(lblComment, c);


        if (menuItemTimePlot.isSelected()) {
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            pnlSouth.add(pnlButtons, c);
        } else {//if menuItemFullOrbitPlot is selected
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            pnlSouth.add(pnlExplore, c);
            pnlSouth.validate();
            pnlSouth.repaint();
            btnShowOrbitData.setText("Show orbit data");
            splitPaneProperties.setBottomComponent(sliderControls);
            splitPaneProperties.setResizeWeight(0.25);
            orbit.reset();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
            odInspector.repaint();
            orbDrawingPanel.setInitialArrowVisible(true);
        }



        //Drawing Panels
        orbDrawingPanel = new OrbitDrawingPanel(orbit);
        orbDrawingPanel.setPreferredMinMaxX(-32, 32);
        orbDrawingPanel.setPreferredMinMaxY(-32, 32);

        pnlOrbit = new JPanel();
        pnlOrbit.setLayout(new BorderLayout());
        pnlOrbit.add(orbDrawingPanel, BorderLayout.CENTER);
        //initial pnlOrbit border settings
        if (menuItemBoyerLindquistM.isSelected()) {
            pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Schwarzschild map m>0"));
        } else if (menuItemBoyerLindquistL.isSelected()) {
            pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Schwarzschild map m=0"));
        } else if (menuItemRainM.isSelected()) {
            pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m>0"));
        } else if (menuItemRainL.isSelected()) {
            pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m=0"));
        } else if (menuItemNewton.isSelected()) {
            pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Newton map m>0"));
        }

        sliderControls = new OrbitSliderControls(orbit);
        potDrawingPanel = new PotentialDrawingPanel(orbit);

        pnlEffPot = new JPanel();
        pnlEffPot.setLayout(new BorderLayout());
        pnlEffPot.add(potDrawingPanel, BorderLayout.CENTER);
        pnlEffPot.setBorder(BorderFactory.createTitledBorder("Effective potential versus r"));
        potDrawingPanel.setPreferredMinMaxX(0, 25);
        sliderControls.removeAll();
        if (menuItemBoyerLindquistM.isSelected() || menuItemRainM.isSelected()) {
            potDrawingPanel.setPreferredMinMaxY(0.94, 1.0038);
            sliderControls.add(sliderControls.slA);
            sliderControls.add(sliderControls.slLm);
        } else if (menuItemNewton.isSelected()) {
            potDrawingPanel.setPreferredMinMaxY(-0.06, 0.0038);
            sliderControls.add(sliderControls.slA);
            sliderControls.add(sliderControls.slLm);
        } else {
            potDrawingPanel.setPreferredMinMaxY(-0.30, 0.30);
            sliderControls.add(sliderControls.slA);
            sliderControls.add(sliderControls.slB);
            sliderControls.setB(1.0 / orbit.getIC().getInvB());
            sliderControls.repaint();

        }

        icInspector = new InitialConditionsInspector(orbit);
        icInspector.setBorder(BorderFactory.createTitledBorder("ORBIT PROPERTIES"));

        odInspector = new OrbitDataInspector(orbit);
        odInspector.setBorder(BorderFactory.createTitledBorder("Orbit data"));

        splitPaneProperties = new JSplitPane(JSplitPane.VERTICAL_SPLIT, icInspector, sliderControls);
        splitPaneProperties.setOneTouchExpandable(true);
        splitPaneProperties.setResizeWeight(0.25);

        splitPaneOrbitAndPotential = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlOrbit, pnlEffPot);
        splitPaneOrbitAndPotential.setOneTouchExpandable(true);
        splitPaneOrbitAndPotential.setResizeWeight(0.7);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPaneOrbitAndPotential, splitPaneProperties);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerLocation(0.75);

        //everything added to content pane...
        cp = getContentPane();
        cp.removeAll();
        cp.setLayout(new BorderLayout());
        cp.add(pnlSouth, BorderLayout.SOUTH);
        cp.add(splitPane, BorderLayout.CENTER);

        cp.validate();
        cp.repaint();

        //setting up property change listeners
        sliderControls.addPropertyChangeListener(this);
        orbit.addPropertyChangeListener(this);
        orbDrawingPanel.addPropertyChangeListener(this);
        potDrawingPanel.addPropertyChangeListener(this);
        icInspector.addPropertyChangeListener(this);
        odInspector.addPropertyChangeListener(this);

    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = GRorbits.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Start")) {
            btnStartStop.setText("Pause");
            btnStartStop.setIcon(iconStop);
            menuProgramMode.setEnabled(false);
            menuInitial.setEnabled(false);
            menuZoom.setEnabled(false);
            menuAbout.setEnabled(false);
            menuAdvanced.setEnabled(false);
            icInspector.table.setEnabled(false);
            odInspector.table.setEnabled(false);
            btnStepForward.setEnabled(false);
            btnStepBack.setEnabled(false);
            btnReset.setEnabled(false);
            sliderControls.slA.setEnabled(false);
            sliderControls.slLm.setEnabled(false);

            splitPaneProperties.setBottomComponent(odInspector);
            splitPaneProperties.setResizeWeight(0.25);

            splitPane.setDividerLocation(1.0);
            
                      
            orbDrawingPanel.setInitialArrowVisible(false);

            startAnimation();

        } else if (e.getActionCommand().equals("Pause")) {
            btnStartStop.setText("Start");
            btnStartStop.setIcon(iconPlay);
            stopAnimation();
            menuProgramMode.setEnabled(true);
            menuInitial.setEnabled(true);
            menuZoom.setEnabled(true);
            menuAbout.setEnabled(true);
            menuAdvanced.setEnabled(true);
            icInspector.table.setEnabled(true);
            odInspector.table.setEnabled(true);
            btnStepForward.setEnabled(true);
            btnStepBack.setEnabled(true);
            btnReset.setEnabled(true);
            sliderControls.slA.setEnabled(true);
            sliderControls.slLm.setEnabled(true);

            splitPane.setDividerLocation(0.75);
            lblComment.setForeground(Color.red);
            lblComment.setText(" ");

            odInspector.repaint();
        } else if (e.getActionCommand().equals("stepForward")) {
            timerSFSteps+=1;
            if(timerSFSteps>5) timerStepForward.setDelay(10);
            else timerStepForward.setDelay(500);
            
            splitPaneProperties.setBottomComponent(odInspector);
            splitPaneProperties.setResizeWeight(0.25);

            orbit.doStep();
            if (menuItemAutoZoom.isSelected()) {
                adjustZoom();
            }
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));

            if ((orbit.getT() < orbit.getIC().getDT()) && (orbit.getT() > -orbit.getIC().getDT())) {
                orbDrawingPanel.setInitialArrowVisible(true);
            } else {
                orbDrawingPanel.setInitialArrowVisible(false);
            }

            orbDrawingPanel.repaint();
            odInspector.repaint();
            potDrawingPanel.repaint();

        } else if (e.getActionCommand().equals("stepBack")) {
            timerSBSteps+=1;
            if(timerSBSteps>5) timerStepBack.setDelay(10);
            else timerStepBack.setDelay(500);
            
            splitPaneProperties.setBottomComponent(odInspector);
            splitPaneProperties.setResizeWeight(0.25);

            orbit.doStepBack();
            if (menuItemAutoZoom.isSelected()) {
                adjustZoom();
            }
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));

            if ((orbit.getT() < orbit.getIC().getDT()) && (orbit.getT() > -orbit.getIC().getDT())) {
                orbDrawingPanel.setInitialArrowVisible(true);
            } else {
                orbDrawingPanel.setInitialArrowVisible(false);
            }

            orbDrawingPanel.repaint();
            odInspector.repaint();
            potDrawingPanel.repaint();

        } else if (e.getActionCommand().equals("Reset")) {
            btnStartStop.setEnabled(true);
            btnStepForward.setEnabled(true);
            btnStepBack.setEnabled(true);
            orbit.reset();
            odInspector.repaint();
            icInspector.repaint();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));


            //set old value of rMax to orbDrawingPanel and potDrawingPanel
            if (menuItemAutoZoom.isSelected()) {
                orbDrawingPanel.setRMax(rMaxOld);
                potDrawingPanel.setRMax(rMaxOld);
                potDrawingPanel.rescale();
            }

            splitPaneProperties.setBottomComponent(sliderControls);
            splitPaneProperties.setResizeWeight(0.25);

            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();

            btnShowOrbitData.setText("Show orbit data");

            orbDrawingPanel.setInitialArrowVisible(true);



        } else if (e.getActionCommand().equals("Read")) {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File inFile = fileChooser.getSelectedFile();
                readFromFile(inFile);
                this.setTitle("GRorbits - ".concat(inFile.getName()));
            } else {
                System.out.println("Load command cancelled by user.");
            }
        } else if (e.getActionCommand().equals("Save")) {
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File outFile = fileChooser.getSelectedFile();
                saveToFile(outFile);
                String fileName = outFile.getName();
                if (!fileName.endsWith(".gsc")) {
                    fileName = fileName.concat(".gsc");
                }
                this.setTitle("GRorbits - ".concat(fileName));


            } else {
                System.out.println("Save command cancelled by user.");
            }
        } else if (e.getActionCommand().equals("Full orbit plot")) {
            pnlSouth.remove(pnlButtons);
            pnlSouth.add(pnlExplore, c);
            pnlSouth.validate();
            pnlSouth.repaint();
            btnShowOrbitData.setText("Show orbit data");
            splitPaneProperties.setBottomComponent(sliderControls);
            splitPaneProperties.setResizeWeight(0.25);
            orbit.reset();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
            odInspector.repaint();
            orbDrawingPanel.setInitialArrowVisible(true);
        } else if (e.getActionCommand().equals("Time plot")) {
            btnStartStop.setEnabled(true);
            pnlSouth.remove(pnlExplore);
            pnlSouth.add(pnlButtons, c);
            pnlSouth.validate();
            pnlSouth.repaint();
            btnShowOrbitData.setText("Show orbit data");
            splitPaneProperties.setBottomComponent(sliderControls);
            splitPaneProperties.setResizeWeight(0.25);
            orbit.reset();
            btnStepForward.setEnabled(true);
            btnStepBack.setEnabled(true);
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
            odInspector.repaint();
        } else if (e.getActionCommand().equals("Plot the orbit")) {
            orbit.reset();
            orbit.computeOrbitAtOnce();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
            odInspector.repaint();
        } else if (e.getActionCommand().equals("Show orbit data")) {
            btnShowOrbitData.setText("Show sliders");
            splitPaneProperties.setBottomComponent(odInspector);
            splitPaneProperties.setResizeWeight(0.25);
        } else if (e.getActionCommand().equals("Show sliders")) {
            btnShowOrbitData.setText("Show orbit data");
            splitPaneProperties.setBottomComponent(sliderControls);
            splitPaneProperties.setResizeWeight(0.25);
        } else if (e.getActionCommand().equals("Show grid")) {
            if (orbDrawingPanel.getShowGrid()) {
                orbDrawingPanel.setShowGrid(false);
            } else {
                orbDrawingPanel.setShowGrid(true);
            }
            orbDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Show trail")) {
            if (orbDrawingPanel.getShowTrail()) {
                orbDrawingPanel.setShowTrail(false);
            } else {
                orbDrawingPanel.setShowTrail(true);
            }
            orbDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Show scale")) {
            if (orbDrawingPanel.getShowScale()) {
                orbDrawingPanel.setShowScale(false);
            } else {
                orbDrawingPanel.setShowScale(true);
            }
            orbDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Show ring")) {
            if (orbDrawingPanel.getShowRing()) {
                orbDrawingPanel.setShowRing(false);
            } else {
                orbDrawingPanel.setShowRing(true);
            }
            orbDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Inward")) {
            orbit.getIC().setInward();
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Outward")) {
            orbit.getIC().setOutward();
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the minimum of upper effective potential outside the black hole")) {
            if (!potDrawingPanel.adjustICToMinLPlusOutside()) {
                JOptionPane.showMessageDialog(this, "The upper effective potential has no local minimum on the diagram!\nOrbiter can not be set to stable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the maximum of upper effective potential outside the black hole")) {
            if (!potDrawingPanel.adjustICToMaxLPlusOutside()) {
                JOptionPane.showMessageDialog(this, "The upper effective potential has no local maximum on the diagram!\nOrbiter can not be set to unstable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the minimum of lower effective potential outside the black hole")) {
            if (!potDrawingPanel.adjustICToMinLMinusOutside()) {
                JOptionPane.showMessageDialog(this, "The lower effective potential has no local minimum on the diagram!\nOrbiter can not be set to stable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the maximum of lower effective potential outside the black hole")) {
            if (!potDrawingPanel.adjustICToMaxLMinusOutside()) {
                JOptionPane.showMessageDialog(this, "The lower effective potential has no local maximum on the diagram!\nOrbiter can not be set to stable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the maximum of upper effective potential inside the black hole")) {
            if (!potDrawingPanel.adjustICToMaxLPlusInside()) {
                JOptionPane.showMessageDialog(this, "The upper effective potential has no local maximum on the diagram!\nOrbiter can not be set to unstable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the minimum of lower effective potential inside the black hole")) {
            if (!potDrawingPanel.adjustICToMinLMinusInside()) {
                JOptionPane.showMessageDialog(this, "The lower effective potential has no local minimum on the diagram!\nOrbiter can not be set to unstable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("at the minimum of effective potential")) {
            if (!potDrawingPanel.adjustICToMinLPlusOutside()) {
                JOptionPane.showMessageDialog(this, "The effective potential has no local minimum on the diagram!\nOrbiter can not be set to stable circular orbit!",
                        "Non existing stable orbit",
                        JOptionPane.WARNING_MESSAGE);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            icInspector.repaint();
            odInspector.repaint();
            orbDrawingPanel.repaint();
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("Schwarzschild/Kerr map m>0")) {
            orbit = new OrbitBoyerLindquistM();
            orbit.getIC().setRingShell();
            prepareLayout(orbit);
        } else if (e.getActionCommand().equals("Schwarzschild/Kerr map m=0")) {
            orbit = new OrbitBoyerLindquistL();
            orbit.getIC().setRingShell();
            prepareLayout(orbit);
        } else if (e.getActionCommand().equals("Rain map m>0")) {
            orbit = new OrbitRainM();
            orbit.getIC().setRingShell();
            prepareLayout(orbit);
        } else if (e.getActionCommand().equals("Rain map m=0")) {
            orbit = new OrbitRainL();
            orbit.getIC().setRingShell();
            prepareLayout(orbit);
        } else if (e.getActionCommand().equals("Newton map m>0")) {
            orbit = new OrbitNewton();
            orbit.getIC().setRingShell();
            prepareLayout(orbit);
            potDrawingPanel.repaint();
        } else if (e.getActionCommand().equals("About this program")) {
            //after selecting menu About->About this program
            JOptionPane.showMessageDialog(this, "This piece of software may be used to accompany\nthe text written by Edwin F. Taylor and John A. Wheeler,\nEXPLORING BLACK HOLES.\n\nGRorbits is based on software developed by Adam Riess in a 1992 \nsenior thesis at the Massachusetts Institute of Technology\nunder Edwin F. Taylor. In 2011 Adam Riess was one of three \nawarded the Nobel Prize in Physics for work on the accelerating \nexpansion of the Universe.\n\nProfessional advisor: Edmund Bertschinger of MIT.\nJava version:\n(c) Slavom\u00EDr Tuleja, Tom\u00E1\u0161 Je\u017Eo, and Jozef Han\u010D\n\nPlease send any comments to:\nstuleja@gmail.com\n\nThis program uses classes from Open Source Physics Project\nwww.opensourcephysics.org\noriginated by Wolfgang Christian et al.\n\nThe program is published under the GNU GPL licence.\n\nVersion of ".concat(sVersionDate),
                    "About this program",
                    JOptionPane.INFORMATION_MESSAGE);
            repaint();
        } else if (e.getActionCommand().equals("menuItemDT")) {
            //after selecting menu 
            String response = JOptionPane.showInputDialog("The present value of dt* is ".concat(format9.format(orbit.odeSolver.getStepSize())).concat(".\nInsert the new value of dt* below. \nDefault value is ".concat(format9.format(orbit.getAutoAdjustedDT())).concat(".")));
            double dtvalue = 1;

            try {
                dtvalue = (format9.parse(response)).doubleValue();

                orbit.getODESolver().initialize(dtvalue);
                orbit.reset();
                pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
                odInspector.repaint();
                icInspector.repaint();
                orbDrawingPanel.setRMax2(orbit.getIC().getR());
                orbDrawingPanel.repaint();
                //potDrawingPanel.rescale();
                potDrawingPanel.repaint();
                rMaxOld = orbDrawingPanel.getRMax();
                repaint();
            } catch (NullPointerException ex) {
                //do nothing
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Wrong input! Please, try again.");
                System.err.println("Bad input: " + response);
            }



        } else if (e.getActionCommand().equals("menuItemNumberOfPoints")) {
            //after selecting menu 
            String response = JOptionPane.showInputDialog("The present value of number of points is ".concat(format0.format(orbit.getNumPoints())).concat(".\nInsert the new number of data points below. \nDefault value is 1500."));
            int num = 1;

            try {
                num = (format0.parse(response)).intValue();

                orbit.numPoints = num;
                orbit.orbitData = new Double[orbit.numPoints][4];
                //initially we load initial condition to the orbitData array
                for (int i = 0; i < orbit.numPoints; i++) {
                    orbit.orbitData[i][0] = new Double(0);
                    orbit.orbitData[i][1] = new Double(orbit.ic.getR());
                    orbit.orbitData[i][2] = new Double(0);
                    orbit.orbitData[i][3] = new Double(0);
                }
                odInspector.setTableModel();
                orbit.reset();
                pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
                odInspector.repaint();
                icInspector.repaint();
                orbDrawingPanel.setRMax(rMaxOld);
                orbDrawingPanel.repaint();

                potDrawingPanel.repaint();
                repaint();
            } catch (NullPointerException ex) {
                //do nothing
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Wrong input! Please, try again.");
                System.err.println("Bad input: " + response);
            }
        } else if (e.getActionCommand().equals("menuItemReset")) {
            //after selecting menu 

            //automatically adjust dt according to r
            orbit.reset();
            //orbit.adjustDTAutomatically();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
            }
            sliderControls.repaint();
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            orbDrawingPanel.repaint();
            //potDrawingPanel.rescale();
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
            repaint();

            //set default value of numPoints
            orbit.numPoints = 1500;
            orbit.orbitData = new Double[orbit.numPoints][4];
            //initially we load initial condition to the orbitData array
            for (int i = 0; i < orbit.numPoints; i++) {
                orbit.orbitData[i][0] = new Double(0);
                orbit.orbitData[i][1] = new Double(orbit.ic.getR());
                orbit.orbitData[i][2] = new Double(0);
                orbit.orbitData[i][3] = new Double(0);
            }


            odInspector.setTableModel();
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.setRMax(rMaxOld);
            orbDrawingPanel.repaint();

            potDrawingPanel.repaint();
            repaint();
        }
    }

    public void setOrbitPanelBorder() {
        if (Math.abs(orbit.getIC().getA()) < 1e-6) {
            if (menuItemBoyerLindquistM.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Schwarzschild map m>0"));
            } else if (menuItemBoyerLindquistL.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Schwarzschild map m=0"));
            } else if (menuItemRainM.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m>0"));
            } else if (menuItemRainL.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m=0"));
            } else if (menuItemNewton.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Newton map m>0"));
            }
        } else {
            if (menuItemBoyerLindquistM.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Kerr map m>0"));
            } else if (menuItemBoyerLindquistL.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Kerr map m=0"));
            } else if (menuItemRainM.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m>0"));
            } else if (menuItemRainL.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Rain map m=0"));
            } else if (menuItemNewton.isSelected()) {
                pnlOrbit.setBorder(BorderFactory.createTitledBorder("Motion: Newton map m>0"));
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("slLmChange")) {
            orbit.getIC().setLm(sliderControls.getLm());
            orbit.reset();

            if (menuItemFullOrbitPlot.isSelected()) {
                orbit.reset();
                orbit.computeOrbitAtOnce();
                potDrawingPanel.repaint();
            }

            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.repaint();
            if (!(menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                potDrawingPanel.rescale(); //Do it only for material particles
            }
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("slBChange")) {
            orbit.getIC().setEffPotParameter(1.0 / sliderControls.getB());
            orbit.reset();
            sliderControls.repaint();


            if (menuItemFullOrbitPlot.isSelected()) {
                orbit.reset();
                orbit.computeOrbitAtOnce();
                potDrawingPanel.repaint();
            }

            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.repaint();
            if (!(menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                potDrawingPanel.rescale(); //Do it only for material particles
            }
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("ICChange")) {//when b slider value is illegal
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getEffPotParameter());
                sliderControls.repaint();
            }
        } else if (evt.getPropertyName().equals("slAChange")) {
            orbit.getIC().setRingShell();
            orbit.getIC().setA(sliderControls.getA());
            orbit.reset();


            if (menuItemFullOrbitPlot.isSelected()) {
                orbit.reset();
                orbit.computeOrbitAtOnce();
                potDrawingPanel.repaint();
            }
            setOrbitPanelBorder();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.repaint();
            if (!(menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                potDrawingPanel.rescale(); //Do it only for material particles
            }
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("icInspectorChangeEm")) {
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                double fooInvB=orbit.getIC().getInvB();
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
                orbit.getIC().setInvB(fooInvB); //this is done because slider change causes a parameter to be rounded to the precission of slider
            }
            sliderControls.repaint();
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            orbDrawingPanel.repaint();
            //potDrawingPanel.rescale();
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("icInspectorChangeLm")) {
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            double fooLm=orbit.getIC().getLm();
            sliderControls.setLm(orbit.getIC().getLm());
            orbit.getIC().setLm(fooLm); //this is done because slider change causes a parameter to be rounded to the precission of slider
            sliderControls.repaint();
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            orbDrawingPanel.repaint();
            if (!(menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                potDrawingPanel.rescale(); //Do it only for material particles
            }
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("icInspectorChangeA")) {
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            setOrbitPanelBorder();
            pnlOrbit.validate();
            double fooa=orbit.getIC().getA();
            sliderControls.setA(orbit.getIC().getA());
            orbit.getIC().setA(fooa); //this is done because slider change causes a parameter to be rounded to the precission of slider
            
            sliderControls.repaint();
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            orbDrawingPanel.repaint();
            potDrawingPanel.rescale();
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("icInspectorChangeR")) {
            orbit.reset();
            //orbit.adjustDTAutomatically();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
            }
            sliderControls.repaint();
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            orbDrawingPanel.repaint();
            //potDrawingPanel.rescale();
            potDrawingPanel.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("icInspectorChangeV0")) {
            orbit.getIC().adjustEmLmSign();
            sliderControls.setLm(orbit.getIC().getLm());
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.setRMax(rMaxOld);
            orbDrawingPanel.repaint();
            potDrawingPanel.rescale();
            potDrawingPanel.repaint();
        } else if (evt.getPropertyName().equals("icInspectorChangeTheta0")) {
            orbit.getIC().adjustEmLmSign();
            sliderControls.setLm(orbit.getIC().getLm());
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
            }
            sliderControls.repaint();

            if (orbit.getIC().getSign() == 1) {
                menuItemOutward.setSelected(true);
            } else {
                menuItemInward.setSelected(true);
            }
            orbit.reset();
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            odInspector.repaint();
            icInspector.repaint();
            orbDrawingPanel.setRMax(rMaxOld);
            orbDrawingPanel.repaint();
            //potDrawingPanel.rescale();
            potDrawingPanel.repaint();
        } else if (evt.getPropertyName().equals("effPotMouseChange")) {
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            orbDrawingPanel.setRMax2(orbit.getIC().getR());
            //orbit.adjustDTAutomatically();
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
            }
            if (menuItemFullOrbitPlot.isSelected()) {
                orbit.reset();
                orbit.computeOrbitAtOnce();
                potDrawingPanel.repaint();
            }

            orbDrawingPanel.repaint();
            icInspector.repaint();
            odInspector.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("effPotMouseXResize")) {
            double rMax = potDrawingPanel.getRMax();
            orbDrawingPanel.setRMax(rMax);
            orbDrawingPanel.repaint();
            potDrawingPanel.setPreferredMinMaxX(0, rMax);
            potDrawingPanel.repaint();
            icInspector.repaint();
            odInspector.repaint();
            rMaxOld = orbDrawingPanel.getRMax();
        } else if (evt.getPropertyName().equals("orbMouseChange")) {
            if (menuItemFullOrbitPlot.isSelected()) {
                orbit.reset();
                orbit.computeOrbitAtOnce();
                potDrawingPanel.repaint();
            }
            if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
                sliderControls.setB(1.0 / orbit.getIC().getInvB());
            }

            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));
            potDrawingPanel.repaint();
            icInspector.repaint();
            odInspector.repaint();
            sliderControls.setLm(orbit.getIC().getLm());
            sliderControls.repaint();
            if (orbit.getIC().getSign() == 1) {
                menuItemOutward.setSelected(true);
            } else {
                menuItemInward.setSelected(true);
            }

            if(menuItemBoyerLindquistL.isSelected() ||  menuItemRainL.isSelected()){
                lblComment.setText("Drag arrowhead direction.");
            }
            else {
                lblComment.setText("Drag arrowhead direction. Drag length by holding ALT or OPTION.");
            }

        } else if (evt.getPropertyName().equals("orbMouseEntered")) {
            if(menuItemBoyerLindquistL.isSelected() ||  menuItemRainL.isSelected()){
                lblComment.setText("Drag arrowhead direction.");
            }
            else {
                lblComment.setText("Drag arrowhead direction. Drag length by holding ALT or OPTION.");
            }
        } else if (evt.getPropertyName().equals("orbMouseExited")) {
            lblComment.setText(" ");
        } else if (evt.getPropertyName().equals("potMouseEntered")) {
            lblComment.setText("Drag orbiter black dot to change initial conditions. Drag the plot to rescale horizontally. Drag the plot by holding ALT or OPTION to rescale vertically. Drag near right edge to shift the plot vertically.");
            //Click near the upper or lower edge to shift the plot vertically.
        } else if (evt.getPropertyName().equals("potMouseExited")) {
            lblComment.setText(" ");
        } else if (evt.getPropertyName().equals("slidersMouseEntered")) {
            lblComment.setText("Change the J/M parameter of the black hole or change the L/m of the orbiter.");
        } else if (evt.getPropertyName().equals("slidersMouseExited")) {
            lblComment.setText(" ");
        } else if (evt.getPropertyName().equals("icMouseEntered")) {
            lblComment.setText("Input numerical values of parameters.");
        } else if (evt.getPropertyName().equals("icMouseExited")) {
            lblComment.setText(" ");
        } else if (evt.getPropertyName().equals("odMouseEntered")) {
            lblComment.setText("To copy the data to a spreadsheet (e.g. Excel) highlight the relevant cells and press CRTL-C.");
        } else if (evt.getPropertyName().equals("odMouseExited")) {
            lblComment.setText(" ");
        } else if (evt.getPropertyName().equals("numConvException")) {
            stopAnimation();
            /*JOptionPane.showMessageDialog(this,
            "Numerical method failed to converge...",
            "Loss of precision warning",
            JOptionPane.WARNING_MESSAGE);
             */
            btnStartStop.setText("Start");
            btnStartStop.setEnabled(false);
            btnStartStop.setIcon(iconPlay);
            menuProgramMode.setEnabled(true);
            menuInitial.setEnabled(true);
            menuZoom.setEnabled(true);
            menuAbout.setEnabled(true);
            menuAdvanced.setEnabled(true);
            icInspector.table.setEnabled(true);
            odInspector.table.setEnabled(true);
            btnStepForward.setEnabled(false);
            btnStepBack.setEnabled(false);
            btnReset.setEnabled(true);
            sliderControls.slA.setEnabled(true);
            sliderControls.slLm.setEnabled(true);

            splitPane.setDividerLocation(0.75);
            lblComment.setForeground(Color.red);
            lblComment.setText(" ");

            odInspector.repaint();
        }

    }

    public void saveToFile(File outFile) {
        Properties prop = getProperties();

        //Now we save the properties to a file

        //here we ensure the file name has the right extension
        String fileName = outFile.getPath();
        if (!fileName.endsWith(".gsc")) {
            fileName = fileName.concat(".gsc");
            outFile = new File(fileName);
        }

        try {
            FileOutputStream outStream = new FileOutputStream(outFile);
            try {
                prop.store(outStream, "This is a scenario file for the program GRorbits.");
            } catch (IOException e) {
                System.out.println("error\n\n" + e.toString());
            }
            try {
                outStream.close();
            } catch (IOException e) {
                System.out.println("error\n\n" + e.toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("error\n\n" + e.toString());
        }


    }

    public void readFromFile(File inFile) {
        //load the Properties object from the file
        Properties prop = new Properties();

        try {
            FileInputStream inStream = new FileInputStream(inFile);
            try {
                prop.load(inStream);
            } catch (IOException e) {
                System.out.println("error\n\n" + e.toString());
            }
            try {
                inStream.close();
            } catch (IOException e) {
                System.out.println("error\n\n" + e.toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("error\n\n" + e.toString());
        }

        reconstructProgramState(prop);

    }

    public void reconstructProgramState(Properties prop) {
        menuItemNewton.setSelected(false);
        menuItemRainM.setSelected(false);
        menuItemRainL.setSelected(false);
        menuItemBoyerLindquistM.setSelected(false);
        menuItemBoyerLindquistL.setSelected(false);
        String sorbit = prop.getProperty("orbit");
        if (sorbit.equals("newton")) {
            menuItemNewton.setSelected(true);
            orbit = new OrbitNewton();
        } else if (sorbit.equals("rain m>0")) {
            menuItemRainM.setSelected(true);
            orbit = new OrbitRainM();
        } else if (sorbit.equals("rain m=0")) {
            menuItemRainL.setSelected(true);
            orbit = new OrbitRainL();
        } else if (sorbit.equals("bookkeeper m>0")) {
            menuItemBoyerLindquistM.setSelected(true);
            orbit = new OrbitBoyerLindquistM();
        } else if (sorbit.equals("bookkeeper m=0")) {
            menuItemBoyerLindquistL.setSelected(true);
            orbit = new OrbitBoyerLindquistL();
        }

        double a = parseDouble(prop.getProperty("a"));
        double r = parseDouble(prop.getProperty("r"));
        double v0 = parseDouble(prop.getProperty("v0"));
        double theta0 = parseDouble(prop.getProperty("theta0"));
        double dt = parseDouble(prop.getProperty("dt"));
        int numPoints = parseInt(prop.getProperty("numPoints"));

        orbit.initialize(a, r, v0, theta0, dt, numPoints);


        double Lm = orbit.getIC().getLm();
        double invB = orbit.getIC().getInvB();

        double rMax = parseDouble(prop.getProperty("rMax"));
        double yMin = parseDouble(prop.getProperty("EffPotYMin"));
        double yMax = parseDouble(prop.getProperty("EffPotYMax"));

        menuItemTimePlot.setSelected(prop.getProperty("time plot").equals("true") ? true : false);
        menuItemFullOrbitPlot.setSelected(prop.getProperty("full orbit plot").equals("true") ? true : false);

        prepareLayout(orbit);

        orbDrawingPanel.setShowGrid(prop.getProperty("show_grid").equals("true") ? true : false);
        menuItemGrid.setSelected(prop.getProperty("show_grid").equals("true") ? true : false);

        orbDrawingPanel.setShowScale(prop.getProperty("show_scale").equals("true") ? true : false);
        menuItemScale.setSelected(prop.getProperty("show_scale").equals("true") ? true : false);

        orbDrawingPanel.setShowTrail(prop.getProperty("show_trail").equals("true") ? true : false);
        menuItemTrail.setSelected(prop.getProperty("show_trail").equals("true") ? true : false);

        orbDrawingPanel.setShowRing(prop.getProperty("show_ring").equals("true") ? true : false);
        menuItemRing.setSelected(prop.getProperty("show_ring").equals("true") ? true : false);

        menuItemInward.setSelected(prop.getProperty("inward").equals("true") ? true : false);
        menuItemOutward.setSelected(prop.getProperty("outward").equals("true") ? true : false);

        menuItemAutoZoom.setSelected(prop.getProperty("autozoom").equals("true") ? true : false);


        potDrawingPanel.setRMax(rMax);
        potDrawingPanel.setPreferredMinMaxX(0, rMax);
        potDrawingPanel.setPreferredMinMaxY(yMin, yMax);

        orbDrawingPanel.setRMax(rMax);
        sliderControls.setA(a);
        if ((menuItemBoyerLindquistL.isSelected()||menuItemRainL.isSelected())) {
            sliderControls.setB(1 / invB);
        } else {
            sliderControls.setLm(Lm);
        }

        if (menuItemFullOrbitPlot.isSelected()) {
            orbit.reset();
            orbit.computeOrbitAtOnce();
        }

        potDrawingPanel.repaint();
        sliderControls.repaint();
        orbDrawingPanel.repaint();
        icInspector.repaint();
        odInspector.repaint();




    }

    public Properties getProperties() {
        Properties prop = new Properties();

        prop.setProperty("time plot", menuItemTimePlot.isSelected() ? "true" : "false");
        prop.setProperty("full orbit plot", menuItemFullOrbitPlot.isSelected() ? "true" : "false");


        if (orbit instanceof OrbitNewton) {
            prop.setProperty("orbit", "newton");
        } else if (orbit instanceof OrbitRainM) {
            prop.setProperty("orbit", "rain m>0");
        } else if (orbit instanceof OrbitRainL) {
            prop.setProperty("orbit", "rain m=0");
        } else if (orbit instanceof OrbitBoyerLindquistM) {
            prop.setProperty("orbit", "bookkeeper m>0");
        } else if (orbit instanceof OrbitBoyerLindquistL) {
            prop.setProperty("orbit", "bookkeeper m=0");
        }

        prop.setProperty("show_grid", menuItemGrid.isSelected() ? "true" : "false");
        prop.setProperty("show_scale", menuItemScale.isSelected() ? "true" : "false");
        prop.setProperty("show_trail", menuItemTrail.isSelected() ? "true" : "false");
        prop.setProperty("show_ring", menuItemRing.isSelected() ? "true" : "false");

        prop.setProperty("inward", menuItemInward.isSelected() ? "true" : "false");
        prop.setProperty("outward", menuItemOutward.isSelected() ? "true" : "false");

        prop.setProperty("autozoom", menuItemAutoZoom.isSelected() ? "true" : "false");

        prop.setProperty("a", format9.format(orbit.ic.getA()));
        prop.setProperty("r", format9.format(orbit.ic.getR()));
        prop.setProperty("v0", format9.format(orbit.ic.getV0()));
        prop.setProperty("theta0", format9.format(orbit.ic.getTheta0()));
        prop.setProperty("dt", format9.format(orbit.ic.getDT()));
        prop.setProperty("numPoints", format0.format(orbit.ic.getNumPoints()));

        prop.setProperty("rMax", format9.format(potDrawingPanel.getRMax()));
        prop.setProperty("EffPotYMin", format9.format(potDrawingPanel.sy1));
        prop.setProperty("EffPotYMax", format9.format(potDrawingPanel.sy2));


        return prop;
    }

    public double parseDouble(String s) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "US"));
        double x = 0;
        try {
            x = (df.parse(s)).doubleValue();
        } catch (ParseException e) {
            System.err.println("Bad input: " + s);
        }
        return x;
    }

    public int parseInt(String s) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("en", "US"));
        int x = 0;
        try {
            x = (df.parse(s)).intValue();
        } catch (ParseException e) {
            System.err.println("Bad input: " + s);
        }
        return x;
    }

    /**
     * startAnimation
     */
    public void startAnimation() {
        if (animationThread != null) {
            return; //already running
        }
        animationThread = new Thread(this);
        animationThread.start();
        potDrawingPanel.setStopped(false);
        orbDrawingPanel.setStopped(false);
        sliderControls.setStopped(false);
        icInspector.setStopped(false);
        odInspector.setStopped(false);
    }

    /**
     * stopAnimation
     */
    public void stopAnimation() {
        Thread tempThread = animationThread; //temporary reference
        animationThread = null; //signal the animation to stop
        if (tempThread != null) {
            try {
                tempThread.interrupt(); //get out of the sleep state
                tempThread.join(); //wait for the thread to die
            } catch (InterruptedException e) {
            }
        }
        potDrawingPanel.setStopped(true);
        orbDrawingPanel.setStopped(true);
        sliderControls.setStopped(true);
        icInspector.setStopped(true);
        odInspector.setStopped(true);
    }

    public void run() {
        while (animationThread == Thread.currentThread()) {
            try {
                Thread.sleep(20);

                orbit.doStep();
            } catch (InterruptedException e) {
                System.out.println("Interrupted...");
            }

            if (menuItemAutoZoom.isSelected()) {
                adjustZoom();
            }
            pnlButtons.setBorder(BorderFactory.createTitledBorder("t = ".concat(format.format(orbit.getT()).concat(" M     \u03c4 = ").concat(format.format(orbit.getTau()))).concat(" M")));

            orbDrawingPanel.repaint();
            odInspector.repaint();
            potDrawingPanel.repaint();
        }

    }

    public void adjustZoom() {
        double p = 0.2;
        double rMax = potDrawingPanel.getRMax();
        rMax = rMax - p * (0.9 * rMax - orbit.getR());
        orbDrawingPanel.setRMax(rMax);
        potDrawingPanel.setRMax(rMax);
        potDrawingPanel.setPreferredMinMaxX(0, rMax);
    }

    public static void main(String[] args) {
        //Apple global menu
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        //Make sure we have nice window decorations.
        //JFrame.setDefaultLookAndFeelDecorated(true);

        //Put icon containing image of black hole to the window.
        Image iLogo;
        java.net.URL imgURL = GRorbits.class.getResource("/org/opensourcephysics/tuleja/images/black-hole.gif");
        if (imgURL != null) {
            iLogo = new ImageIcon(imgURL).getImage();
        } else {
            System.err.println("Couldn't find file.");
            iLogo = null;
        }

        GRorbits frame = new GRorbits();
        frame.setTitle("GRorbits - Untitled");
        frame.setSize(1024, 633);
        frame.setIconImage(iLogo);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.initialize();
        frame.setVisible(true);
    }
}

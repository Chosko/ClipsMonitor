package monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import xclipsjni.ClipsException;
import xclipsjni.ClipsView;
import xclipsjni.ControlPanel;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Luca Ribero, Noemi Mauro,
 * Stefano Rossi
 */
public class MonitorView extends ClipsView implements Observer {

    /**
     * Tipi di verbosità.
     */
    final int Verbosity_HIGH = 2;
    final int Verbosity_MEDIUM = 1;
    final int Verbosity_LOW = 0;

    private MonitorModel model;
    private JFrame view;
    private MapPanel mapPanel;
    private JPanel cp_JPanel; // Pannello
    private ControlPanel cp_frame; //Effettivo frame (classe ControlPanel)

    //Modificabile dall'interfaccia
    private int verbose_mode = Verbosity_MEDIUM;

    PrintOutWindow outputFrame;

    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private final Map<String, BufferedImage> images;
    private Dimension dim;

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public MonitorView() {
        images = new HashMap<>();
        try {
            images.put("wall", ImageIO.read(new File("img" + File.separator + "wall.png")));
            images.put("empty", ImageIO.read(new File("img" + File.separator + "empty.png")));
            images.put("gate", ImageIO.read(new File("img" + File.separator + "gate.png")));
            images.put("outdoor", ImageIO.read(new File("img" + File.separator + "outdoor.png")));
            images.put("debris", ImageIO.read(new File("img" + File.separator + "debris.png")));
            images.put("debris_injured", ImageIO.read(new File("img" + File.separator + "debris_injured.png")));
            images.put("informed", ImageIO.read(new File("img" + File.separator + "informed.png")));
            images.put("undiscovered", ImageIO.read(new File("img" + File.separator + "undiscovered.png")));

            images.put("agent_east_unloaded", ImageIO.read(new File("img" + File.separator + "agent_east_empty.png")));
            images.put("agent_west_unloaded", ImageIO.read(new File("img" + File.separator + "agent_west_empty.png")));
            images.put("agent_north_unloaded", ImageIO.read(new File("img" + File.separator + "agent_north_empty.png")));
            images.put("agent_south_unloaded", ImageIO.read(new File("img" + File.separator + "agent_south_empty.png")));
            images.put("agent_east_loaded", ImageIO.read(new File("img" + File.separator + "agent_east_load.png")));
            images.put("agent_west_loaded", ImageIO.read(new File("img" + File.separator + "agent_west_load.png")));
            images.put("agent_north_loaded", ImageIO.read(new File("img" + File.separator + "agent_north_load.png")));
            images.put("agent_south_loaded", ImageIO.read(new File("img" + File.separator + "agent_south_load.png")));
            images.put("person_rescuer", ImageIO.read(new File("img" + File.separator + "person_rescuer.png")));
        } catch (IOException ex) {
            Logger.getLogger(MonitorView.class.getName()).log(Level.SEVERE, null, ex);
        }

        model = new MonitorModel();
        model.addObserver((Observer) this);
        initializeInterface();
    }

    @Override
    protected void onSetup() {
        initializeMap();
        DebugFrame.appendText("[SYSTEM] Setup completato. Mi appresto a visualizzare la mappa.");
    }

    @Override
    protected void onAction() {
        try {
            updateMap();
            updateOutput();
        } catch (IOException ex) {
            Logger.getLogger(MonitorView.class.getName()).log(Level.SEVERE, null, ex);
        }
        DebugFrame.appendText("[SYSTEM] Azione effettuata.");
    }

    @Override
    protected void onDispose() {
        DebugFrame.appendText("[SYSTEM] Dispose effettuato.");
        String result = model.getResult();
        int score = model.getScore();
        @SuppressWarnings("UnusedAssignment")
        String advise = "";
        if (result.equals("disaster")) {
            advise = "DISASTRO\n";
        } else if (result.equals("done")) {
            advise = "The agent says DONE.\n";
        } else if (model.getTime() >= model.getMaxDuration()) {
            advise = "Maxduration has been reached.\n";
        }
        advise = advise + "Penalties: " + score;

        JOptionPane.showMessageDialog(mapPanel, advise,
                "Termine Esecuzione", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Chiamato nel costruttore inizializza l'interfaccia della finestra,
     * caricando il modulo del pannello di controllo.
     *
     */
    private void initializeInterface() {
        dim = Toolkit.getDefaultToolkit().getScreenSize();
        view = new JFrame();
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        view.setSize(new Dimension((int) dim.getWidth() - 720, 160));
        view.setResizable(true);
        view.setTitle("Rescue2015");
        view.setLayout(new BorderLayout());
        cp_frame = createControlPanel(model);
        cp_JPanel = cp_frame.getControlPanel();
        view.add(cp_JPanel, BorderLayout.NORTH);

        //comando inserito in questa posizione per settare il focus iniziale sulla finestra principale e non su quella di output
        view.setVisible(true);
    }

    /**
     * Crea la prima versione della mappa, quella corrispondente all'avvio
     * dell'ambiente. Inserisce in ogni elemento del grid (mappa) la corretta
     * immagine.
     *
     */
    private void initializeMap() {
        Long timeLeft = model.getMaxDuration();
        cp_frame.getLeftTimeTextField().setText(timeLeft.toString()); //Aggiorna il timeleft
        String[][] mapString = model.getMap();

        int x = mapString.length;
        int y = mapString[0].length;

        mapPanel = new MapPanel();
        view.add(mapPanel);
        view.setSize(view.getWidth(), (int) (dim.getHeight() * 3 / 4));
        view.validate();
    }

    /**
     * Aggiorna la mappa visualizzata nell'interfaccia per farla allineare alla
     * versione nel modello.
     *
     */
    private void updateMap() throws IOException {
        Integer step = model.getStep();
        Integer time = model.getTime();
        Long leftTime = model.getMaxDuration() - model.getTime();
        cp_frame.getTimeTextField().setText(time.toString());
        cp_frame.getLeftTimeTextField().setText(leftTime.toString());
        cp_frame.getStepTextField().setText(step.toString());
        try {
            Thread.sleep(200);

        } catch (InterruptedException ex) {
            Logger.getLogger(MonitorView.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        mapPanel.repaint();
        DebugFrame.appendText("[SYSTEM] Step attuale: " + model.getStep());

    }

    /**
     * Restituisce l'immagine che è la sovrapposizione fra object e background.
     * La dimensione è quella dell'immagine più piccola
     *
     * @param object
     * @param background
     * @return
     */
    private BufferedImage overlapImages(BufferedImage object, BufferedImage background) {
        BufferedImage combined;
        Graphics g;
        // crea una nuova immagine, la dimensione è quella più grande tra le 2 img
        int w = Math.max(background.getWidth(), object.getWidth());
        int h = Math.max(background.getHeight(), object.getHeight());
        combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // SOVRAPPONE le immagini, preservando i canali alpha per le trasparenze (figo eh?)
        g = combined.getGraphics();
        g.drawImage(background, 0, 0, null);
        g.drawImage(object, 0, 0, null);

        return combined;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) {
        new MonitorView();
    }

    /*
     Metodo chiamato per resettare l'interfaccia grafica.
     Cancella il frame corrente
     Aggiorna il modello
     e reinizializza l'interfaccia
     */
    @Override
    protected void reset() {
        outputFrame.dispose();
        outputFrame = null;
        view.dispose();

        model = new MonitorModel();
        model.addObserver((Observer) this);

        initializeInterface();
    }

    @Override
    protected void updateOutput() {
        if (!model.isStarted()) {
            return;
        }

        if (outputFrame == null) {
            outputFrame = new PrintOutWindow(this);
            outputFrame.setSize(700, 500);
            outputFrame.setLocation((int) dim.getWidth() - 710, view.getY());
            outputFrame.setVisible(true);        //inizializzo il valore dello slider di verbosità
            outputFrame.getVerboseSlider().setValue(this.verbose_mode);
        }
        //########################### AGGIORNO LA FINESTRA DEI MESSAGGI DI OUTPUT ############################
        outputFrame.resetDocument(); //Svuotiamo la finestra per ripopolarla coi nuovi fatti
        String[] slots = {"time", "step", "source", "verbosity", "text", "param1", "param2", "param3", "param4"};
        try {
            /**
             * Ogni fatto viene considerato nella forma: [source] testo (con
             * parametri corretti).
             *
             * È necessario compiere alcune operazioni di processing poiché: 1 -
             * le virgolette fanno parte della stringa. 2 - i parametri devono
             * essere sostituiti.
             */
            String[][] facts = model.findAllFacts("printGUI", "TRUE", slots);

            for (String[] fact : facts) {
                int fact_verbosity = Integer.parseInt(fact[3]); //Consideriamo la verbosità
                if (fact_verbosity <= this.verbose_mode) {
                    String source = removeFistAndLastChar(fact[2]);
                    String line = fact[1] + "\t" + source + "\t" + removeFistAndLastChar(fact[4]); //prendiamo il testo così com'è
                    //E applichiamo le sostituzioni, appendendo il risultato alla finestra
                    String parameters[] = {fact[5], fact[6], fact[7], fact[8]};
                    outputFrame.write(mapParameters(line, parameters), source);

                }
            }

        } catch (ClipsException | NumberFormatException ex) {
            Logger.getLogger(MonitorView.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        //################# AGGIORNO ANCHE L'AGENT STATUS WINDOW #####################
        outputFrame.updateAgentStatusWindow(model.getStep(), model.getRow(), model.getColumn(), model.getDirection(), model.getLoaded());
    }

    /**
     * Sostiuisce i parametri nella forma %par<i> con param<i>.
     *
     * @param text
     * @param parameters
     * @return
     */
    protected String mapParameters(String text, String[] parameters) {
        for (int i = 1; i <= parameters.length; i++) {
            text = text.replace("%p" + i, parameters[i - 1]);
        }
        return text;
    }

    /**
     * Rimuove il primo e l'ultimo carattere di una stringa. Nel nostro caso le
     * virgolette di un text.
     *
     * @param text
     * @return
     */
    protected String removeFistAndLastChar(String text) {
        return text.substring(1).replace(text.substring(text.length() - 1), "");
    }

    public void setVerbosityMode(int mode) {
        this.verbose_mode = mode;
    }

    public int getVerbosityMode() {
        return verbose_mode;

    }

    private class MapPanel extends JPanel {

        public MapPanel() {
            super();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            String[][] mapString = model.getMap();

            int cellWidth = Math.round((this.getWidth() - 20) / mapString.length);
            int cellHeight = Math.round((this.getHeight() - 20) / mapString[0].length);

            if (cellWidth > cellHeight) {
                cellWidth = cellHeight;
            } else {
                cellHeight = cellWidth;
            }

            int x0 = (this.getWidth() - cellWidth * mapString.length) / 2;
            int y0 = (this.getHeight() - 30 - cellHeight * mapString[0].length) / 2;

            for (int i = 0; i < mapString.length; i++) {
                g2.drawString((i + 1) + "", x0 - cellWidth, y0 + cellHeight / 2 + cellHeight * (mapString.length - i));
                for (int j = 0; j < mapString[0].length; j++) {
                    if (i == 0) {
                        g2.drawString((j + 1) + "", x0 + cellWidth / 2 + cellWidth * j, y0 + cellHeight / 2);
                    }
                    BufferedImage icon;
                    BufferedImage background;
                    BufferedImage robot;
                    BufferedImage rescue;

                    if (mapString[i][j].contains("agent")) {
                        String direction = model.getDirection();
                        String mode = model.getMode();
                        String arr[] = mapString[i][j].split("_");
                        background = images.get(arr[1]);
                        robot = images.get("agent_" + direction + "_" + mode);
                        icon = overlapImages(robot, background);
                    } else if (mapString[i][j].contains("person")) {
                        String[] arr = mapString[i][j].split("_");
                        background = images.get(arr[1]);
                        rescue = images.get("person_rescuer");
                        icon = overlapImages(rescue, background);
                    } else if (mapString[i][j].contains("wall")) {
                        BufferedImage wall = images.get("wall");
                        background = images.get("empty");
                        icon = overlapImages(wall, background);
                    } else if (mapString[i][j].contains("debris")) {
                        BufferedImage debris;
                        if (mapString[i][j].contains("injured")) {
                            debris = images.get("debris_injured");
                        } else {
                            debris = images.get("debris");
                        }

                        background = images.get("empty");
                        icon = overlapImages(debris, background);
                    } else {
                        String[] arr = mapString[i][j].split("_");
                        icon = images.get(arr[0]);
                    }

                    if (mapString[i][j].contains("informed")) {
                        BufferedImage informed = images.get("informed");
                        icon = overlapImages(informed, icon);
                    }

                    if (mapString[i][j].contains("undiscovered")) {
                        BufferedImage undiscovered = images.get("undiscovered");
                        icon = overlapImages(undiscovered, icon);
                    }

                    g2.drawImage(icon, x0 + cellWidth * j, y0 + cellHeight * (mapString.length - i), cellWidth, cellHeight, this);
                }
            }
        }
    }
}

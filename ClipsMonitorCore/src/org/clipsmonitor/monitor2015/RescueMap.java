package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna
 */
public class RescueMap extends MonitorMap implements Observer {

    private RescueModel model;
    private ClipsConsole console;
    private JFrame view;
    private MapPanel mapPanel;
    //private JLabel[][] map;
    private final int MAP_DIMENSION = 550;
    private final int DEFAULT_IMG_SIZE = 85;

    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private final Map<String, BufferedImage> map_img;
    private final Map<String, BufferedImage> map_img_robot;
    private Dimension dim;

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueMap() {
        console = ClipsConsole.getInstance();
        
        //Primo campo: coerente con i file di CLIPS
        //Secondo campo: nome del file (a piacere)
        map_img = new HashMap<String, BufferedImage>();
        map_img_robot = new HashMap<String, BufferedImage>();

        try {
            map_img.put("Wall", ImageIO.read(new File("img" + File.separator + "wall.jpg")));
            map_img.put("Empty", ImageIO.read(new File("img" + File.separator + "empty.png")));
            map_img.put("Seat", ImageIO.read(new File("img" + File.separator + "seat.png")));
            map_img.put("Table_clean", ImageIO.read(new File("img" + File.separator + "table_clean.png")));
            map_img.put("Table_dirty", ImageIO.read(new File("img" + File.separator + "table_dirty.png")));
            map_img.put("TB", ImageIO.read(new File("img" + File.separator + "trash_basket.png")));
            map_img.put("RB", ImageIO.read(new File("img" + File.separator + "recycle_basket.png")));
            map_img.put("DD", ImageIO.read(new File("img" + File.separator + "drink_dispenser.png")));
            map_img.put("FD", ImageIO.read(new File("img" + File.separator + "food_dispenser.png")));

            map_img.put("Parking", ImageIO.read(new File("img" + File.separator + "parking.png")));

            //Per l'agente, il primo campo deve essere di tipo agent_<direction>
            //Dove <direction> è il valore del campo preso da CLIPS.
            map_img_robot.put("agent_east", ImageIO.read(new File("img" + File.separator + "agent_east.png")));
            map_img_robot.put("agent_west", ImageIO.read(new File("img" + File.separator + "agent_west.png")));
            map_img_robot.put("agent_north", ImageIO.read(new File("img" + File.separator + "agent_north.png")));
            map_img_robot.put("agent_south", ImageIO.read(new File("img" + File.separator + "agent_south.png")));

            //Per gestire le persone. Non vengono sovrapposte, sono vere e proprie celle
            //Le persone hanno valore correlato nella map: person_<Posizione>_<ident>
            //Il terzo valore è l'ident della persona (slot del personstatus)
            //Per poter funzionare, è necessario che vi siano immagini del tipo
            //<ident>-person_empty.png
            //Qualora non si trovi tale immagine, ne viene usata una di default
            //(che ha nome come il secondo campo della mappa)
            map_img.put("person_Person", ImageIO.read(new File("img" + File.separator + "person_empty.png"))); //Persona in piedi
            map_img.put("person_Seat", ImageIO.read(new File("img" + File.separator + "person_seat.png"))); //Persona seduta

        } catch (IOException e) {
            console.error(e);
        }

        model = RescueModel.getInstance();
        model.addObserver((Observer) this);
    }

    @Override
    protected void onSetup() {

        initializeMap(); //Va
        console.debug("Setup completato. Mi appresto a visualizzare la mappa");
    }

    @Override
    protected void onAction() {
        //System.out.println("actionDone");
        try {
            updateMap();
            updateOutput();
        } catch (IOException ex) {
            console.error(ex);
        }
        console.info("Azione effettuata.");
    }

    @Override
    protected void onDispose() {
        console.debug("Dispose effettuato");
        String result = model.getResult();
        int score = model.getScore();
        @SuppressWarnings("UnusedAssignment")
        String advise = "";
        if (result.equals("disaster")) {
            advise = "DISASTRO\n";
        } else if (model.getTime() == model.getMaxDuration()) {
            advise = "Maxduration has been reached.\n";
        } else {
            advise = "The agent says DONE.\n";
        }
        advise = advise + "Penalties: " + score;
        JOptionPane.showMessageDialog(mapPanel, advise, "Termine Esecuzione", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Crea la prima versione della mappa, quella corrispondente all'avvio
     * dell'ambiente. Inserisce in ogni elemento del grid (mappa) la corretta
     * immagine.
     *
     */
    private void initializeMap() {
        String[][] mapString = model.getMap();

        int x = mapString.length;
        int y = mapString[0].length;
        int cellDimension = Math.round(MAP_DIMENSION / x);

        // bloccata la dimensione massima delle singole immagini
        if (cellDimension > DEFAULT_IMG_SIZE) {
            cellDimension = DEFAULT_IMG_SIZE;
        }

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
//        Integer step = model.getStep();
//        Integer time = model.getTime();
//        Integer leftTime = model.getMaxDuration() - model.getTime();
//        cp_frame.getTimeTextField().setText(time.toString());
//        cp_frame.getLeftTimeTextField().setText(leftTime.toString());
//        cp_frame.getStepTextField().setText(step.toString());
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        mapPanel.repaint();
        console.info("Step attuale: " + model.getStep());

    }

//    private BufferedImage imageIcon2Buffered(ImageIcon tempicon) {
//        BufferedImage bi = new BufferedImage(
//                tempicon.getIconWidth(),
//                tempicon.getIconHeight(),
//                BufferedImage.TYPE_INT_RGB);
//        Graphics g = bi.createGraphics();
//        // paint the Icon to the BufferedImage.
//        tempicon.paintIcon(null, g, 0, 0);
//        g.dispose();
//        return bi;
//    }
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

    protected void updateOutput() {
        //########################### AGGIORNO LA FINESTRA DEI MESSAGGI DI OUTPUT ############################
        String[] slots = {"time", "step", "source", "verbosity", "text", "param1", "param2", "param3", "param4", "param5"};
        try {
            /**
             * Ogni fatto viene considerato nella forma: [source] testo (con
             * parametri corretti).
             *
             * È necessario compiere alcune operazioni di processing poiché: 1 -
             * le virgolette fanno parte della stringa. 2 - i parametri devono
             * essere sostituiti.
             */
            String[][] matriceFatti = model.findAllFacts("printGUI", "TRUE", slots);

            for (String[] fatto : matriceFatti) {
                int fact_verbosity = Integer.parseInt(fatto[3]); //Consideriamo la verbosità
                String source = removeFistAndLastChar(fatto[2]);
                String line = fatto[1] + "\t" + source + "\t" + removeFistAndLastChar(fatto[4]); //prendiamo il testo così com'è
                //E applichiamo le sostituzioni, appendendo il risultato alla finestra
                String parameters[] = {fatto[5], fatto[6], fatto[7], fatto[8], fatto[9]};
                console.log(source + mapParameters(line, parameters));
            }

        } catch (Exception ex) {
            console.error(ex);
        }
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
     * @return
     */
    protected String removeFistAndLastChar(String text) {
        return text.substring(1).replace(text.substring(text.length() - 1), "");
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

            for (int i = mapString.length - 1; i >= 0; i--) {
                g2.drawString((i + 1) + "", x0 - cellWidth, y0 + cellHeight / 2 + cellHeight * (mapString.length - i));
                for (int j = 0; j < mapString[0].length; j++) {
                    if (i == 0) {
                        g2.drawString((j + 1) + "", x0 + cellWidth / 2 + cellWidth * j, y0 + cellHeight / 2);
                    }
                    @SuppressWarnings("UnusedAssignment")
                    String direction = "";
                    BufferedImage icon;
                    BufferedImage background;
                    BufferedImage robot;

                    // cerca se, nei primi 6 caratteri (se ce ne sono almeno 6), c'è la stringa "agent_", vedere metodo updateMap in MonitorModel.java
                    // Nel modello si ha una stringa del tipo agent_empty se l'agent si trova su una cella empty.
                    // In modo da inserire l'icona del robot sopra la cella in cui si trova (le due immagini vengono sovrapposte)
                    // ##### SE AGENTE #####
                    if (mapString[i][j].length() >= 6 && mapString[i][j].substring(0, 6).equals("agent_")) {
                        direction = model.getDirection();
                        // ...nel, caso prosegue dal 6° carattere in poi.

                        background = map_img.get(mapString[i][j].substring(6, mapString[i][j].length()));
                        robot = map_img_robot.get("agent_" + direction);

                        icon = overlapImages(robot, background);

                        //Imposta il tooltip
                        //map[i][j].setToolTipText("Agent (" + (i + 1) + ", " + (j + 1) + ")");
                        // ##### SE PERSONA #####
                    } else if (mapString[i][j].length() >= 7 && mapString[i][j].substring(0, 7).equals("person_")) {
                        //Nella forma person_<Background>_<ident>
                        String map_contains = mapString[i][j];
                        String[] person_info = map_contains.split("_"); //prendiamo i tre campi
                        //path dell'immagine apposita (se esiste) per la persona

                        if (map_img.get(person_info[2] + "-" + person_info[0] + "_" + person_info[1]) != null) { //se esiste immagine apposita per quell'id
                            icon = map_img.get(person_info[2] + "-" + person_info[0] + "_" + person_info[1]);
                        } else { //Se il file non esiste si usa quello di default (senza ident davanti)
                            icon = map_img.get(person_info[0] + "_" + person_info[1]);
                        }
                        //Imposta il tooltip
                        //map[i][j].setToolTipText("Client " + person_info[2] + " " + "(" + (i + 1) + ", " + (j + 1) + ")");

                        // ##### SE TAVOLO ####
                    } else if (mapString[i][j].length() >= 5 && mapString[i][j].substring(0, 5).equals("Table")) {
                        //Nella forma Table_<status>_<table-id>
                        String map_contains = mapString[i][j];
                        if (!map_contains.equals("Table")) {
                            String[] table_info = map_contains.split("_"); //prendiamo i tre campi
                            icon = map_img.get(table_info[0] + "_" + table_info[1]);
                        } else {
                            icon = map_img.get("table_clean");
                        }

                        //Imposta il tooltip
                        //map[i][j].setToolTipText("Table " + table_info[2] + " " + "(" + (i + 1) + ", " + (j + 1) + ")");
                        // ##### ALTRIMENTI ####
                        // Era una cella che non aveva bisogno di sovrapposizioni e non è una persona
                    } else {
                        icon = map_img.get(mapString[i][j]);
                        //map[i][j].setToolTipText("(" + (i + 1) + ", " + (j + 1) + ")");
                    }

                    g2.drawImage(icon, x0 + cellWidth * j, y0 + cellHeight * (mapString.length - i), cellWidth, cellHeight, this);

                }
            }

        }
    }
}

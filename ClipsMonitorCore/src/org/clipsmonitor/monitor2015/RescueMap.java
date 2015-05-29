package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
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
        model.addObserver(this);
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
        this.setChanged();
        this.notifyObservers(advise);
    }

    /**
     * Crea la prima versione della mappa, quella corrispondente all'avvio
     * dell'ambiente. Inserisce in ogni elemento del grid (mappa) la corretta
     * immagine.
     *
     */
    private void initializeMap() {
//        String[][] mapString = model.getMap();
//
//        int x = mapString.length;
//        int y = mapString[0].length;
//        int cellDimension = Math.round(MAP_DIMENSION / x);
//
//        // bloccata la dimensione massima delle singole immagini
//        if (cellDimension > DEFAULT_IMG_SIZE) {
//            cellDimension = DEFAULT_IMG_SIZE;
//        }
//
//        mapPanel = new MapPanel();
//        view.add(mapPanel);
//        view.setSize(view.getWidth(), (int) (dim.getHeight() * 3 / 4));
//        view.validate();
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
        this.setChanged();
        this.notifyObservers("repaint");
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

    public String[][] getMap() {
        return model.getEnvMap();
    }

    public Map<String, BufferedImage> getMapImg() {
        return map_img;
    }

    public Map<String, BufferedImage> getMapImgRobot() {
        return map_img_robot;
    }
}

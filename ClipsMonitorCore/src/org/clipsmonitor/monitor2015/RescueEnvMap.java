package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observer;
import javax.swing.JOptionPane;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsCore;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */
public class RescueEnvMap extends MonitorMap implements Observer {

    private RescueModel model;
    protected MonitorImages images;
    private ClipsConsole console;
    private ClipsCore core;
    
    private final String UNKNOWN_COLOR = "#000000";
    
    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private Dimension dim;
    
    private String projectDirectory;
    
    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueEnvMap(String projectDirectory) {
        this.projectDirectory = projectDirectory;
        init();
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
        model.setAdvise(advise);
        this.setChanged();
        this.notifyObservers("advise");
    }
    
    @Override
    public void initMap() throws CLIPSError {
        console.debug("Inizializzazione del modello (EnvMap).");
        
        String[][] mp = core.findAllFacts("ENV", RescueFacts.RealCell.factName(), "TRUE", RescueFacts.RealCell.slotsArray());
        int maxr = 0;
        int maxc = 0;
        for (int i = 0; i < mp.length; i++) {
            int r = new Integer(mp[i][RescueFacts.RealCell.POSR.index()]);
            int c = new Integer(mp[i][RescueFacts.RealCell.POSC.index()]);
            if (r > maxr) {
                maxr = r;
            }
            if (c > maxc) {
                maxc = c;
            }
        }
        map = new String[maxr][maxc];//Matrice di max_n_righe x max_n_colonne
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = UNKNOWN_COLOR;
            }
        }
    }
    
    /**
     * Aggiorna la mappa visualizzata nell'interfaccia per farla allineare alla
     * versione nel modello.
     *
     */
    @Override
    public void updateMap() throws CLIPSError {
        updateCells();
        updateAgentStatus();
        updatePersonStatus();
        // debugMap();
    }

    public void updateCells() throws CLIPSError {
        console.debug("Aggiornamento mappa reale in corso...");

        String[][] cellFacts = core.findAllFacts("ENV", RescueFacts.Cell.factName(), "TRUE", RescueFacts.Cell.slotsArray());

        for (String[] fact : cellFacts) {
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int c = new Integer(fact[RescueFacts.Cell.POSC.index()]);
            int r = new Integer(fact[RescueFacts.Cell.POSR.index()]);
            String contains = fact[RescueFacts.Cell.CONTAINS.index()];
            String injured = fact[RescueFacts.Cell.INJURED.index()];
            String discovered = fact[RescueFacts.Cell.DISCOVERED.index()];
            String checked = fact[RescueFacts.Cell.CHECKED.index()];
            String clear = fact[RescueFacts.Cell.CLEAR.index()];

            //caso di default preleviamo il valore dello slot contains e lo applichiamo alla mappa
            map[r - 1][c - 1] = contains;  
            
            // controlla se lo slot injured sia impostato a yes
            
            if (injured.equals("yes")) {
                map[r - 1][c - 1] += "_injured";
            }

            if (discovered.equals("no")) {
                map[r - 1][c - 1] += "+undiscovered";
            }

            if (checked.equals("yes")) {
                map[r - 1][c - 1] += "+checked";
            }

            if (clear.equals("yes")) {
                map[r - 1][c - 1] += "+clear";
            }
        }
    }

    public void updateAgentStatus() throws CLIPSError{
        console.debug("Acquisizione posizione dell'agente...");
        int r = model.getRow();
        int c = model.getColumn();
        map[r - 1][c - 1] = map[r - 1][c - 1] + "+agent_" + model.getDirection() + "_" + model.getMode();
    }


    public void updatePersonStatus() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti...");
        ArrayList<int[]> personPositions = model.getPersonPositions();
        
        for (int[] person : personPositions) {
            int r = person[0];
            int c = person[1];
            map[r - 1][c - 1] = map[r - 1][c - 1] + "+person";
        }
    }
    
    @Override
    public BufferedImage[][] getIconMatrix(){
        images = MonitorImages.getInstance();
        
        if(map==null){
            return null;
        }
        
        BufferedImage[][] iconMatrix = new BufferedImage[map.length][map[0].length];
    
        /*
            casi possibili per le icone :
        
            - agent_direction_load
            - person
            - gate
            - outdoor
            - empty
            
        */
        
        for (int i=map.length-1;i>=0;i--){
  
            for (int j = 0; j < map[0].length; j++) {
                String direction = "";
                String loaded = "";
                String key_agent_map = "";
                BufferedImage tmpImage;

                // Split the map string in arguments
                String[] curCel = map[i][j].split("\\+");

                // Background image is the first argument
                iconMatrix[i][j] = images.getImage(curCel[0]);

                // All the others arguments are overlaps
                for (int k = 1; k < curCel.length; k++) {
                    String curOverlap = curCel[k];

                    // If agent, we must check directiona and loaded
                    if(curOverlap.equals("agent")){
                        direction = model.getDirection();
                        loaded= model.getMode();
                        key_agent_map="agent_"+ direction + "_" + loaded;
                        tmpImage = images.getImage(key_agent_map);
                        iconMatrix[i][j] = overlapImages(tmpImage, iconMatrix[i][j]);
                    }

                    // If other, just overlap
                    else {
                        tmpImage = images.getImage(curOverlap);
                        iconMatrix[i][j] = overlapImages(tmpImage, iconMatrix[i][j]);
                    }
                }
            }
        }
        return iconMatrix;   
    }
    
    @Override
    public int[] getSize() {
        int[] size = null;
        if(map == null || map.length == 0 || map[0].length == 0){
            return null;
        }
        else{
            size = new int[2];
            size[0] = map.length;
            size[1] = map[0].length;
        }
        return size;
    }

    @Override
    protected void clear() {
        this.console = null;
        this.dim = null;
        this.model = null;
        this.images = null;
        this.core = null;
    }

    @Override
    protected void init() {
        model = RescueModel.getInstance();
        model.addObserver(this);
        console = ClipsConsole.getInstance();
        images = MonitorImages.getInstance();
        core = ClipsCore.getInstance();
    }

   
}







/*
     * Sostiuisce i parametri nella forma %par<i> con param<i>.
     *
     * @param text
     * @param parameters
     * @return
     /
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
     /
    protected String removeFistAndLastChar(String text) {
        return text.substring(1).replace(text.substring(text.length() - 1), "");
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

    // protected void updateOutput() {
    //     //########################### AGGIORNO LA FINESTRA DEI MESSAGGI DI OUTPUT ############################
    //     String[] slots = {"time", "step", "source", "verbosity", "text", "param1", "param2", "param3", "param4", "param5"};
    //     try {
    //         /**
    //          * Ogni fatto viene considerato nella forma: [source] testo (con
    //          * parametri corretti).
    //          *
    //          * È necessario compiere alcune operazioni di processing poiché: 1 -
    //          * le virgolette fanno parte della stringa. 2 - i parametri devono
    //          * essere sostituiti.
    //          */
    //         String[][] matriceFatti = model.findAllFacts("printGUI", "TRUE", slots);

    //         for (String[] fatto : matriceFatti) {
    //             int fact_verbosity = Integer.parseInt(fatto[3]); //Consideriamo la verbosità
    //             String source = removeFistAndLastChar(fatto[2]);
    //             String line = fatto[1] + "\t" + source + "\t" + removeFistAndLastChar(fatto[4]); //prendiamo il testo così com'è
    //             //E applichiamo le sostituzioni, appendendo il risultato alla finestra
    //             String parameters[] = {fatto[5], fatto[6], fatto[7], fatto[8], fatto[9]};
    //             console.clips(source + mapParameters(line, parameters));
    //         }

    //     } catch (Exception ex) {
    //         console.error(ex);
    //     }
    // }


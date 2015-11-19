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
 * 2014/2015
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */
public class RescueAgentMap extends MonitorMap implements Observer {

    private RescueModel model;
    protected MonitorImages images;
    private ClipsConsole console;
    private ClipsCore core;
    
    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private Dimension dim;
    
    private String projectDirectory;
    private final String UNKNOWN_COLOR = "#333333";
    private final String SOUND_COLOR = "rgba(0,70,255,0.3)";

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueAgentMap(String projectDirectory) {
        this.projectDirectory = projectDirectory;
        init();
    }

    @Override
    protected void onDispose() {
        
    }
    
    @Override
    public void initMap() throws CLIPSError {
        console.debug("Inizializzazione del modello (AgentMap).");

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
        updateAgentStatus();
        updateKCells();
//        updatePersonStatus();
        // debugMap();
    }

    private void updateKCells() throws CLIPSError{
        console.debug("Aggiornamento mappa dell'agente in corso...");

        String[][] cellFacts = core.findAllFacts("AGENT", RescueFacts.KCell.factName(), "TRUE", RescueFacts.KCell.slotsArray());

        for (String[] fact : cellFacts) {
             
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int c = new Integer(fact[RescueFacts.KCell.POSC.index()]) - 1;
            int r = new Integer(fact[RescueFacts.KCell.POSR.index()]) - 1;
            int agentR = model.getRow() - 1;
            int agentC = model.getColumn() - 1;
            String contains = fact[RescueFacts.KCell.CONTAINS.index()];
            String injured = fact[RescueFacts.KCell.INJURED.index()];
            String sound = fact[RescueFacts.KCell.SOUND.index()];
            
            // Inseriamo nella mappa ciò che contiene
            if(c != agentC || r != agentR){
                map[r][c] = contains;
                
                // Se è unknown, sostituiamo con un nero
                if(contains.equals("unknown")){
                    map[r][c] = UNKNOWN_COLOR;
                }
            }
            

            // Se contiene debris e injured è yes
            if (contains.equals("debris") && injured.equals("yes")) {
                map[r][c] += "_injured";
            }

            // Se injured è unknwon
            if(injured.equals("unknown")) {
                map[r][c] += "+question_mark";
            }

            // Se c'è il suono
            if (sound.equals("yes")) {
                map[r][c] += "+sound";
            }
        }
    }

    private void updateAgentStatus() throws CLIPSError{
        console.debug("Acquisizione posizione dell'agente...");
        int r = model.getRow() - 1;
        int c = model.getColumn() - 1;
        map[r][c] = "agent_" + model.getDirection() + "_" + model.getMode();
    }
//
//    private void updatePersonStatus() throws CLIPSError{
//        ArrayList<int[]> personPositions = model.getPersonPositions();
//        
//        for (int[] person : personPositions) {
//            int r = person[0];
//            int c = person[1];
//            map[r - 1][c - 1] = map[r - 1][c - 1] + "+person";
//        }
//    }
    
    @Override
    public BufferedImage[][] getIconMatrix() {
        images = MonitorImages.getInstance();

        if (map == null) {
            return null;
        }

        BufferedImage[][] iconMatrix = new BufferedImage[map.length][map[0].length];

        for (int i = map.length - 1; i >= 0; i--) {

            for (int j = 0; j < map[0].length; j++) {
                BufferedImage tmpImage;

                // Split the map string in arguments
                String[] curCel = map[i][j].split("\\+");

                // Background image is the first argument
                iconMatrix[i][j] = images.getImage(curCel[0]);

                // All the others arguments are overlaps
                for (int k = 1; k < curCel.length; k++) {
                    String curOverlap = curCel[k];

                    tmpImage = images.getImage(curOverlap);
                    iconMatrix[i][j] = images.overlapImages(tmpImage, iconMatrix[i][j]);
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




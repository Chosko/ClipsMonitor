package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observer;
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
    private final String UNDISCOVERED_COLOR = "rgba(0,0,0,0.3)";
    
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
            int c = new Integer(fact[RescueFacts.Cell.POSC.index()]) - 1;
            int r = new Integer(fact[RescueFacts.Cell.POSR.index()]) - 1;
            String contains = fact[RescueFacts.Cell.CONTAINS.index()];
            String injured = fact[RescueFacts.Cell.INJURED.index()];
            String discovered = fact[RescueFacts.Cell.DISCOVERED.index()];
            String checked = fact[RescueFacts.Cell.CHECKED.index()];
            String clear = fact[RescueFacts.Cell.CLEAR.index()];
            String previous = fact[RescueFacts.Cell.PREVIOUS.index()];

            //caso di default preleviamo il valore dello slot contains e lo applichiamo alla mappa
            map[r][c] = contains;  
            
            if(contains.equals("robot")){
                map[r][c] = previous;
            }

            // controlla se lo slot injured sia impostato a yes
            
            if (injured.equals("yes")) {
                map[r][c] += "_injured";
            }

            if (discovered.equals("no")) {
                map[r][c] += "+" + UNDISCOVERED_COLOR;
            }

            if (checked.equals("yes")) {
                map[r][c] += "+checked";
            }

            if (clear.equals("yes")) {
                map[r][c] += "+clear";
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

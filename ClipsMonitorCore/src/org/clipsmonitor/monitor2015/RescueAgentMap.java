package org.clipsmonitor.monitor2015;

import java.util.ArrayList;
import java.util.Observer;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2014/2015
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */
public class RescueAgentMap extends MonitorMap implements Observer {
    private final String UNKNOWN_COLOR = "#333333";
    private final String SOUND_COLOR = "rgba(0,70,255,0.3)";
    private final String WHITE = "#ffffff";

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueAgentMap() {
        super();
    }

    @Override
    protected void onDispose() {

    }

    @Override
    protected void initializeMap() throws CLIPSError {
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
    public void refreshMap() throws CLIPSError {
        updateKCells();
        updatePersonStatus();
        updateAgentStatus();
        updatePNodes();
        // debugMap("k-cell");
    }

    private void updateKCells() throws CLIPSError{
        console.debug("Aggiornamento mappa dell'agente in corso...");

        String[][] cellFacts = core.findAllFacts("AGENT", RescueFacts.KCell.factName(), "TRUE", RescueFacts.KCell.slotsArray());

        for (String[] fact : cellFacts) {

            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int c = new Integer(fact[RescueFacts.KCell.POSC.index()]) - 1;
            int r = new Integer(fact[RescueFacts.KCell.POSR.index()]) - 1;
            String contains = fact[RescueFacts.KCell.CONTAINS.index()];
            String injured = fact[RescueFacts.KCell.INJURED.index()];
            String sound = fact[RescueFacts.KCell.SOUND.index()];
            String discovered = fact[RescueFacts.KCell.DISCOVERED.index()];
            String checked = fact[RescueFacts.KCell.CHECKED.index()];
            String clear = fact[RescueFacts.KCell.CLEAR.index()];

            // Inseriamo nella mappa ciò che contiene
            map[r][c] = contains;

            if(contains.equals("robot")){
                map[r][c] = WHITE;
            }

            // Se è unknown, sostituiamo con un nero
            if(contains.equals("unknown")){
                map[r][c] = UNKNOWN_COLOR;
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

            if (discovered.equals("yes")) {
                map[r][c] += "+discovered";
            }

            if (checked.equals("yes")) {
                map[r][c] += "+checked";
            }

            if (clear.equals("yes")) {
                map[r][c] += "+clear";
            }
        }
    }

    private void updateAgentStatus() throws CLIPSError{
        console.debug("Acquisizione posizione dell'agente...");
        int r = model.getKRow() - 1;
        int c = model.getKColumn() - 1;
        map[r][c] = map[r][c] + "+agent_" + model.getKDirection() + "_" + model.getKMode();
        if(model.getBumped()){
          int [] offset = model.getOffset().get(model.getKDirection());
          map[r + offset[0]][c + offset[1]] = map[r + offset[0]][c + offset[1]] + "+bump";
        }

    }

    
    private void updatePNodes() throws CLIPSError{
        int r = model.getPRow() - 1;
        int c = model.getPColumn() - 1;

        console.debug("Acquisizione P-node...");
        if(r >= 0 && c >= 0){
            ArrayList<int[]> openNodes = model.getOpenNodes();
            ArrayList<int[]> closedNodes = model.getClosedNodes();
            for (int[] current : openNodes) {
                int nrow = current[0] - 1;
                int ncolumn = current[1] - 1;
                map[nrow][ncolumn] = map[nrow][ncolumn] + "+rgba(31, 17, 111,0.15)";
            }
            for (int[] current : closedNodes) {
                int nrow = current[0] - 1;
                int ncolumn = current[1] - 1;
                map[nrow][ncolumn] = map[nrow][ncolumn] + "+rgba(111,61,17,0.15)";
            }
            map[r][c] = map[r][c] + "+p_agent_" + model.getPDirection() + "_" + model.getPMode();
        }
    }


    public void updatePersonStatus() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti...");
        ArrayList<int[]> personPositions = model.getKPersonPostions();

        for (int[] person : personPositions) {
            int r = person[0] - 1;
            int c = person[1] - 1;
            map[r][c] = map[r][c] + "+person";
        }
    }
}

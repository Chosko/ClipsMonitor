package org.clipsmonitor.monitor2015;

import java.util.ArrayList;
import java.util.Observer;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */
public class RescueEnvMap extends MonitorMap implements Observer {    
    private final String UNKNOWN_COLOR = "#000000";
    private final String DISCOVERED_COLOR = "rgba(0,255,0,0.3)";
    private final String CHECKED_COLOR = "rgba(255,255,0,0.3)";
    private final String CLEAR_COLOR = "rgba(182,20,91,0.3)";
    
    public RescueEnvMap(){
        super();
    }
    
    @Override
    protected void onDispose() {
        console.debug("Dispose effettuato");
        String result = model.getResult();
        double score = model.getScore();
        @SuppressWarnings("UnusedAssignment")
        String advise = "";
        if (result.equals("disaster")) {
            advise = "Distaster \n";
        } else if (model.getTime() > model.getMaxDuration()) {
            advise = "Maxduration has been reached \n";
        } else {
            advise = "The agent says DONE.\n";
        }
        advise = advise + "Penalties: " + score;
        model.setAdvise(advise);
        this.setChanged();
        this.notifyObservers("advise");
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
    protected void refreshMap() throws CLIPSError {
        updateCells();
        updatePersonStatus();
        updateAgentStatus();
        if(model.getShowGoalEnabled()){
            updateGoal();
            updateGoalsToDo();
        }
        // debugMap("cell");
    }

    private void updateCells() throws CLIPSError {
        console.debug("Aggiornamento mappa reale in corso...");

        String[][] cellFacts = core.findAllFacts("ENV", RescueFacts.Cell.factName(), "TRUE", RescueFacts.Cell.slotsArray());

        for (String[] fact : cellFacts) {
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int c = new Integer(fact[RescueFacts.Cell.POSC.index()]) - 1;
            int r = new Integer(fact[RescueFacts.Cell.POSR.index()]) - 1;
            String contains = fact[RescueFacts.Cell.CONTAINS.index()];
            String injured = fact[RescueFacts.Cell.INJURED.index()];
            String previous = fact[RescueFacts.Cell.PREVIOUS.index()];
            String discovered = fact[RescueFacts.Cell.DISCOVERED.index()];
            String checked = fact[RescueFacts.Cell.CHECKED.index()];
            String clear = fact[RescueFacts.Cell.CLEAR.index()];
            
            //caso di default preleviamo il valore dello slot contains e lo applichiamo alla mappa
            map[r][c] = contains;  
            
            if(contains.equals("robot")){
                map[r][c] = previous;
            }

            // controlla se lo slot injured sia impostato a yes
            
            if (injured.equals("yes")) {
                map[r][c] += "_injured";
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

    public void updateAgentStatus() throws CLIPSError{
        console.debug("Acquisizione posizione dell'agente...");
        int r = model.getRow() - 1;
        int c = model.getColumn() - 1;
        map[r][c] = map[r][c] + "+agent_" + model.getDirection() + "_" + model.getMode();
        if(model.getBumped()){
          int [] offset = model.getOffset().get(model.getDirection());
          map[r + offset[0]][c + offset[1]] = map[r + offset[0]][c + offset[1]] + "+bump";
        }
    }


    public void updatePersonStatus() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti...");
        ArrayList<int[]> personPositions = model.getPersonPositions();
        
        for (int[] person : personPositions) {
            int r = person[0] - 1;
            int c = person[1] - 1;
            map[r][c] = map[r][c] + "+person";
        }
    }
    
    public void updateGoal()throws CLIPSError{
    
      console.debug("Acquisizione posizione del goal selezionato...");
      int [] goal = model.getGoalSelected();
      if(goal[1]!=0 && goal[1]!=0){
        int r = goal[0]-1;
        int c = goal[1]-1;
        map[r][c]+="+"+ images.creatergbafromName("green", 0.6);
        
        if (model.getTypeGoalSelected().equals("drill")){
          map[r][c] +="+D";
        }
         if (model.getTypeGoalSelected().equals("explore")
            || model.getTypeGoalSelected().equals("explore-sound")
           ){
          map[r][c] +="+E";
        }
        if (model.getTypeGoalSelected().equals("done")){
          map[r][c] +="+G";
        }
 
        if (model.getTypeGoalSelected().equals("discover")||  model.getTypeGoalSelected().equals("check")){
          map[r][c] +="+I";
        }
        if (model.getTypeGoalSelected().equals("unload")){
          map[r][c] +="+U";
        }
 
      }
    }
    
    public void updateGoalsToDo() throws CLIPSError{
      console.debug("Acquisizione posizione dei goal nella lista to-do...");
      ArrayList<int []> goals = model.getGoalsToDo();
      for(int [] goal : goals){
         int r = goal[0]-1;
         int c = goal[1]-1;
         map[r][c]+="+"+ images.creatergbafromName("orange", 0.5);
      }
    }
}

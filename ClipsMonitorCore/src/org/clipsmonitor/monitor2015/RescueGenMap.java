/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import org.clipsmonitor.core.MonitorGenMap;
import java.util.LinkedList;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.monitor2015.RescueFacts;

/*
 * Classe che definisce il concetto di scena all'interno del progetto e tutti i metodi per accedervi e
 * modificarla. Inoltre permette la possibilità di salvare e caricare le mappe per poter essere poi
 * successiviamente utilizzate dal simulatore
 *
 * @author Tobia Giani, Alessandro Basile, Marco Corona
 */
public class RescueGenMap extends MonitorGenMap {

    private static RescueGenMap instance;

    private String loaded; // stato iniziale del robot

    /*
     La classe viene definita singleton 
     */
    public static RescueGenMap getInstance() {
        if (instance == null) {
            instance = new RescueGenMap();
            instance.init();
        }
        return instance;
    }

    private RescueGenMap() {

    }

    @Override
    public void init() {
        this.img = MonitorImages.getInstance();
        this.log="";
        log("Inizializzazione del Map generator in corso...");
        this.NumCellX = 0;
        this.NumCellY = 0;
        this.MapWidth = 0;
        this.MapHeight = 0;
        this.CellHeight = 0;
        this.CellWidth = 0;
        this.direction = "north";
        this.loaded = "unloaded";
        this.maxduration = 300;
        this.mode = "scene";
        this.personName = "person";
        this.defaulagentcondition = "gate+agent_north_unloaded";
        this.defaultagentposition = new int[2];
        this.defaultagentposition[0] = 3;
        this.defaultagentposition[1] = 2;
        this.agentposition = new int[2];
        this.agentposition[0] = this.defaultagentposition[0];
        this.agentposition[1] = this.defaultagentposition[1];
        this.NumPerson = 0;
        this.Persons = new LinkedList<Person>();
        this.setKeyMap = new String[]
        { "agent_north_unloaded", 
          "agent_north_loaded",
          "agent_west_unloaded",
          "agent_west_loaded", 
          "agent_east_unloaded",
          "agent_east_loaded",
          "agent_south_unloaded", 
          "agent_south_loaded", 
          "gate", 
          "empty", 
          "outdoor", 
          "wall", 
          "debris", 
          "debris_injured"
        };
        this.setKeyColor=new String[]
        {
          "green",
          "blue",
          "red",
          "yellow",
          "black",
          "magenta",
          "orange"
        };
        
        this.MaxNumPerson = this.setKeyColor.length;
        log("Map generator inizializzato correttamente");
    }

    /**
     * Inizializzazione della scena eseguita mettendo nel perimetro della scena
     * l'outdoor e riempiendo il resto con le celle empty. Il metodo risulta
     * chiaramente adattato ai vincoli del progetto e a come si dovrebbe
     * presentare di default uno scenario
     *
     * @param scene matrice di stringe che dovrà essere riempita
     */
    
    
   /**
    * Inizializzazione della scena eseguita mettendo nel perimetro della scena
    * l'outdoor e riempiendo il resto con le celle empty. Il metodo risulta
    * chiaramente adattato ai vincoli del progetto e a come si dovrebbe
    * presentare di default uno scenario
    *
    * @param scene matrice di stringe che dovrà essere riempita
    */


    @Override
    public void initScene(String[][] scene) {

        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j] = "outdoor";
                } else if (i == 1 || i == scene.length - 2 || j == 1 || j == scene[0].length - 2) {
                    scene[i][j] = "wall";
                } else {

                    scene[i][j] = "empty";
                }
            }
        }

        scene[this.agentposition[0]][this.agentposition[1]] = "gate" + "+" + "agent_" + direction + "_" + loaded;
        this.move = this.clone(scene);
    }

  
    /**
     * Genera una stringa rappresentante la history da scrivere successivamante
     * su un file di testo Il testo prodotto contiene le informazioni
     * riguardanti l'agente nel suo stato iniziale, la durata massima messa a
     * disposizione dell'agente per eseguire le sue operazioni e l'elenco
     * completo di tutti i path che sono stati definiti per i vari agenti
     *
     * @return la stringa rappresentante la history
     */
    @Override
    public String exportHistory() {
        String history = "";
        history += "(maxduration " + this.maxduration + ") \n\n";
        int[] agentPos = GenMapToMap(this.agentposition);
        
        // posizione iniziale dell'agente
        history += "(initial_agentposition ( pos-r " + agentPos[0]  + ")";
        history += "( pos-c " + agentPos[1] + ")";
        history += "(direction " + this.direction + ")) \n\n";
        
        for(Person elem : Persons){
            String personName = elem.getColor();
            Path firstPath = getPathByName(personName + "_0");
            StepMove firstMove = firstPath.getMoves().getFirst();
            int[] firstMovePos = GenMapToMap(firstMove.getX(), firstMove.getY());
            int step =firstMove.getStep();
            int r = firstMovePos[0];
            int c = firstMovePos[1];
            history += RescueFacts.PersonStatus.getPersonStatus(personName,step , r, c);
        }
        history += "\n";
        
        Path[] paths = this.getPaths(-1);
        
        for(Path elem : paths){
            String name = elem.getName();
            String person = name.substring(0,name.indexOf("_"));
            int step = elem.getStartStep();
            history += RescueFacts.PersonMove.getPersonMove(step, person,name);
        }
        
        history +="\n";
        
        
        
        for(Path elem : paths){
            String name = elem.getName();
            String person = name.substring(0,name.indexOf("_"));
            int step = elem.getStartStep();
            LinkedList<StepMove> slist = elem.getMoves(); 
            for( StepMove s : slist){
                int idstep = s.getStep() - step;
                int[] stepPos = GenMapToMap(s.getX(), s.getY());
                history += "( move-path " + name + " " + idstep + " " + person 
                            + " " + stepPos[0] + " " + stepPos[1] + " ) \n"; 
            
            }
        
        }
        
        return history;
    }

    /**
     * Genera il testo contenente i fatti che descrivono una possibile mappa. Il
     * metodo sfrutta un metodo ausiliario getEnvCell per costruire le stringhe
     * dei fatti che descrivono le varie celle
     *
     * @return la stringa con la descrizione di tutte le celle da salvare su un
     * file di testo
     */
    @Override
    public String exportScene() {

        //variabili per impostare la posizione delle componenti
        String s = "";
        
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                int[] realCellPos = GenMapToMap(i,j);
                boolean testInjured = scene[i][j].contains("injured");
                s += RescueFacts.RealCell.getRealCell(realCellPos[0],realCellPos[1], scene[i][j], testInjured);
            }
        }
        return s;
    }

    /**
     * Verifica se la posizione dell'agente richiesta risulta essere compatibile
     * rispetto ai vincoli del progetto. Ritorna true se la condizione è
     * rispettata
     *
     * @param mapPos stringa da valutare come effettivamente valida
     * @return true se risulta una posizione valida per il robot, false
     * altrimenti
     */
    @Override
    public boolean RobotPositionIsValid(String mapPos) {

        return !mapPos.contains("debris") && !mapPos.contains("wall") && !mapPos.contains("outdoor");

    }

    /*
     *  Verifica se la posizione dell'agente richiesta risulta essere compatibile
     *  rispetto ai vincoli del progetto. Ritorna true se la condizione è rispettata
     */
    @Override
    public boolean PersonPositionIsValid(String mapPos) {

        return !mapPos.contains("debris") && !mapPos.contains("wall");

    }

    /**
     * Setta tutti i paramentri del modello relativi al robot, la sua posizione
     * e eventualmente se risulta essere carico o scarico
     *
     * @param state stringa rappresentante il robot
     * @param x riga della cella
     * @param y colonna della cella
     */
    @Override
    public void SetRobotParams(String state, int x, int y) {

        this.agentposition[0] = x;
        this.agentposition[1] = y;

        String[] params = state.split("_");
        String AgentDirection = params[1];
        String AgentLoaded = params[2];

        this.direction = AgentDirection;
        this.loaded = AgentLoaded;

    }
    
    

    /**
     * Metodo per la creazione della matrice di icone da disegnare sulla mappa
     * del generatore quando questo è impostato in modalità scene.Il metodo si occupa 
     * di creare le icone con l'overlap.
     *
     * @return la matrice di icone da disegnare sul pannello
     */
    
    
    
    public boolean getEmptyPerson() {
    
      return this.Persons.isEmpty();
    }    

    
    public void error(String message){
      this.AppendLogMessage(message,"error");
    }
    
    
    public void log(String message){
      this.AppendLogMessage(message, "log");
    }

  public void clear() {
     instance=null;
  }
}

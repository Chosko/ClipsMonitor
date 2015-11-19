package org.clipsmonitor.monitor2015;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorModel;
import org.clipsmonitor.core.MonitorCore;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsModel specifica per il progetto Rescue 2014/2015. 
 * L'oggetto fondamentale è il map, una matrice che in ogni elemento
 * contiene la stringa corrispondente al contenuto.
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto
 * @edit by Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */

public class RescueModel extends MonitorModel {

    
    private String direction;
    private String mode;
    private String loaded; // presenza di un carico
    private String kdirection;
    private String kmode;
    private String kloaded; // presenza di un carico
    private int krow;
    private int kcolumn;
    private int kstep;
    private int ktime;
    
    private ClipsConsole console;
    private static RescueModel instance;
    private String advise;
    private Map<String, MonitorMap> maps;
    private ArrayList<int[]> personPositions;
    
    /*costanti enumerative intere per un uso più immediato delle posizioni all'interno 
     degli array che definiscono i fatti di tipo (real-cell)*/
    
    
    
    
    /**
     * Singleton
     */
    public static RescueModel getInstance(){
        if(instance == null){
            instance = new RescueModel();
        }
        return instance;
    }
    
    public static void clearInstance() {
        if(instance != null){
            instance.advise = null;
            instance.direction = null;
            instance.mode = null;
            instance.maps = null;
            instance.durlastact = 0;
            instance.time = null;
            instance.step = null;
            instance.maxduration = null;
            instance.result = null;
            instance.score = 0;
            instance.loaded = null;
            instance.console = null;
            instance.row = 0;
            instance.column = 0;
            instance.krow = 0;
            instance.kcolumn = 0;
            instance.kdirection = null;
            instance.kmode = null;
            instance.kloaded = null;
            instance.kstep = 0;
            instance.ktime = 0;
            instance.personPositions = null;
            instance = null;
        }
    }
    
    /**
     * Costruttore del modello per il progetto Monitor
     *
     */
    private RescueModel() {
        super();
        console = ClipsConsole.getInstance();
        MonitorCore.getInstance().registerModel(this);
        maps = new HashMap<String, MonitorMap>();
        personPositions = new ArrayList<int[]>();
    }

    /**
     * Inizializza il modello in base al contenuto del file clips caricato.
     */
    @Override
    protected synchronized void initModel() {
        result = "no";
        time = 0;
        step = 0;
        maxduration = Integer.MAX_VALUE;
        try {
            console.debug("Esecuzione degli step necessari ad aspettare che l'agente sia pronto.");
            
            /* Eseguiamo un passo fino a quando il fatto init-agent viene dichiarato
             * con lo slot (done yes): il mondo è pronto.
             */
           
            
            core.run();
            

            maxduration = new Integer(core.findOrderedFact("MAIN", "maxduration"));
            for (MonitorMap map : maps.values()) {
                map.initMap();
            }
            console.debug("Il modello è pronto.");

        } catch (CLIPSError ex) {
            console.error("L'inizializzazione è fallita:");
            ex.printStackTrace();
            console.error(ex);
        }
    }
    
    /**
     * Register a map to a MapTopComponent
     * @param target
     * @param map 
     */
    
    
    public void registerMap(String target, MonitorMap map){
        maps.put(target, map);
        this.setChanged();
        this.notifyObservers(target);
    }
    
    
    
    public MonitorMap getMapToRegister(String target){
        return maps.get(target);
    }
    
    /**
     * Aggiorna la mappa leggendola dal motore clips. Lanciato ogni volta che si
     * compie un'azione.
     *
     * @throws ClipsExceptionF
     */
    @Override
    protected synchronized void updateModel() throws CLIPSError {

        console.debug("Aggiornamento del modello...");
        
        // Update the agent
        updateAgent();
        
        // Update the agent's perception about itself
        updateKAgent();
        
        // Update the other agents
        updatePeople();
        
        // Update all the maps (they read the values created by updateAgent)
        for(MonitorMap map : maps.values()){
            map.updateMap();
        }

        // Update the simulation status
        updateStatus();
    }

    private void updateAgent() throws CLIPSError{
        String[] robot = core.findFact("ENV", RescueFacts.AgentStatus.factName(), "TRUE", RescueFacts.AgentStatus.slotsArray());
        if (robot[0] != null) { //Se hai trovato il fatto
            step = new Integer(robot[RescueFacts.AgentStatus.STEP.index()]);
            time = new Integer(robot[RescueFacts.AgentStatus.TIME.index()]);
            row = new Integer(robot[RescueFacts.AgentStatus.POSR.index()]);
            column = new Integer(robot[RescueFacts.AgentStatus.POSC.index()]);
            direction = robot[RescueFacts.AgentStatus.DIRECTION.index()];
            loaded = robot[RescueFacts.AgentStatus.LOADED.index()];
            mode = loaded.equals("yes") ? "loaded" : "unloaded";
        }
    }
    
    
    private void updateKAgent() throws CLIPSError{
        String[] robot = core.findFact("AGENT", RescueFacts.KAgent.factName(), "TRUE", RescueFacts.KAgent.slotsArray());
        if (robot[0] != null) { //Se hai trovato il fatto
            kstep = new Integer(robot[RescueFacts.KAgent.STEP.index()]);
            ktime = new Integer(robot[RescueFacts.KAgent.TIME.index()]);
            krow = new Integer(robot[RescueFacts.KAgent.POSR.index()]);
            kcolumn = new Integer(robot[RescueFacts.KAgent.POSC.index()]);
            kdirection = robot[RescueFacts.KAgent.DIRECTION.index()];
            kloaded = robot[RescueFacts.KAgent.LOADED.index()];
            kmode = kloaded.equals("yes") ? "loaded" : "unloaded";
        }
    }
    
    private void updatePeople() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti...");
        String[][] persons = core.findAllFacts("ENV", RescueFacts.PersonStatus.factName(), "TRUE", RescueFacts.PersonStatus.slotsArray());
        personPositions.clear();
        if (persons != null) {
            for (int i = 0; i < persons.length; i++) {
                if(persons[i][0] != null){
                    int r = new Integer(persons[i][RescueFacts.PersonStatus.POSR.index()]);
                    int c = new Integer(persons[i][RescueFacts.PersonStatus.POSC.index()]);
                    personPositions.add(new int[]{r, c});
                }
            }
        }
    }

    private void updateStatus() throws CLIPSError{
        String[] status = core.findFact("MAIN", RescueFacts.Status.factName(), "TRUE", RescueFacts.Status.slotsArray());
        if (status[0] != null) {
            step = new Integer(status[RescueFacts.Status.STEP.index()]);
            time = new Integer(status[RescueFacts.Status.TIME.index()]);
            result = status[RescueFacts.Status.RESULT.index()];
            console.debug("Step: " + step + " Time: " + time + " Result: " + result);
        }
    }
    
    public ArrayList<int[]> getPersonPositions(){
        return personPositions;
    }

    @Override
    protected boolean hasDone() {
        // ritorna true se time>=maxduration o se result non è "no" e quindi è "disaster" o "done"
        return time >= maxduration || !result.equalsIgnoreCase("no");
    }

    @Override
    protected void dispose() {
        try{
            score = new Integer(core.findOrderedFact("MAIN", "penalty"));
        }
        catch(CLIPSError ex){
            console.error(ex);
        }
    }

    public String[][] findAllFacts(String template, String conditions, String[] slots) throws CLIPSError{
        String[][] empty = {};
        return core != null ? core.findAllFacts(template, conditions, slots) : empty;
    }

    public String getLoaded() {
        return loaded;
    }



    public String getMode() {
       return mode;
    }
    


    public void setAdvise(String advise) {
        this.advise = advise;
    }

    public String getAdvise() {
        return this.advise;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public String getKDirection() {
        return kdirection;
    }
    
    public String getKLoaded() {
        return kloaded;
    }

    public String getKMode() {
        return kmode;
    }
    
    public int getKRow(){
        return krow;
    }
    
    public int getKColumn(){
        return kcolumn;
    }

}

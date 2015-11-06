package org.clipsmonitor.monitor2015;

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
    private ClipsConsole console;
    private static RescueModel instance;
    private String advise;
    private Map<String, MonitorMap> maps;
    
    
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
        instance = null;
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
            long run_feedback;
            String[] initAgent;
            do {
                run_feedback = core.run(1);
                initAgent = core.findFact("AGENT", "init-agent", "TRUE", new String[]{"done"});
            } while (run_feedback == 1 && (initAgent[0] == null || !initAgent[0].equals("yes")));
            /* Facciamo ancora un core.run(1) per allinearci al nostro step.
             * Questo è molto specifico a come funziona il nostro codice.
             */
            core.run(1);

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

    public enum Cellslots{
    
        POSC (0),
        POSR (1),
        CONTAINS(2),
        INJURIED (3),
        DISCOVERED (4),
        CHECKED (5), 
        CLEAR(6);
        
        
        private final int slot;
        
        Cellslots(int num){
            this.slot=num;
        }
    
        int slot(){
            return slot;
        }
    }
    
    /**
     * Aggiorna la mappa leggendola dal motore clips. Lanciato ogni volta che si
     * compie un'azione.
     *
     * @throws ClipsExceptionF
     */
    @Override
    protected synchronized void updateModel() throws CLIPSError {

        console.debug("Aggiornamento modello in corso...");
        String[] kcellArray = {"pos-r", "pos-c", "contains"};

        String[][] kcellFacts = core.findAllFacts("AGENT", "K-cell", "TRUE", kcellArray);
        
        String[] arrayRobot = {"step", "time", "pos-r", "pos-c", "direction", "loaded"};
        String[] robot = core.findFact("ENV", "agentstatus", "TRUE", arrayRobot);
        if (robot[0] != null) { //Se hai trovato il fatto
            step = new Integer(robot[0]);
            time = new Integer(robot[1]);
            row = new Integer(robot[2]);
            column = new Integer(robot[3]);
            direction = robot[4];
            loaded = robot[5];
            mode = loaded.equals("yes") ? "loaded" : "unloaded";

            console.debug("Acquisizione background dell'agente...");
        }
        /*
            prendo tutti i fatti di tipo kcell e valuto se esistono celle contenenti nello slot
            unknown 
        */
        
//        
//        for (String[] fact : kcellFacts) {
//            int r = new Integer(fact[Cellslots.POSC.slot()]) - 1;
//            int c = new Integer(fact[Cellslots.POSR.slot()]) - 1;
//            if (fact[Cellslots.CONTAINS.slot()].equals("unknown")) {
//                map[r][c] += "_undiscovered";
//            }
//            
//        }
       
        // Update all the maps!
        for(MonitorMap map : maps.values()){
            map.updateMap();
        }

        // ######################## FATTO agentstatus ##########################
        
        // ######################## FATTO status ##########################
        String[] arrayStatus = {"step", "time", "result"};
        String[] status = core.findFact("MAIN", "status", "TRUE", arrayStatus);
        if (status[0] != null) {
            step = new Integer(status[0]);
            time = new Integer(status[1]);
            result = status[2];
            console.debug("Step: " + step + " Time: " + time + " Result: " + result);
        }

        console.debug("Aggiornamento modello completato");
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
    
    public synchronized String getDirection() {
        return direction;
    }


}

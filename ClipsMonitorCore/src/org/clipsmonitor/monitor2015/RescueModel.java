package org.clipsmonitor.monitor2015;

import java.util.HashMap;
import java.util.Map;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsModel;
import org.clipsmonitor.core.MonitorCore;
import org.clipsmonitor.core.MonitorMap;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.Checked;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.Clear;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.Contains;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.Discovered;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.Injured;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.PosC;
import static org.clipsmonitor.monitor2015.RescueModel.cellslots.PosR;

/**
 * L'implementazione della classe ClipsModel specifica per il progetto Rescue 2014/2015. 
 * L'oggetto fondamentale è il map, una matrice che in ogni elemento
 * contiene la stringa corrispondente al contenuto.
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto
 * @edit by Enrico Mensa, Matteo Madeddu, Davide Dell'Anna
 */

public class RescueModel extends ClipsModel {

    
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
        instance.map = null;
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

            console.debug("Inizializzazione del modello (mappa).");
            String[] array = {"pos-r", "pos-c", "contains", "injured"};
            String[][] mp = core.findAllFacts("ENV", "real_cell", "TRUE", array);
            int maxr = 0;
            int maxc = 0;
            for (int i = 0; i < mp.length; i++) {
                int r = new Integer(mp[i][0]);
                int c = new Integer(mp[i][1]);
                if (r > maxr) {
                    maxr = r;
                }
                if (c > maxc) {
                    maxc = c;
                }
            }
            map = new String[maxr][maxc];//Matrice di max_n_righe x max_n_colonne
            for (String[] mp1 : mp) {
                int r = new Integer(mp1[0]);
                int c = new Integer(mp1[1]);
                map[r - 1][c - 1] = mp1[2]; //contains
                //String m = cellFacts[i][3];
                if (mp1[3].equals("yes")) {
                    map[r - 1][c - 1] += "_injured";
                }
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

    protected enum cellslots{
    
        PosC (0),
        PosR (1),
        Contains(2),
        Injured (3),
        Discovered (4),
        Checked (5), 
        Clear(6);
        
        
        private final int slot;
        
        cellslots(int num){
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
    protected synchronized void updateMap() throws CLIPSError {

        // ######################## FATTI DI TIPO cell ##########################
        console.debug("Aggiornamento modello mappa in corso...");
        String[] cellArray = {"pos-r", "pos-c", "contains", "injured", "discovered", "checked", "clear"};
        String[] kcellArray = {"pos-r", "pos-c", "contains"};

        //Per ogni cella prendiamo il nuovo valore e lo aggiorniamo
        String[][] cellFacts = core.findAllFacts("ENV", "cell", "TRUE", cellArray);
        String[][] kcellFacts = core.findAllFacts("AGENT", "K-cell", "TRUE", kcellArray);
        

        for (String[] fact : cellFacts) {
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int r = new Integer(fact[PosC.slot()]);
            int c = new Integer(fact[PosR.slot()]);

            //caso di default preleviamo il valore dello slot contains e lo applichiamo alla mappa
            map[r - 1][c - 1] = fact[Contains.slot()];  
            
            // controlla se lo slot injured sia impostato a yes
            
            if (fact[Injured.slot()].equals("yes")) {
                map[r - 1][c - 1] += "_injured";
            }
            /*
                Aggiorno la mappa in modo da valutare le celle di cui il robot ha fatto già precedentemente
                l'inform: i casi in cui avviene sono :
                 - se la cella contiene debris allora o vale che l'ho scoperta ma non ci sono feriti
                   oppure ci sono feriti e l'ho controllata
                 - se la cella non contiene nulla e ho detto che risulta clear
            */
            
            if ((fact[Contains.slot()].equals("debris") && 
                 (fact[Discovered.slot()].equals("yes") || 
                  fact[Checked.slot()].equals("yes"))) || 
                  (fact[Contains.slot()].equals("empty") && fact[Clear.slot()].equals("yes"))) {
                map[r - 1][c - 1] += "_informed";
            }
           
        }
        
        /*
            prendo tutti i fatti di tipo kcell e valuto se esistono celle contenenti nello slot
            unknown 
        */
        
        
        for (String[] fact : kcellFacts) {
            int r = new Integer(fact[PosC.slot()]) - 1;
            int c = new Integer(fact[PosR.slot()]) - 1;
            if (fact[Contains.slot()].equals("unknown")) {
                map[r][c] += "_undiscovered";
            }
            
        }
       
          
        console.debug("Modello aggiornato.");

        // ######################## FATTO agentstatus ##########################
        console.debug("Acquisizione posizione dell'agente...");
        String[] arrayRobot = {"step", "time", "pos-r", "pos-c", "direction", "loaded"};
        String[] robot = core.findFact("ENV", "agentstatus", "TRUE", arrayRobot);
        if (robot[0] != null) { //Se hai trovato il fatto
            step = new Integer(robot[0]);
            time = new Integer(robot[1]);
            int r = new Integer(robot[2]);
            row = r;
            int c = new Integer(robot[3]);
            column = c;
            direction = robot[4];
            loaded = robot[5];
            mode = loaded.equals("yes") ? "loaded" : "unloaded";

            console.debug("Acquisizione background dell'agente...");

            String[] arrayRobotBackground = {"pos-r", "pos-c", "contains", "injured", "previous", "clear"};
            String[] robotBackground = core.findFact("ENV", "cell", "and (eq ?f:pos-r " + Integer.toString(r) + ") (eq ?f:pos-c " + Integer.toString(c) + ")", arrayRobotBackground);

            //Nel modello abbiamo la stringa agent_background, la quale verrà interpretata nella View (updateMap())
            
            String background = robotBackground[4];

            map[r - 1][c - 1] = "agent_" + background;
            if (robotBackground[5].equals("yes")) {
                map[r - 1][c - 1] += "_informed";
            }
        }
        console.debug("Aggiornato lo stato dell'agente.");

        // ######################## FATTI personstatus ##########################
        console.debug("Acquisizione posizione degli altri agenti...");
        String[] arrayPersons = {"step", "time", "ident", "pos-r", "pos-c", "activity", "move"};
        String[][] persons = core.findAllFacts("ENV", "personstatus", "TRUE", arrayPersons);

        if (persons != null) {
            for (String[] person : persons) {
                if (person[0] != null) {
                    //Se hai trovato il fatto 
                    int person_r = new Integer(person[3]);
                    int person_c = new Integer(person[4]);
                    String ident = person[2];
                    String[] arrayPersonBackground = {"pos-r", "pos-c", "contains", "injured", "previous", "clear"};
                    String[] personBackground = core.findFact("ENV", "cell", "and (eq ?f:pos-r " + Integer.toString(person_r) + ") (eq ?f:pos-c " + Integer.toString(person_c) + ")", arrayPersonBackground);
                    //Nel modello abbiamo la stringa agent_background_ident, la cosa verrà interpretata nella View (updateMap())
                    String background = personBackground[4];
                    String oldValue = map[person_r - 1][person_c - 1];
                    map[person_r - 1][person_c - 1] = "person_" + background + "_" + ident;
                    if (personBackground[5].equals("yes")) {
                        map[person_r - 1][person_c - 1] += "_informed";
                    }
                    if (oldValue.contains("undiscovered")) {
                        map[person_r - 1][person_c - 1] += "_undiscovered";
                    }
                }
            }
        }
        console.debug("Aggiornati gli stati degli altri agenti.");

        // ######################## FATTO status ##########################
        String[] arrayStatus = {"step", "time", "result"};
        String[] status = core.findFact("MAIN", "status", "TRUE", arrayStatus);
        if (status[0] != null) {
            step = new Integer(status[0]);
            time = new Integer(status[1]);
            result = status[2];
            console.debug("Step: " + step + " Time: " + time + " Result: " + result);
        }

        console.debug("Aggiornato lo stato del mondo.");
        console.debug("Aggiornamento completato.");
    }

    /**
     * metodo per ottenere la mappa dell'ambiente come vista nel modulo ENV.
     *
     * @return la mappa come matrice di stringhe
     */
    public synchronized String[][] getEnvMap() {
        String[][] value = map.clone();
        for (int i = 0; i < map.length; i++) {
            value[i] = map[i].clone();
        }
        return value;
    }

    /**
     * metodo per ottenere il verso in cui è girato l'agente
     *
     * @return up, down, left, right
     */
    

    @Override
    protected void setup(){
        initModel();
    }

    @Override
    protected void action() {
        try{
            updateMap();
        }
        catch (CLIPSError ex){
            console.error(ex);
        }
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

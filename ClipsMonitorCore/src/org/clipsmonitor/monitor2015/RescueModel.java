package org.clipsmonitor.monitor2015;

import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsModel;

/**
 * L'implementazione della classe ClipsModel specifica per il progetto Waitor
 * 2013/2014. L'oggetto fondamentale è il map, una matrice che in ogni elemento
 * contiene la stringa corrispondente al contenuto.
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto
 * @edit by Enrico Mensa, Matteo Madeddu, Davide Dell'Anna
 */
public class RescueModel extends ClipsModel {

    private String[][] map;
    private String direction;
    private int durlastact;
    private Integer time;
    private Integer step;
    private Integer maxduration;
    private String result;
    private int score;
    private String l_f_waste; // presenza di spazzatura food
    private String l_d_waste; // presenza di spazzatura drink
    private Integer l_drink; // quantità di drink contenuta
    private Integer l_food; //quantità di food contenuta
    private ClipsConsole console;
    private static RescueModel instance;
    
    /**
     * Singleton
     */
    public static RescueModel getInstance(){
        if(instance == null){
            instance = new RescueModel();
        }
        return instance;
    }
    
    /**
     * Costruttore del modello per il progetto Monitor
     *
     */
    private RescueModel() {
        super();
        console = ClipsConsole.getInstance();
    }

    /**
     * Inizializza il modello in base al contenuto del file clips caricato.
     *
     *
     */
    private synchronized void init() {
        result = "no";
        time = 0;
        step = 0;
        maxduration = Integer.MAX_VALUE;
        try {
            console.debug("Esecuzione del primo passo al fine di caricare i fatti relativi alla mappa.");
            core.evaluate("MAIN", "(run 1)"); //Eseguiamo la prima regola create-world1 per far sì che venga eseguita la regola di init della mappa [POCO GENERALE]
            maxduration = new Integer(core.findOrderedFact("MAIN", "maxduration"));

            console.debug("Inizializzazione del modello (mappa).");
            String[] array = {"pos-r", "pos-c", "contains"};
            String[][] mp = core.findAllFacts("MAIN", "prior-cell", "TRUE", array);
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
            map = new String[maxr][maxc];
            for (int i = 0; i < mp.length; i++) {
                int r = new Integer(mp[i][0]);
                int c = new Integer(mp[i][1]);
                map[r - 1][c - 1] = mp[i][2];
                //System.out.println(mp[i][2]);  // COMMENTAMI
            }
            console.debug("Il modello è pronto.");

        } catch (CLIPSError ex) {
            console.error("L'inizializzazione è fallita:");
            ex.printStackTrace();
            console.error(ex);
        }
    }

    /**
     * Aggiorna la mappa leggendola dal file clips. Lanciato ogni volta che si
     * compie un'azione.
     *
     * @throws ClipsException
     */
    private synchronized void updateMap() throws CLIPSError {

        // ######################## FATTI DI TIPO cell ##########################
        console.debug("Aggiornamento modello mappa in corso...");
        String[] array = {"pos-r", "pos-c", "contains"};
        String[][] mp;

        //Per ogni cella prendiamo il nuovo valore e lo aggiorniamo
        mp = core.findAllFacts("ENV", "cell", "TRUE", array);
        for (String[] mp1 : mp) {
            int r = new Integer(mp1[0]);
            int c = new Integer(mp1[1]);

            //caso di default
            map[r - 1][c - 1] = mp1[2]; //prendiamo il valore

            //System.out.println(map[r - 1][c - 1]);
        }
        console.debug("Modello aggiornato.");

        // ######################## FATTO agentstatus ##########################
        console.debug("Acquisizione posizione dell'agente...");
        String[] arrayRobot = {"step", "time", "pos-r", "pos-c", "direction", "l-drink", "l-food", "l_d_waste", "l_f_waste"};
        String[] robot = core.findFact("ENV", "agentstatus", "TRUE", arrayRobot);
        if (robot[0] != null) { //Se hai trovato il fatto
            step = new Integer(robot[0]);
            time = new Integer(robot[1]);
            int r = new Integer(robot[2]);
            int c = new Integer(robot[3]);
            direction = robot[4];
            l_drink = new Integer(robot[5]);
            l_food = new Integer(robot[6]);
            l_d_waste = robot[7];
            l_f_waste = robot[8];

            //Nel modello abbiamo la stringa agent_background, la cosa verrà interpretata nella View (updateMap())
            String background = map[r - 1][c - 1];
            map[r - 1][c - 1] = "agent_" + background;
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
                    //Nel modello abbiamo la stringa agent_background_ident, la cosa verrà interpretata nella View (updateMap())
                    String background = map[person_r - 1][person_c - 1];
                    map[person_r - 1][person_c - 1] = "person_" + background + "_" + ident;
                }
            }
        }
        console.debug("Aggiornato lo stato dell'agente.");

        // ######################## FATTO status ##########################
        String[] arrayStatus = {"step", "time", "result"};
        String[] status = core.findFact("MAIN", "status", "TRUE", arrayStatus);
        if (status[0] != null) {
            step = new Integer(status[0]);
            time = new Integer(status[1]);
            result = status[2];
            console.debug("Step: " + step + " Time: " + time + " Result: " + result);
        }

        // ######################## FATTO tablestatus ##########################
        String[] tableStatus = {"step", "table-id", "clean"};
        String[][] tables = core.findAllFacts("ENV", "tablestatus", "TRUE", tableStatus);
        if (tables != null) {
            //Per ogni tavolo
            for (String[] table : tables) {

                if (table[0] != null) { //bisogna fare qualcosa solo se non è pulito
                    String table_status = table[2];
                    String table_id = table[1];

                    if (table_status.equals("yes")) {
                        table_status = "clean";
                    } else {
                        table_status = "dirty";
                    }

                    //Recupera il fatto relativo al tavolo con id table_id
                    String[] that_table_slots = {"table-id", "pos-r", "pos-c"};
                    String[] that_table = core.findFact("ENV", "Table", "eq ?f:table-id " + table_id, that_table_slots);

                    //Prendiamo le posizioni
                    int table_r = new Integer(that_table[1]);
                    int table_c = new Integer(that_table[2]);

                    map[table_r - 1][table_c - 1] = "Table" + "_" + table_status + "_" + table_id;

                    console.debug("Table-id: " + table_id + " at position (" + table_r + "," + table_c + ") is " + table_status);
                }
            }
        }

        console.debug("Aggiornato lo stato del mondo.");
        console.debug("Aggiornamento completato.");
    }

    /**
     * metodo per ottenere la mappa dell'ambiente come vista nel modulo ENV.
     *
     * @return la mappa come matrice di stringhe
     */
    public synchronized String[][] getMap() {
        return map;
    }

    /**
     * metodo per ottenere il verso in cui è girato l'agente
     *
     * @return up, down, left, right
     */
    public synchronized String getDirection() {
        return direction;
    }

    /**
     * metodo per ottenere il punteggio dell'agente totalizzato a seguito delle
     * sue azioni
     *
     * @return il punteggio come intero
     */
    public synchronized int getScore() {
        return score;
    }

    /**
     * metodo per ottenere il motivo della terminazione dell'ambiente
     *
     * @return disaster, done
     */
    public synchronized String getResult() {
        return result;
    }

    /**
     * metodo da chiamare per ottenere il turno attuale
     *
     * @return il turno attuale come intero
     */
    public synchronized int getTime() {
        return time;
    }

    /**
     * metodo da chiamare per ottenere il turno attuale
     *
     * @return il turno attuale come intero
     */
    public synchronized int getStep() {
        return step;
    }

    /**
     * metodo per ottenere il tempo massimo a disposizione dell'agente
     *
     * @return il tempo massimo come intero
     */
    public synchronized int getMaxDuration() {
        return maxduration;
    }

    /**
     * metodo per ottenere il campo dur-last-act
     *
     * @return il tempo massimo come intero
     */
    public synchronized int getDurLastAct() {
        return durlastact;
    }

    @Override
    protected void setup(){
        init();
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
        //System.out.println("=======>Questo è quanto vale result: "+result);

//ritorna true se time==maxduration o se result non e' "no" e quindi e' "disaster" o "done"
        // CHIEDERE AL PROF: aggiungere un default per result (di valore nil nel metodo creation5)
        //if (time >= maxduration || (!(result.equalsIgnoreCase("no")) && !(result.equalsIgnoreCase("nil")))) {
        if (time >= maxduration || !result.equalsIgnoreCase("no")) {
            return true;
        }
        return false;
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
        return core.findAllFacts(template, conditions, slots);
    }

    public String getL_f_waste() {
        return l_f_waste;
    }

    public String getL_d_waste() {
        return l_d_waste;
    }

    public Integer getL_drink() {
        return l_drink;
    }

    public Integer getL_food() {
        return l_food;
    }
}

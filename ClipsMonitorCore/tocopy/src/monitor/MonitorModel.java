package monitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import xclipsjni.ClipsException;
import xclipsjni.ClipsModel;

/**
 * L'implementazione della classe ClipsModel specifica per il progetto Waitor
 * 2013/2014. L'oggetto fondamentale è il map, una matrice che in ogni elemento
 * contiene la stringa corrispondente al contenuto.
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto
 * @edit by Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Luca Ribero, Noemi
 * Mauro, Stefano Rossi
 */
public class MonitorModel extends ClipsModel {

    private String[][] map;
    private String direction;
    private String mode;
    private int durlastact;
    private Integer time;
    private Integer step;
    private Long maxduration;
    private String result;
    private int score;
    private String loaded; // presenza di un carico
    /*private String l_d_waste; // presenza di spazzatura drink
     private Integer l_injured; // quantità di persone intrappolate
     private Integer l_injured_discovered; //quantità di food contenuta*/
    private Integer row, column;

    /**
     * Costruttore del modello per il progetto Monitor
     *
     */
    public MonitorModel() {
        super();
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
        maxduration = Long.MAX_VALUE;
        try {
            DebugFrame.appendText("[SYSTEM] Esecuzione del primo passo al fine di caricare i fatti relativi alla mappa.");

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

            maxduration = new Long(core.findOrderedFact("MAIN", "maxduration"));

            DebugFrame.appendText("[SYSTEM] Inizializzazione del modello (mappa).");
            String[] array = {"pos-r", "pos-c", "contains", "injured"};
            String[][] mp = core.findAllFacts("ENV", "real_cell", "TRUE", array);
            int maxr = 0;
            int maxc = 0;
            for (String[] mp1 : mp) {
                int r = new Integer(mp1[0]);
                int c = new Integer(mp1[1]);
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
            DebugFrame.appendText("[SYSTEM] Il modello è pronto.");

        } catch (ClipsException ex) {
            DebugFrame.appendText("[ERROR] L'inizializzazione è fallita:");
            DebugFrame.appendText(ex.toString());
            Logger.getLogger(MonitorModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Aggiorna la mappa leggendola dal file clips. Lanciato ogni volta che si
     * compie un'azione.
     *
     * @throws ClipsException
     */
    private synchronized void updateMap() throws ClipsException {

        // ######################## FATTI DI TIPO cell ##########################
        DebugFrame.appendText("[SYSTEM] Aggiornamento modello mappa in corso...");
        String[] cellArray = {"pos-r", "pos-c", "contains", "injured", "discovered", "checked", "clear"};
        String[] kcellArray = {"pos-r", "pos-c", "contains"};

        //Per ogni cella prendiamo il nuovo valore e lo aggiorniamo
        String[][] cellFacts = core.findAllFacts("ENV", "cell", "TRUE", cellArray);
        String[][] kcellFacts = core.findAllFacts("AGENT", "K-cell", "TRUE", kcellArray);

        for (String[] fact : cellFacts) {
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int r = new Integer(fact[0]);
            int c = new Integer(fact[1]);

            //caso di default
            map[r - 1][c - 1] = fact[2]; //prendiamo il valore
            if (fact[3].equals("yes")) {
                map[r - 1][c - 1] += "_injured";
            }
            if ((fact[2].equals("debris") && (fact[4].equals("yes") || fact[5].equals("yes"))) || (fact[2].equals("empty") && fact[6].equals("yes"))) {
                map[r - 1][c - 1] += "_informed";
            }
        }

        for (String[] fact : kcellFacts) {
            int r = new Integer(fact[0]) - 1;
            int c = new Integer(fact[1]) - 1;
            if (fact[2].equals("unknown")) {
                map[r][c] += "_undiscovered";
            }
        }
        DebugFrame.appendText("[SYSTEM] Modello aggiornato.");

        // ######################## FATTO agentstatus ##########################
        DebugFrame.appendText("[SYSTEM] Acquisizione posizione dell'agente...");
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

            DebugFrame.appendText("[SYSTEM] Acquisizione background dell'agente...");
            String[] arrayRobotBackground = {"pos-r", "pos-c", "contains", "injured", "previous", "clear"};
            String[] robotBackground = core.findFact("ENV", "cell", "and (eq ?f:pos-r " + Integer.toString(r) + ") (eq ?f:pos-c " + Integer.toString(c) + ")", arrayRobotBackground);

            //Nel modello abbiamo la stringa agent_background, la cosa verrà interpretata nella View (updateMap())
            String background = robotBackground[4];

            map[r - 1][c - 1] = "agent_" + background;
            if (robotBackground[5].equals("yes")) {
                map[r - 1][c - 1] += "_informed";
            }
        }
        DebugFrame.appendText("[SYSTEM] Aggiornato lo stato dell'agente.");

        // ######################## FATTI personstatus ##########################
        DebugFrame.appendText("[SYSTEM] Acquisizione posizione degli altri agenti...");
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
        DebugFrame.appendText("[SYSTEM] Aggiornato lo stato dell'agente.");

        // ######################## FATTO status ##########################
        String[] arrayStatus = {"step", "time", "result"};
        String[] status = core.findFact("MAIN", "status", "TRUE", arrayStatus);
        if (status[0] != null) {
            step = new Integer(status[0]);
            time = new Integer(status[1]);
            result = status[2];
            DebugFrame.appendText("[SYSTEM] Step: " + step + " Time: " + time + " Result: " + result);
        }
        DebugFrame.appendText("[SYSTEM] Aggiornato lo stato del mondo.");

        DebugFrame.appendText("[SYSTEM] Aggiornamento completato.");
    }

    /**
     * metodo per ottenere la mappa dell'ambiente come vista nel modulo ENV.
     * Dato che è tutto su più thread ritorniamo una copia della matrice, così
     * evitiamo il problema di avere una risorsa condivisa (che non ci serve).
     *
     * @return la mappa come matrice di stringhe
     */
    public synchronized String[][] getMap() {
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
    public synchronized String getDirection() {
        return direction;
    }

    /**
     * metodo per la modalità in cui si trova l'agente
     *
     * @return loaded, unloaded
     */
    public synchronized String getMode() {
        return mode;
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
    public synchronized long getMaxDuration() {
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
    protected void setup() throws ClipsException {
        init();
    }

    @Override
    protected void action() throws ClipsException {
        updateMap();

    }

    @Override
    protected boolean hasDone() {
        // ritorna true se time>=maxduration o se result non è "no" e quindi è "disaster" o "done"
        return time >= maxduration || !result.equalsIgnoreCase("no");
    }

    @Override
    protected void dispose() throws ClipsException {
        score = new Integer(core.findOrderedFact("MAIN", "penalty"));
    }

    public String[][] findAllFacts(String template, String conditions, String[] slots) throws ClipsException {
        String[][] empty = {};
        return core != null ? core.findAllFacts(template, conditions, slots) : empty;
    }

    public String getLoaded() {
        return loaded;
    }

    public Integer getRow() {
        return row;
    }

    public Integer getColumn() {
        return column;
    }
}

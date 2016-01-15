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
    private boolean bumped;
    private String kdirection;
    private String kmode;
    private String kloaded; // presenza di un carico
    private int krow;
    private int kcolumn;
    private int kstep;
    private int ktime;
    private Map<String,int[]> offsetPosition;
    private ClipsConsole console;
    private static RescueModel instance;
    private String advise;
    private Map<String, MonitorMap> maps;
    private ArrayList<int[]> personPositions;
    private ArrayList<int[]> kpersonPositions;
    private String pdirection;
    private String pmode;
    private String ploaded; // presenza di un carico
    private int prow;
    private int pcolumn;
    private ArrayList<int[]> openNodes;
    private ArrayList<int[]> closedNodes;

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
            instance.bumped=false;
            instance.kdirection = null;
            instance.kmode = null;
            instance.kloaded = null;
            instance.kstep = 0;
            instance.ktime = 0;
            instance.personPositions = null;
            instance.kpersonPositions = null;
            instance.offsetPosition = null;
            instance.pdirection = null;
            instance.pmode = null;
            instance.ploaded = null; // presenza di un carico
            instance.prow = 0;
            instance.pcolumn = 0;
            instance.openNodes = null;
            instance.closedNodes = null;
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
        kpersonPositions = new ArrayList<int[]>();
        offsetPosition = new HashMap<String,int[]>();
    }

    /**
     * Inizializza il modello in base al contenuto del file clips caricato.
     */
    @Override
    protected synchronized void initModel() {
        result = "no";
        time = 0;
        step = 0;
        offsetPosition.put("north",new int[]{1,0});
        offsetPosition.put("south",new int[]{-1,0});
        offsetPosition.put("east",new int[]{0,1});
        offsetPosition.put("west",new int[]{0,-1});

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

        // Update the planning nodes
        updatePNodes();

        // Update the other agents
        updatePeople();
        updateKPeople();
        checkBumpCondition();

        // Update all the maps (they read the values created by updateAgent)
        for(MonitorMap map : maps.values()){
            map.updateMap();
        }

        this.setChanged();
        this.notifyObservers("repaint");
    }

    private void updateAgent() throws CLIPSError{
        String[] robot = core.findFact("ENV", RescueFacts.AgentStatus.factName(), "TRUE", RescueFacts.AgentStatus.slotsArray());
        if (robot[0] != null) { //Se hai trovato il fatto
            step = new Integer(robot[RescueFacts.AgentStatus.STEP.index()]);
//            time = new Integer(robot[RescueFacts.AgentStatus.TIME.index()]);
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

    private void updatePNodes() throws CLIPSError{
        openNodes = new ArrayList<int[]>();
        closedNodes = new ArrayList<int[]>();
        instance.pdirection = null;
        instance.pmode = null;
        instance.ploaded = null; // presenza di un carico
        instance.prow = -1;
        instance.pcolumn = -1;

        String[][] pnodes = core.findAllFacts("REASONING", RescueFacts.PNode.factName(), "TRUE", RescueFacts.PNode.slotsArray());
        for(String[] pnode : pnodes){
            if (pnode[0] != null) { //Se hai trovato il fatto
                int ident = new Integer(pnode[RescueFacts.PNode.IDENT.index()]);
                String nodetype = pnode[RescueFacts.PNode.NODETYPE.index()];

                String[] robot = core.findFact("REASONING", RescueFacts.PAgent.factName(), "eq ?f:ident " + ident, RescueFacts.PAgent.slotsArray());
                if (robot[0] != null) { //Se hai trovato il fatto
                  prow = new Integer(robot[RescueFacts.PAgent.POSR.index()]);
                  pcolumn = new Integer(robot[RescueFacts.PAgent.POSC.index()]);
                  
                  if(nodetype.equals("selected")){
                    pdirection = robot[RescueFacts.PAgent.DIRECTION.index()];
                    ploaded = robot[RescueFacts.PAgent.LOADED.index()];
                    pmode = ploaded.equals("yes") ? "loaded" : "unloaded";
                  }
                  else if(nodetype.equals("open")){
                    openNodes.add(new int[]{prow,pcolumn});
                  }
                  else if(nodetype.equals("closed")){
                    closedNodes.add(new int[]{prow, pcolumn});
                  }
                }
            }
        }
    }


    private void updatePeople() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti per EnvMap...");
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



    private void updateKPeople() throws CLIPSError{
        console.debug("Acquisizione posizione degli altri agenti per agentMap...");
        String[][] persons = core.findAllFacts("AGENT", RescueFacts.KPerson.factName(), "= ?f:step " + this.step, RescueFacts.KPerson.slotsArray());
        kpersonPositions.clear();
        if (persons != null) {
            for (int i = 0; i < persons.length; i++) {
                if(persons[i][0] != null){
                    int r = new Integer(persons[i][RescueFacts.KPerson.POSR.index()]);
                    int c = new Integer(persons[i][RescueFacts.KPerson.POSC.index()]);
                    kpersonPositions.add(new int[]{r, c});
                }
            }
        }
    }

    private void checkBumpCondition() throws CLIPSError{

      console.debug("Controllo di evento bump...");
      boolean bumped = false;
      String[][] bump = core.findAllFacts("AGENT",RescueFacts.SpecialCondition.factName(),"TRUE", RescueFacts.SpecialCondition.slotsArray());
      this.bumped= bump.length!=0 ? true: false;

    }



    protected void updateStatus() throws CLIPSError{
        String[] status = core.findFact("MAIN", RescueFacts.Status.factName(), "TRUE", RescueFacts.Status.slotsArray());
        if (status[0] != null) {
            step = new Integer(status[RescueFacts.Status.STEP.index()]);
            time = new Integer(status[RescueFacts.Status.TIME.index()]);
            result = status[RescueFacts.Status.RESULT.index()];
            console.debug("Step: " + step + " Time: " + time + " Result: " + result);
        }
        score = new Double(core.findOrderedFact("MAIN", "penalty"));
    }

    public ArrayList<int[]> getPersonPositions(){
        return personPositions;
    }

    public ArrayList<int[]> getKPersonPostions(){

      return kpersonPositions;
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
    
    public String getPDirection() {
        return pdirection;
    }

    public String getPLoaded() {
        return ploaded;
    }

    public String getPMode() {
        return pmode;
    }

    public int getPRow(){
        return prow;
    }

    public int getPColumn(){
        return pcolumn;
    }
    
    public ArrayList<int[]> getOpenNodes(){
        return openNodes;
    }
    
    public ArrayList<int[]> getClosedNodes(){
        return closedNodes;
    }

    public boolean getBumped(){

        return bumped;
    }

    public Map<String,int[]> getOffset(){
      return this.offsetPosition;
    }

    @Override
    public void injectExecutionRules() throws CLIPSError{
        super.injectExecutionRules();

        String overrideExecTemplate = "" +
            "(deftemplate override-exec \n" +
            "  (slot step) \n" +
            "  (slot action  \n" +
            "    (allowed-values \n" +
            "      forward turnright turnleft\n" +
            "      drill load_debris unload_debris\n" +
            "      wait inform done\n" +
            "    )\n" +
            "  )\n" +
            "  (slot param1)\n" +
            "  (slot param2)\n" +
            "  (slot param3)\n" +
            ")\n";

        String overrideExecRule = "" +
            "(defrule override-exec\n" +
            "  (declare (salience 1))\n" +
            "  (status (step ?s))\n" +
            "  ?exec <- (exec (step ?s))\n" +
            "  ?override <- (override-exec (step ?s)(action ?a)(param1 ?p1)(param2 ?p2)(param3 ?p3))\n" +
            "  =>\n" +
            "  (retract ?exec)\n" +
            "  (retract ?override)\n" +
            "  (assert (exec (step ?s)(action ?a)(param1 ?p1)(param2 ?p2)(param3 ?p3)))\n" +
            ")";

        boolean check = core.build("AGENT", overrideExecTemplate);
        boolean check2 = core.build("AGENT", overrideExecRule);
        if (check && check2) {
            console.debug("Injected override exec rule");
        } else {
            console.error("Injection of override exec rule failed");
        }
    }

    public void actionForward() {
        assertOverrideExec("forward", null, null, null);
    }

    public void actionTurnLeft() {
        assertOverrideExec("turnleft", null, null, null);
    }

    public void actionTurnRight() {
        assertOverrideExec("turnright", null, null, null);
    }

    public void actionWait() {
        assertOverrideExec("wait", null, null, null);
    }

    public void actionLoadNorth() {
        String action = kloaded.equals("yes") ? "unload_debris" : "load_debris";
        assertOverrideExec(action, krow + 1, kcolumn, null);
    }

    public void actionLoadEast() {
        String action = kloaded.equals("yes") ? "unload_debris" : "load_debris";
        assertOverrideExec(action, krow, kcolumn + 1, null);
    }

    public void actionLoadWest() {
        String action = kloaded.equals("yes") ? "unload_debris" : "load_debris";
        assertOverrideExec(action, krow, kcolumn - 1, null);
    }

    public void actionLoadSouth() {
        String action = kloaded.equals("yes") ? "unload_debris" : "load_debris";
        assertOverrideExec(action, krow -1 , kcolumn, null);
    }

    public void actionDrillNorth() {
        assertOverrideExec("drill", krow + 1, kcolumn, null);
    }

    public void actionDrillEast() {
        assertOverrideExec("drill", krow, kcolumn + 1, null);
    }

    public void actionDrillWest() {
        assertOverrideExec("drill", krow, kcolumn - 1, null);
    }

    public void actionDrillSouth() {
        assertOverrideExec("drill", krow -1 , kcolumn, null);
    }

    private void assertOverrideExec(String action, Object param1, Object param2, Object param3){
        String cmd = "(assert (override-exec (action " + action + ")(step " + step + ")";
        if(param1 != null){
            cmd += "(param1 " + param1 + ")";
        }
        if(param2 != null){
            cmd += "(param2 " + param2 + ")";
        }
        if(param3 != null){
            cmd += "(param3 " + param3 + ")";
        }
        cmd += "))";
        console.info("Sending action: " + action);
        evalComandLine(cmd);
    }
}

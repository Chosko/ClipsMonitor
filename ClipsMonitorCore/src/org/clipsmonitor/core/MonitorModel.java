package org.clipsmonitor.core;

import java.util.Observable;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsCore;

/**
 * Questa classe astratta è la parte di model (in un'architettura MVC) che si
 * interfaccia e mantiene con i dati. Nella fattispecie, le implentazioni di
 * questa classe dovranno mantenere copie dei fatti rilevanti estratti
 * dall'ambiente di clips, cosicchè possano essere usati per le interfacce.
 * Estendendo Observable qualunque view che voglia essere aggiornata con i
 * cambiamenti del model deve implementare Observer.
 *
 * @author Piovesan Luca, Verdoja Francesco Edited by: @author Violanti Luca,
 * Varesano Marco, Busso Marco, Cotrino Roberto, Enrico Mensa, Matteo Madeddu,
 * Davide Dell'Anna, Ruben Caliandro, Marco Corona
 */

public abstract class MonitorModel extends Observable implements Runnable {
    
    private long targetUpdateTime;
    protected ClipsCore core;
    private int executionMode;
    private ClipsConsole console;
    private final Thread t;
    private int paramMode = 1;
    private boolean partialUpdateEnabled = false;
    private boolean showGoalEnabled = false;
    public static final int EX_MODE_START = 1;
    public static final int EX_MODE_RUN1 = 2;
    public static final int EX_MODE_STEP = 3;
    public static final int EX_MODE_RUN = 4;
    public static final int EX_MODE_RUNN = 5;
    public static final int EX_MODE_STOP = 6;
    public static final int EX_MODE_BREAK = 7;


    //  variabili di esecuzione del modello

    protected Integer time;
    protected Integer step;
    protected Integer maxduration;
    protected String result;
    protected int row, column;
    protected double score;
    protected int durlastact;


    /**
     * costruttore del modello.
     */
    protected MonitorModel() {
        t = new Thread(this);
        init();
    }

    /**
     * Esegue l'ambiente in singlethread. Per eseguire l'ambiente in multithread
     * vedere il metodo execute().
     *
     */

    @Override
    public void run() {
        String clipsMonitorFact = "";
        boolean finished = false;
        try {


            while (!finished) {

                long startTime = System.nanoTime();

                switch (executionMode) {
                    case EX_MODE_STEP:
                    case EX_MODE_BREAK:
                    case EX_MODE_RUN:
                        core.RecFromRouter();
                        core.run();
                        core.StopRecFromRouter();
                        if(!core.GetStdoutFromRouter().equals("")){
                            console.clips(core.GetStdoutFromRouter());
                        }
                        break;
                    case EX_MODE_RUNN:
                        core.RecFromRouter();
                        core.run(paramMode);
                        core.StopRecFromRouter();
                        if(!core.GetStdoutFromRouter().equals("")){
                            console.clips(core.GetStdoutFromRouter());
                        }
                        break;
                    case EX_MODE_START:
                    case EX_MODE_STOP:
                    default:
                        break;

                }
                clipsMonitorFact = core.findOrderedFact("AGENT", "clips-monitor");

                console.debug("clipsMonitorFacts: " + clipsMonitorFact);

                /*
                    solo in fase di run non si richiede la suspend
                */
                boolean suspend = false;
                boolean update = false;
                String partialUpdateString = null;
         
                updateStatus();
                
                if(partialUpdateEnabled && clipsMonitorFact.startsWith("update-")){
                    suspend = false;
                    update = false;
                    partialUpdateString = clipsMonitorFact;
                }
                
                if (result.equals("done") || result.equals("disaster") || (time >= maxduration)){
                    finished = true;
                    update = true;
                }
                else if (clipsMonitorFact.equals("stop")){
                    suspend = true;
                    update = true;
                }
                else if (clipsMonitorFact.equals("do-step")){
                    update = true;
                    executionMode = EX_MODE_STEP;
                }
                else if(executionMode == EX_MODE_BREAK){
                    suspend = true;
                    update = true;
                }
                else if(executionMode == EX_MODE_STEP && clipsMonitorFact.equals("step-done")){
                    suspend = true;
                    update = true;
                }
                else if(executionMode == EX_MODE_STOP || executionMode == EX_MODE_RUNN){
                    suspend = true;
                    update = true;
                }
                else if(executionMode == EX_MODE_RUN && clipsMonitorFact.equals("step-done")){
                    update = true;
                }
                
                if(update){
                    try{
                        updateModel();
                    }
                    catch(CLIPSError er){
                        console.error(er);
                    }
                    this.setChanged();
                    this.notifyObservers("actionDone");
                }
                else if(partialUpdateString != null){
                    try{
                        partialUpdate(partialUpdateString);
                    }
                    catch(CLIPSError er){
                        console.error(er);
                    }
                    this.setChanged();
                    this.notifyObservers(partialUpdateString);
                }
                if(suspend){
                    this.suspend();
                }
                else if(update){
                    long millisecondsLeft;
                    do{ // Horrible busy waiting
                      long elapsedMilliseconds = (System.nanoTime() - startTime) / 1000000;
                      millisecondsLeft = targetUpdateTime - elapsedMilliseconds;
                    } while(millisecondsLeft > 0);
                }
           else if(partialUpdateString != null){
                    long millisecondsLeft;
                    do{ // Horrible busy waiting
                      long elapsedMilliseconds = (System.nanoTime() - startTime) / 1000000;
                      millisecondsLeft = (targetUpdateTime - elapsedMilliseconds) / 2;
                    } while(millisecondsLeft > 0);
                }
            }

            // Aggiorna le penalità
            updateStatus();
            this.setChanged();
            this.notifyObservers("disposeDone");

        } catch (NumberFormatException ex) {
            console.error(ex);
        } catch(CLIPSError ex) {
            core.StopRecFromRouter();
            console.error(core.GetStdoutFromRouter());
        }
    }



    /**
     * Esegue l'ambiente su un nuovo thread. Meglio a livello di prestazioni
     * rispetto al metodo run() perchè sfrutta il multithread.
     *
     */
    public void execute() {
        t.start();
    }

    /**
     * Cambia la modalita' di esecuzione dell'environment.
     *
     * @param mode ha 3 valori ammessi: RUN per eseguire i passi
     * consecutivamente senza interruzioni, RUNONE per eseguire un passo Clips
     * alla volta (in fase di debug), STEP per eseguire una exec alla volta.
     */
    public synchronized void setMode(int mode) {
        this.executionMode = mode;
    }



    public synchronized void setMode(int mode, int param) {
        this.executionMode = mode;
        this.paramMode = param;
    }

    /**
     * Avvia l'environment di clips
     *
     * @param strategyFolder_name Nome della cartella in CLP che contiene tutti
     * i file di CLIPS (CLP/strategyFolder_name)
     * @param envsFolder_name Nome della cartella in CLP che contiene tutti i
     * file relativi all'environment (envs/envFolder_name)
     */

    public void startCore(String projectDirectory, String strategyFolder_name, String envsFolder_name) throws CLIPSError {
        /*inizializza l'ambiente clips caricando i vari file*/
        core.initialize(projectDirectory, strategyFolder_name, envsFolder_name);
        this.injectExecutionRules();
        console.debug("Clips Environment created and ready to run");
        /*effettua una reset di clips dopo aver caricato i file e
         carica le info iniziali dei file clips, per poi terminare la fase di setup*/
        core.reset();
        setup();
        this.setChanged();
        this.notifyObservers("setupDone");
        console.setActive(true);
    }

    /**
     * Equivalente alla funzione facts di Clips. Restituisce la lista dei fatti
     * del modulo corrente.
     *
     * @return una stringa che rappresenta i fatti del modulo corrente
     * @throws ClipsException
     */
    public synchronized String getFactList() {
        return core.getFactList();
    }

    public synchronized String getFactList(String query){
        return core.getFactList(query);
    }

    /**
     * Equivalente alla funzione agenda di Clips. Restituisce la lista delle
     * regole attualmente attivabili, in ordine di priorita' di attivazione.
     *
     * @return una stringa che rappresenta le azioni attivabili al momento
     * @throws ClipsException
     */
    public synchronized String getAgenda() {
        return core.getAgenda();
    }

    public synchronized String getFocus() {
        return core.getFocus();
    }

    public synchronized String[] getFocusStack(){
        return core.getFocusStack();
    }

    /**
     * riprende il thread sospeso tramite il metodo suspend()
     *
     */
    @SuppressWarnings("CallToThreadStopSuspendOrResumeManager")
    public void resume() {
        t.resume();
    }

    /**
     * sospende il thread, può essere ripreso con il metodo resume()
     *
     */
    @SuppressWarnings("CallToThreadStopSuspendOrResumeManager")
    private void suspend() {
        t.suspend();
    }

    public String evalComandLine(String command) {
        String result = "";
        try{
            result = core.evaluateOutput(null, command);
            if(result.length() > 0){
                console.clips(result);
            }
        }
        catch(CLIPSError ex){
            console.error(ex);
        }
        this.setChanged();
        this.notifyObservers("cmd");
        return result;
    }

    public void finished() {
        this.setChanged();
        this.notifyObservers("finished");
    }

    public void init() {
        executionMode = EX_MODE_START;
        console = ClipsConsole.getInstance();
        core = ClipsCore.getInstance();
        targetUpdateTime = 200;
        partialUpdateEnabled = false;
        showGoalEnabled = false;
        this.setChanged();
        this.notifyObservers("startApp");
    }

    public void clear() {
        console = null;
        core = null;
        this.setChanged();
        this.notifyObservers("clearApp");
    }

    public void restart() {
        this.setChanged();
        this.notifyObservers("startApp");
    }

    public int getExecutionMode(){

        return this.executionMode;
    }


    public void injectExecutionRules() throws CLIPSError{

        String defruleStepDone = "(defrule clips-monitor-step-done "            +
                                      "(declare (salience 1000))"                +
                                      "(status (step ?s))"                      +
                                      "(last-perc (step ?s))"                   +
                                      "(not (clips-monitor step-done))"         +
                                      "(not (clips-monitor-step started))"      +
                                        "=>"                                    +
                                      "(assert (clips-monitor step-done))"      +
                                      "(halt)"                                  +
                                  ")";

        String defruleStepStarted =  "(defrule clips-monitor-step-started "     +
                                      "(declare (salience 1000))"               +
                                      "?step-done <- (clips-monitor step-done)" +
                                        "=>"                                    +
                                      "(retract ?step-done)"                    +
                                      "(assert (clips-monitor-step started))"   +
                                      "(halt)"                                  +
                                  ")";



        String defruleRetractStepStarted = "(defrule clips-monitor-retract-step-started " +
                                        "  (declare (salience 1000))"           +
                                        "  ?step-started <- (clips-monitor-step started)"  +
                                        "  (last-perc (step ?old-s))"           +
                                        "  (status (step ?act-s))"              +
                                        "  (test (> ?act-s ?old-s))"            +
                                        "  => "                                 +
                                        "  (retract ?step-started)"             +
                                        ")";

        String defruleInitialStep =     "(defrule clips-monitor-initial"        +
                                        " (declare (salience 1000))"            +
                                        " (init-agent (done yes))"              +
                                        " (not (clips-monitor stop))"           +
                                        " (not (clips-monitor-initial done))"   +
                                        "  => "                                 +
                                        "  (assert (clips-monitor do-step))"    +
                                        "  (assert (clips-monitor-initial done))"+
                                        "  (halt)"                              +
                                        ")";

        String defruleRetractStep =     "(defrule clips-monitor-retract-initial"+
                                        "  (declare (salience 1000))"           +
                                        "  ?do-step <-(clips-monitor do-step)"  +
                                        "  => "                                 +
                                        "  (retract ?do-step)"                  +
                                        ")";

        boolean check = core.build("AGENT", defruleStepDone);
        boolean check2 = core.build("AGENT", defruleStepStarted);
        boolean check3 = core.build("AGENT", defruleRetractStepStarted);
        boolean check4 = core.build("AGENT", defruleInitialStep);
        boolean check5 = core.build("AGENT", defruleRetractStep);
        if(check && check2 && check3 && check4 && check5){
            console.debug("Injection rule done");
        }
        else{
           console.error("Injection failed");
        }
    }

    /**
     * metodo per ottenere il punteggio dell'agente totalizzato a seguito delle
     * sue azioni
     *
     * @return il punteggio come intero
     */
    public synchronized double getScore() {
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

    /*metodo per ottenere la riga in cui si trova il robot
     @return il valore della riga intero
    */

    public synchronized Integer getRow() {
        return row;
    }

    /*metodo per ottenere la colonna in cui si trova il robot
     @return il valore della colonna intero
    */

    public synchronized Integer getColumn() {
        return column;
    }
    
    public long getTargetUpdateTime(){
        return targetUpdateTime;
    }
    
    public void setTargetUpdateTime(long value){
        if(value > 0){
            targetUpdateTime = value;
        }
    }
    
    public boolean getPartialUpdateEnabled() {
        return partialUpdateEnabled;
    }
    
    public void setPartialUpdateEnabled(boolean value){
        partialUpdateEnabled = value;
    }
    
    public boolean getShowGoalEnabled() {
        return showGoalEnabled;
    }
    
    public void setShowGoalEnabled(boolean value){
        showGoalEnabled = value;
    }

    protected void setup(){
        initModel();
    }

    // PARTE ASTRATTA

    /**
     * Aggiorna lo stato della simulazione (time, step, result, score)
     * @throws CLIPSError 
     */
    protected abstract void updateStatus() throws CLIPSError;
    
   /*
     Metodo per l'inizializzazione del modello in base al contenuto dei file
     clips caricati
    */
    protected abstract void initModel();

    /**
     * Aggiorna tutte le informazioni del modello leggendo da clips (status + agent + person + cells.....)
     * @throws CLIPSError 
     */
    protected abstract void updateModel() throws CLIPSError;

    /**
     * Aggiorna solo alcune informazioni, basate sulla stringa in input
     * @throws CLIPSError 
     */
    protected abstract void partialUpdate(String partial) throws CLIPSError;

}

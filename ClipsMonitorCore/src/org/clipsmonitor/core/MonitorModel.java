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

    protected ClipsCore core;
    private int executionMode;
    private ClipsConsole console;
    private boolean started = false;
    private final Thread t;
    private int paramMode = 1;
    public static final int ex_mode_START = 1;
    public static final int ex_mode_RUN1 = 2;
    public static final int ex_mode_STEP = 3;
    public static final int ex_mode_RUN = 4;
    public static final int ex_mode_RUNN = 5;
    public static final int ex_mode_STOP = 6;
    
    
    //  variabili di esecuzione del modello
    
    protected Integer time;
    protected Integer step;
    protected Integer maxduration;
    protected String result;
    protected int row, column;
    protected int score;
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
        
        String[] arrayPercept = {"step"};
        String[] current;
        String[] done = {"no"};;
        
        try {
            
            
            while (!hasDone()) {
                
                switch (executionMode) {
                    
                    
                    case ex_mode_STEP: 
                        
                        
                    case ex_mode_RUN:
                       
                        current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);

                        
                        
                        
                        /* Lo stato precedente viene inizalizzato al valore dello stato attuale
                         Fino a che lo stato precedente è uguale allo stato attuale, allora
                         proseguo (devo arrivare alla prossima percezione, facendo una run.
                         */
                                             
                            core.run();
                            done = core.findFact("MAIN", "status", "TRUE", new String[]{"result"});
                        
                        
                        
                        
                       break;
                    case ex_mode_RUNN:
                        core.run(paramMode);
                        break;
                    case ex_mode_START:
                        started = true;
                        break;
                    case ex_mode_STOP:
                        
                        break;
                    default:

                }
                action();
                this.setChanged();
                this.notifyObservers("actionDone");
                
                /* 
                    solo in fase di run non si richiede la suspend 
                
                */
                if (executionMode != ex_mode_RUN) {
                    this.suspend();
                }
            }
            // Aggiorna le penalità
            dispose();
            this.setChanged();
            this.notifyObservers("disposeDone");
        
        } catch (NumberFormatException ex) {
            console.error(ex);
        } catch(CLIPSError ex) {
            console.error(ex);
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
        this.InjectExecutionRules();
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
        executionMode = ex_mode_START;
        console = ClipsConsole.getInstance();
        core = ClipsCore.getInstance();
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


    public void InjectExecutionRules() throws CLIPSError{
    
        String defruleBeginStep = "(defrule begin-step "            +
                                    "(declare (salience 100))"       +
                                    "(status (step ?s))"             +
                                    "(not (exec-mode (step ?s)))"    +
                                                      "  => "        +
                                    "(assert (exec-mode (step ?s)))" +
                                                             ")";

            
            String defruleRetractExecMode = "(defrule retract-exec-mode " +
                                            "  (declare (salience 100))" +
                                            "  ?exec <- (exec-mode (step ?old-s))" +
                                            "  (status (step ?act-s))" +
                                            "  (last-perc (step ?act-s))" +
                                            "  (test (> ?act-s ?old-s))" +
                                            "  => " +
                                            "  (retract ?exec)" +
                                            "  (halt)" +
                                            ")";
            
            
            boolean check = core.build("AGENT", defruleBeginStep);
            boolean check2 = core.build("AGENT", defruleRetractExecMode);
            if(check && check2){
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
    
    protected void setup(){
        initModel();
    }

    protected void action() {
        try{
            updateModel();
        }
        catch (CLIPSError ex){
            console.error(ex);
        }
    }
    
    // PARTE ASTRATTA

    /**
     * Indica se l'ambiente ha finito la naturale esecuzione.
     *
     * @return true se l'esecuzione dell'ambiente e' terminata, false altrimenti
     */
    protected abstract boolean hasDone();

    /**
     * Pone fine all'ambiente costruendo i risultati e le statistiche finali.
     * Eseguita un'unica volta dopo che hasDone == true.
     *
     * @throws ClipsException
     */
    protected abstract void dispose();

   /*
     Metodo per l'inizializzazione del modello in base al contenuto dei file
     clips caricati 
    */
    
    protected abstract void initModel();
    
    /*
     Esegue l'aggiornamento della matrice di stringhe che descrive le celle 
     dell'ambiente, in base alla valutazione dei fatti prodotti dal motore di
    Clips 
    */
    
    protected abstract void updateModel() throws CLIPSError;
    
    
}
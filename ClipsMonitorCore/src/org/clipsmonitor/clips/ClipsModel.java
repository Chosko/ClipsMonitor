package org.clipsmonitor.clips;

import java.util.Observable;
import net.sf.clipsrules.jni.CLIPSError;

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
 * Davide Dell'Anna
 */
public abstract class ClipsModel extends Observable implements Runnable {

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
    
    /**
     * costruttore del modello.
     *
     */
    protected ClipsModel() {
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
        try {
            while (!hasDone()) {
                switch (executionMode) {
                    case ex_mode_STEP:
                    case ex_mode_RUN:
                        String[] arrayPercept = {"step"};
                        String[] current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);

                        /* Se lo stato last-perc non esiste, allora lo stato attuale viene impostato a -1
                         Diversamente viene impostato allo stato di last-perc. Questo viene fatto per poter
                         completare uno step ""sporcato"" dal click di una runOne e riallinearsi con le azioni
                         necessarie ad arrivare alla prossima percezione del mondo (conseguente azione).
                         */
                        Integer actual = current[0] == null ? -1 : new Integer(current[0]);

                        Integer prec = actual;
                        /* Lo stato precedente viene inizalizzato al valore dello stato attuale
                         Fino a che lo stato precedente è uguale allo stato attuale, allora
                         proseguo (devo arrivare alla prossima percezione, facendo una run.
                         */
                        String[] done = {"no"};
                        while (prec.equals(actual) && done[0].equals("no")) {
                            core.run(1);
                            current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);
                            actual = current[0] == null ? -1 : new Integer(current[0]);
                            done = core.findFact("MAIN", "status", "TRUE", new String[]{"result"});
                        }
                        break;
                    case ex_mode_RUNN:
                        core.run(paramMode);
                        break;
                    case ex_mode_START:
                        started = true;
                        break;
                    default:

                }
                action();
                this.setChanged();
                this.notifyObservers("actionDone");
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
    public void startCore(String strategyFolder_name, String envsFolder_name) {
        /*inizializza l'ambiente clips caricando i vari file*/
        core.initialize(strategyFolder_name, envsFolder_name);
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

    /**
     * Inizializza l'intero ambiente. Questo metodo viene invocato una sola
     * volta all'inizio dell'applicazione.
     *
     * @throws ClipsException
     */
    protected abstract void setup();

    /**
     * Fa avanzare l'ambiente di un turno. Viene invocato ciclicamente finche'
     * hasDone == false.
     *
     * @throws ClipsException
     */
    protected abstract void action();

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
            result = core.evaluateOutput("AGENT", command);
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
            
}
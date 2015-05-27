package org.clipsmonitor.clips;

import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private final Thread t;
    private ClipsConsole console;
    
    /**
     * costruttore del modello.
     *
     */
    protected ClipsModel() {
        executionMode = 0;
        t = new Thread(this);
        console = ClipsConsole.getInstance();
        core = ClipsCore.getInstance();
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
                /*
                 * HO CLICCATO IL TASTO START
                 */
                if (executionMode == 1) {
                    console.debug("Clicked on START BUTTON");
                    String[] arrayPercept = {"step"};
                    long run_feedback = 1;
                    /* Alla pressione del tasto START, il while entra una volta nel ciclo.
                     *  Esegue una runOne(). Controlla che il fatto init-agent sia dichiarato (il mondo è pronto)
                     *  Se è così, allora aggiorna l'interfaccia poiché il mondo è pronto. All'uscita dal metodo
                     *  il thread principale si sospende in attesa di una nuova azione (pressione tasto) che farà la resume.
                     */
                    while (run_feedback == 1) {
                        run_feedback = core.runOne();
                        String[] arrayInitAgent = {"done"};
                        String[] initAgent = core.findFact("AGENT", "init-agent", "TRUE", arrayInitAgent);
                        if (initAgent[0] != null && initAgent[0].equals("yes")) {
                            action();
                            this.setChanged();
                            this.notifyObservers("actionDone");
                            break;
                        }
                        //System.out.println("Eseguo una run one, cstep = " + current_perc  +" last_step = " + last_known_perc +" e step ="+last_perc[0]);
                    }
                    this.suspend();
                } /*
                 * HO CLICCATO IL TASTO RUN 1
                 */ else if (executionMode == 2) {
                    console.debug("Clicked on RUN 1 BUTTON");
                    core.runOne();
                    action();
                    this.setChanged();
                    this.notifyObservers("actionDone");
                    this.suspend();
                } /*
                 * HO CLICCATO IL TASTO STEP
                 */ else if (executionMode == 3) {
                    console.debug("Clicked on STEP BUTTON");
                    long run_feedback = 1;
                    String[] arrayPercept = {"step"};
                    String[] current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);
                    Integer actual;
                    Integer prec;
                    /* Se lo stato last-perc non esiste, allora lo stato attuale viene impostato a -1
                     Diversamente viene impostato allo stato di last-perc. Questo viene fatto per poter
                     completare uno step ""sporcato"" dal click di una runOne e riallinearsi con le azioni
                     necessarie ad arrivare alla prossima percezione del mondo (conseguente azione).
                     */
                    if (current[0] == null) {
                        actual = -1;
                    } else {
                        actual = new Integer(current[0]);
                    }
                    prec = actual;
                    /* Lo stato precedente viene inizalizzato al valore dello stato attuale
                     Fino a che lo stato precedente è uguale allo stato attuale, allora
                     proseguo (devo arrivare alla prossima percezione, facendo una run.
                     */
                    while (prec.equals(actual)) {
                        run_feedback = core.runOne();
                        current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);
                        if (current[0] == null) {
                            actual = -1;
                        } else {
                            actual = new Integer(current[0]);
                        }
                    }
                    //System.out.println("Eseguo uno step, cstep = " + actual  +" last_step = " + prec);
                    core.runOne();
                    /* Aggiorno l'interfaccia */
                    action();
                    this.setChanged();
                    this.notifyObservers("actionDone");
                    // QUESTA SUSPEND E' L'UNICA DIFFERENZA CON IL TASTO START CHE INVECE NON SI SOSPENDE
                    this.suspend();
                } /*
                 * HO CLICCATO IL TASTO RUN
                 */ else if (executionMode == 4) {
                    console.debug("Clicked on RUN (infinite)");
                    long run_feedback = 1;
                    String[] arrayPercept = {"step"};
                    String[] current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);
                    Integer actual;
                    Integer prec;
                    /* Se lo stato last-perc non esiste, allora lo stato attuale viene impostato a -1
                     Diversamente viene impostato allo stato di last-perc. Questo viene fatto per poter
                     completare uno step ""sporcato"" dal click di una runOne e riallinearsi con le azioni
                     necessarie ad arrivare alla prossima percezione del mondo (conseguente azione).
                     */
                    if (current[0] == null) {
                        actual = -1;
                    } else {
                        actual = new Integer(current[0]);
                    }
                    prec = actual;
                    /* Lo stato precedente viene inizalizzato al valore dello stato attuale
                     Fino a che lo stato precedente è uguale allo stato attuale, allora
                     proseguo (devo arrivare alla prossima percezione, facendo una run.
                     */
                    while (prec.equals(actual)) {
                        run_feedback = core.runOne();
                        current = core.findFact("AGENT", "last-perc", "TRUE", arrayPercept);
                        if (current[0] == null) {
                            actual = -1;
                        } else {
                            actual = new Integer(current[0]);
                        }
                    }
                    // Per concludere faccio una runOne() e chiudo l'esecuzione.
                    core.runOne();
                    action();
                    this.setChanged();
                    this.notifyObservers("actionDone");
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
    public synchronized void setMode(String mode) {
        if (mode.equals("START")) {
            this.executionMode = 1;
        }
        if (mode.equals("RUNONE")) {
            this.executionMode = 2;
        }
        if (mode.equals("STEP")) {
            this.executionMode = 3;
        }
        if (mode.equals("RUN")) {
            this.executionMode = 4;
        }
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
    public void resume() {
        t.resume();
    }

    /**
     * sospende il thread, può essere ripreso con il metodo resume()
     *
     */
    private void suspend() {
        t.suspend();
    }

    public String evalComandLine(String command) {
        String result = "";
        try{
            result = core.evaluateOutput("AGENT", command);
            this.setChanged();
            this.notifyObservers();
        }
        catch(CLIPSError ex){
            console.error(ex);
        }
        return result;
    }
}

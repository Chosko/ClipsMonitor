package org.clipsmonitor.clips;

import net.sf.clipsrules.jni.Environment;
import net.sf.clipsrules.jni.MultifieldValue;
import net.sf.clipsrules.jni.PrimitiveValue;
import net.sf.clipsrules.jni.FactAddressValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;
import net.sf.clipsrules.jni.CLIPSError;
import net.sf.clipsrules.jni.FactAddressValue;

/**
 * Questa classe implemente il cuore di connessione con l'ambiente clips,
 * estende e migliora i metodi già offerti dalle librerie ClipsJNI, che usa per
 * interfacciarsi a clips.
 *
 * @author Piovesan Luca, Verdoja Francesco Edited by: @author Violanti Luca,
 * Varesano Marco, Busso Marco, Cotrino Roberto, Enrico Mensa, Matteo Madeddu,
 * Davide Dell'Anna
 */
public class ClipsCore {

    public synchronized static void clearInstance() {
        instance.router = null;
        instance.clips.destroy();
        instance.clips = null;
        instance.console = null;
        instance = null;
    }

    private Environment clips;
    private RouterDialog router;
    private static ClipsCore instance;
    private ClipsConsole console;
    
    /**
     * Singleton
     */
    public synchronized static ClipsCore getInstance(){
        if(instance == null){
            instance = new ClipsCore();
            instance.init();
        }
        return instance;
    }
    
    /**
     * Private constructor (Singleton)
     */
    private ClipsCore(){}
    
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    private synchronized void init(){
        console = ClipsConsole.getInstance();
        clips = new Environment();
        router = new RouterDialog("routerCore");
        clips.addRouter(router);
        console.internal("ClipsCore initialized");
    }
    
    /**
     * Costruisce una nuova istanza di CLIPS, prelevando dalla cartella
     * CPL/nome_strategia tutti i file .cpl oppure .txt. Il nome_strategia è
     * dato dalla variabile strategyFolder_name (che viene settata a seconda di
     * quanto selezionato nella GUI).
     *
     * Allo stesso modo vengono prelevati dalla cartella envFolder_name
     * (anch'essa variabile definita a seconda della select della GUI) tutti i
     * file relativi all'environment (qualsiasi essi siano) e copiati
     * (temporanamente, durante l'esecuzione del progetto) nella cartella
     * principale del sistema (dove CLIPS può trovarli). Tali file dovrebbero
     * contenere solamente dei fatti (ad esempio la definizione di una mappa
     * oppure la storia di altri agenti). Tale operazione permette al progetto
     * di CLIPS di effettuare diverse load-fact dei vari files contenuti nella
     * envFolder_name. Al termine dell'esecuzione i file vengono rimossi.
     *
     * I path (a partire dalla root del progetto) dove vengono cercate le
     * cartelle sono "CLP/" (per le strategie) e "envs/" (per gli enviornment).
     *
     * @param strategyFolder_name Nome della cartella in CLP che contiene tutti
     * i file di CLIPS (CLP/strategyFolder_name)
     * @param envsFolder_name Nome della cartella in CLP che contiene tutti i
     * file relativi all'environment (envs/envFolder_name)
     */
    public synchronized void initialize(String projectDirectory, String strategyFolder_name, String envsFolder_name) {
        /* ------- Prima di tutto carichiamo i file CLP in CLIPS -------- */
        File str_folder = new File(projectDirectory + File.separator + "CLP" + File.separator + strategyFolder_name); //Recupera la lista dei file nella cartella della strategia scelta
        File[] str_listOfFiles = str_folder.listFiles();

        Arrays.sort(str_listOfFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.compareTo(o2);
            }
        });

        for (File clpFile : str_listOfFiles) {
            try {
                router.startRec();
                String fileName = clpFile.getName();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                if (!clpFile.isHidden() && !clpFile.getName().startsWith(".") && (extension.equalsIgnoreCase("clp") || extension.equalsIgnoreCase("txt"))) {
                    String fs = File.separator;
                    String path = projectDirectory + fs + "CLP" + fs + strategyFolder_name + fs + fileName;
                    console.debug("Loading in CLIPS the file: " + path);
                    clips.load(path); //carica ogni file
                }
                router.stopRec();
                String out = router.getStdout();
                if(out.length() > 0){
                    console.clips(router.getStdout());
                }
            } catch (CLIPSError e) {
                router.stopRec();
                console.error(router.getStdout());
            }
        }

        /* ------- Spostiamo nella cartella della strategia i file presi dalla cartella ENV -------- */
        File env_folder = new File(projectDirectory + File.separator + "envs" + File.separator + envsFolder_name); //Recupera la lista dei file nella cartella della strategia scelta
        File[] env_listOfFiles = env_folder.listFiles();
        String destPath = System.getProperty("user.dir");

        for (File envFile : env_listOfFiles) {

            String fileName = envFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            if (!envFile.isHidden() && !envFile.getName().startsWith(".") && (extension.equalsIgnoreCase("clp") || extension.equalsIgnoreCase("txt"))) {
                File source = envFile;
                String sourceName = source.getName();
                String destName;
                if(sourceName.startsWith("RealMap")){
                    destName = "RealMap.txt";
                }
                else if(sourceName.startsWith("history")){
                    destName = "history.txt";
                } 
                else{
                    destName = sourceName;
                }
                File dest = new File(destPath + File.separator + destName);

                console.debug("Copying the file: " + source.getAbsolutePath() + " into " + dest.getAbsolutePath());

                copyFileUsingFileStreams(source, dest); //Copiamo il file
                dest.deleteOnExit(); //imposto la cancellazione automatica del file temporaneo all'uscita dall'applicazione
            }
        }
        
        
    }
    
    /**
     * Metodo da usare con cautela, serve per un comando su un modulo in clips.
     * La sintassi dev'essere quella di clips, non verranno eseguiti controlli
     * sulla stringa passata, quindi fare attenzione.
     *
     * @param module il modulo su cui svolgere l'interrogazione
     * @param eval l'interrogazione da passare a clips
     * @return un PrimitiveValue che contiene il risultato dell'interrogazione
     */
    public synchronized PrimitiveValue evaluate(String module, String eval) throws CLIPSError{
        if(module == null) {
            return clips.eval(eval);
        }
        boolean isModuleOk = true;
        PrimitiveValue fc = clips.eval("(get-focus)");
        String focus = fc.toString();
        if (!focus.equals(module)) {
            isModuleOk = false;
            clips.eval("(focus " + module + ")");
        }
        PrimitiveValue result = clips.eval(eval);
        if (!isModuleOk) {
            clips.eval("(pop-focus)");
        }
        return result;
    }
    
    public synchronized boolean build(String module , String eval) throws CLIPSError{
    

        if(module == null) {
            return clips.build(eval);
        }
        boolean isModuleOk = true;
        PrimitiveValue fc = clips.eval("(get-focus)");
        String focus = fc.toString();
        if (!focus.equals(module)) {
            isModuleOk = false;
            clips.eval("(focus " + module + ")");
        }
        boolean result = clips.build(eval);
        if (!isModuleOk) {
            clips.eval("(pop-focus)");
        }
        return result;
    
    }
    
    /**
     * Resetta l'Environment Clips: cancella tutti i facts presenti nella WM e
     * asserisce tutti i facts dichiarati negli initial
     *
     */
    public synchronized void reset() {
        clips.reset();
    }

    /**
     * Equivalente al run di Clips.
     *
     * @return ritona 1 se ha successo 0 se fallisce
     */
    public synchronized long run() {
        return clips.run();
    }

    /**
     * Equivalente ad eseguire step l volte
     *
     * @param l il numero di "passi" da fare
     * @return ritona 1 se ha successo 0 se fallisce
     */
    public synchronized long run(long l) {
        return clips.run(l);
    }

    /**
     * Equivalente allo step di Clips
     *
     * @return ritona 1 se ha successo 0 se fallisce
     */
    public synchronized long runOne() {
        return clips.run(1);
    }

    /**
     * Equivalente alla funzione di Clips find-all-facts (vedere manuale per
     * maggiori informazioni e sintassi delle conditions). Restituisce tutti i
     * facts corrispondenti ad una certa interrogazione. Prestare attenzione
     * alla precisione nei campi inseriti, che devono corrispondere ai campi del
     * file Clips.
     *
     * @param module il modulo in cui sono visibili i facts
     * @param template il "tipo" di fatto non ordinato a cui si e' interessati
     * @param conditions le condizioni di ricerca dei facts, inserire TRUE se si
     * vogliono tutti i facts di un certo tipo
     * @param slots gli slot dei facts che si vuole vengano restituiti
     * @return una tabella di stringhe in cui ad ogni riga corrisponde un fatto
     * che soddisfa l'interrogazione, e ad ogni colonna uno slot di quel fatto,
     * secondo l'ordine in cui sono dichiarati nel campo slots. null se non c'e'
     * nessun fatto che soddisfa l'interrogazione
     * @throws ClipsException
     */
    public synchronized String[][] findAllFacts(String module, String template, String conditions, String[] slots)  {
        router.startRec();
        if (!conditions.equalsIgnoreCase("TRUE")) {
            conditions = "(" + conditions + ")";
        }
        String eval = "(find-all-facts ((?f " + template + ")) " + conditions + ")";
        String[][] result = null;
        try{
            MultifieldValue facts = (MultifieldValue) evaluate(module, eval);
            result = new String[facts.size()][slots.length];
            for (int i = 0; i < facts.size(); i++) {
                for (int j = 0; j < slots.length; j++) {
                    FactAddressValue fact = (FactAddressValue) facts.get(i);
                    String slot = fact.getFactSlot(slots[j]).toString();
                    result[i][j] = slot;
                }
            }
        }
        catch (NullPointerException ex) {
            console.error("Impossible to find fact: " + template + " with conditions: " + conditions + " in module " + module);
        }
        catch(CLIPSError ex){
          router.stopRec();
          console.error(router.getStdout());
        }
        return result;
    }

    /**
     * Restituisce tutti i facts corrispondenti ad una certa interrogazione.
     * Prestare attenzione alla precisione nei campi inseriti, che devono
     * corrispondere ai campi del file Clips.
     *
     * @param template il "tipo" di fatto non ordinato a cui si e' interessati
     * @param conditions le condizioni di ricerca dei facts, inserire TRUE se si
     * vogliono tutti i facts di un certo tipo
     * @param slots gli slot dei facts che si vuole vengano restituiti
     * @return una tabella di stringhe in cui ad ogni riga corrisponde un fatto
     * che soddisfa l'interrogazione, e ad ogni colonna uno slot di quel fatto,
     * secondo l'ordine in cui sono dichiarati nel campo slots. null se non c'e'
     * nessun fatto che soddisfa l'interrogazione
     * @throws ClipsException
     */
    public synchronized String[][] findAllFacts(String template, String conditions, String[] slots) throws CLIPSError {
        if (!conditions.equalsIgnoreCase("TRUE")) {
            conditions = "(" + conditions + ")";
        }
        PrimitiveValue fc = clips.eval("(get-focus)");
        String focus = fc.toString();
        String eval = "(find-all-facts ((?f " + template + ")) " + conditions + ")";
        MultifieldValue facts = (MultifieldValue) evaluate(focus, eval);
        String[][] result = null;
        
        try {
            result = new String[facts.size()][slots.length];
            for (int i = 0; i < facts.size(); i++) {
                for (int j = 0; j < slots.length; j++) {
                    FactAddressValue fact = (FactAddressValue)facts.get(i);
                    PrimitiveValue factSlot = fact.getFactSlot(slots[j]);
                    if (factSlot != null) {
                        result[i][j] = factSlot.toString();
                    } else {
                        result[i][j] = "";
                    }
                }
            }
        }
        catch (Exception ex) {
            console.error(ex);
        }
        return result;
    }

    /**
     * Equivalente alla funzione di Clips find-fact(vedere manuale per maggiori
     * informazioni e sintassi delle conditions). Restituisce il primo fatto non
     * ordinato corrispondente ad una certa interrogazione. Prestare attenzione
     * alla precisione nei campi inseriti, che devono corrispondere ai campi del
     * file Clips.
     *
     * @param module il modulo in cui e' visibile il fatto
     * @param template il "tipo" di fatto non ordinato a cui si e' interessati
     * @param conditions le condizioni di ricerca del fatto, inserire TRUE se
     * non ci sono condizioni
     * @param slots gli slot del fatto che si vuole vengano restituiti
     * @return un array di stringhe in cui ad ogni colonna corrisponde uno slot
     * di quel fatto, secondo l'ordine in cui sono dichiarati nel campo slots.
     * null se non c'e' nessun fatto che corrisponde all'interrogazione
     * @throws ClipsException
     */
    public synchronized String[] findFact(String module, String template, String conditions, String[] slots) throws CLIPSError {
        if (!conditions.equalsIgnoreCase("TRUE")) {
            conditions = "(" + conditions + ")";
        }
        String eval = "(find-fact ((?f " + template + ")) " + conditions + ")";
        MultifieldValue facts = (MultifieldValue)evaluate(module, eval);
        String[] result = new String[slots.length];
        try {
            if (facts.size() > 0) {
                for (int j = 0; j < slots.length; j++) {
                    result[j] = ((FactAddressValue)facts.get(0)).getFactSlot(slots[j]).toString();
                }
            }
        }
        catch (Exception ex){
            console.error(ex);
        }
        return result;
    }

    /**
     * Versione per facts ordinati della funzione di Clips find-fact.
     * Restituisce il primo fatto ordinato corrispondente ad una certa
     * interrogazione. Prestare attenzione alla precisione nei campi inseriti,
     * che devono corrispondere ai campi del file Clips.
     *
     * @param module il modulo in cui e' visibile il fatto
     * @param template il symbol con cui inizia il fatto ordinato a cui si e'
     * interessati
     * @return il resto del fatto, null se il fatto non esiste
     * @throws ClipsException
     */
    public synchronized String findOrderedFact(String module, String template) throws CLIPSError {
        String eval = "(find-fact ((?f " + template + ")) TRUE)";
        MultifieldValue facts = (MultifieldValue)evaluate(module, eval);
        String result = "";
        try{
            if (facts.size() != 0) {
                String fatto = facts.get(0).toString();
                StringTokenizer st = new StringTokenizer(fatto, "<Fact- >");
                facts = (MultifieldValue)clips.eval("(fact-slot-value " + (new Integer(st.nextToken())) + " implied)");
                result = facts.get(0).toString();
            }
        }
        catch (Exception ex) {
            console.error(ex);
        }
        return result;
    }

    /**
     * Equivalente alla funzione facts di Clips. Restituisce la lista dei facts
     * del modulo corrente.
     *
     * @return una stringa che rappresenta i facts del modulo corrente
     * @throws ClipsException
     */
    public synchronized String getFactList() {
        router.startRec();
        try{
            PrimitiveValue fc = clips.eval("(get-focus)");
            String focus = fc.toString();
            String eval = "(facts)";
            evaluate(focus, eval);
            router.stopRec();
        }
        catch(CLIPSError ex){
            router.stopRec();
            console.error(router.getStdout());
        }
        return router.getStdout();
    }

    /**
     * Equivalente alla funzione agenda di Clips. Restituisce la lista delle
     * funzioni attualmente attivabili, in ordine di priorita' di attivazione.
     *
     * @return una stringa che rappresenta le azioni attivabili al momento
     * @throws ClipsException
     */
    public synchronized String getAgenda() {
        router.startRec();
        try{
            PrimitiveValue fc = clips.eval("(get-focus)");
            String focus = fc.toString();
            String eval = "(agenda)";
            evaluate(focus, eval);
            router.stopRec();
        }
        catch(CLIPSError ex){
            router.stopRec();
            console.error(router.getStdout());
        }
        return router.getStdout();
    }

    public synchronized String getFocus() {
        try {
            PrimitiveValue fc = clips.eval("(get-focus)");
            return fc.toString();
        }
        catch (CLIPSError ex){
            console.error(ex);
            return "";
        }
    }
    
    /*
    *  Questa funzione ritorna un array di stringhe corrispondenti allo stack 
    *  dei moduli attualmente attivi. Le stringhe dei moduli sono ordinate in maniera
    * crescente a seconda della profondità in cui si trova un determinato modulo attivo
    *
    * @return : un array di stringhe con i nomi dei moduli attivi
    */
    
    public synchronized String[] getFocusStack(){
    
        Stack<String> FocusStack=new Stack<String>();
        ArrayList<String> stack = new ArrayList<String>();
        String [] modules;
        try{
            PrimitiveValue fc=clips.eval("(get-focus)");
            while(!fc.toString().contains("FALSE")){
                 FocusStack.push(fc.toString());
                clips.eval("(pop-focus)");
                 fc = clips.eval("(get-focus)");
            }
            
            while(!FocusStack.empty()){
                
                String module = FocusStack.pop();
                stack.add(module);               
                clips.eval("(focus " + module + ")");
            }
            modules = new String[stack.size()];
            stack.toArray(modules);
            return modules;
        }
        catch(CLIPSError ex){
            console.error(ex);
            return null;
        
        }
        
    
    }

    
    /**
     * Inserisce una regola nell'ambiente clips caricato
     *
     * @param module il modulo su cui inserire la regola
     * @param rule la regola da inserire. Attenzione a scriverla bene in
     * linguaggio clips, perchè non sarà controllata.
     * @return true se non ci sono stati problemi, false altrimenti
     */
    public synchronized boolean defrule(String module, String rule) {
        try{
            
            PrimitiveValue val = evaluate(module, rule);
            return !val.toString().equalsIgnoreCase("FALSE");
        }
        catch(CLIPSError ex){
            console.error(ex);
            return false;
        }
    }

    /**
     * Metodo di supporto per copiare i file da una cartella all'altra.
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    private synchronized static void copyFileUsingFileStreams(File source, File dest) {
        ClipsConsole console = ClipsConsole.getInstance();
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } catch (FileNotFoundException ex) {
            console.error(ex);
        } catch (IOException ex) {
            console.error(ex);
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException ex) {
                console.error(ex);
            }
        }
    }
    /*
     Chiama la clear di clips
     */

    synchronized void clear() {
        clips.clear();
    }

    public synchronized String evaluateOutput(String module, String command) throws CLIPSError {
        router.startRec();
        try{
            this.evaluate(module, command);
            router.stopRec();
        }
        catch(CLIPSError er){
            router.stopRec();
            throw new CLIPSError(er.getModule(), er.getCode(), router.getStdout());
        }
        return router.getStdout();
    }
    
    public synchronized String getBanner(){
        router.startRec();
        clips.printBanner();
        router.stopRec();
        return router.getStdout();
    }
    
    public synchronized String getPrompt(){
        router.startRec();
        clips.printPrompt();
        router.stopRec();
        return router.getStdout();
    }

    
 
  public String getFactList(String query) {
    router.startRec();
        try{
            PrimitiveValue fc = clips.eval("(get-focus)");
            String focus = fc.toString();
            String eval = "(find-all-facts ((?f " + query + ")) TRUE )";
            evaluate(focus, eval);
            router.stopRec();
        }
        catch(CLIPSError ex){
            router.stopRec();
            console.error(router.getStdout());
        }
        return router.getStdout();
  }

  public void RecFromRouter(){
  
    router.startRec();
  }
  
  public void StopRecFromRouter(){
  
    router.stopRec();
  }

  public String GetStdoutFromRouter(){
  
      return router.getStdout();
  }

}

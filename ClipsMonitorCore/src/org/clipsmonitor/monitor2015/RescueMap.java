package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Observer;
import javax.swing.JOptionPane;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsCore;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.core.MonitorMap;
import org.clipsmonitor.monitor2015.RescueModel.Cellslots;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna, Ruben Caliandro
 */
public class RescueMap extends MonitorMap implements Observer {

    private RescueModel model;
    protected MonitorImages images;
    private ClipsConsole console;
    private ClipsCore core;
    private String[][] map;
    
    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private Dimension dim;
    
    private String projectDirectory;

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueMap(String projectDirectory) {
        this.projectDirectory = projectDirectory;
        init();
    }

    @Override
    protected void onDispose() {
        console.debug("Dispose effettuato");
        String result = model.getResult();
        int score = model.getScore();
        @SuppressWarnings("UnusedAssignment")
        String advise = "";
        if (result.equals("disaster")) {
            advise = "DISASTRO\n";
        } else if (model.getTime() == model.getMaxDuration()) {
            advise = "Maxduration has been reached.\n";
        } else {
            advise = "The agent says DONE.\n";
        }
        advise = advise + "Penalties: " + score;
        model.setAdvise(advise);
        this.setChanged();
        this.notifyObservers("advise");
    }
    
    @Override
    public void initMap() throws CLIPSError {
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
    }
    
    /**
     * Aggiorna la mappa visualizzata nell'interfaccia per farla allineare alla
     * versione nel modello.
     *
     */
    @Override
    public void updateMap() throws CLIPSError {
        console.debug("Aggiornamento mappa reale in corso...");
        
        String[] cellArray = {"pos-r", "pos-c", "contains", "injured", "discovered", "checked", "clear"};
        String[][] cellFacts = core.findAllFacts("ENV", "cell", "TRUE", cellArray);

        for (String[] fact : cellFacts) {
            // Nei fatti si conta partendo da 1, nella matrice no, quindi sottraiamo 1.
            int r = new Integer(fact[Cellslots.POSC.slot()]);
            int c = new Integer(fact[Cellslots.POSR.slot()]);

            //caso di default preleviamo il valore dello slot contains e lo applichiamo alla mappa
            map[r - 1][c - 1] = fact[Cellslots.CONTAINS.slot()];  
            
            // controlla se lo slot injured sia impostato a yes
            
            if (fact[Cellslots.INJURIED.slot()].equals("yes")) {
                map[r - 1][c - 1] += "_injured";
            }
            /*
                Aggiorno la mappa in modo da valutare le celle di cui il robot ha fatto già precedentemente
                l'inform: i casi in cui avviene sono :
                 - se la cella contiene debris allora o vale che l'ho scoperta ma non ci sono feriti
                   oppure ci sono feriti e l'ho controllata
                 - se la cella non contiene nulla e ho detto che risulta clear
            */
            
            if ((fact[Cellslots.CONTAINS.slot()].equals("debris") && 
                 (fact[Cellslots.DISCOVERED.slot()].equals("yes") || 
                  fact[Cellslots.CHECKED.slot()].equals("yes"))) || 
                  (fact[Cellslots.CONTAINS.slot()].equals("empty") && fact[Cellslots.CLEAR.slot()].equals("yes"))) {
                map[r - 1][c - 1] += "_informed";
            }
           
        }

        console.debug("Acquisizione posizione dell'agente...");
        String[] arrayRobot = {"step", "time", "pos-r", "pos-c", "direction", "loaded"};
        String[] robot = core.findFact("ENV", "agentstatus", "TRUE", arrayRobot);
        if (robot[0] != null) { //Se hai trovato il fatto
            int r = new Integer(robot[2]);
            int c = new Integer(robot[3]);

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
        console.debug("Aggiornata la posizione dell'agente.");

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

        
    }
    
    @Override
    public BufferedImage[][] getIconMatrix(){
        images = MonitorImages.getInstance();
        
        if(map==null){
            return null;
        }
    
        Map<String, BufferedImage> map_img = images.getMapImg();
        Map<String, BufferedImage> map_img_robot = images.getMapImg();
        BufferedImage[][] iconMatrix = new BufferedImage[map.length][map[0].length];
    
        /*
            casi possibili per le icone :
        
            - agent_direction_load
            - person_rescuer
            - gate
            - outdoor
            - empty
            
        */
        
        for (int i=map.length-1;i>=0;i--){
  
                for (int j = 0; j < map[0].length; j++) {
                    
                    @SuppressWarnings("UnusedAssignment")
                    String direction = "";
                    String loaded = "";
                    String key_agent_map = "";
                    BufferedImage background;
                    BufferedImage robot;
                    BufferedImage undiscovered;

                    

                    // cerca se c'è la stringa "agent_", vedere metodo updateMap in MonitorModel.java
                    // Nel modello si ha una stringa del tipo agent_empty se l'agent si trova su una cella empty.
                    // In modo da inserire l'icona del robot sopra la cella in cui si trova (le due immagini vengono sovrapposte)
                    
                    // ##### SE AGENTE #####
                    
                    
                    if ( map[i][j].contains("agent_")) {
                            direction = model.getDirection();
                            loaded= model.getMode();
                            key_agent_map="agent_"+ direction + "_" + loaded;
                            background = map_img.get(map[i][j].substring(6, map[i][j].length()));
                            robot = map_img_robot.get(key_agent_map);
                            iconMatrix[i][j] = overlapImages(robot, background);
                        
                    }
                    
                    // ##### SE PERSONA #####
                    
                    else if ( map[i][j].equals("person_rescuer")) {
                            iconMatrix[i][j]=map_img.get("person_rescuer");
                    }
                    
                    // ##### SE MACERIE ####
                    else if (map[i][j].contains("debris")) {
                        
                        if(!map[i][j].equals("debris")){
                             iconMatrix[i][j]=map_img.get("debris_injured");
                        } 
                        else{
                             iconMatrix[i][j]=map_img.get("debris");
                        }

          
                        // ##### ALTRIMENTI ####
                        // Era una cella che non aveva bisogno di sovrapposizioni e non è una persona
                    }  
                    
                    // ##### ALTRO #######
                    else {
                        
                        iconMatrix[i][j] = map_img.get(map[i][j]);
                        
                    }
                    
                     //overlap celle non esplorate
                    
                   
                   if(map[i][j].contains("undiscovered")){
                   
                        undiscovered = map_img.get("undiscovered");      
                        String  map_substr =  map[i][j].substring(0,map[i][j].length()- 13); // recupero il tipo di immagine di background a cui
                                                                                                      // vado a sovrapporre l'iconMatrix[i][j]a di undiscover
                        try{                                                                              // escludo il termine "undiscovered" dalla precedente stringa   
                        iconMatrix[i][j]= map_img.get(map_substr);     
                        iconMatrix[i][j]= overlapImages(undiscovered,iconMatrix[i][j]);
                        }
                        catch(NullPointerException e){

                            String err = "Overlap failed: " + map_substr + " pos: (" + i + "," + j + ")" ;
                            JOptionPane.showMessageDialog(null , err);

                        }
                      
                    }     
              }
       
          }
        
          return iconMatrix;
        
    }
    
    @Override
    public int[] getSize() {
        int[] size = null;
        if(map == null || map.length == 0 || map[0].length == 0){
            return null;
        }
        else{
            size = new int[2];
            size[0] = map.length;
            size[1] = map[0].length;
        }
        return size;
    }

    @Override
    protected void clear() {
        this.console = null;
        this.dim = null;
        this.model = null;
        this.images = null;
        this.core = null;
    }

    @Override
    protected void init() {
        model = RescueModel.getInstance();
        model.addObserver(this);
        console = ClipsConsole.getInstance();
        images = MonitorImages.getInstance();
        core = ClipsCore.getInstance();
    }

   
}







/*
     * Sostiuisce i parametri nella forma %par<i> con param<i>.
     *
     * @param text
     * @param parameters
     * @return
     /
    protected String mapParameters(String text, String[] parameters) {
        for (int i = 1; i <= parameters.length; i++) {
            text = text.replace("%p" + i, parameters[i - 1]);
        }
        return text;
    }
    
    
    /**
     * Rimuove il primo e l'ultimo carattere di una stringa. Nel nostro caso le
     * virgolette di un text.
     *
     * @return
     /
    protected String removeFistAndLastChar(String text) {
        return text.substring(1).replace(text.substring(text.length() - 1), "");
    }
    
//    private BufferedImage imageIcon2Buffered(ImageIcon tempicon) {
//        BufferedImage bi = new BufferedImage(
//                tempicon.getIconWidth(),
//                tempicon.getIconHeight(),
//                BufferedImage.TYPE_INT_RGB);
//        Graphics g = bi.createGraphics();
//        // paint the Icon to the BufferedImage.
//        tempicon.paintIcon(null, g, 0, 0);
//        g.dispose();
//        return bi;
//    }

    // protected void updateOutput() {
    //     //########################### AGGIORNO LA FINESTRA DEI MESSAGGI DI OUTPUT ############################
    //     String[] slots = {"time", "step", "source", "verbosity", "text", "param1", "param2", "param3", "param4", "param5"};
    //     try {
    //         /**
    //          * Ogni fatto viene considerato nella forma: [source] testo (con
    //          * parametri corretti).
    //          *
    //          * È necessario compiere alcune operazioni di processing poiché: 1 -
    //          * le virgolette fanno parte della stringa. 2 - i parametri devono
    //          * essere sostituiti.
    //          */
    //         String[][] matriceFatti = model.findAllFacts("printGUI", "TRUE", slots);

    //         for (String[] fatto : matriceFatti) {
    //             int fact_verbosity = Integer.parseInt(fatto[3]); //Consideriamo la verbosità
    //             String source = removeFistAndLastChar(fatto[2]);
    //             String line = fatto[1] + "\t" + source + "\t" + removeFistAndLastChar(fatto[4]); //prendiamo il testo così com'è
    //             //E applichiamo le sostituzioni, appendendo il risultato alla finestra
    //             String parameters[] = {fatto[5], fatto[6], fatto[7], fatto[8], fatto[9]};
    //             console.clips(source + mapParameters(line, parameters));
    //         }

    //     } catch (Exception ex) {
    //         console.error(ex);
    //     }
    // }


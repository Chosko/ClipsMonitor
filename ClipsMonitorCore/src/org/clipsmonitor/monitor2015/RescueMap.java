package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Observer;
import javax.swing.JOptionPane;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna
 */
public class RescueMap extends MonitorMap implements Observer {

    private RescueModel model;
    protected MonitorImages images;
    private ClipsConsole console;
    public final int MAP_DIMENSION = 550;
    public final int DEFAULT_IMG_SIZE = 85;
    
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
    protected void onSetup() {
        this.setChanged();
        this.notifyObservers("initializeMap");
        console.info("Setup completato");
    }

    @Override
    protected void onAction() {
        try {
            updateMap();
        } catch (IOException ex) {
            console.error(ex);
        }
        console.info("Azione effettuata.");
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

    /**
     * Aggiorna la mappa visualizzata nell'interfaccia per farla allineare alla
     * versione nel modello.
     *
     */
    private void updateMap() throws IOException {
        this.setChanged();
        this.notifyObservers("repaint");
        console.info("Step attuale: " + model.getStep());
    }

    
    
    
   
    @Override
    public BufferedImage[][] makeIconMatrix(String[][] mapString){
        images = MonitorImages.getInstance();
        
        if(mapString==null){
            return null;
        }
    
        Map<String, BufferedImage> map_img = images.getMapImg();
        Map<String, BufferedImage> map_img_robot = images.getMapImg();
        BufferedImage[][] iconMatrix = new BufferedImage[mapString.length][mapString[0].length];
    
        /*
            casi possibili per le icone :
        
            - agent_direction_load
            - person_rescuer
            - gate
            - outdoor
            - empty
            
        */
        
        for (int i=mapString.length-1;i>=0;i--){
  
                for (int j = 0; j < mapString[0].length; j++) {
                    
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
                    
                    
                    if ( mapString[i][j].contains("agent_")) {
                            direction = model.getDirection();
                            loaded= model.getMode();
                            key_agent_map="agent_"+ direction + "_" + loaded;
                            background = map_img.get(mapString[i][j].substring(6, mapString[i][j].length()));
                            robot = map_img_robot.get(key_agent_map);
                            iconMatrix[i][j] = overlapImages(robot, background);
                        
                    }
                    
                    // ##### SE PERSONA #####
                    
                    else if ( mapString[i][j].equals("person_rescuer")) {
                            iconMatrix[i][j]=map_img.get("person_rescuer");
                    }
                    
                    // ##### SE MACERIE ####
                    else if (mapString[i][j].contains("debris")) {
                        
                        if(!mapString[i][j].equals("debris")){
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
                        
                        iconMatrix[i][j] = map_img.get(mapString[i][j]);
                        
                    }
                    
                     //overlap celle non esplorate
                    
                   
                   if(mapString[i][j].contains("undiscovered")){
                   
                        undiscovered = map_img.get("undiscovered");      
                        String  map_substr =  mapString[i][j].substring(0,mapString[i][j].length()- 13); // recupero il tipo di immagine di background a cui
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
    
    
    
    public String[][] getMap() {
        return model.getEnvMap();
    }

    

    @Override
    protected void clear() {
        this.console = null;
        this.dim = null;
        this.model = null;
        this.images = null;
    }

    @Override
    protected void init() {
        model = RescueModel.getInstance();
        model.addObserver(this);
        console = ClipsConsole.getInstance();
        images = MonitorImages.getInstance();
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


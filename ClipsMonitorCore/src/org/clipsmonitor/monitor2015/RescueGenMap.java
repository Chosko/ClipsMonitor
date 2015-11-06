/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import org.clipsmonitor.core.MonitorGenMap;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;
import org.clipsmonitor.core.MonitorImages;

/*
 * Classe che definisce il concetto di scena all'interno del progetto e tutti i metodi per accedervi e
 * modificarla. Inoltre permette la possibilità di salvare e caricare le mappe per poter essere poi
 * successiviamente utilizzate dal simulatore
 *
 * @author Tobia Giani, Alessandro Basile, Marco Corona
 */


public class RescueGenMap extends MonitorGenMap {
 
    
    private static RescueGenMap instance;
    
    
    private String direction; // direzione iniziale del robot
    
    /*
        La classe viene definita singleton 
    */
    
    public static RescueGenMap getInstance(){
        if(instance == null){
            instance = new RescueGenMap();
            instance.init();
        }
        return instance;
    }
    
    private RescueGenMap() {

    }

    public void init(){
  
        this.NumCellX=0;
        this.NumCellY=0;
        this.MapWidth=0;
        this.MapHeight=0;
        this.CellHeight=0;
        this.CellWidth=0;
        this.direction="north";
        this.maxduration=100;
        this.defaultagentposition= new int [2];
        this.defaultagentposition[0]=3;
        this.defaultagentposition[1]=2 ;
        this.agentposition= new int [2];
        this.agentposition[0]=this.defaultagentposition[0];
        this.agentposition[1]=this.defaultagentposition[1];
        
        //carico tutte le image in ram
        this.loadImages();
        
    }
    
    
    

    /*
    Carica le immagini del progetto e genera l'array di stringhe che possono essere
    utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
    */
    
    public void loadImages() {
        HashMap<String,BufferedImage> mapicons;
        mapicons = (HashMap<String,BufferedImage>) MonitorImages.getInstance().getMapImg();
        this.images = mapicons;
        Set<String> keys = images.keySet();
        setKeyMap= keys.toArray(new String[keys.size()]);
    }

    

    /*
        Inizializzazione della scena eseguita mettendo nel perimetro della scena l'outdoor
        e riempiendo il resto con le celle empty
    */
    @Override
    public void initScene(String[][] scene) {

        //imposto i muri sul perimetro
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j]="outdoor"; 
                }
                else{
                
                    scene[i][j]="empty";
                }
            }
        }
       
        scene[this.agentposition[0]][this.agentposition[1]]="agent_" + direction + "_unloaded";
    }
    
    
    
    
    @Override
    public String exportHistory() {
        String history = "";
        
        history +="(maxduration " + this.maxduration + ") \n\n";
        
        // posizione iniziale dell'agente
        history +="(initial_agentposition ( pos-r " + this.agentposition[0] + ")";
        history +="( pos-c " + this.agentposition[1] + ")";
        history +="(direction " + this.direction + ")) \n\n";
                
        // posizione iniziale dei person rescuer
        int count = 1;
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (scene[i][j].equals("person_rescuer")) {  //controllo che la cella contenga una persona
                    history += "\n(personstatus\n\t(step 0)\n\t(time 0)\n\t(ident C" + count + ")\n";
                    history += "\t(pos-r " + (scene[i].length - j) + ")\n";
                    history += "\t(pos-c " + (i + 1) + ")\n";
                    history += "\t(activity out)\n)\n";
                    count++;
                }
                
            }
        }
        return history;
    }
    
    /*
     Genera il testo contenente i fatti che descrivono una possibile mappa, includenedo
     anche la massima durata di tempo concesso all'agente e alla sua posizione iniziale
    */
    
    @Override
    public String exportScene() {
        
        String map ="";
        //variabili per impostare la posizione delle componenti
               
        String s = "";
        
        //Scansione della matrice di celle
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                boolean injuredPresence = scene[i][j].contains("_injured");
                if(injuredPresence){
                s += "(real-cell (pos-r " + (scene[i].length - j) + ") (pos-c " + (i + 1) + ") (contains " + scene[i][j].substring(0,scene[i][j].length()-"_injured".length() +1) + ")"
                        + "(injured yes))\n";
                }
                else{
                  
                  if(scene[i][j].contains("agent")){
                      s+="(real-cell (pos-r " + (scene[i].length - j) + ") (pos-c " + (i + 1) + ") (contains empty)"
                        + "(injured no))\n";
                  }
                  else{
                    s += "(real-cell (pos-r " + (scene[i].length - j) + ") (pos-c " + (i + 1) + ") (contains " + scene[i][j] + ")"
                      + "(injured no))\n";
                    }
                
                }
                
                
             }
            
        }

        //costuisco la string da salvare sul file
                
        //concateno con la definizione delle celle;
        map += "\n" + s;
        return map;
    }
    
    
    /*
    * Metodo per l'aggiornamento consistente delle celle
    *  @param x ,y : possibile in riga e colonna della cella da modificare
    *  @param state : nuovo stato da inserire
    */
   
    @Override
    public boolean UpdateCell(int x, int y, String state) {
        
        boolean result = true;
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
             
            // se è stato richiesto un aggiornamento della posizione di agent
            // controllo se attualmente non si trova nella stessa cella in cui vado a fare la modifica
            
            if(state.contains("agent")){ 
                
                // se la nuova posizione agente è diversa dalla precedente
                if(x!=this.agentposition[0] || y!=this.agentposition[1]){ 
                    
                    scene[x][y]=state;                                    
                    scene[this.agentposition[0]][this.agentposition[1]]="empty"; // rimuovo l'agente
                    this.agentposition[0]=x; // setto la nuova posizione dell'agente
                    this.agentposition[1]=y;
                    
                    // setto la nuova direzione
                    if(state.contains("north")){
                        this.direction="north";
                    }
                    if(state.contains("west")){
                        this.direction="west";
                    }
                    if(state.contains("east")){
                        this.direction="east";
                    }
                    if(state.contains("south")){
                        this.direction="south";
                    }
                    
                }
                else{ // stessa posizione attuale dell'agent position
                    
                    scene[x][y]=state; 
                }
            }
            else{ // valori di state differenti dalla posizione dell'agente
                if(x==this.agentposition[0] && y==this.agentposition[1]){ 
                    scene[x][y]=state;                                    
                    this.agentposition[0]=this.defaultagentposition[0]; // setto la posizione di default dell'agent
                    this.agentposition[1]=this.defaultagentposition[1];
                    scene[this.agentposition[0]][this.agentposition[1]]="agent_north_unloaded";
                }
                else{
                scene[x][y]=state;
                }
            }
        } 
        else { // caso di modifiche non permesse dal modello
            
            result = false;
        }
        return result;
    }


    
}

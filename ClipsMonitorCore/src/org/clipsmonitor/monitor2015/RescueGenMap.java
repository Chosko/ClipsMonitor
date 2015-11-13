/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import org.clipsmonitor.core.MonitorGenMap;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorImages;
import org.clipsmonitor.monitor2015.RescueFacts;

/*
 * Classe che definisce il concetto di scena all'interno del progetto e tutti i metodi per accedervi e
 * modificarla. Inoltre permette la possibilità di salvare e caricare le mappe per poter essere poi
 * successiviamente utilizzate dal simulatore
 *
 * @author Tobia Giani, Alessandro Basile, Marco Corona
 */


public class RescueGenMap extends MonitorGenMap {
 
    
    private static RescueGenMap instance;
    private static RescueFacts facts;
    
    
    private String direction; // direzione iniziale del robot
    private String loaded; // stato iniziale del robot
    
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
    
    @Override
    public void init(){
        this.console = ClipsConsole.getInstance();
        this.img = MonitorImages.getInstance();
        console.debug("Inizializzazione del map geneator");
        this.NumCellX=0;
        this.NumCellY=0;
        this.MapWidth=0;
        this.MapHeight=0;
        this.CellHeight=0;
        this.CellWidth=0;
        this.direction="north";
        this.loaded="unloaded";
        this.maxduration=100;
        this.mode="scene";
        this.personName="person_rescuer";
        this.defaulagentcondition="gate_agent_north_unloaded";
        this.defaultagentposition= new int [2];
        this.defaultagentposition[0]=3;
        this.defaultagentposition[1]=2 ;
        this.agentposition= new int [2];
        this.agentposition[0]=this.defaultagentposition[0];
        this.agentposition[1]=this.defaultagentposition[1];
        this.NumPerson=0;
        this.Persons= new LinkedList<Person>();
        this.setKeyColor = new String[]{"blue","green","red","yellow","grey"};
        this.setKeyMap =  new String[]{"agent_north_unloaded","agent_north_loaded",
                "agent_west_unloaded","agent_west_loaded","agent_east_unloaded","agent_east_loaded",
        "agent_south_unloaded","agent_south_loaded","gate","empty","outdoor","wall","debris","debris_injured"};
        this.MaxNumPerson=this.setKeyColor.length;
        console.debug("Inizializzzione terminata del map generator");
    }
    
    


    

    /*
    *   Inizializzazione della scena eseguita mettendo nel perimetro della scena l'outdoor
    *   e riempiendo il resto con le celle empty
    */
    @Override
    public void initScene(String[][] scene) {

        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j]="outdoor"; 
                }
                else if (i==1 || i==scene.length-2 || j ==1 || j==scene[0].length-2){
                    scene[i][j]="wall";
                }
                else{
                
                    scene[i][j]="empty";
                }
            }
        }
       
        scene[this.agentposition[0]][this.agentposition[1]]="gate" + "_" + "agent_" + direction + "_" + loaded;
        this.move = this.clone(scene);
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
                if (scene[i][j].equals(personName)) {  //controllo che la cella contenga una persona
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
    *  Verifica se la posizione dell'agente richiesta risulta essere compatibile
    *  rispetto ai vincoli del progetto. Ritorna true se la condizione è rispettata
    */
    
    @Override
    public boolean RobotPositionIsValid(String mapPos){
    
       return !mapPos.contains("debris") && !mapPos.contains("wall") && !mapPos.contains("outdoor");
    
    }

        /*
    *  Verifica se la posizione dell'agente richiesta risulta essere compatibile
    *  rispetto ai vincoli del progetto. Ritorna true se la condizione è rispettata
    */
    
    @Override
    public boolean PersonPositionIsValid(String mapPos){
    
        return !mapPos.contains("debris") && !mapPos.contains("wall");
     
    }
    
    @Override 
    public void SetRobotParams(String state , int x , int y){
    
        this.agentposition[0]=x; 
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

        if(state.equals("unloaded")){
            this.loaded="unloaded";
        }

        if(state.equals("loaded")){

            this.loaded="loaded";
        }

    
    }
    
    
    
    
    /*
     *   Metodo per la creazione della matrice di icone da disegnare sulla mappa del generatore.
     *   Il metodo si occupa di creare le icone con l'overlap . 
     *   @param typeMap : determina quale tipologia di mappa si sta chiedendo la visualizzazione
    */
    
    @Override
    public BufferedImage[][] makeIconMatrix(String typeMap){
    
        BufferedImage[][] icons = new BufferedImage[mapActive.length][mapActive[0].length];
    
        if(typeMap.equals("scene")){
            
            for(int i=0;i<this.NumCellX;i++){
            
                for(int j=0;j<this.NumCellY;j++){
                
                    if(!mapActive[i][j].equals("")){
                        
                        if(mapActive[i][j].contains(personName)){
                            int underscoreSeparate = mapActive[i][j].indexOf("_");
                            String background = mapActive[i][j].substring(0,underscoreSeparate);
                            BufferedImage backImg = img.getImage(background);
                            BufferedImage overlapImg = img.overlapImages(img.getImage(personName),backImg);
                            icons[i][j]=overlapImg;
                            
                        }
                        
                        else if(mapActive[i][j].contains("agent")){
                            int underscoreSeparate = mapActive[i][j].indexOf("_");
                            String background = mapActive[i][j].substring(0,underscoreSeparate);
                            String key_agent_map="agent_"+ direction + "_" + loaded;
                            BufferedImage overlapImage = img.overlapImages(img.getImage(key_agent_map),img.getImage(background));
                            icons[i][j]=overlapImage;
                    
                        }
                        else{
                            icons[i][j]=img.getImage(mapActive[i][j]);
                        }
                    }
                }
            
            }
        
        
        } 
    
        // determino le regole nel caso in cui ci sia attiva la mappa di move
        
        if(typeMap.equals("move")){
        
            for(int i=0;i<this.NumCellX;i++){
            
                for(int j=0;j<this.NumCellY;j++){
                    
                    if(mapActive[i][j].contains("agent")){
                    
                        int underscoreSeparate = mapActive[i][j].indexOf("_");
                        String background = mapActive[i][j].substring(0,underscoreSeparate);
                        BufferedImage backImg = img.getImage(background);
                        String key_agent_map="agent_"+ direction + "_" + loaded;
                        BufferedImage overlapImage = img.overlapImages(img.getImage(key_agent_map),backImg);
                        icons[i][j]=overlapImage;
                    
                    }
                    else if(mapActive[i][j].contains("last")){
                            String [] underscoreSplit = mapActive[i][j].split("_");
                            String color = underscoreSplit[1];
                            String background = underscoreSplit[0];
                            BufferedImage tmp = img.overlapImages(img.getImage(color),img.getImage(background));
                            BufferedImage overlapImage = img.overlapImages(img.getImage(personName), tmp);
                            icons[i][j]=overlapImage;
                    }
                    else{
                        if(mapActive[i][j].contains("empty") && !mapActive[i][j].equals("empty")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage overlapImage = img.overlapImages(img.getImage(color),img.getImage("empty"));
                            icons[i][j]=overlapImage;
                        }
                        else if(mapActive[i][j].contains("gate") && !mapActive[i][j].equals("gate")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage overlapImage = img.overlapImages(img.getImage(color),img.getImage("gate"));
                            icons[i][j]=overlapImage;
                        }

                        else if(mapActive[i][j].contains("outdoor") && !mapActive[i][j].equals("outdoor")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage overlapImage = img.overlapImages(img.getImage(color),img.getImage("outdoor"));
                            icons[i][j]=overlapImage;
                        }
                    
                        else{

                            icons[i][j]=img.getImage(mapActive[i][j]);
                        }
                    
                    }
                }
        
            }
                
       }
        
        return icons;
    }
    
    
}

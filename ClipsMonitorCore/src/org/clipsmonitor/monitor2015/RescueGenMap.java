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
import java.util.ListIterator;
import org.clipsmonitor.clips.ClipsConsole;
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
        console.debug("Inizializzazione del map geneator");
        this.NumCellX=0;
        this.NumCellY=0;
        this.MapWidth=0;
        this.MapHeight=0;
        this.CellHeight=0;
        this.CellWidth=0;
        this.direction="north";
        this.direction="unloaded";
        this.maxduration=100;
        this.mode="scene";
        this.personName="person_rescuer";
        this.defaultagentposition= new int [2];
        this.defaultagentposition[0]=3;
        this.defaultagentposition[1]=2 ;
        this.agentposition= new int [2];
        this.agentposition[0]=this.defaultagentposition[0];
        this.agentposition[1]=this.defaultagentposition[1];
        this.NumPerson=0;
        this.Persons= new LinkedList<Person>();
        this.images= new HashMap<String,BufferedImage>();
        this.colors= new HashMap<String,BufferedImage>();
        this.setKeyColor = null;
        this.setKeyMap = null;
       
        this.loadImages();
        console.debug("Inizializzzione terminata del map generator");
    }
    
    


    

    /*
        Inizializzazione della scena eseguita mettendo nel perimetro della scena l'outdoor
        e riempiendo il resto con le celle empty
    */
    @Override
    public void initScene(String[][] scene) {

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
    public int UpdateCell(int x, int y, String state) {
        
        final int Success = 0;
        final int IllegalPosition = 1 ;
        final int keyColorEmpty = 2; 
        final int keyColorFull = 3;
        
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
             
            // se è stato richiesto un aggiornamento della posizione di agent
            // controllo se attualmente non si trova nella stessa cella in cui vado a fare la modifica
            
            if(state.contains("agent")){ 
                
                // se la nuova posizione agente è diversa dalla precedente
                if(x!=this.agentposition[0] || y!=this.agentposition[1]){ 
                    
                    // rimuovo l'agente dalla posizione corrente sostuiendolo con un empty
                    // e successivamente inserisco il nuovo agente
                    scene[x][y]=state;                                    
                    scene[this.agentposition[0]][this.agentposition[1]]="empty"; 
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
                    
                    if(state.contains("unloaded")){
                        this.loaded="unloaded";
                    }
                    
                    if(state.contains("loaded")){
                    
                        this.loaded="loaded";
                    }
                }
                else{ // stessa posizione attuale dell'agent position
                    
                    scene[x][y]=state; 
                }
            }
            // si vuole aggiungere una nuova persona alla storia
            else if(state.contains("person_rescuer")){
              
              if(this.setKeyColor.length==0){
                  return keyColorEmpty;
              }
              // ho ancora disponibilita di colori per indicare le person
              if(this.NumPerson<this.colors.size() && !scene[x][y].contains("person_rescuer")){          
                        this.NumPerson++;
                        String color=this.setKeyColor[this.NumPerson-1];
                        this.Persons.add(new Person(color));
                        String path ="P"+ this.Persons.size();
                        this.Persons.getLast().getMoves().add(new StepMove(x,y,path,0));
                        scene[x][y]=state + "_" + color;
                }  
              // ho terminato il numero di aggiunte che posso fare
              else{
                  return keyColorFull;
              }  
                
            }
            // si richiedono modifiche alla scena diverse da tipologie di state agent
            else{ 
                // nel caso in cui dovessi sovrascrivere la posizione attuale dell'agente
                // allora semplicemente reimposto la posizione di default dell'agente
                if(x==this.agentposition[0] && y==this.agentposition[1]){ 
                    scene[x][y]=state;                                    
                    this.agentposition[0]=this.defaultagentposition[0]; 
                    this.agentposition[1]=this.defaultagentposition[1];
                    scene[this.agentposition[0]][this.agentposition[1]]="agent_north_unloaded";
                }
                else if(scene[x][y].contains("person_rescuer")){
                    
                    int lastUnderScore = scene[x][y].lastIndexOf("_");
                    String color = scene[x][y].substring(lastUnderScore+1);
                    ListIterator<Person> it = this.Persons.listIterator();
                    while(it.hasNext()){
                    
                        Person p = it.next();
                        if(p.getColor().equals(color)){
                            this.Persons.remove(p);
                        }
                    }
                    scene[x][y]=state;
                }
                else{
                    scene[x][y]=state;
                }
            }
        } 
        else { // caso di modifiche non permesse dal modello
            
            return IllegalPosition;
        }
        return Success;
    }
    
    /*
    * Questo metodo genera l'aggiornamento delle celle della mappa del generatore in modalità
    * move, determinando quali movimenti sono possibili per un agente e in tal caso aggiorna la
    * lista dei movimenti 
    */

    @Override
    public int UpdateMoveCell(int x, int y, Person p){
    
        final int Success = 0;
        final int IllegalPosition = 1;
        
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
            
             StepMove s = p.getMoves().getLast();
             // distanza di manhattam
             if((s.getRow()-x)+ (s.getColumn()-y)<=1){
                 p.getMoves().addLast(new StepMove(x,y,p.getMoves().getLast().getPath() , p.getMoves().getLast().getStep()));
             }
             
             return Success;
        }
        else{
        
            return IllegalPosition;
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
            
                for(int j=0;i<this.NumCellY;j++){
                
                    if(!mapActive[i][j].equals("")){
                        
                        if(mapActive[i][j].contains(personName)){
                            icons[i][j]=this.images.get(personName);
                            
                        }
                        
                        else if(mapActive[i][j].contains("agent")){
                    
                        String background = mapActive[i][j].substring("agent_".length());
                        BufferedImage backImg = this.images.get(background);
                        String key_agent_map="agent_"+ direction + "_" + loaded;
                        BufferedImage img = MonitorImages.getInstance().overlapImages(backImg,this.images.get(key_agent_map));
                        icons[i][j]=img;
                    
                        }
                        else{
                            icons[i][j]=this.images.get(mapActive[i][j]);
                        }
                    }
                }
            
            }
        
        
        } 
    
        // determino le regole nel caso in cui ci sia attiva la mappa di move
        
        if(typeMap.equals("move")){
        
            for(int i=0;i<this.NumCellX;i++){
            
                for(int j=0;i<this.NumCellY;j++){
                    
                    if(mapActive[i][j].contains("agent")){
                    
                        String background = mapActive[i][j].substring("agent_".length());
                        BufferedImage backImg = this.images.get(background);
                        String key_agent_map="agent_"+ direction + "_" + loaded;
                        BufferedImage img = MonitorImages.getInstance().overlapImages(backImg,this.images.get(key_agent_map));
                        icons[i][j]=img;
                    
                    }
                    
                    else if(mapActive[i][j].contains("empty") && !mapActive[i][j].equals("empty")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get("empty"),this.colors.get(color));
                            icons[i][j]=img;
                        }
                    else if(mapActive[i][j].contains("gate") && !mapActive[i][j].equals("gate")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get("gate"),this.colors.get(color));
                            icons[i][j]=img;
                        }

                    else if(mapActive[i][j].contains("outdoor") && !mapActive[i][j].equals("outdoor")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get("outdoor"),this.colors.get(color));
                            icons[i][j]=img;
                        }
                    else{
                    
                        icons[i][j]=this.images.get(mapActive[i][j]);
                    }
                    
                    
                }
        
            }
                
       }
        
        return icons;
    }
    
    
}

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
        this.loaded="unloaded";
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
    * Metodo per l'aggiornamento consistente delle celle. Il metodo ritorna interi corrispondenti
    * ad un particolare conclusione dell'esecuzione. L'aggiornamento viene sostanzialmente separato
    * in tre casi (richiesta di una posizione dell'agente robotico , richiesta di una nuova posizione iniziale
    * di un agente umano, modifiche allo scenario).
    * Il robot può essere modificato nella sua posizione, solo se si trova in celle contenenti (empty e gate) 
    * mentre le person possono stare nelle celle contenenti(empty,gate e outdoor)
    * @param x ,y : possibile in riga e colonna della cella da modificare
    * @param state : nuovo stato da inserire
    * @return   Success ==0 : aggiornamento consistente
    *           IllegalPosition ==1 : posizione del cursore non valida
    *           KeyColorEmpty ==2 : le chiavi dei colori non sono state correttamente generate
    *           KeyColorFull ==3 : le chiavi per nuove person sono terminate
    *           IllegalRobotPosition ==4 : posizione del robot non valida
    *           IllegalAgentPosition ==5 : posizione dell'agente umano non valida
    *           PersonOverride ==6 : sovrascrittura di un agente umano
    */
   
    @Override
    public int UpdateCell(int x, int y, String state) {
        
        final int Success = 0;
        final int IllegalPosition = 1 ;
        final int IllegalRobotPosition = 2;
        
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
             
            // se è stato richiesto un aggiornamento della posizione di agent
            // controllo se attualmente non si trova nella stessa cella in cui vado a fare la modifica
            
                    if(state.contains("agent")){ 

                        // se la nuova posizione agente è diversa dalla precedente
                        if(x!=this.agentposition[0] || y!=this.agentposition[1]){ 

                            if(this.RobotPositionIsValid(scene[x][y])){

                                // rimuovo l'agente dalla posizione corrente sostuiendolo con un empty
                                // e successivamente inserisco il nuovo agente

                                int separate = scene[this.agentposition[0]][this.agentposition[1]].indexOf("_");
                                String background = scene[this.agentposition[0]][this.agentposition[1]].substring(0,separate);
                                scene[x][y] += "_" + state;
                                scene[this.agentposition[0]][this.agentposition[1]]=background; 
                                this.SetRobotParams(state, x, y);

                            }
                            else{

                                return IllegalRobotPosition;
                            }

                        }
                        else{ // stessa posizione attuale dell'agent position

                            int separate = scene[x][y].indexOf("_");
                            String background = scene[x][y].substring(0,separate);
                            scene[x][y]=background+state; 
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
                            scene[this.agentposition[0]][this.agentposition[1]]="gate_agent_north_unloaded";
                        }
                        
                        else{
                            scene[x][y]=state;
                        }
                    }
        } 
        else {  // punto della mappa non disponibile per la modifica
            
                return IllegalPosition;
        }
        return Success;
    }
    
    
    public int AddNewPerson( int x , int y , String color ){
    
        final int Success = 0;
        final int IllegalPosition = 1 ;
        final int keyColorEmpty = 2; 
        final int keyColorFull = 3;
        final int IllegalAgentPosition = 5;
        final int PersonOverride = 6;

        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {

            if(this.setKeyColor.length==0){
                return keyColorEmpty;
            }

            if(x==this.agentposition[0] && y==this.agentposition[1]){

                return IllegalAgentPosition;
            }

            if(this.CheckBusyStartCellFromPerson(x, y)!=-1){

                return PersonOverride;
            }
            
            if(this.findPosByColor(color)!=-1){
            
                Person p = this.findByColor(color);
                p.getMoves().getFirst().setRow(x);
                p.getMoves().getFirst().setColumn(y);
                return PersonOverride;
            
            }
            
            // ho ancora disponibilita di colori per indicare le person
            if(this.NumPerson<this.colors.size()){          
                    this.NumPerson++;
                    this.Persons.add(new Person(color));
                    String path ="P"+ this.Persons.getLast().getPaths().size();
                    this.Persons.getLast().getPaths().add(path);
                    this.Persons.getLast().getMoves().add(new StepMove(x,y,path,0,0));
                    if(this.PersonPositionIsValid(move[x][y])){
                        String background = move[x][y];
                        move[x][y]=background + "_" + personName + "_" + color;
                    }
                    else{

                          return IllegalAgentPosition;
                    }
              }  
            // ho terminato il numero di aggiunte che posso fare
            else{
                return keyColorFull;
            }  

            return Success;
        }
        else{
            
            return IllegalPosition;
        }
    }
    

    public boolean Remove(String color){
   
        ListIterator<Person> it = this.Persons.listIterator();
        while(it.hasNext()){

            Person p = it.next();
            if(p.getColor().equals(color)){
                this.Persons.remove(p);
                this.NumPerson--;
                return true;
            }
        }
       
        return false;
    }
    
    /*
    * Questo metodo genera l'aggiornamento delle celle della mappa del generatore in modalità
    * move, determinando quali movimenti sono possibili per un agente e in tal caso aggiorna la
    * lista dei movimenti 
    * @param x : numero di riga
    * @param y : numero di colonna
    * @param p : persona a cui aggiungere la move
    */

    @Override
    public int UpdateMoveCell(int x, int y, String color){
    
        final int Success = 0;
        final int IllegalPosition = 1;
        final int UnavaibleMove = 2;
        
        
        Person p=this.findByColor(color);
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
            
             StepMove s = p.getMoves().getLast();
             // distanza di manhattam e check sulla attraversabilità della cella
             if(this.ManhattamDistance(s.getRow(),s.getColumn(), x, y)==1 && this.PersonPositionIsValid(scene[x][y])){
                 String pathName = "P"+ p.getPaths().get(p.getPaths().size()-1);
                 int start = s.getStepStart();
                 int step = p.getMoves().getLast().getStep()+1;
                 p.getMoves().addLast(new StepMove(x,y, pathName , start , step));
                 return Success;
             }
             else{
                 return UnavaibleMove ;
             
             }
             
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
            
                for(int j=0;j<this.NumCellY;j++){
                
                    if(!mapActive[i][j].equals("")){
                        
                        if(mapActive[i][j].contains(personName)){
                            int underscoreSeparate = mapActive[i][j].indexOf("_");
                            String background = mapActive[i][j].substring(0,underscoreSeparate);
                            BufferedImage backImg = this.images.get(background);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get(personName),backImg);
                            icons[i][j]=img;
                            
                        }
                        
                        else if(mapActive[i][j].contains("agent")){
                            int underscoreSeparate = mapActive[i][j].indexOf("_");
                            String background = mapActive[i][j].substring(0,underscoreSeparate);
                            BufferedImage backImg = this.images.get(background);
                            String key_agent_map="agent_"+ direction + "_" + loaded;
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get(key_agent_map),backImg);
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
            
                for(int j=0;j<this.NumCellY;j++){
                    
                    if(mapActive[i][j].contains("agent")){
                    
                        int underscoreSeparate = mapActive[i][j].indexOf("_");
                        String background = mapActive[i][j].substring(0,underscoreSeparate);
                        BufferedImage backImg = this.images.get(background);
                        String key_agent_map="agent_"+ direction + "_" + loaded;
                        BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get(key_agent_map),backImg);
                        icons[i][j]=img;
                    
                    }
                    else if(mapActive[i][j].contains("last")){
                            String [] underscoreSplit = mapActive[i][j].split("_");
                            String color = underscoreSplit[1];
                            String background = underscoreSplit[0];
                            BufferedImage tmp = MonitorImages.getInstance().overlapImages(this.colors.get(color),this.images.get(background));
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.images.get(personName), tmp);
                            icons[i][j]=img;
                    }
                    else{
                        if(mapActive[i][j].contains("empty") && !mapActive[i][j].equals("empty")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.colors.get(color),this.images.get("empty"));
                            icons[i][j]=img;
                        }
                        else if(mapActive[i][j].contains("gate") && !mapActive[i][j].equals("gate")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.colors.get(color),this.images.get("gate"));
                            icons[i][j]=img;
                        }

                        else if(mapActive[i][j].contains("outdoor") && !mapActive[i][j].equals("outdoor")){

                            int lastUnderScore = mapActive[i][j].lastIndexOf("_");
                            String color = mapActive[i][j].substring(lastUnderScore+1);
                            BufferedImage img = MonitorImages.getInstance().overlapImages(this.colors.get(color),this.images.get("outdoor"));
                            icons[i][j]=img;
                        }
                    
                        else{

                            icons[i][j]=this.images.get(mapActive[i][j]);
                        }
                    
                    }
                }
        
            }
                
       }
        
        return icons;
    }
    
    
}

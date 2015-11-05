package org.clipsmonitor.mapgenerator;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;
import org.clipsmonitor.monitor2015.RescueImages;

/*
 * Classe che definisce il concetto di scena all'interno del progetto e tutti i metodi per accedervi e
 * modificarla
 *
 * @author Tobia Giani, Alessandro Basile, Marco Corona
 */


public class MapGeneratorSceneModel {

    private static MapGeneratorSceneModel instance;
    
    private int NumCellX, NumCellY; //numero di celle sulle x e sulle y
    private float CellWidth, CellHeight; //largezza e altezza celle
    private float MapWidth, MapHeight;  //largezza e altezza finestra
    
    private String[][] scene; //matrice fondamentale rappresentante la scena
    private String direction; // direzione iniziale del robot
    private int maxduration; // massima durata temporale di attività del robot nell scena
            
    private int perc;   //percentuale della finestra che viene occupata dalla scena
    
    private HashMap<String,BufferedImage> images; // hashmap delle immagini
    private String[] setKeyMap; // array dei possibili valori di scene corrispondenti alle 
                                // chiavi di accesso per l'hash map delle immagini
    private int [] agentposition;
    private int[] defaultagentposition;
    
    /*
        La classe viene definita singleton 
    */
    
    public static MapGeneratorSceneModel getInstance(){
        if(instance == null){
            instance = new MapGeneratorSceneModel();
            instance.init();
        }
        return instance;
    }
    
    private MapGeneratorSceneModel() {

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
    
    
    public void initModelMap(int NumCellX, int NumCellY, float MapWidth, float MapHeight){
    
        this.MapWidth = MapWidth;
        this.MapHeight = MapHeight;
        //imposto la dimensione iniziale della scena
        scene = new String[NumCellX][NumCellY];
        //genero la scena della dimensione specificata
        
        this.defaultagentposition[0]=NumCellX/2;
        this.defaultagentposition[1]=NumCellY -2 ;
        this.agentposition[0]=this.defaultagentposition[0];
        this.agentposition[1]=this.defaultagentposition[1];
        
        this.resize(NumCellX, NumCellY);
        
//inizializzo la scena con i valori di default e cioè con i muri su tutto il bordo della scena
        this.initScene(scene);
    
    }

    /*
    Carica le immagini del progetto e genera l'array di stringhe che possono essere
    utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
    */
    
    public void loadImages() {
        HashMap<String,BufferedImage> mapicons;
        mapicons = (HashMap<String,BufferedImage>) RescueImages.getInstance().getMapImg();
        this.images = mapicons;
        Set<String> keys = images.keySet();
        setKeyMap= keys.toArray(new String[keys.size()]);
    }

    
    
    public void drawScene(Graphics2D g, float MapWidth, float MapHeight) {

        //aggiorno le dimensioni della finestra
        this.MapWidth = MapWidth;
        this.MapHeight = MapHeight;
        //calcolo la larghezza delle celle
        CellWidth = (MapWidth - 20) / NumCellX;
        CellHeight = (MapHeight - 20) / NumCellY;

        //verifico chi delle due dimensioni é minore e setto quella maggiore uguale a quella minore 
        // per rendere le celle quadrate
        if (CellWidth > CellHeight) {
            CellWidth = CellHeight;
        } else {
            CellHeight = CellWidth;
        }

        //calcolo le coordinate di inizio della scena partendo a disegnare
        //dall'angolo in alto a sinistra della nostra scena
        float x0 = (MapWidth - CellWidth * NumCellX) / 2;
        float y0 = (MapHeight - CellHeight * NumCellY) / 2;

        //setto colore delle scritte
        g.setColor(Color.BLACK);

        //doppio ciclo sulla matrice
        for (int i = 0; i < NumCellX; i++) {
            for (int j = 0; j < NumCellY; j++) {
                //calcolo la posizione x,y dell'angolo in alto a sinistra della
                //cella corrente
                int x = (int) (x0 + i * CellWidth);
                int y = (int) (y0 + j * CellHeight);
                //se la cella non è vuota, allora disegno l'immagine corrispondente
                if (!scene[i][j].equals("")) {
                    //disegno l'immagine corretta usando la stringa che definisce la chiave per l'hashmap
                    g.drawImage(images.get(scene[i][j]), x, y, (int) (CellWidth - 1), (int) (CellHeight - 1), null);
                }

                //traccio il rettangolo della cella
                g.drawRect(x, y, (int) (CellWidth - 1), (int) (CellHeight - 1));
            }
        }
    }

    
    
    
    public void resize(int NumCellX, int NumCellY) {
        //creo una scena con la nuova dimensione
        String[][] new_scene = new String[NumCellX][NumCellY];
        //salvo il numero di celle sulle x e sulle y
        this.NumCellX = NumCellX;
        this.NumCellY = NumCellY;
        //calcolo la larghezza delle celle
        this.CellWidth = (this.MapWidth - 20) / NumCellX;
        this.CellHeight = (this.MapHeight - 20) / NumCellY;

        if (this.CellWidth > this.CellHeight) {
            this.CellWidth = this.CellHeight;
        } else {
            this.CellHeight = this.CellWidth;
        }

        // aggiorno la posizione dell'agent in base alla nuova dimensione della griglia
        this.defaultagentposition[0]=NumCellX/2;
        this.defaultagentposition[1]=NumCellY -2 ;
        this.agentposition[0]=this.defaultagentposition[0];
        this.agentposition[1]=this.defaultagentposition[1];

        //inizializzo la nuova scena per farsi che abbia i muri sul perimetro
        initScene(new_scene);
        
        //ricopio ogni cella della vecchia mappa nella nuova mappa senza uscire fuori dalle celle a disposizione
        for (int i = 1; i < new_scene.length - 1; i++) {
            for (int j = 1; j < new_scene[i].length - 1; j++) {
                if (i <= scene.length - 1 && j <= scene[0].length - 1) {
                    new_scene[i][j] = scene[i][j];
                }
            } 
        }
        scene = new_scene;
    
    }

    /*
        Inizializzazione della scena
    */
    
    public void initScene(String[][] scene) {

        //imposto i muri sul perimetro
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j]="outdoor"; // la condizione varia da progetto a progetto
                }
                else{
                
                    scene[i][j]="empty";
                }
            }
        }
        
        
        
        scene[this.agentposition[0]][this.agentposition[1]]="agent_" + direction + "_unloaded";
    }
    
    

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
       Verifica e resistuisce la cella corrispondente alle coordinate ottenute
       dal click sul mouse
    */
    
    public int[] getCellPosition(int x, int y){
    
        int [] posCell = new int [2]; 
        float x0 = (MapWidth - CellWidth * NumCellX) / 2;
        float y0 = (MapHeight - CellHeight * NumCellY) / 2;
        float cordx = x - x0;
        float cordy = y - y0;
        cordx = cordx / CellWidth;
        cordy = cordy / CellHeight;
        posCell[0] =(int) cordx;
        posCell[1]= (int) cordy;
       
        return posCell;
        
    }
    
    /*
    * Metodo per l'aggiornamento consistente delle celle
    *  @param x ,y : possibile in riga e colonna della cella da modificare
    *  @param state : nuovo stato da inserire
    */
   
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

    
    public void setNumCell(int NumCellX, int NumCellY) {
        this.NumCellX = NumCellX;
        this.NumCellY = NumCellY;
        
    }

    public void setCell(int x, int y, String value) {
        scene[x][y] = value;
    }

    public void setSizeScreen(float MapWidth, float MapHeight) {
        this.MapHeight = MapHeight;
        this.MapWidth = MapWidth;
    }
    
    public void setMaxDuration(int max_dur){
    
        this.maxduration=max_dur;
    }
    
    public String[][] getScene(){
    
        return this.scene;
    }
    
    public int getNumx(){
      return this.NumCellX;
    }
    
    public int getNumy(){
      return this.NumCellY;
    }
            
    public HashMap<String,BufferedImage> getImages(){
        return this.images;
    }

    public String[] getSetKey(){
    
        return this.setKeyMap;
    }
}

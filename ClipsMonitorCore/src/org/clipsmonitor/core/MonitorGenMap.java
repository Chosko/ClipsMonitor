/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.monitor2015.RescueGenMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Questa classe astratta è la parte di view (in un'architettura MVC) e
 * implementa Observer, per osservare il model (i.e. ClipsModel). Le
 * implementazioni di questa classe dovranno implementare i metodi per il
 * mantenimento dell'interfaccia grafica specifica per ogni progetto.
 * 
 * @author Marco Corona, Alessandro Basile, Tobia Giani
 */

public abstract class MonitorGenMap {
    

    protected int NumCellX, NumCellY; //numero di celle sulle x e sulle y
    protected float CellWidth, CellHeight; //largezza e altezza celle
    protected float MapWidth, MapHeight;  //largezza e altezza finestra
    
    protected String[][] scene; //matrice fondamentale rappresentante la scena
    protected String[][] move ; // matrice fondamentale rappresentante i movimenti delle persone
    protected String[][] mapActive; //matrice per la visualizzazione sull'interfaccia
    protected String mode; // modalità di esecuzione del generatore 
    
    protected int maxduration; // massima durata temporale di attività del robot nell scena
    protected ClipsConsole console;    // istanza della console clips     
    
    protected HashMap<String,BufferedImage> images; // hashmap delle immagini
    protected HashMap<String,BufferedImage> colors; // hashmap delle immagini
    protected String[] setKeyMap; // array dei possibili valori di scene corrispondenti alle 
                                // chiavi di accesso per l'hash map delle immagini
    protected String[] setKeyColor; // set di chiavi colori disponibili 
    protected int NumPerson; // numero di persone attualmente inserite
    protected String personName; //chiave che identifa le persona all'interno dell'hashmap
    protected int [] agentposition; // posizione attuale dell'agente all'inizio dello scenario
    protected int[] defaultagentposition; // posizione iniziale di default dell'agente
    protected LinkedList<Person> Persons; // Struttura che contiene i path delle varie persone
    protected String defaulagentcondition; // stringa di default utilizzata per inizializzare la scena
                                           // formata come background_keyagentdefault
    
    
    /*
        Carica le immagini del progetto e genera l'array di stringhe che possono essere
        utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
    */
    
    protected void loadImages() {
        // carico le icone per la selezione dei contenuti della mappa
        
        HashMap<String,BufferedImage> mapicons;
        mapicons = (HashMap<String,BufferedImage>) MonitorImages.getInstance().getMapImg();
        this.images = mapicons;
        this.images.remove("informed");
        this.images.remove("undiscovered");
        this.setKeyMap=MonitorImages.getInstance().getSetKeyMap();
        
        // carico le icone per i colori 
        HashMap<String,BufferedImage> coloricons;
        coloricons=(HashMap<String,BufferedImage>) MonitorImages.getInstance().getMapColor();
        this.colors=coloricons;
        this.setKeyColor=MonitorImages.getInstance().getSetKeyColor();
        
    }
    
    
    /*
     *   Metodo per l'inizializzazione del modello della mappa e la posizione iniziale dell'agente
     *   @param  numCellX : numero di righe
     *   @param  numCellY : numero di colonne
     *   @param  mapWidth : larghezza in pixel della mappa
     *   @param  mapHeight : altezza in pixel della mappa
    */
    
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
        this.move=this.clone(scene);
        this.mapActive=scene;
    }
    
    
    /*
        Metodo per il ridimensionamento delle celle a seguito della modifica della dimensione della
        griglia.
    */
    
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
     *   Metodo per il disegno della scena utilizzando i valori in stringhe della mappa.le mappe
     *  sono di due tipologie per cui devono essere riempite in maniera distinta.
     *  @param g : per effettuare il draw del pannello
     *  @param MapWidth : larghezza in pixel del pannello della mappa
     *  @param MapHeight : altezza in pixel del pannello della mappa
     *  @param type : tipologia di mappa di cui si deve eseguire il disegno
    */
    
    public void drawScene(Graphics2D g, float MapWidth, float MapHeight,String type) {

        
        BufferedImage[][] icons = this.makeIconMatrix(type);
        
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
                     g.drawImage(icons[i][j], x, y, (int) (CellWidth - 1), (int) (CellHeight - 1), null);
                }

                //traccio il rettangolo della cella
                g.drawRect(x, y, (int) (CellWidth - 1), (int) (CellHeight - 1));
            }
        }
    }
    
   
    
    
    /*
    *   Metodo per clonare le mappe di stringhe, viene utilizzato per trasportare la scena
    *   all'interno della mappa di move per ottenere una nuova copia su cui lavorare
    *   @param map : mappa in input da clonare
    */
    
    public String[][] clone(String [][] map){
    
        String[][] clone = new String[map.length][map[0].length];
    
        for (int i=0;i<map.length;i++){
            for(int j=0;j<map[0].length;j++){
            
                clone[i][j]=map[i][j];
            }                        
        }
        
        return clone;
    
    }
    
   
    
    
    /*
    *  Verifica se la posizione in pixel (x,y) ottenuta dal click sul mouse, risulta valida e in caso affermativo 
    *  resistuisce la cella corrispondente  alle coordinate  
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
    
    
    //  SET E GET
  
    
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
    
    
    public void setMode(String mode){
    
        this.mode=mode;
    }
    
    public void setMapImg(HashMap<String,BufferedImage> map){
        this.images=map;
    }
    
    public void setMapColor(HashMap<String,BufferedImage>  map){
        this.colors=map;
    }
    
    public void setKeyMap(String [] keys){
    
        this.setKeyMap=keys;
    }
    
    public void setKeyColor(String [] keys){
    
       this.setKeyColor=keys;
    }   
    
    /*
    *   Copia una delle mappe in input sulla mappa attiva di modo che venga visualizzata
    *   successivamente soltanto la mappa attiva
    */
    
    public void CopyToActive(String[][] map){
    
        this.mapActive=this.clone(map);
    }
  
    
    
    /*
    * Copia la nuova scena come base per le nuove move in modo da far coincidere
    * la mappa dello scenario con la mappa in cui si determinano le moves
    */
    
    public void CopySceneToMove(){
    
        this.move = this.clone(scene);
    }
    
    
    
    /*
    * Questo metodo serve per generare la nuova mappa di stringhe move a partire
    * dalle celle coinvolte. Le celle coinvolte sono state generate prelevando
    * le informazioni dalla lista linkata delle persone
    */
    
    
    public void ApplyUpdateOnMoveMap(String[][] cellMove){
    
        this.move = this.loadMoveonMap(scene, cellMove);
    }
    
    
    public String[][] getScene(){
    
        return this.scene;
    }
    
    public String[][] getMove(){
    
        return this.move;
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
    
    public HashMap<String,BufferedImage> getColors(){
        return this.colors;
    }

    public String[] getSetKey(){
    
        return this.setKeyMap;
    }
    
    public String[] getSetKeyColor(){
    
        return this.setKeyColor;
    }
    
    public String getMode(){
    
        return this.mode;
    }

    
    
    /*
    * Restituisce la distanza di Manhattam tra due celle  
    */
    
    public int ManhattamDistance(int xstart , int ystart , int xtarget , int ytarget){
        
            return Math.abs(ytarget - ystart) + Math.abs(xtarget - xstart);
    }
    
    
    // Metodi per l'utilizzo del generatore della history dei movimenti 
    
    
    /*
     *  Classe utilizzata per memorizzare i movimenti che possono essere eseguiti
     *  da agenti che si trovano a condividere l'ambiente con l'agente robotico.
     *  Ogni istanza di StepMove descrive la posizione di un agente ad un certo step
    */
        
    protected class StepMove{
        
            protected int row;
            protected int column;
            protected String path;
            protected int stepStart;
            protected int step;
            
            public StepMove(int r , int c , String p, int st , int s){
                this.row=r;
                this.column=c;
                this.path=p;
                this.stepStart=st;
                this.step=s;
            }
            
            
            public int getRow(){
                return row;
            }
            
            public int getColumn(){
                return column;
            }
            
            public String getPath(){
                return path;
            }
        
            public int getStep(){
            
                return step;
            }
            
            public int getStepStart(){
                return stepStart;
            }
            
            
            public void setRow(int nr){
                this.row=nr;
            }
            
            public void setColumn(int nc){
                this.column=nc;
            }
            
            public void getPath(String np){
                this.path=np;
            }
        
            public void getStep(int ns){
            
                this.step=ns;
            }
            
            public void getStepStart(int nss){
                this.stepStart=nss;
            }
        }
    
    /*
        Classe che descrive gli agenti che condividono l'ambiente assieme all'agente robotico
        Per il loro riconoscimento sono stati utilizzati dei colori i quali vengono utilizzati 
        sulla mappa per indicare gli spostamenti di cella di un determinato agentes
        Ad ogni agenete viene definita una lista di tutti i movimenti fino ad adesso introdotti
        nella scena.
    */

   
    
    protected class Person{
    
        protected LinkedList<StepMove> move;
        protected String associatedColor;
        protected ArrayList<String> paths;
    
        public Person(String color){
        
            this.associatedColor=color;
            this.move=new LinkedList<StepMove>();
            this.paths= new ArrayList<String>();
        }
        

        
        public String getColor(){
        
            return this.associatedColor;
        }
    
        public LinkedList<StepMove> getMoves(){
        
            return this.move;
        }
        
        public ArrayList<String> getPaths(){
            
            return this.paths;
        }  
    
    }
   
    
    
    
    /*
    *   Metodo che resistuisce l'indice della persona associata al colore nella linkedList 
    *   @param color : colore associato
    *   @return position : indice nella linkedList
    */
    
    public int findPosByColor(String color){
    
       int position = 0;
       ListIterator<Person> it = this.Persons.listIterator();
       Person p = null;
       while(it.hasNext()){
           p = it.next();
           if(p.associatedColor.equals(color)){
               return position;
           }
           position++;
       }
       return -1;
    }
    
    
    /*
    *  Restituisce l'indice della posizione della Person che occupa attualemnte quella 
    *   cella oppure restituisce -1 in caso la cella sia libera.
    *   @param x : numero di riga della cella
    *   @param y : numero di colonna della cella
    */
    
    public int CheckBusyStartCellFromPerson(int x , int y){
        int position = 0;
        ListIterator<Person> it = this.Persons.listIterator();
        Person p = null;
        while(it.hasNext()){
           p = it.next();
           if(p.getMoves().getFirst().getRow()==x && p.getMoves().getFirst().getColumn()==y ){
               return position;
           }
           position++;
       }
       return -1;
        
    }
    
    
    
    /*
    *  Questo metodo ricerca l'oggetto Person corrispondente al suo colore associato
    *  @param color : stringa del colore associato alla persona
    *  @return p : oggetto Person da restituire
    */
    
    public Person findByColor(String color){
    
       ListIterator<Person> it = this.Persons.listIterator();
       Person p = null;
       while(it.hasNext()){
       
           p = it.next();
           if(p.associatedColor.equals(color)){
               return p;
           }
       
       }
       return p;
    
    }
    
    /*
    *  Restituisce un array di tutti colori attualmente attivi nella mappa in modo 
    *  da controllare quali risultano essere le opzioni disponibili al generatore 
    *  delle move. 
    */
    
    public String[] getListColorActive(){
    
        ListIterator<Person> it = this.Persons.listIterator();
        ArrayList<String> listColor = new ArrayList<String>();
        String [] colors ;
        
        while(it.hasNext()){
        
            Person p = it.next();
            listColor.add(p.associatedColor);
        }
        
        if(listColor.size()>0){
            colors = new String[listColor.size()];
            colors = listColor.toArray(colors);
        }
        else{
            colors = new String[1];
            colors[0]="";
        }
        return colors;
    }
    
    
    
    /*
        ritorna un array di stringhe che descrive le attuali persone attive nella scena
        Questo metodo verrà poi richiesto per popolare la JList 
    */
    
    public String[] getListPerson(){
    
        String [] list=null;
        if(this.Persons.size()>0){
            
            list = new String [this.Persons.size()+1];
            ListIterator<Person> it = this.Persons.listIterator();
            int i =0;

            while(it.hasNext()){

                list[i]="person_" + it.next().getColor();
                i++;
            }
        
            list[this.Persons.size()]="all";
        }
        return list;
    }
    
    /*
    *  Ritorna una stringa indicante il numero di step disponibili alla modifica in base
    *  al parametro che li viene dato .
    * @param : param>-1 può indicare l'indice della persona su cui costruire la lista 
    *          param==-1 indica che bisogna richiedere la lista globale di tutti gli step 
    *                    in cui è stato definito almeno un move
    */
    
    public String[] getListStep(int param){
        
        String [] list= null;
        int maxStep= 0;
        if(param==-1){
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){
                int numStepPerson = it.next().move.size();
                if(numStepPerson>maxStep){
                    maxStep=numStepPerson;
                }
            }
            
            list = new String[maxStep];
            
        }
        else{
        
            Person paramPerson = this.Persons.get(param);
            maxStep = paramPerson.move.size();
        }
        
        list = new String[maxStep];
        for(int i= 0; i<list.length;i++){
            list[i]="Step " + i;
        }
        
        return list;
    }
    
    
    /*
    *   Ritorna la lista dei movimenti definiti fino a questo istante in base al parametro param
    *    @paramPerson : paramPerson>-1  indica l'indice della persona da cui prelevare tutti le move definite
    *                   paramPerson==-1 indica la richiesta delle move per tutti le persone nella lista
    *
    *   @paramStep : paramStep>-1  indica il numero dello step da cui prelevare tutti le move definite
    *                paramStep==-1 indica la richieste delle move per tutti gli step
    */
    
    
    public String[] getListMove(int paramPerson , int paramStep ){
            
        String [] list= null;
        ArrayList<String> moveslist = new ArrayList<String>();
        // richiesta della lista completa degli step;
        if(paramPerson==-1 && paramStep==-1){
            
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){
                Person p = it.next();
                ListIterator<StepMove> moves = p.move.listIterator();
                while(moves.hasNext()){
                    StepMove s = moves.next();
                    String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + s.path 
                    + "\t (" + s.row + "," + s.column + ")"; 
                    moveslist.add(move);
                }
            }        
        }
        // richiesta della lista completa delle move in un determinato step
        if(paramPerson==-1 && paramStep>-1){
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){
                Person p = it.next();
                if(p.move.size()>paramStep){
                    StepMove s = p.move.get(paramStep);
                    String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + s.path 
                    + "\t (" + s.row + "," + s.column + ")";
                    moveslist.add(move);
                }
            
            }
        }
        
        // richiesta della lista delle move eseguita da una determinata persona
        if(paramPerson>-1 && paramStep==-1){
        
            Person p = this.Persons.get(paramPerson);
            ListIterator<StepMove> moves = p.move.listIterator();
            while(moves.hasNext()){
                StepMove s = moves.next();
                String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + s.path 
                    + "\t (" + s.row + "," + s.column + ")";  
                moveslist.add(move);
            }
        
        }
        
        list = new String[moveslist.size()];
        list = moveslist.toArray(list);
        return list;
    
    }
    
    /*
    * Restituisce l'elenco dei path già dichiarati all'interno della linkedList delle persons.
    * Può essere richiesto o l'elenco totale o l'elenco dei path specifici per una determinata person
    * Le stringhe sono costruite tutte con la struttura personName_pathName. 
    * @paramPerson : paramPerson>-1  indica l'indice della persona da cui prelevare tutti i path definite
    *                   paramPerson==-1 indica la richiesta dei path per tutti le persone nella lista
    * @return : array di stringhe equivalente all'elenco
    */
    
    public String[] getPaths(int paramPerson){
    
        String[] paths = null ;
        ArrayList<String> listPaths = new ArrayList<String>();
    
       if(paramPerson==-1){ 
        
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){

                Person p = it.next();
                ArrayList<String> tmp = p.paths;
                for(String elem : tmp){
                    listPaths.add(p.associatedColor + "_" + elem);
                }

            }
        }
       else{
       
           Person p = this.Persons.get(paramPerson);
           ArrayList<String> tmp = p.paths;
           for(String elem : tmp){
                listPaths.add(p.associatedColor + "_" + elem);
            }
       
       }
       
        paths = new String[listPaths.size()];
        paths = listPaths.toArray(paths);
        
        return paths;
    
    }
    
    /*
    * Restituisce un mappa temporanea di move per la visualizzazione delle modifiche
    * sulla mappa. La move map restituita e soltanto temporanea
    * @param x : riga della cella da inserire la move
    * @param y : colonna della cella da inserire la move
    * @param color : colore temporaneo 
    */
    
    public String[][] getTmpMoveMap(int x , int y , String color){
    
        String [][] newmap = new String[this.NumCellX][this.NumCellY];
        int index = -1;
        
        for(int i = 0 ; i<newmap.length;i++){
        
            for(int j=0;j<newmap[0].length;j++){
                if((index=this.CheckBusyStartCellFromPerson(i, j))!=-1){
                    
                    newmap[i][j]=this.Persons.get(index).associatedColor;
                }
                else{
                    newmap[i][j]="";
                }
            }
        }
    
        newmap[x][y]=color;
        
        
        
        return newmap;
        
    }
    
    /*
    * Questo metodo genera la mappa delle celle coinvolte in un certo movimento in base
    * ai parametri della persona o dello step a cui si è interessati.
    * @param paramPerson : indice della persona nella linkedList
    * @param paramStep : numero di step a cui siamo interessati
    * @return newmap : stringa delle celle occupate da un movimento
    */
    
    public String[][] getMoveCellMap(int paramPerson , int paramStep){
    
        String [][] newmap = new String[this.NumCellX][this.NumCellY];
        
        for(int i = 0 ; i<newmap.length;i++){
        
            for(int j=0;j<newmap[0].length;j++){
                newmap[i][j]="";
            }
        }
        // caso di richiesta di uno specifico step
        if(paramPerson==-1){
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){
                Person p = it.next();
                if(p.getMoves().size()>paramStep){
                    StepMove s = p.move.get(paramStep);
                    int r = s.getRow();
                    int c = s.getColumn();
                    newmap[r][c]=p.associatedColor;
                }
            }
        
        }
        // caso di richiesta di una specifico agente
        else{
            int r = 0;
            int c = 0;
            Person p = this.Persons.get(paramPerson);
            ListIterator<StepMove> it = p.move.listIterator();
            while(it.hasNext()){
                StepMove s = it.next();
                r = s.getRow();
                c = s.getColumn();
                newmap[r][c]=p.associatedColor;
            
            }
            
            newmap[r][c]+="_last";
        }
        
        return newmap;
    }
   
    /*
    *  Metodo per il caricamento delle move da visualizzare sulla mappa.Il metodo
    *  prende in input due mappe di stringhe, la prima rappresenta i valori della
    * scena, il background. Il secondo una mappa dove vengono messe le etichette dei
    * colori raffiguranti le celle occupate dai movimenti di un certo agente
    * @param map : la mappa delle stringhe di background
    * @param move : la mappa delle celle occupate da un certo movimento
    *
    * @return : la matrice di stringhe risultante
     */
    
    
    public String[][] loadMoveonMap(String [][] map, String[][] move){
    
        String [][] newmap = new String[map.length][map[0].length];
        
        for(int i=0 ; i<newmap.length;i++){
        
            for(int j=0; j<newmap.length;j++){
            
                if(!move[i][j].equals("")){
                
                    newmap[i][j]=map[i][j] + "_" + move[i][j];
                }
                else{
                
                    newmap[i][j]=map[i][j];
                }
                
            }
            
        }
    
        return newmap;
    }
    
    /*
    * Metodo per la verifica e la rimozione delle Move non piu valide a seguito di un 
    * resize della mappa.Il metodo scorre la lista linkata e rimuove tutte le move
    * a partire dalla prima occorrenza non può valida. Se ad un certo step la move non è
    * valida non lo saranno più tutte quelle successivamente create
    */
    
    
    public void RemoveStepAfterResize(){
    
        ListIterator<Person> it = this.Persons.listIterator();
        while(it.hasNext()){
        
            Person p = it.next();
            ListIterator<StepMove> its = p.move.listIterator();
            StepMove s=its.next();;
            while(its.hasNext()){
                if(s.getRow()<0 || s.getRow()>this.NumCellX || s.getColumn()<0 || s.getColumn()>this.NumCellY){
                    break;
                }
                s=its.next();
            }
            
            while(its.hasNext()){
               p.move.remove(s);
               s=its.next();
            }
            
        }
    
    
    }
    
        /*
    * Metodo per l'aggiornamento consistente delle celle. Il metodo ritorna interi corrispondenti
    * ad un particolare conclusione dell'esecuzione. L'aggiornamento viene sostanzialmente separato
    * in tre casi (richiesta di una posizione dell'agente robotico , richiesta di una nuova posizione iniziale
    * di un agente umano, modifiche allo scenario).
    * Il robot può essere modificato nella sua posizione solo se vengono rispettate le condizoioni del progetto
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
                            scene[this.agentposition[0]][this.agentposition[1]]=this.defaulagentcondition;
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
    
    
    /*
    * Questo metodo genera l'aggiornamento delle celle della mappa del generatore in modalità
    * move, determinando quali movimenti sono possibili per un agente e in tal caso aggiorna la
    * lista dei movimenti 
    * @param x : numero di riga
    * @param y : numero di colonna
    * @param p : persona a cui aggiungere la move
    */

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
    * Rimuove una persona in base al colore ad esso assegnata
    * @param color : colore rappresentante la persona da rimuovere
    * @return boolean : verifica se la rimozione è avvenuta correttamente
    */

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
    *  Aggiunge una nuova persona allo scenario dichiarandone il colore associato
    *  e la posizione di partenza
    * @param x : riga della cella iniziale
    * @param y : colonna della cella iniziale
    * @param color : colore da associare
    */

    
    
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
    
    
    
    //  METODI PER SAVE E LOAD DELLA MAPPA
    

    
    
    public String exportScene(File file) throws JSONException {
        //richiamo l'export della scena il quale mi dará una stringa con tutto il codice clips corrispondente
        String sceneFile = this.exportScene();
        //richiamo l'export della history il quale mi dará una stringa con tutto il codice clips corrispondente
        String historyFile = this.exportHistory();
        String dirpath="";
        String parentpath="";
        String consoleOutput = ""; 
        
        try{
            dirpath = file.getName();
            parentpath=file.getParent();
             // creazione nuovo file
               
                //scrivo il file della mappa
                Files.write(Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_RealMap.txt"), sceneFile.getBytes());
                consoleOutput +="File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_RealMap.txt \n");

                if (historyFile.length() > 0) //scrivo il file della history solo se sono
                {                               //sono state aggiunte persone alla scena
                    Files.write(Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_History.txt"), historyFile.getBytes());
                 consoleOutput +="File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_History.txt \n");
                }
                //scrivo il file json con la mappa scritta
                this.saveJSONMap(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json");
                consoleOutput += "File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json + \n");

          
            
        }
        catch(Exception err){
            err.printStackTrace();
        }
        
       return consoleOutput;          
       
    }
    
    /*
    * Questo metdo utilizza dati strutturati JSON per il salvataggio della scena. La struttura 
    * dati JSON cosi restituita viene successivamente utilizzata per il caricamento della mappa
    * cosi da poter essere modificata in un secondo momento.
    * @param nome : rappresenta il nome del file su cui eseguire il salvataggio
    * @return boolean : valuta se la scrittura sul JSON è stata eseguita correttamente
    */
    
    public boolean saveJSONMap(String nome) throws JSONException {
        
       
        try {
            //Creo la radice con le informazioni sulla griglia
            JSONObject info = new JSONObject();
            info.put("cell_x", this.getNumx());
            info.put("cell_y", this.getNumy());

            //ciclo sulla matrice degli stati per creare la struttura
            JSONArray ArrayCell = new JSONArray();
            for (int i = 0; i < this.getNumx(); i++) {
                for (int j = 0; j < this.getNumy(); j++) {
                    JSONObject cella = new JSONObject();
                    cella.put("x", i);
                    cella.put("y", j);
                    cella.put("stato",scene[i][j]);
                    //salvo solo l'intero dello stato che viene
                    //scenepato internamente;
                    ArrayCell.put(cella);
                }
            }
            info.put("celle", ArrayCell);
            //salvo le informazioni in un file JSON della scenea
            Files.write(Paths.get(nome), info.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RescueGenMap.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    
    /*
     * Metodo per il caricamnento di una scena a partire da un file JSON precedentemente creato
     * Viene costruita la mappa e popolata dai valori contenuti nel JSON.
     * @param jsonFile : il file JSON da cui eseguire il load 
    */
    
    
    @SuppressWarnings("UnnecessaryUnboxing")
    public void load_scene(File jsonFile) throws ParseException {
        //creo una nuova istanza di scena
       
       
        try {
            //converto il file in un oggetto JSON
            FileReader jsonreader = new FileReader(jsonFile);
            char[] chars = new char[(int) jsonFile.length()];
            jsonreader.read(chars);
            String jsonstring = new String(chars);
            jsonreader.close();
            JSONObject json = new JSONObject(jsonstring);
            //leggo il numero di celle dalla radice del JSON
            
            System.out.println(json.get("cell_x").toString());
            
            int NumCellX = Integer.parseInt(json.get("cell_x").toString());
            int NumCellY = Integer.parseInt(json.get("cell_y").toString());
            
            //setto il numero di celle nella scena
            this.setNumCell(NumCellX, NumCellY);
            this.resize(NumCellX, NumCellY);
            //estraggo il JSONArray dalla radice
            JSONArray arrayCelle = json.getJSONArray("celle");
            for (int i =0 ; i<arrayCelle.length();i++) {
                //ciclo su ogni cella e setto il valore della cella letta nella scena
                JSONObject cell = arrayCelle.getJSONObject(i);
                int x = Integer.parseInt(cell.get("x").toString());
                int y = Integer.parseInt(cell.get("y").toString());;
                String stato = cell.get("stato").toString();
                
                this.setCell (x,y,stato);
            }
        } catch (Exception ex) {
            //Logger.getLogger(MapGeneratorLoader.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
       
        
    }

    /*
    * metodo che converte lo stream di file in un oggetto JSON
    */
    private static JSONObject convertStreamToJson(InputStream is) throws ParseException {
        try {
            return (JSONObject) (new JSONParser()).parse(new BufferedReader(new InputStreamReader(is)));
        } catch (IOException ex) {
            return null;
        }
    }
    
    
    // PARTE ASTRATTA
    
        
    /*
        Metodo per l'inizializzazione della mappa. Dichiarato astratto poichè ogni progetto 
        determina caratteristiche custom da dare all'envirorment
     */
        
    public abstract void initScene(String[][] scene);    


    /*
     Metodo per la scrittura su file della history della scena
    */
    
    public abstract String exportHistory();
    
    /*
     Metodo per la scrittura su file della mappa  iniziale
    */
    
    public abstract String exportScene();
        

    
    /*
    *    Esegue l'init del generatore, viene eseguito a livello di classe derivata
    *    specifica per il progetto 
    */
    

    
    public abstract void init();
    
    /*
    * Genera le icone per il disegno delle mappe 
    */
    
    public abstract BufferedImage[][] makeIconMatrix(String type);
    
    
    /*
    *  Verifica le condizioni imposte alla posizione del robot
    */

    public abstract boolean RobotPositionIsValid(String mapPos);
    
    
    /*
    *  Verifica le condizioni imposte alla posizione del robot
    */

    public abstract boolean PersonPositionIsValid(String mapPos);
    
    /*
    * Setta le impostazioni del robot nel modello
    */
    
    public abstract void SetRobotParams(String state, int x , int y);
    
}

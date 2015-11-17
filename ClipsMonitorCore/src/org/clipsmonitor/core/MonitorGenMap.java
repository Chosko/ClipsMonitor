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
    
    protected MonitorImages img;

    protected int NumCellX, NumCellY; //numero di celle sulle x e sulle y
    protected float CellWidth, CellHeight; //largezza e altezza celle
    protected float MapWidth, MapHeight;  //largezza e altezza finestra
    
    protected String[][] scene; //matrice fondamentale rappresentante la scena
    protected String[][] move ; // matrice fondamentale rappresentante i movimenti delle persone
    protected String[][] mapActive; //matrice per la visualizzazione sull'interfaccia
    protected String mode; // modalità di esecuzione del generatore 
    
    protected int maxduration; // massima durata temporale di attività del robot nell scena
    protected ClipsConsole console;    // istanza della console clips     
    
   
    protected String[] setKeyMap; // array dei possibili valori di scene corrispondenti alle 
                                // chiavi di accesso per l'hash map delle immagini
    protected String[] setKeyColor; // set di chiavi colori disponibili 
    protected int NumPerson; // numero di persone attualmente inserite
    protected int MaxNumPerson ; // numero massimo di persone rappresentabili
    protected String personName; //chiave che identifa le persona all'interno dell'hashmap
    protected int [] agentposition; // posizione attuale dell'agente all'inizio dello scenario
    protected int[] defaultagentposition; // posizione iniziale di default dell'agente
    protected LinkedList<Person> Persons; // Struttura che contiene i path delle varie persone
    protected String defaulagentcondition; // stringa di default utilizzata per inizializzare la scena
                                           // formata come background_keyagentdefault
    
    protected String direction; // direzione iniziale del robot
    /*
        Carica le immagini del progetto e genera l'array di stringhe che possono essere
        utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
    */
        
    
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
            protected int step;
            
            public StepMove(int r , int c, int s){
                this.row=r;
                this.column=c;
                this.step=s;
            }
            
            
            public int getRow(){
                return row;
            }
            
            public int getColumn(){
                return column;
            }
            
        
            public int getStep(){
            
                return step;
            }
            
            
            
            public void setRow(int nr){
                this.row=nr;
            }
            
            public void setColumn(int nc){
                this.column=nc;
            }
            
        
            public void setStep(int ns){
            
                this.step=ns;
            }
            
            
        }
    
        protected class Path{
        
            protected String name;
            protected int startStep;
            protected int lastStep;
            protected LinkedList<StepMove> move;
            
            public Path(String name , int startStep ){
            
                this.name=name;
                this.startStep=startStep;
                this.lastStep=startStep;
                move = new LinkedList<StepMove>();
            
            }
            
            private Path(String name , int startStep,int lastStep){
            
                this.name=name;
                this.startStep=startStep;
                this.lastStep=lastStep;
                move= new LinkedList<StepMove>();
            
            }
            
            public LinkedList<StepMove> getMoves(){
        
                return this.move;
            }
            
            public void AddMove(int r , int c ){
                
                int step = (this.move.size()==0) ? startStep : lastStep+1;  
                move.add(new StepMove(r,c,step));
                this.lastStep=step;
            }
            
            
            public void RemoveLast(){
                if(this.move.size()>1){
                    this.move.removeLast();
                    this.lastStep= this.move.getLast().step;
                }
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
    
        
        protected String associatedColor;
        protected LinkedList<Path> paths;
        
        public Person(String color){
        
            this.associatedColor=color;
            paths= new LinkedList<Path> ();
        }
        

        
        public String getColor(){
        
            return this.associatedColor;
        }
    
        public LinkedList<Path> getPaths(){
        
            return this.paths;
        }
        
        
        public void AddPath(int waitTime){
            String name = this.associatedColor + "_" + this.paths.size();
            int startStep;
            if(this.paths.size()>0){
                startStep = this.paths.getLast().lastStep + waitTime +1;
            }
            else{
                startStep = waitTime;
            }
            paths.add(new Path(name,startStep));  
       }
    
        private void InsertPath(String name,int startStep , int lastStep){
      
            paths.add(new Path(name,startStep,lastStep));
       
      
        } 
        
        public void RemoveLastPath(){
            
            if(this.paths.size()>1){
                this.paths.removeLast();
                }
        }
        
        
        
    }
   
    
    /*
    * Restituisce l'oggetto path in base al nome che lo rappresenta.La ricerca si basa
    * sulla sintassi adottata, ovvero nomepath = color_numPath
    * @param name : nome del path da ricercare
    */
    
    public Path getPathByName(String name){
        
        if(name.equals("empty")){
        
            return null;
        }
        String [] nameSplit = name.split("_");
        String color = nameSplit[0];
        Person p = this.findByColor(color);
        int numPath = Integer.parseInt(nameSplit[1]);
        Path result = p.paths.get(numPath);
        return result;
    }
    
    /*
    * Restituisce l'ultima occorrenza di path associata alla persona con l'index i 
    */
    
    public String getLastPathOfPerson(String state){
    
        String pathName = "empty";
        int pos = this.findPosByColor(state);
        if(pos!=-1){
            Person p = this.Persons.get(pos);
            pathName = p.paths.getLast().name;
        
        }
        
        return pathName;
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
    *  Restituisce il path della persona che attualmente che occupa attualemnte quella 
    *   cella oppure restituisce -1 in caso la cella sia libera.
    *   @param x : numero di riga della cella
    *   @param y : numero di colonna della cella
    *   @param 
    */
    
    public String CheckBusyCellFromPerson(int x , int y , int Step){
        ListIterator<Person> it = this.Persons.listIterator();
        Person p = null;
        while(it.hasNext()){
           p = it.next();
           ListIterator<Path> itp = p.paths.listIterator();
           Path succ= null;
           while(itp.hasNext()){
               succ = itp.next();
               if(succ.startStep<=Step && succ.lastStep>=Step ){
                   break;
               }
               
           }
           
           int offset = Step - succ.startStep;
           if(offset<succ.move.size()){
                if(succ.move.get(offset).getRow()==x && succ.move.get(offset).getColumn()==y ){
                    return succ.name;
                }
           }
       }
       return "empty";
        
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
                ListIterator<Path> itp = it.next().paths.listIterator();
                int numStepPerson=0;
                while(itp.hasNext()){
                    int numStepPath = itp.next().move.size();
                    numStepPerson +=numStepPath;
                }
                if(numStepPerson>maxStep){
                    maxStep=numStepPerson;
                }
            }
            
            list = new String[maxStep];
            
        }
        else{
        
            Person paramPerson = this.Persons.get(param);
            ListIterator<Path> itp = paramPerson.paths.listIterator();
            int numStepPerson=0;
                while(itp.hasNext()){
                    int numStepPath = itp.next().move.size();
                    numStepPerson +=numStepPath;
            }
           list = new String[numStepPerson];     
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
                ListIterator<Path> itpath = p.paths.listIterator();
                while (itpath.hasNext()){
                
                    Path succ = itpath.next();
                    ListIterator<StepMove> moves =succ.move.listIterator();
                    while(moves.hasNext()){
                        StepMove s = moves.next();
                        String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + succ.name 
                        + "\t (" + s.row + "," + s.column + ")"; 
                        moveslist.add(move);
                    }
                }        
            }
        }
            
        // richiesta della lista completa delle move in un determinato step
        if(paramPerson==-1 && paramStep>-1){
            ListIterator<Person> itPerson = this.Persons.listIterator();
            while(itPerson.hasNext()){
                Person p = itPerson.next();
                ListIterator<Path> itPath = p.paths.listIterator();
                Path succ = null;
                while(itPath.hasNext()){
                    succ = itPath.next();
                    if(succ.startStep>=paramStep && succ.lastStep<=paramStep){
                        break;
                    }
                }
                int offset = paramStep - succ.startStep;
                if(succ.move.size()>paramStep){
                    StepMove s = succ.move.get(paramStep);
                    String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + succ.name 
                    + "\t (" + s.row + "," + s.column + ")";
                    moveslist.add(move);
                }
            
            }
        }
        
        // richiesta della lista delle move eseguita da una determinata persona
        if(paramPerson>-1 && paramStep==-1){
        
            Person p = this.Persons.get(paramPerson);
            ListIterator<Path> itPath = p.paths.listIterator();
            while(itPath.hasNext()){
                Path succ = itPath.next();
                ListIterator<StepMove> moves = succ.move.listIterator();
                while(moves.hasNext()){
                    StepMove s = moves.next();
                    String move = "C: " + p.associatedColor + "\t   S: " + s.step + "\t   Path: "  + succ.name 
                    + "\t (" + s.row + "," + s.column + ")";  
                    moveslist.add(move);
                }
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
                ListIterator<Path> itPath = p.paths.listIterator();
                while(itPath.hasNext()){
                    Path succ = itPath.next();
                    listPaths.add(succ.name);
                }
                
            }
        }
       else{
       
           Person p = this.Persons.get(paramPerson);
           
           ListIterator<Path> itPath = p.paths.listIterator();
                while(itPath.hasNext()){
                    Path succ = itPath.next();
                    listPaths.add(succ.name);
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
        
       
        
        for(int i = 0 ; i<newmap.length;i++){
        
            for(int j=0;j<newmap[0].length;j++){
                String result=this.CheckBusyCellFromPerson(i, j, 0);
                if(!(result.equals("empty"))){
                    String [] resultSplit = result.split("_");
                    newmap[i][j]=resultSplit[0];
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
    
    public String[][] getMoveCellMap(String paramPath , int paramStep){
    
        String [][] newmap = new String[this.NumCellX][this.NumCellY];
        
        for(int i = 0 ; i<newmap.length;i++){
        
            for(int j=0;j<newmap[0].length;j++){
                newmap[i][j]="";
            }
        }
        
        if(paramPath.equals("empty")){
            return newmap;
        }
        
        // caso di richiesta di uno specifico step
        if(paramPath.equals("none")){
            ListIterator<Person> it = this.Persons.listIterator();
            while(it.hasNext()){
                Person p = it.next();
                ListIterator<Path> itPath = p.paths.listIterator();
                Path succ = null;
                while(itPath.hasNext()){
                    succ = itPath.next();
                    if(succ.startStep>=paramStep && succ.lastStep<=paramStep){
                        break;
                    }
                }
                int offset = paramStep - succ.startStep;
                if(succ.move.size()>paramStep){
                    StepMove s = succ.move.get(offset);
                    int r = s.getRow();
                    int c = s.getColumn();
                    newmap[r][c]=p.associatedColor;
                }
            }
        
        }
        // caso di richiesta di una specifico path agente
        else{
            int r = 0;
            int c = 0;
            
            Path result = this.getPathByName(paramPath);
            String [] splitResult = result.name.split("_");
            ListIterator<StepMove> it = result.move.listIterator();
            
            while(it.hasNext()){
                StepMove s = it.next();
                r = s.getRow();
                c = s.getColumn();
                
                newmap[r][c]=splitResult[0];
            
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
            ListIterator<Path> itPath = p.paths.listIterator();
            while(itPath.hasNext()){
                Path succ = itPath.next();
                ListIterator<StepMove> its = succ.move.listIterator();
                StepMove s=its.next();;
                while(its.hasNext()){
                    if(s.getRow()<0 || s.getRow()>this.NumCellX || s.getColumn()<0 || s.getColumn()>this.NumCellY){
                        break;
                    }
                    s=its.next();
                }

                while(its.hasNext()){
                    succ.move.remove(s);
                    s=its.next();
                }

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
    * Crea un nuovo path e lo aggiunge alla lista dei path della persona indicata. L'aggiunta del path
    * comporta sempre l'inserimento di una move che determina lo stato iniziale per il nuovo path da
    * eseguire. Un path viene etichettato attraverso una label che risulta composta nel seguente modo :
    * (color_numPathPerson)
    * @param color : colore associato alla person a cui si vuole aggiungere il path
    * @param xstartStep : riga della cella iniziale del path
    * @param ystartStep : colonna della cella iniziale del path
    * @param waitStep : tempo di attesa dalla fine del path precedente
    */
    
    public int AddNewPathToPerson(String color, int xStartStep , int yStartStep, int waitStep){
    
        final int Success = 0;
        final int IllegalStartCell = 1;
        final int IllegalPerson = 2;
        final int PersonOverride = 3;
        Person p = this.findByColor(color);
        if(p==null){
            return IllegalPerson ;
        
        }
        int start = p.paths.getLast().lastStep + waitStep+1;
        String result = this.CheckBusyCellFromPerson(xStartStep,yStartStep,start);
        
        if(result.equals("empty")){
            if(this.PersonPositionIsValid(scene[xStartStep][yStartStep])){
                p.AddPath(waitStep);
                p.paths.getLast().AddMove(xStartStep, yStartStep);
                return Success;
            }
            else{
                return IllegalStartCell;
            }
        }
        else{
        
            return PersonOverride;
        }
       
    }
    
    /*
    * Questo metodo genera l'aggiornamento delle celle della mappa del generatore in modalità
    * move, determinando quali movimenti sono possibili per un agente e in tal caso aggiorna la
    * lista dei movimenti 
    * @param x : numero di riga
    * @param y : numero di colonna
    * @param p : persona a cui aggiungere la move
    */

    public int UpdateMoveCell(int x, int y , String path ){
    
        final int Success = 0;
        final int IllegalPosition = 1;
        final int UnavaibleCellScenario = 2;
        final int PersonOverride = 3;
        final int LastMoveRemove = 4;
        
        String [] pathSplit = path.split("_");
        String color = pathSplit[0];
        
        Path p=this.getPathByName(path);
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {
            
             StepMove s = p.getMoves().getLast();
             int step = p.getMoves().getLast().getStep()+1;
             String result=this.CheckBusyCellFromPerson(x, y, step);
             if(!(result.equals("empty"))){
             
                 return PersonOverride;
             }
             // distanza di manhattam e check sulla attraversabilità della cella
             if(this.ManhattamDistance(s.getRow(),s.getColumn(), x, y)==1 && this.PersonPositionIsValid(scene[x][y]) ){
                 
                 p.AddMove(x, y);
                 return Success;
             }
             else if(this.ManhattamDistance(s.getRow(),s.getColumn(), x, y)==0){
                 p.RemoveLast();
                 return LastMoveRemove;
             }
             else{
                 return UnavaibleCellScenario ;
             
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

    
    
    public int AddNewPerson( int x , int y , String color , int waitTime ){
    
        final int Success = 0;
        final int IllegalPosition = 1 ;
        final int keyColorEmpty = 2; 
        final int keyColorFull = 3;
        final int IllegalAgentPosition = 5;
        final int PersonOverride = 6;
        String result = "";
        
        if (x >= 0 && x < NumCellX  && y >= 0 && y < NumCellY) {

            if(this.setKeyColor.length==0){
                return keyColorEmpty;
            }

            if(x==this.agentposition[0] && y==this.agentposition[1]){

                return IllegalAgentPosition;
            }
            result=this.CheckBusyCellFromPerson(x, y,0);
            if(!result.equals("empty")){

                return PersonOverride;
            }
            
            if(this.findPosByColor(color)!=-1){
            
                Person p = this.findByColor(color);
                Path first = p.paths.get(0);
                first.move.getFirst().setRow(x);
                first.move.getFirst().setColumn(y);
                return PersonOverride;
            
            }
            
            // ho ancora disponibilita di colori per indicare le person
            if(this.NumPerson<this.MaxNumPerson){          
                    this.NumPerson++;
                    this.Persons.add(new Person(color));
                    this.Persons.getLast().AddPath(waitTime);
                    
                    if(this.PersonPositionIsValid(move[x][y])){
                        this.Persons.getLast().paths.getLast().AddMove(x, y);
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
    
    /*
    * Rimuove l'ultimo path aggiunto ad una persona e restuisce un intero equivalente
    * al risultato ottenuto.
    * @param color  colore assegnato alla persona
    */
    
    public int RemoveLastPath(String color){
       
       final int Success = 0;
       final int PersonNotFound = 1;
       final int FirstPathRemove = 2;
               
       Person p = this.findByColor(color);
       if(p!=null){
       
           if(p.paths.size()>1){
               p.RemoveLastPath();
               return Success;
           }
           else{
               return FirstPathRemove;
           }
       }
       else{
           return PersonNotFound;
       }
    
    }
    
    
    //  METODI PER SAVE E LOAD DELLA MAPPA
    

    /*
    *  Scrive su un file di testo la scena sviluppata tramite tool grafico
    */
    
    public String SaveFiles(File directory)throws JSONException{
        
        String consoleOutput = "";
        consoleOutput += this.WriteSceneOnFile(directory);
        consoleOutput += this.WriteHistoryOnFile(directory);
        return consoleOutput ;
        
        
    }
    
    private String WriteSceneOnFile(File directory) throws JSONException {
        //richiamo l'export della scena il quale mi dará una stringa con tutto il codice clips corrispondente
        String sceneFile = this.exportScene();

        String DirName="";
        String Parent="";
        String consoleOutput = ""; 
        
        try{
            DirName = directory.getName();
            Parent=directory.getParent();
             // creazione nuovo file
            String infoMapPath = Parent + File.separator + DirName + File.separator + DirName +"_RealMap.txt";
            //scrivo il file della mappa
            Files.write(Paths.get(infoMapPath), sceneFile.getBytes());
            consoleOutput +="File creato \n" + Paths.get(infoMapPath);

            String JSONMapPath = Parent + File.separator + DirName + File.separator + DirName + "_InfoMap.json"; 
            //scrivo il file json con la mappa scritta
            this.saveJSONMap(JSONMapPath);
            consoleOutput += "File creato \n" + Paths.get(JSONMapPath);

          
            
        }
        catch(IOException err){
            console.error(err);
        }
        
       return consoleOutput;          
       
    }
    
    /*
    * Scrive su file di testo la history sviluppata mediante il tool move all'interno del generatore
    */
    
    private String WriteHistoryOnFile(File directory) throws JSONException {
    
        String historyFile = this.exportHistory();
        String DirName="";
        String Parent="";
        String consoleOutput = ""; 
        
        try{
            DirName = directory.getName();
            Parent=directory.getParent();
             // creazione nuovo file
                
                if (historyFile.length() > 0) //scrivo il file della history solo se sono
                {                               //sono state aggiunte persone alla scena
                    String HistoryPath = Parent + File.separator + DirName + File.separator + DirName +"_History.txt";
                    Files.write(Paths.get(HistoryPath), historyFile.getBytes());
                    consoleOutput +="File creato \n" + Paths.get(HistoryPath);
                }
                //scrivo il file json con la mappa scritta
                String JSONMovePath = Parent + File.separator + DirName + File.separator + DirName + "_InfoMove.json";
                boolean result = this.saveJSONMoves(JSONMovePath);
                if(result){
                    consoleOutput += "File creato \n" + Paths.get(JSONMovePath);
                }
                

          
            
        }
        catch(IOException err){
            console.error(err);
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
            info.put("robot_x_default", this.defaultagentposition[0]);
            info.put("robot_y_default", this.defaultagentposition[1]);
            //ciclo sulla matrice degli stati per creare la struttura
            JSONArray ArrayCells = new JSONArray();
            for (int i = 0; i < this.getNumx(); i++) {
                for (int j = 0; j < this.getNumy(); j++) {
                    JSONObject cell = new JSONObject();
                    cell.put("x", i);
                    cell.put("y", j);
                    cell.put("stato",scene[i][j]);
                    //salvo solo l'intero dello stato che viene
                    //scenepato internamente;
                    ArrayCells.put(cell);
                }
            }
            info.put("celle", ArrayCells);
            //salvo le informazioni in un file JSON della scenea
            Files.write(Paths.get(nome), info.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RescueGenMap.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void LoadFiles(File directory) throws ParseException{
    
        String jsonMapPath = directory.getAbsolutePath() + File.separator + directory.getName() + "_InfoMap.json";
        String jsonMovePath = directory.getAbsolutePath() + File.separator + directory.getName() + "_InfoMove.json";
        File jsonMap = new File (jsonMapPath);
        File jsonMove = new File (jsonMovePath);
        
        this.LoadScene(jsonMap);
        this.LoadMoves(jsonMove);
    }
    
    /*
     * Metodo per il caricamnento di una scena a partire da un file JSON precedentemente creato
     * Viene costruita la mappa e popolata dai valori contenuti nel JSON.
     * @param jsonFile : il file JSON da cui eseguire il load 
    */
   
    
    @SuppressWarnings("UnnecessaryUnboxing")
    private void LoadScene(File jsonFile) throws ParseException {
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
                int x = cell.getInt("x");
                int y = cell.getInt("y");
                String stato = cell.getString("stato");
                
                this.setCell (x,y,stato);
                if(stato.contains("agent")){
                    this.SetRobotParams(stato, x, y);
                }
                this.defaultagentposition=new int []{json.getInt("robot_x_default"),json.getInt("robot_y_default")};
            }
            
        } catch (JSONException ex) {
            
            console.error(ex);
        } catch (IOException ex) {
            console.error(ex);
        } catch (NumberFormatException ex) {
            console.error(ex);
        }
       
        
    }

    /*
    * Genera un file JSON corrispondente alla lista linkata salvata per le move fino ad ora definite
    * per la history. Il JSON viene utilizzato per semplificare il caricamente della history e dello
    * scenario.
    *@param name : nome del file su cui scrivere il JSON 
    */
    
    private boolean saveJSONMoves(String name) throws JSONException{
    
        try{
            JSONObject Info = new JSONObject();
            JSONArray PersonsArray =  new JSONArray();
            for(int i=0;i<this.Persons.size();i++){
                Person p = this.Persons.get(i);
                JSONObject person = new JSONObject();
                person.put("color", p.associatedColor);
                JSONArray paths = new JSONArray();
                for(int j=0;j<p.paths.size();j++){
                    Path pts = p.paths.get(j);
                    JSONObject path = new JSONObject();
                    path.put("name", pts.name);
                    path.put("startStep", pts.startStep);
                    path.put("lastStep", pts.lastStep);
                    JSONArray moves = new JSONArray();
                    for(int k=0;k<pts.move.size();k++){
                        JSONObject move = new JSONObject();
                        StepMove s = pts.move.get(k);
                        move.put("row", s.row);
                        move.put("column", s.column);
                        move.put("step", s.step);
                        moves.put(move);
                    }
                   path.put("moves",moves);
                   paths.put(path);
                }
                person.put("paths", paths);
                PersonsArray.put(person);
            }
            
            Info.put("personList",PersonsArray);
            
            Files.write(Paths.get(name), Info.toString().getBytes());
            return true;
        }
        catch(IOException err){
            console.error(err.getMessage());
            return false;
        }
    }
    
    /*
    * Esegue il load della linkedList delle person partendo da un file JSON precedentemente creato.
    * Il file viene convertito nuovamente in un oggetto JSON e parsificato secondo la struttura 
    * definita dal metodo LoadMoves sopra
    * @param jsonFile : file jsonFile da cui eseguire il load
    */
    
    
    public void LoadMoves(File jsonFile) throws ParseException{
    
        try {
            //converto il file in un oggetto JSON
            FileReader jsonreader = new FileReader(jsonFile);
            char[] chars = new char[(int) jsonFile.length()];
            jsonreader.read(chars);
            String jsonstring = new String(chars);
            jsonreader.close();
            JSONObject json = new JSONObject(jsonstring);
            
            this.Persons=new LinkedList<Person>();
            
            //estraggo il JSONArray dalla radice
            JSONArray arrayPersons = json.getJSONArray("personList");
            for (int i =0 ; i<arrayPersons.length();i++) {

                JSONObject person = arrayPersons.getJSONObject(i);
                String color = person.getString("color");
                Person p = new Person(color);
                JSONArray arrayPaths = person.getJSONArray("paths");
                for(int j=0;j<arrayPaths.length();j++){
                    JSONObject path = arrayPaths.getJSONObject(j);
                    String pathName = path.getString("name");
                    int startStep = path.getInt("startStep");
                    int lastStep = path.getInt("lastStep");
                    p.paths.add(new Path(pathName,startStep,lastStep));
                    JSONArray arrayMoves = path.getJSONArray("moves");
                    for(int k=0;k<arrayMoves.length();k++){
                        JSONObject move = arrayMoves.getJSONObject(k);
                        int row = move.getInt("row");
                        int column = move.getInt("column");
                        int step = move.getInt("step");
                        p.paths.getLast().move.add(new StepMove(row,column,step));
                    }
                }
              this.Persons.add(p);
            }
        } catch (JSONException ex) {
            
            console.error(ex);
        } catch (IOException ex) {
            console.error(ex);
        } catch (NumberFormatException ex) {
            console.error(ex);
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

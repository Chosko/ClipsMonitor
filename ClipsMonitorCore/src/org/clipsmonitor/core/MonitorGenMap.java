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
    protected String[][] move ; 
     
    protected int maxduration; // massima durata temporale di attività del robot nell scena
    protected ClipsConsole console;        
    
    protected HashMap<String,BufferedImage> images; // hashmap delle immagini
    protected HashMap<String,BufferedImage> colors; // hashmap delle immagini
    protected String[] setKeyMap; // array dei possibili valori di scene corrispondenti alle 
                                // chiavi di accesso per l'hash map delle immagini
    protected String[] setKeyColor;
    protected int NumPerson;
    protected String personName;
    protected int [] agentposition;
    protected int[] defaultagentposition;
    protected LinkedList<Person> Persons;
    
    
    /*
        Classe che definisce il concetto di persone e tiene traccia dei suoi movimenti
    */
    
    protected class StepMove{
        
            protected int row;
            protected int column;
            protected String path;
            protected int step;
            
            public StepMove(int r , int c , String p , int s){
                this.row=r;
                this.column=c;
                this.path=p;
                this.step=s;
            }
            
            
            protected int getRow(){
                return row;
            }
            
            protected int getColumn(){
                return column;
            }
            
            protected String path(){
                return path;
            }
        
        }
    
    protected class Person{
    
        protected LinkedList<StepMove> move;
        protected String associatedColor;
        
    
        public Person(String color){
        
            this.associatedColor=color;
            this.move=new LinkedList<StepMove>();
        }
        

        
        public String getColor(){
        
            return this.associatedColor;
        }
    
        public LinkedList<StepMove> getMoves(){
        
            return this.move;
        }
        
            
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
        Carica le immagini del progetto e genera l'array di stringhe che possono essere
        utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
    */
    
    protected void loadImages() {
        // carico le icone per la selezione dei contenuti della mappa
        
        HashMap<String,BufferedImage> mapicons;
        mapicons = (HashMap<String,BufferedImage>) MonitorImages.getInstance().getMapImg();
        this.images = mapicons;
        this.setKeyMap=MonitorImages.getInstance().getSetKeyMap();
        
        // carico le icone per i colori 
        HashMap<String,BufferedImage> coloricons;
        coloricons=(HashMap<String,BufferedImage>) MonitorImages.getInstance().getMapColor();
        this.colors=coloricons;
        this.setKeyColor=MonitorImages.getInstance().getSetKeyColor();
        
    }
    
    
    /*
        Metodo per l'inizializzazione del modello della mappa e la posizione iniziale dell'agente
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
        Metodo per il disegno della scena utilizzando i valori in stringhe della mappa
    */
    
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
                    if(scene[i][j].contains(personName)){
                        g.drawImage(images.get(personName), x, y, (int) (CellWidth - 1), (int) (CellHeight - 1), null);
                    }
                    else{
                        g.drawImage(images.get(scene[i][j]), x, y, (int) (CellWidth - 1), (int) (CellHeight - 1), null);
                    }
                }

                //traccio il rettangolo della cella
                g.drawRect(x, y, (int) (CellWidth - 1), (int) (CellHeight - 1));
            }
        }
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
    
    public HashMap<String,BufferedImage> getColors(){
        return this.colors;
    }

    public String[] getSetKey(){
    
        return this.setKeyMap;
    }
    
    public String[] getSetKeyColor(){
    
        return this.setKeyColor;
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
                this.saveMap(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json");
                consoleOutput += "File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json + \n");

          
            
        }
        catch(Exception err){
            err.printStackTrace();
        }
        
       return consoleOutput;          
       
    }
    
    
    
    public boolean saveMap(String nome) throws JSONException {
        
       
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
            //salvo le informazioni in un file JSON della scenepa
            Files.write(Paths.get(nome), info.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(RescueGenMap.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    
    
    
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

    //metodo che converte lo stream di file in un oggetto JSON
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
        metodo per l'aggiornamento della mappa in base alle modifiche richieste dalla gui
    */
    
    public abstract int UpdateCell(int x, int y, String state);
    
    /*
        Esegue l'init del generatore, viene eseguito a livello di classe derivata
        specifica per il progetto 
    */
    
    public abstract void init();
    
    
}

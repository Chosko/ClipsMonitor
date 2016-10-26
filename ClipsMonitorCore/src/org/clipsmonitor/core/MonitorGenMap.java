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

    protected int NumCellX, NumCellY;       //numero di celle sulle x e sulle y
    protected float CellDimension;          // dimensione in pixel della cella
    protected float MapWidth, MapHeight;    //largezza e altezza finestra
    protected float PreferredWidth, PreferredHeight ;  // larghezza in pixel della mappa
    
    protected String[][] scene;             //matrice fondamentale rappresentante la scena
    protected String[][] move;              // matrice fondamentale rappresentante i movimenti delle persone
    protected String[][] mapActive;         //matrice per la visualizzazione sull'interfaccia
    protected String mode;                  // modalità di esecuzione del generatore
    protected int maxduration;              // massima durata temporale di attività del robot nell scena
    protected ClipsConsole console;         // istanza della console clips
    protected String[] setKeyMap;           // array dei possibili valori di scene corrispondenti alle
                                            // chiavi di accesso per l'hash map delle immagini
    
    protected String[] setKeyColor;         // set di chiavi colori disponibili
    protected int NumPerson;                // numero di persone attualmente inserite
    protected int MaxNumPerson;             // numero massimo di persone rappresentabili
    protected String personName;            //chiave che identifa le persona all'interno dell'hashmap
    protected int[] agentposition;          // posizione attuale dell'agente all'inizio dello scenario
    protected int[] defaultagentposition;   // posizione iniziale di default dell'agente
    protected LinkedList<Person> Persons;   // Struttura che contiene i path delle varie persone
    protected String defaulagentcondition;  // stringa di default utilizzata per inizializzare la scena
                                            // formata come background_keyagentdefault
    protected String direction;             // direzione iniziale del robot;
    protected String log;

    /*
     Carica le immagini del progetto e genera l'array di stringhe che possono essere
     utilizzate per la scena (corrspondono alle chiavi dell'hash map di images)
     */

    /**
     * Metodo per l'inizializzazione del modello della mappa in base al numero
     * di celle e alla dimensione totale dell'area di visualizzazione della
     * mappa. Inoltre il metodo setta la posizione iniziale dell'agente
     *
     * @param numCellX : numero di colonne
     * @param numCellY : numero di righr
     * @param mapWidth : larghezza in pixel della mappa
     * @param mapHeight : altezza in pixel della mappa
     */
    public void initModelMap(int NewNumCellX, int NewNumCellY, float NewMapWidth, float NewMapHeight) {

        setSizeScreen(NewMapWidth,NewMapHeight);
        SetNumCell(NewNumCellX,NewNumCellY);
        //imposto la dimensione iniziale della scena
        scene = new String[NumCellX][NumCellY];

        //genero la scena della dimensione specificata

        SetPreferredSizeMap();
        setAgentDefaultPosition();
        initScene(scene);
        move = clone(scene);
        CopyToActive(scene);
    }


    /**
     * Metodo per determinare la dimensione in pixel della mappa dato 
     * il numero di celle 
     *
     */
    
    
    public void SetPreferredSizeMap() {
        
        PreferredWidth = (NumCellX + 2) * CellDimension;
        PreferredHeight = (NumCellY + 2) * CellDimension;
        
    }

    public void setAgentDefaultPosition(){
        // aggiorno la posizione dell'agent in base alla nuova dimensione della griglia
        defaultagentposition[0] = NumCellX / 2;
        defaultagentposition[1] = NumCellY - 2;
        agentposition[0] = defaultagentposition[0];
        agentposition[1] = defaultagentposition[1];
    }
    
    
    
    /**
     * Metodo per il disegno della scena utilizzando i valori in stringhe della
     * mappa.le mappe sono di due tipologie per cui devono essere riempite in
     * maniera distinta. Per prima cosa viene eseguito il controllo su quale
     * matrice bisogna costruire.
     *
     * @param g : per effettuare il draw del pannello
     * @param MapWidth : larghezza in pixel del pannello della mappa
     * @param MapHeight : altezza in pixel del pannello della mappa
     */
    
    
    public void drawScene(Graphics2D g, float mapWidth, float mapHeight) {

        BufferedImage[][] icons = makeIconMatrix();


        //aggiorno le dimensioni della finestra
        MapWidth = mapWidth;
        MapHeight = mapHeight;



        //calcolo le coordinate di inizio della scena partendo a disegnare
        //dall'angolo in alto a sinistra
        
        float x0 = (MapWidth - CellDimension * NumCellX) / 2;
        float y0 = (MapHeight - CellDimension * NumCellY) / 2;

        //setto colore delle scritte
        
        g.setColor(Color.BLACK);

        
        //doppio ciclo sulla matrice
        for (int i = 0; i < NumCellX; i++) {
            for (int j = 0; j < NumCellY; j++) {
                //calcolo la posizione x,y dell'angolo in alto a sinistra della
                //cella corrente
                int x = (int) (x0 + i * CellDimension );
                int y = (int) (y0 + j * CellDimension);
                //se la cella non è vuota, allora disegno l'immagine corrispondente
                if (!scene[i][j].equals("")) {
                    //disegno l'immagine corretta usando la stringa che definisce la chiave per l'hashmap
                    g.drawImage(icons[i][j], x, y, (int) (CellDimension - 1), (int) (CellDimension - 1), null);
                }

                //traccio il rettangolo della cella
                g.drawRect(x, y, (int) (CellDimension - 1), (int) (CellDimension - 1));
            }
        }
    }

    /**
     * Metodo per clonare le mappe di stringhe, viene utilizzato per trasportare
     * la scena all'interno della mappa di moves per ottenere una nuova copia su
     * cui lavorare
     *
     * @param map : mappa in input da clonare
     **/
    
    public String[][] clone(String[][] map) {

        String[][] clone = new String[map.length][map[0].length];

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {

                clone[i][j] = map[i][j];
            }
        }

        return clone;

    }

    /**
     * Verifica se la posizione in pixel (x,y) ottenuta dal click sul mouse,
     * risulta valida e in caso affermativo resistuisce la cella corrispondente
     * alle coordinate
     *
     * @param x : coordinata x del pixel cliccato
     * @param y : coordinata y del pixel cliccato
     * @return : array indicante riga e colonna della cella corrispondente alle
     * coordinate in pixel
     */
    public int[] getCellPosition(int x, int y) {

        int[] posCell = new int[2];
        float x0 = (MapWidth - CellDimension * NumCellX) / 2;
        float y0 = (MapHeight - CellDimension * NumCellY) / 2;
        float cordx = x - x0;
        float cordy = y - y0;
        cordx = cordx / CellDimension ;
        cordy = cordy / CellDimension ;
        posCell[0] = (int) cordx;
        posCell[1] = (int) cordy;

        return posCell;

    }

     /****************
      *  SET E GET
      ****************/


    /**
     *
     * @param NumCellX
     * @param NumCellY
     */
    public void SetNumCell(int NewNumCellX, int NewNumCellY) {
        NumCellX = NewNumCellX;
        NumCellY = NewNumCellY;

    }

    /**
     * Imposta il valore di una singola cella nelle coordinate x,y
     *
     * @param x
     * @param y
     * @param value
     */

    public void setCell(int x, int y, String value) {
        scene[x][y] = value;
    }
    

    public void setSizeScreen(float NewMapWidth, float NewMapHeight) {
        MapHeight = NewMapHeight;
        MapWidth = NewMapWidth;
    }


    public void setMaxDuration(int max_dur) {
      maxduration = max_dur;
    }

    public void setMode(String mode) {
      this.mode = mode;
    }


    public void setKeyMap(String[] keys) {
      setKeyMap = keys;
    }

    public void setLog(String log){
      this.log= log;

    }

    public void setKeyColor(String[] keys) {
      setKeyColor = keys;
    }

    public String[][] getScene() {
      return scene;
    }

    public String getLog(){
      return log;
    }


    public String[][] getMove() {
      return move;
    }


    public int getNumx() {
        return NumCellX;
    }

    public int getNumy() {
        return NumCellY;
    }

    public int getMaxDuration(){
        return maxduration;
    }


    public String[] getSetKey() {

        return setKeyMap;
    }


    public String[] getSetKeyColor() {
      return setKeyColor;
    }

    public String getMode() {
          return mode;
    }


    public float getPreferredWidth(){
        return PreferredWidth;
    }
    
    public float getPreferredHeight(){
        return PreferredHeight;
    }



    /**
     * Copia una delle mappe in input sulla mappa attiva di modo che venga
     * visualizzata successivamente soltanto la mappa attiva
     *
     * @param map mappa di stringhe da copiare sulla active per la
     * visualizzazione
     */
    public void CopyToActive(String[][] map) {

        mapActive = clone(map);
    }

    /**
     * Copia la nuova scena come base per le nuove moves in modo da far
 coincidere la mappa dello scenario con la mappa in cui si determinano le
 moves
     */
    public void CopySceneToMove() {

        move = clone(scene);
    }

     /*
     * Restituisce un mappa temporanea di moves per la visualizzazione delle modifiche
     * sulla mappa. La moves map restituita e soltanto temporanea
     * @param x : riga della cella da inserire la moves
     * @param y : colonna della cella da inserire la moves
     * @param color : colore temporaneo
     */
    public String[][] getTmpMoveMap(int x, int y, String color) {

        String[][] newmap = new String[NumCellX][NumCellY];

        for (int i = 0; i < newmap.length; i++) {

            for (int j = 0; j < newmap[0].length; j++) {
                String result = CheckBusyCell(i, j, 0);
                if (!(result.equals("empty"))) {
                    String[] resultSplit = result.split("_");
                    newmap[i][j] = resultSplit[0];
                } else {
                    newmap[i][j] = "";
                }
            }
        }

        newmap[x][y] = color ;
        return newmap;

    }


/**
* Questo metodo genera la mappa delle celle coinvolte in un certo movimento in base
* ai parametri della persona o dello step a cui si è interessati.
* @param paramPerson : indice della persona nella linkedList
* @param paramStep : numero di step a cui siamo interessati
* @return newmap : stringa delle celle occupate da un movimento
*/

    
    public String[][] getMoveCellMap(String paramPath, int paramStep) {

      String[][] newmap = new String[NumCellX][NumCellY];

      for (int i = 0; i < newmap.length; i++) {
        for (int j = 0; j < newmap[0].length; j++) {
              newmap[i][j] = "";
        }
      }
      if (paramPath.equals("empty")) {
          return newmap;
      }

      // caso di richiesta di uno specifico step
      
      if (paramPath.equals("none")) {
          ListIterator<Person> itPerson = Persons.listIterator();
          while (itPerson.hasNext()) {
              Person p = itPerson.next();
              ListIterator<Path> itPath = p.paths.listIterator();
              Path succ = null;
              while (itPath.hasNext()) {
                  succ = itPath.next();
                
                  if (paramStep >= succ.startStep && paramStep<= succ.lastStep) {
                    int offset = paramStep - succ.startStep;
                    try{
                        StepMove s = succ.moves.get(offset);
                        String rgba= MonitorImages.getInstance().creatergbafromName(p.associatedColor, 0.25);
                        newmap[s.x][s.y] = rgba + "+" + personName;
                        break;
                    }
                    catch(IndexOutOfBoundsException ex){
                     String debug = "Error with ParamStep " + paramStep +
                                    " startStep " + succ.startStep + 
                                    " laststep" + succ.lastStep ;
                     AppendLogMessage(debug,"error");
                    }     
                  }
              }
             
          }
          
      }
      // caso di richiesta di una specifico path agente
      else {
          int x = 0;
          int y = 0;

          Path result = getPathByName(paramPath);
          String[] splitResult = result.name.split("_");
          ListIterator<StepMove> it = result.moves.listIterator();

          while (it.hasNext()) {
              StepMove s = it.next();
              x = s.getX();
              y = s.getY();
              String rgba= MonitorImages.getInstance().creatergbafromName(splitResult[0], 0.25);
              if(newmap[x][y].equals("")){
                newmap[x][y]= rgba;
              }
              else{
                 newmap[x][y] +="+"+rgba;
              }
          }

          newmap[x][y] += "+" + personName;
      }

      return newmap;
      }


    /*
     * Questo metodo serve per generare la nuova mappa di stringhe moves a partire
     * dalle celle coinvolte. Le celle coinvolte sono state generate prelevando
     * le informazioni dalla lista linkata delle persone
     */
    public void ApplyUpdateOnMoveMap(String[][] cellMove) {

        move = loadMoveonMap(scene, cellMove);
    }

    /*
     * Metodo per il caricamento delle moves da visualizzare sulla mappa.Il metodo
     * prende in input due mappe di stringhe, la prima rappresenta i valori della
     * scena, il background. Il secondo una mappa dove vengono messe le etichette dei
     * colori raffiguranti le celle occupate dai movimenti di un certo agente
     * @param map : la mappa delle stringhe di background
     * @param moves : la mappa delle celle occupate da un certo movimento
     *
     * @return : la matrice di stringhe risultante
     */
    
    public String[][] loadMoveonMap(String[][] map, String[][] move) {

        String[][] newmap = new String[map.length][map[0].length];
        for (int i = 0; i < newmap.length; i++) {
            for (int j = 0; j < newmap[0].length; j++) {
                newmap[i][j] = map[i][j];
                if (!move[i][j].equals("")) {
                    newmap[i][j] += "+" + move[i][j];
                }
            }
        }
        return newmap;
    }




    /**
     * Metodo per la creazione della matrice di icone da disegnare sulla mappa
     * del generatore .Il metodo si occupa di creare le icone con l'overlap.
     * @return la matrice di icone da disegnare sul pannello
     */

    public BufferedImage[][] makeIconMatrix() {

        BufferedImage[][] iconMatrix = new BufferedImage[mapActive.length][mapActive[0].length];

            for (int i = 0; i < NumCellX; i++) {

                for (int j = 0; j < NumCellY; j++) {

                  BufferedImage tmpImage;

                  // Split the map string in arguments
                  String[] curCel = mapActive[i][j].split("\\+");

                  // Background image is the first argument
                  iconMatrix[i][j] = img.getImage(curCel[0]);

                  // All the others arguments are overlaps
                  for (int k = 1; k < curCel.length; k++) {
                      String curOverlap = curCel[k];

                      tmpImage = img.getImage(curOverlap);
                      iconMatrix[i][j] = img.overlapImages(tmpImage, iconMatrix[i][j]);
                }

            }

        }

        return iconMatrix;
    }
    

    



    /**
     * Classe utilizzata per memorizzare i movimenti che possono essere eseguiti
     * da agenti che si trovano a condividere l'ambiente con l'agente robotico.
     * Ogni istanza di StepMove descrive la posizione di un agente ad un certo
     * step
     */

    protected class StepMove {

        protected int x;
        protected int y;
        protected int step;

        public StepMove(int r, int c, int s) {
            x = r;
            y = c;
            step = s;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getStep() {

            return step;
        }

        public void setX(int nr) {
            x = nr;
        }

        public void setY(int nc) {
            y = nc;
        }

        public void setStep(int ns) {

            step = ns;
        }

    }

    
    /**
     * Classe per la gestione dei singoli percorsi agente, ognuno dei quali possiede
     * un nome specifico, un inizio ed una fine e da una lista di posizione in cui
     * l'agente deve trovarsi nei vari step
     */
    
    
    protected class Path {

        protected String name;
        protected int startStep;
        protected int lastStep;
        protected LinkedList<StepMove> moves;

        public Path(String NewName, int NewStartStep) {

            name = NewName;
            startStep = NewStartStep;
            lastStep = startStep;
            moves = new LinkedList<StepMove>();

        }

        private Path(String NewName, int NewstartStep, int NewlastStep) {

            name = NewName;
            startStep = NewstartStep;
            lastStep = NewlastStep;
            moves = new LinkedList<StepMove>();

        }

        public LinkedList<StepMove> getMoves() {

            return moves;
        }

        public String getName(){

            return name;
        }

        public int getLastStep(){
            return lastStep;
        }

        public int getStartStep(){
            return startStep;
        }

        public void AddMove(int r, int c) {

            int step = (moves.size() == 0) ? startStep : lastStep + 1;
            moves.add(new StepMove(r, c, step));
            lastStep = step;
        }

        public void RemoveLast() {
            if (moves.size() > 1) {
                moves.removeLast();
                lastStep = moves.getLast().step;
            }
        }

    }

    
    /**
     * Classe che descrive gli agenti che condividono l'ambiente assieme all'agente robotico
     * Per il loro riconoscimento sono stati utilizzati dei colori i quali vengono utilizzati
     * sulla mappa per indicare gli spostamenti di cella di un determinato agentes
     * Ad ogni agenete viene definita una lista di tutti i movimenti fino ad adesso introdotti
     * nella scena.
     */
    protected class Person {

        protected String associatedColor;
        protected LinkedList<Path> paths;

        public Person(String color) {

            associatedColor = color;
            paths = new LinkedList<Path>();
        }

        public String getColor() {

            return associatedColor;
        }

        public LinkedList<Path> getPaths() {

            return paths;
        }

        public void AddPath(int waitTime) {
            String name = associatedColor + "_" + paths.size();
            int startStep;
            if (paths.size() > 0) {
                startStep = paths.getLast().lastStep + waitTime + 1;
            } else {
                startStep = waitTime;
            }
            paths.add(new Path(name, startStep));
        }

        private void InsertPath(String name, int startStep, int lastStep) {

            paths.add(new Path(name, startStep, lastStep));

        }

        public void RemoveLastPath() {

            if (paths.size() > 1) {
                paths.removeLast();
            }
        }

    }



    /**************************************************************
     *   Ricerca e reperimento informazione dalle Linked list
     *
     *************************************************************/



    /*
     * Restituisce l'oggetto path in base al nome che lo rappresenta.La ricerca si basa
     * sulla sintassi adottata, ovvero nomepath = color_numPath
     * @param name : nome del path da ricercare
     */
    
    
    public Path getPathByName(String name) {

        if (name.equals("empty")) {

            return null;
        }
        String[] nameSplit = name.split("_");
        String color = nameSplit[0];
        Person p = findPersonByColor(color);
        int numPath = Integer.parseInt(nameSplit[1]);
        Path result = p.paths.get(numPath);
        return result;
    }

    
    
    public int getLastStepofPerson(String state){
      int lastStep=0;
        int pos = findIndexPosByColor(state);
        if (pos != -1) {
            Person p = Persons.get(pos);
            lastStep = p.paths.getLast().lastStep;

        }
        return lastStep;
    }

    /**
     * Restituisce il nome dell'ultimo percorso associato alla person
     * @param person name
     * @return ultimo path name associato alla person
     */
    public String getLastPathOfPerson(String person) {
        String pathName = "empty";
        int pos = findIndexPosByColor(person);
        if (pos != -1) {
            Person p = Persons.get(pos);
            pathName = p.paths.getLast().name;

        }

        return pathName;
    }

    /*
     *   Metodo che resistuisce l'indice della persona associata al colore nella linkedList
     *   @param color : colore associato
     *   @return position : indice nella linkedList
     */
    public int findIndexPosByColor(String color) {

        for(int pos = 0; pos<Persons.size(); pos++) {
           Person currPers = Persons.get(pos);
            if (currPers.associatedColor.equals(color)) {
                return pos;
            }
        }
        
        return -1;
    }

    /*
     *  Questo metodo ricerca l'oggetto Person corrispondente al suo colore associato
     *  @param color : stringa del colore associato alla persona
     *  @return p : oggetto Person da restituire
     */
    public Person findPersonByColor(String color) {

        Person result = null;
        for(int i = 0 ; i< Persons.size(); i++) {

            Person currPers = Persons.get(i);
            if (currPers.associatedColor.equals(color)) {
                result = currPers;
                break;
            }
        }
        return result;

    }

    /*
     *  Restituisce un array di tutti colori attualmente attivi nella mappa in modo
     *  da controllare quali risultano essere le opzioni disponibili al generatore
     *  delle moves.
     */
    public String[] getListColorActive() {

        int p = 0;
        ArrayList<String> listColor = new ArrayList<String>();
        String[] colors;

        while (p<Persons.size()) {

            Person currP = Persons.get(p);
            listColor.add(currP.associatedColor);
            p++;
        }

        // inizializzo lista colori
        
        colors = new String[1];
        colors[0] = "";
        
        if (listColor.size() > 0) {
            colors = new String[listColor.size()];
            colors = listColor.toArray(colors);
        } 
        
        return colors;
    }

    /*
     ritorna un array di stringhe che descrive le attuali persone attive nella scena
     Questo metodo verrà poi richiesto per popolare la JList
     */
    public String[] getListPerson() {

        String[] list = null;
        if (Persons.size() > 0) {

            list = new String[Persons.size() + 1];
            int i = 0;

            while (i<Persons.size()) {
                Person currP = Persons.get(i);
                list[i] = "person_" + currP.getColor();
                i++;
            }

            list[Persons.size()] = "all";
        }
        return list;
    }

    
    public String getMoveString(String color, int step , String pathname , int x , int y){
      return "C: " + color + "\t   S: " + step + "\t   Path: " + pathname
                              + "\t (" + x + "," + y + ")";
    }

    
    
    /*
     *  Ritorna una stringa indicante il numero di step disponibili alla modifica in base
     *  al parametro che li viene dato .
     * @param : param>-1 può indicare l'indice della persona su cui costruire la lista
     *          param==-1 indica che bisogna richiedere la lista globale di tutti gli step
     *                    in cui è stato definito almeno un moves
     */
    
    
    public String[] getListStep(int param) {

        String[] list = null;
        int maxStep = 0;
        Person currPers;
        if (param == -1) {
            
            for(int p = 0; p<Persons.size(); p++ ){
                
                currPers = Persons.get(p);
                int numStepPerson = 0;
                
                for (int path=0; path<currPers.paths.size();path++) {
                    Path currPath = currPers.paths.get(path);
                    int numStepPath = currPath.moves.size();
                    numStepPerson += numStepPath;
                }
                if (numStepPerson > maxStep) {
                    maxStep = numStepPerson;
                }
            }


        } else {

            currPers = Persons.get(param);
            int numStepPerson = 0;
            
            for (int path=0; path<currPers.paths.size();path++) {
                    Path currPath = currPers.paths.get(path);
                    int numStepPath = currPath.moves.size();
                    numStepPerson += numStepPath;
                }
            
            maxStep=numStepPerson;
            
        }

        list = new String[maxStep];
        for (int i = 0; i < list.length; i++) {
            list[i] = "Step " + i;
        }

        return list;
    }
    
    
    

    /*
     *   Ritorna la lista dei movimenti definiti fino a questo istante in base al parametro param
     *    @paramPerson : paramPerson>-1  indica l'indice della persona da cui prelevare tutti le moves definite
     *                   paramPerson==-1 indica la richiesta delle moves per tutti le persone nella lista
     *
     *   @paramStep : paramStep>-1  indica il numero dello step da cui prelevare tutti le moves definite
     *                paramStep==-1 indica la richieste delle moves per tutti gli step
     */
    public String[] getListMove(int paramPerson, int paramStep, String paramPath) {

        String[] list = null;
        ArrayList<String> moveslist = new ArrayList<String>();
        // richiesta della lista completa degli step;
        Person currPers;
        
        if (paramPerson == -1 && paramStep == -1 && paramPath.equals("all")) {
            
            
            for (int pers=0;pers<Persons.size(); pers++) {
                currPers = Persons.get(pers);
                
                // per ogni percorso legato all'agente
                
                for (int path=0; path<currPers.paths.size();path++) {

                    Path currPath = currPers.paths.get(path);
                    
                 // per ogni movimento legato allo stesso percorso 
                 
                    for (int m=0; m<currPath.moves.size();m++ ) {
                        StepMove s = currPath.moves.get(m);
                        
                        String move = getMoveString(currPers.associatedColor,s.step,currPath.getName(),s.x,s.y);
                        moveslist.add(move);
                    }
                }
            }
        }
        
        else{
          
          // richiesta della lista completa delle moves in un determinato step
          
          if (paramStep > -1) {
              
              for(int pers = 0; pers< Persons.size(); pers++) {
                  
                  currPers = Persons.get(pers);
                  
                  for(int path=0; path< currPers.paths.size();path++){
                    
                    Path currPath = currPers.paths.get(path);
                    
                    if (paramStep >= currPath.startStep && paramStep<= currPath.lastStep) {
                      
                        int offset = paramStep - currPath.startStep;
                        try{
                            StepMove s = currPath.moves.get(offset);
                            String move = getMoveString(currPers.associatedColor,s.step,currPath.getName(),s.x,s.y);
                            moveslist.add(move);
                            break;
                        }
                        catch(IndexOutOfBoundsException ex){
                            
                            String debug = "Error with ParamStep " + paramStep 
                                       + " startStep " + currPath.startStep 
                                       + " laststep" + currPath.lastStep ;
                            AppendLogMessage(debug,"error");
                        }     
                    }
                  }

              }
          }

          // richiesta della lista delle moves eseguita da una determinata persona
          
          else if (paramPerson > -1) {

              currPers = Persons.get(paramPerson);
              
              for(int path=0; path<currPers.paths.size(); path++) {
                  
                  Path currPath = currPers.paths.get(path);
                  
                  
                  for(int m = 0; m<currPath.moves.size(); m++) {
                      
                      StepMove s = currPath.moves.get(m);
                      String move = getMoveString(currPers.associatedColor,s.step,currPath.getName(),s.x,s.y);
                      moveslist.add(move);
                  }
              }

          }
          
          
          // richiesta della lista delle moves eseguite all'interno di un determinato path
          
          else{
              
              Path currPath = getPathByName(paramPath);
              
              String[] split = paramPath.split("_");
              
         
              for (int m = 0; m<currPath.moves.size(); m++) {
                  
                  StepMove s = currPath.moves.get(m);
                  String move = getMoveString(split[0],s.step,currPath.getName(),s.x,s.y);
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
     *                paramPerson==-1 indica la richiesta dei path per tutti le persone nella lista
     * @return : array di stringhe equivalente all'elenco
     */
    public String[] getStringPaths(int paramPerson) {

        String[] paths = null;
        ArrayList<String> listPaths = new ArrayList<String>();
        Person currPers;
        
        if (paramPerson == -1) {

            
            for(int pers=0 ; pers<Persons.size(); pers ++) {

               currPers  = Persons.get(pers);
                
                for(int path=0; path < currPers.paths.size() ; path++){
                    Path currPath = currPers.paths.get(path);
                    listPaths.add(currPath.name);
                }

            }
        } else {

            currPers = Persons.get(paramPerson);

            for(int path=0; path<currPers.paths.size(); path++) {
                Path currPath = currPers.paths.get(path);
                listPaths.add(currPath.name);
            }

        }

        paths = new String[listPaths.size()];
        paths = listPaths.toArray(paths);

        return paths;

    }

    /**
     * Restituisce un array di oggetti Path corrispondenti alla persona passata come parametro
     * @param paramPerson
     * @return un array di Path
     */
    
    public Path[] getPaths(int paramPerson) {

        Path[] paths = null;
        ArrayList<Path> listPaths = new ArrayList<Path>();
        Person currPers;

        if (paramPerson == -1) {
            
            for(int pers = 0 ; pers < Persons.size() ; pers++ ) {

                currPers = Persons.get(pers);
                
                for(int path=0; path< currPers.paths.size(); path++){
                    
                    Path currPath = currPers.paths.get(path); 
                    listPaths.add(currPath);
                }

            }
        } else {

            currPers = Persons.get(paramPerson);
            
            for(int path=0; path< currPers.paths.size(); path++){
                    
                Path currPath = currPers.paths.get(path); 
                listPaths.add(currPath);
            }

        }

        paths = new Path[listPaths.size()];
        paths = listPaths.toArray(paths);

        return paths;

    }

    /**
     * Restituisce un array di stringhe corrispondenti all'elenco dei path per la
     * person passato come parametro
     * @param paramPerson indice della person 
     * @return un array di nomi di path
     */
    
    public String[] getPersonPaths(int paramPerson){
        String[] paths = null;
        ArrayList<String> listPaths = new ArrayList<String>();

        if (paramPerson >= 0) {

            Person currPers = Persons.get(paramPerson);
            for(int path=0; path< currPers.paths.size(); path++){
                    
                Path currPath = currPers.paths.get(path); 
                listPaths.add(currPath.name);
            }
            
            paths = new String[listPaths.size()];
            paths = listPaths.toArray(paths);

        }

        return paths;
    
    }
    

    /**
     * Metodo per la verifica e la rimozione delle Move non piu valide a seguito
 di un resize della mappa.Il metodo scorre la lista linkata e rimuove
 tutte le moves a partire dalla prima occorrenza non può valida. Se ad un
 certo step la moves non è valida non lo saranno più tutte quelle
 successivamente create
     */
    public int [] RemoveStepAfterResize() {

        
        
        int [] pos = new int [2];
        Path currPath;
        boolean flag;
        // per ogni agente sulla lista
        
        for(int pers=0; pers < Persons.size(); pers++){
            
            Person currPers = Persons.get(pers);
            
            // per ogni percorso associato all'agente currPers
            int path = 0;
            while(path<currPers.paths.size()){
                
                currPath = currPers.paths.get(path);
                int m = 0;
                flag  = false;
                
                while (m<currPath.moves.size()) {
                    StepMove s = currPath.moves.get(m);
                    
                    // se trovo una posizione dell'agente non valida
                    
                    if (s.getX() < 0 || s.getX() >= NumCellX || s.getY() < 0 || s.getY() >= NumCellY) {
                      flag = true;
                      break;
                    }
                    
                    if(!PersonPositionIsValid(scene[s.getX()][s.getY()]))
                    {
                        flag=true;
                        break;
                    }
                    
                    m++;
                }
                
                
               // se è stata trovata una posizione non valida 
               
                if(flag){
                    
                    for(int index = currPath.moves.size()-1;index>=m;index--){
                   
                        // rimuovo tutti i passi successivi a quello non valido

                        currPath.moves.remove(index);
                        
                    }
                    break;
                }
           
                path++;
            }

            if(path<currPers.paths.size()-1){
                     
                   for(int nextPath =currPers.paths.size()-1; nextPath>path ;nextPath--){
                       currPers.paths.remove(nextPath);
                   }
            }
                           

            if(currPers.paths.getFirst().moves.isEmpty()){
                
              currPers.paths.clear();
              Remove(currPers.associatedColor);
            }
            
            else{
              StepMove s = currPers.paths.getLast().getMoves().getLast();
              pos[0]=s.x;
              pos[1]=s.y;
            }
        }

        return pos;
    }


/********************************************************************
 *          UPDATE AND MODIFIDY MAPS
 *
 *******************************************************************/

/**
 *  Classe enumerativa per una gestione uniforme dei messaggi di Log
 *  per il generatore di mappe
 */
    
public enum MapGenMessage{
    SUCCESS(0,"Operazione eseguita con successo"),
    ILLEGALPOS(1,"Posizione del robot non valida"),
    ILLEGALPERSPOS(2,"Posizione per l'agente non valida"),
    KEYCOLOREMPTY(3,"Nessun agente trovato"),
    KEYCOLORFULL(4,"Nessun slot di colore libero"),
    PERSONOVERRIDE(5,"Soprapposizione di due agenti"),
    ILLEGALMAPPOS(6,"Posizione della mappa non valida"),
    INTEGERMISMATCH(7,"Necessità di numero intero"),
    NOPERSONWITHCOLOR(8, "Agente non presente"),
    STEPROLLBACK(9,"Ultimo step eliminato"),
    UNAVAIBLEMOVE(10, "Movimento non disponibile"),
    FIRSTPATH(11,"Impossibile eliminare il primo percorso agente");

    private int code;
    private String message;
    MapGenMessage(int code, String message)
    {
        this.code = code;
        this.message = message;
    }
    
    public int getCode(){
        return code;
    }
    
    public String getMessage(){
        return message;
    }
    
    
    public static String getMessage(int code){
    
     for(MapGenMessage mgm : MapGenMessage.values())
     {
         if(mgm.code==code)
         {
            return "Codice (" + code + ") : " + mgm.getMessage();
         }
     }    
    
    return "Nessun messaggio con codice (" + code + ") \n" ;
   }
    
}
    
    
/**
 * Metodo per l'aggiornamento consistente delle celle. Il metodo ritorna
 * interi corrispondenti ad un particolare conclusione dell'esecuzione.
 * L'aggiornamento viene sostanzialmente separato in tre casi (richiesta di
 * una posizione dell'agente robotico , richiesta di una nuova posizione
 * iniziale di un agente umano, modifiche allo scenario). Il robot può
 * essere modificato nella sua posizione solo se vengono rispettate le
 * condizoioni del progetto
 *
 * @param x ,y : possibile in riga e colonna della cella da modificare
 * @param state : nuovo stato da inserire
 */

public int UpdateCell(int x, int y, String state) {

   

    if(x< 0 || x >= NumCellX || y < 0 || y >= NumCellY)
    {
        return MapGenMessage.ILLEGALMAPPOS.code;
    }
    
    // se è stato richiesto un aggiornamento della posizione di agent
    // controllo se attualmente non si trova nella stessa cella in cui 
    // vado a fare la modifica
    
    if (state.contains("agent")) {

        // se la nuova posizione agente è diversa dalla precedente
        
        if (x != agentposition[0] || y != agentposition[1]) {

            if (RobotPositionIsValid(scene[x][y])) {

                // rimuovo l'agente dalla posizione corrente sostuiendolo con un empty
                // e successivamente inserisco il nuovo agente
                
                String [] split = scene[agentposition[0]][agentposition[1]].split("\\+");
                String background = split[0];
                scene[x][y] += "+" + state;
                scene[agentposition[0]][agentposition[1]] = background;
                SetRobotParams(state, x, y);
            }
            else {
                return MapGenMessage.ILLEGALPOS.code;
            }

        }
        else { // stessa posizione attuale dell'agent position

            String [] split = scene[x][y].split("\\+");
            String background = split[0];
            scene[x][y] = background + "+" + state;
        }
    
    }
    
    // si richiedono modifiche alla scena diverse da tipologie di state agent
    
    else {
        
        scene[x][y] = state;
        
        // nel caso in cui dovessi sovrascrivere la posizione attuale dell'agente
        // allora semplicemente reimposto la posizione di default dell'agente
        
        if (x == agentposition[0] && y == agentposition[1]) {
        
            agentposition[0] = defaultagentposition[0];
            agentposition[1] = defaultagentposition[1];
            scene[agentposition[0]][agentposition[1]] = defaulagentcondition;
        
        } 
        
    }
    
    return MapGenMessage.SUCCESS.code;
}




    /*
     *  Restituisce il path della persona che attualmente che occupa attualemnte quella
     *   cella oppure restituisce -1 in caso la cella sia libera.
     *   @param x : numero di riga della cella
     *   @param y : numero di colonna della cella
     *   @param Step : numero di step in cui si controlla la cella
     */
    public String CheckBusyCell(int x, int y, int Step) {
        
        Person currPers;
        
        for(int pers=0; pers<Persons.size();pers++) {
            
            currPers = Persons.get(pers);
            
            Path currPath;
            
            for(int path=0; path<currPers.paths.size();path++) {
                currPath=currPers.paths.get(path);
                if (currPath.startStep <= Step && currPath.lastStep >= Step) {
                    
                    int offset = Step - currPath.startStep;
                    if (offset < currPath.moves.size()) {
                        
                        StepMove moveSelect = currPath.moves.get(offset);
                        
                        if (moveSelect.getX() == x && moveSelect.getY() == y) {
                            return currPath.name;
                        }
                    }
                    
                }

            }

        }
        
        return "empty";

    }



    /**
     * Crea un nuovo path e lo aggiunge alla lista dei path della persona
     * indicata. L'aggiunta del path comporta sempre l'inserimento di una move 
     * he determina lo stato iniziale per il nuovo path da eseguire. Un path
     * viene etichettato attraverso una label che risulta composta nel seguente
     * modo : (color_numPathPerson)
     *
     * @param color : colore associato alla person a cui si vuole aggiungere il path
     * @param xstartStep : riga della cella iniziale del path
     * @param ystartStep : colonna della cella iniziale del path
     * @param waitStep : tempo di attesa dalla fine del path precedente
     **/
    
    
    public int AddNewPathToPerson(String color, int waitStep) {

        Person p = findPersonByColor(color);
        if (p == null) {
            return MapGenMessage.NOPERSONWITHCOLOR.code;
        }
        
        int start = p.paths.getLast().lastStep + waitStep + 1;
        int xStartStep = p.paths.getLast().moves.getLast().x;
        int yStartStep = p.paths.getLast().moves.getLast().y;
        String result = CheckBusyCell(xStartStep, yStartStep, start);

        if (result.equals("empty")) {
            
            if (PersonPositionIsValid(scene[xStartStep][yStartStep])) {
                p.AddPath(waitStep);
                p.paths.getLast().AddMove(xStartStep, yStartStep);
                return MapGenMessage.SUCCESS.code;
            } 
            else {
                return MapGenMessage.ILLEGALPERSPOS.code;
            }
        } 
        else {
            return MapGenMessage.PERSONOVERRIDE.code;
        }

    }


    
    /**
     *  Restituisce la distanza di Manhattam tra due celle 
     */
    
    public int ManhattamDistance(int xstart, int ystart, int xtarget, int ytarget) {

        return Math.abs(ytarget - ystart) + Math.abs(xtarget - xstart);
    }


    /**
    * Questo metodo genera l'aggiornamento delle celle della mappa del
    * generatore in modalità moves, determinando quali movimenti sono possibili
    * per un agente e in tal caso aggiorna la lista dei movimenti
    *
    * @param x : numero di riga
    * @param y : numero di colonna
    * @param p : persona a cui aggiungere la moves
    */
    
    public int UpdateMoveCell(int x, int y, String path) {

        
        Path p = getPathByName(path);

        if (!(x >= 0 && x < NumCellX && y >= 0 && y < NumCellY)) {
            return MapGenMessage.ILLEGALMAPPOS.code;
        }

        StepMove s = p.getMoves().getLast();
        int step = p.getMoves().getLast().getStep() + 1;
        String result = CheckBusyCell(x, y, step);

        if (!(result.equals("empty"))) {
            return MapGenMessage.PERSONOVERRIDE.code;
        }

        // distanza di manhattam e check sulla attraversabilità della cella

        if (ManhattamDistance(s.getX(), s.getY(), x, y) == 1 &&
                PersonPositionIsValid(scene[x][y])) {
            p.AddMove(x, y);
            return MapGenMessage.SUCCESS.code;
        } 
        else if (ManhattamDistance(s.getX(), s.getY(), x, y) == 0) {
            p.RemoveLast();
            return MapGenMessage.STEPROLLBACK.code;
        } 
        else {
            return MapGenMessage.UNAVAIBLEMOVE.code;
        }

    }

    /**
     * Rimuove una persona in base al colore ad esso assegnata
     * @param color : colore rappresentante la persona da rimuovere
     * @return boolean : verifica se la rimozione è avvenuta correttamente
     */
    
    public boolean Remove(String color) {
        
        int i = 0;
        while(i<Persons.size()) {

            Person p = Persons.get(i);
            if (p.associatedColor.equals(color)) {
                Persons.remove(p);
                NumPerson--;
                return true;
            }
            i++;
        }

        return false;
    }



    /**
   * Aggiunge una nuova persona allo scenario dichiarandone il colore
   * associato e la posizione di partenza
   *
   * @param x : riga della cella iniziale
   * @param y : colonna della cella iniziale
   * @param color : colore da associare
   */
  
    public int AddNewPerson(int x, int y, String color, int waitTime) {


      // posizione illegale sulla mappa

      if (!(x >= 0 && x < NumCellX && y >= 0 && y < NumCellY)){

           return MapGenMessage.ILLEGALMAPPOS.code;
      }

      // keycolor non ancora inizializzato
      if (setKeyColor.length == 0) {
          return MapGenMessage.KEYCOLOREMPTY.code;
      }

      // sovrascrittura del robot
      if (x == agentposition[0] && y == agentposition[1]) {

          return MapGenMessage.ILLEGALPERSPOS.code;
      }

      // sovrascrittura di un agente

      String result = CheckBusyCell(x, y, 0);
      if (!result.equals("empty")) {

          return MapGenMessage.PERSONOVERRIDE.code;
      }



      if (findIndexPosByColor(color) != -1) {

          Person p = findPersonByColor(color);
          Path first = p.paths.getFirst();
          first.moves.getFirst().setX(x);
          first.moves.getFirst().setY(y);
          return MapGenMessage.PERSONOVERRIDE.code;

      }

      // ho ancora dei colori liberi

      if (NumPerson < MaxNumPerson) {
          NumPerson++;
          Persons.add(new Person(color));
          Persons.getLast().AddPath(waitTime);

          if (PersonPositionIsValid(move[x][y])) {

              Persons.getLast().paths.getLast().AddMove(x, y);
              String background = move[x][y];
              move[x][y] = background + "+" + color + "+" + personName;

          } 
          else {
              return MapGenMessage.ILLEGALPOS.code;
          }
      } 
      else {
          
          // nessun colore disponibile per l'aggiunta
          return MapGenMessage.KEYCOLORFULL.code;
      }

          return MapGenMessage.SUCCESS.code;

      } 
   


    /**
     * Rimuove l'ultimo path aggiunto ad una persona e restuisce un intero
     * equivalente al risultato ottenuto.
     *
     * @param color colore assegnato alla persona
     */
    public int RemoveLastPath(String color) {


        Person p = findPersonByColor(color);
        if (p == null) {
            return MapGenMessage.NOPERSONWITHCOLOR.code;
        }

        
        if (p.paths.size() > 1) {
             p.RemoveLastPath();
             return MapGenMessage.SUCCESS.code;
        } 
        else {
              return MapGenMessage.FIRSTPATH.code;
        }
        

    }

    /* **************************************************************
                LOAD AND SAVE MAP FUNCTIONS
    ******************************************************************/


    /**
     * Scrive su un file di testo la scena sviluppata tramite tool grafico
     *
     * @param directory : su cui salvare i file di RealMap e history con
     * rispettivi JSON
     * @return consoleOutput con il feedback delle operazioni di scrittura dei
     * file
     * @throws JSONException
     */
    public String SaveFiles(File directory) throws JSONException {

        String consoleOutput = "";
        consoleOutput += WriteSceneOnFile(directory);
        consoleOutput += WriteHistoryOnFile(directory);
        return consoleOutput;

    }

    /**
     * Scrive la scena generata con MapGenerator all'interno di un file RealMap.txt
     * 
     * @param directory in cui salvare il file RealMap.txt
     * @return
     * @throws JSONException
     */
    private String WriteSceneOnFile(File directory) throws JSONException {
        
        //richiamo l'export della scena il quale mi dará una stringa con tutto il codice clips corrispondente
        
        String sceneFile = exportScene();

        String DirName = "";
        String Parent = "";
        String consoleOutput = "";

        try {
            DirName = directory.getName();
            Parent = directory.getParent();
            // creazione nuovo file
            String infoMapPath = Parent + File.separator + DirName + File.separator + "RealMap.txt";
            //scrivo il file della mappa
            Files.write(Paths.get(infoMapPath), sceneFile.getBytes());
            consoleOutput += "File creato \n" + Paths.get(infoMapPath);

            String JSONMapPath = Parent + File.separator + DirName + File.separator + "InfoMap.json";
            //scrivo il file json con la mappa scritta
            saveJSONMap(JSONMapPath);
            
            consoleOutput += "File creato \n" + Paths.get(JSONMapPath);

        } catch (IOException err) {
            return err.getMessage();
        }

        return consoleOutput;

    }

    /**
     * Scrive su file di testo la history sviluppata mediante il tool moves
     * all'interno del generatore
     *
     * @param directory
     * @return
     * @throws JSONException
     */
    private String WriteHistoryOnFile(File directory) throws JSONException {

        String historyFile = exportHistory();
        String DirName = "";
        String Parent = "";
        String consoleOutput = "";

        try {
            DirName = directory.getName();
            Parent = directory.getParent();
             // creazione nuovo file

            if (historyFile.length() > 0) //scrivo il file della history solo se sono
            {                               //sono state aggiunte persone alla scena
                String HistoryPath = Parent + File.separator + DirName + File.separator + "history.txt";
                Files.write(Paths.get(HistoryPath), historyFile.getBytes());
                consoleOutput += "File creato \n" + Paths.get(HistoryPath);
            }
            //scrivo il file json con la mappa scritta
            String JSONMovePath = Parent + File.separator + DirName + File.separator + "InfoMove.json";
            boolean MoveResult = saveJSONMoves(JSONMovePath);
            File json = new File(JSONMovePath);
            boolean RobotResult = this.SaveJsonRobotParams(json);
            if (MoveResult && RobotResult) {
                consoleOutput += "File creato \n" + Paths.get(JSONMovePath);
            }

        } catch (IOException err) {
            return err.getMessage();
        }

        return consoleOutput;

    }




    public void LoadFiles(File directory) throws ParseException {

        String jsonMapPath = directory.getAbsolutePath() + File.separator + "InfoMap.json";
        String jsonMovePath = directory.getAbsolutePath() + File.separator + "InfoMove.json";
        File jsonMap = new File(jsonMapPath);
        File jsonMove = new File(jsonMovePath);

        LoadJsonMap(jsonMap);
        LoadMoves(jsonMove);
        LoadJsonRobotParams(jsonMove);
    }

    /**
     * Questo metdo utilizza dati strutturati JSON per il salvataggio della
     * scena. La struttura dati JSON cosi restituita viene successivamente
     * utilizzata per il caricamento della mappa cosi da poter essere modificata
     * in un secondo momento.
     *
     * @param nome : rappresenta il nome del file su cui eseguire il salvataggio
     * @return boolean : valuta se la scrittura sul JSON è stata eseguita
     * correttamente
     */
    
    public boolean saveJSONMap(String nome) throws JSONException {

        try {
            //Creo la radice con le informazioni sulla griglia
            JSONObject info = new JSONObject();
            info.put("cell_x", getNumx());
            info.put("cell_y", getNumy());
            
            //ciclo sulla matrice degli stati per creare la struttura
            JSONArray ArrayCells = new JSONArray();
            for (int i = 0; i < getNumx(); i++) {
                for (int j = 0; j < getNumy(); j++) {
                    JSONObject cell = new JSONObject();
                    cell.put("x", i);
                    cell.put("y", j);
                    String background = scene[i][j];
                    if(scene[i][j].contains("agent")){
                      String [] split = scene[i][j].split("\\+");
                      background=split[0];
                    }
                    cell.put("state", background);
                    ArrayCells.put(cell);
                }
            }
            info.put("cells", ArrayCells);
            //salvo le informazioni in un file JSON della scenea
            Files.write(Paths.get(nome), info.toString(2).getBytes());
        } catch (IOException ex) {
            AppendLogMessage(ex.getMessage(),"error");
            Logger.getLogger(RescueGenMap.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

/**
 * Metodo per il caricamnento di una scena a partire da un file JSON precedentemente creato
 * Viene costruita la mappa e popolata dai valori contenuti nel JSON.
 * @param jsonFile : il file JSON da cui eseguire il load
 */

    @SuppressWarnings("UnnecessaryUnboxing")
private void LoadJsonMap(File jsonFile) throws ParseException {
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

        
        SetNumCell(NumCellX, NumCellY);
        SetPreferredSizeMap();
        
        //imposto la dimensione iniziale della scena
        scene = new String[NumCellX][NumCellY];
        //initScene(scene);
        move = clone(scene);
        
        //estraggo il JSONArray dalla radice
        JSONArray arrayCelle = json.getJSONArray("cells");
        for (int i = 0; i < arrayCelle.length(); i++) {
            //ciclo su ogni cella e setto il valore della cella letta nella scena
            JSONObject cell = arrayCelle.getJSONObject(i);
            int x = cell.getInt("x");
            int y = cell.getInt("y");
            String state = cell.getString("state");
            setCell(x, y, state);
        }

        CopyToActive(scene);

    } catch (JSONException ex) {

        AppendLogMessage(ex.getMessage(),"error");
    } catch (IOException ex) {
        AppendLogMessage(ex.getMessage(),"error");
    } catch (NumberFormatException ex) {
        AppendLogMessage(ex.getMessage(),"error");
    }

}




    /**
     * Genera un file JSON corrispondente alla lista linkata salvata per le moves fino ad ora definite
     * per la history. Il JSON viene utilizzato per semplificare il caricamente della history e dello
     * scenario.
     * @param name : nome del file su cui scrivere il JSON
     */

    private boolean saveJSONMoves(String name) throws JSONException {

        try {

            JSONObject Info = new JSONObject();
            JSONArray PersonsArray = new JSONArray();

            for (int i = 0; i < Persons.size(); i++) {

                Person currPers = Persons.get(i);
                JSONObject person = new JSONObject();
                person.put("color", currPers.associatedColor);
                JSONArray paths = new JSONArray();

                for (int j = 0; j < currPers.paths.size(); j++) {

                    Path pts = currPers.paths.get(j);
                    JSONObject path = new JSONObject();
                    path.put("name", pts.name);
                    path.put("startStep", pts.startStep);
                    path.put("lastStep", pts.lastStep);
                    JSONArray moves = new JSONArray();

                    for (int k = 0; k < pts.moves.size(); k++) {

                        JSONObject move = new JSONObject();
                        StepMove s = pts.moves.get(k);
                        move.put("x", s.x);
                        move.put("y", s.y);
                        move.put("step", s.step);
                        moves.put(move);
                    }

                    path.put("moves", moves);
                    paths.put(path);
                }

                person.put("paths", paths);
                PersonsArray.put(person);
            }

            Info.put("personList", PersonsArray);
            Info.put("time",maxduration);
            Files.write(Paths.get(name), Info.toString(2).getBytes());
            return true;

        } catch (IOException err) {

            AppendLogMessage(err.getMessage(),"error");
            return false;
        }
    }

    /*
     * Esegue il load della linkedList delle person partendo da un file JSON precedentemente creato.
     * Il file viene convertito nuovamente in un oggetto JSON e parsificato secondo la struttura
     * definita dal metodo LoadMoves sopra
     * @param jsonFile : file jsonFile da cui eseguire il load
     */
    public void LoadMoves(File jsonFile) throws ParseException {

        try {
            //converto il file in un oggetto JSON
            FileReader jsonreader = new FileReader(jsonFile);
            char[] chars = new char[(int) jsonFile.length()];
            jsonreader.read(chars);
            String jsonstring = new String(chars);
            jsonreader.close();
            JSONObject json = new JSONObject(jsonstring);

            Persons = new LinkedList<Person>();

            //estraggo il JSONArray dalla radice
            JSONArray arrayPersons = json.getJSONArray("personList");
            for (int i = 0; i < arrayPersons.length(); i++) {

                JSONObject person = arrayPersons.getJSONObject(i);
                String color = person.getString("color");
                Person p = new Person(color);
                JSONArray arrayPaths = person.getJSONArray("paths");

                for (int j = 0; j < arrayPaths.length(); j++) {

                    JSONObject path = arrayPaths.getJSONObject(j);
                    String pathName = path.getString("name");
                    int startStep = path.getInt("startStep");
                    int lastStep = path.getInt("lastStep");
                    p.paths.add(new Path(pathName, startStep, lastStep));
                    JSONArray arrayMoves = path.getJSONArray("moves");

                    for (int k = 0; k < arrayMoves.length(); k++) {

                        JSONObject move = arrayMoves.getJSONObject(k);
                        int x = move.getInt("x");
                        int y = move.getInt("y");
                        int step = move.getInt("step");
                        p.paths.getLast().moves.add(new StepMove(x, y, step));
                    }
                }
                Persons.add(p);
            }
           
           maxduration = json.getInt("time"); 
           
           
        } catch (JSONException ex) {
            AppendLogMessage(ex.getMessage(),"error");
        } catch (IOException ex) {
            AppendLogMessage(ex.getMessage(),"error");
        } catch (NumberFormatException ex) {
            AppendLogMessage(ex.getMessage(),"error");
        }

    }


    public String getNewPathName(String pathName)
    {
      String [] split = pathName.split("_");
      String newpath =  "";
      for (int i=0; i<split.length;i++)
      {
        newpath += split[i];
      }

       return newpath;
    }
    
    

    /***************************************************************
     * Metodi di conversione delle coordinate tra il sistema adottato
     * dal MapGenerator e quello adottato da MonitorModel
     * 
     * N.B : Risulta necessario ai fini di rendere compatibili le vostre
     * mappe con il simulatore vero e prorio eseguire le conversione qui
     * sotto indicati quando dovete esportare i files dello scenario
     * 
     **************************************************************/
    
    
    
    protected int[] GenMapToMap(int i, int j){
        return new int[]{scene[0].length - j, i+1};
    }

    protected int[] GenMapToMap(int [] pos){
        return new int[]{scene[0].length - pos[1], pos[0]+1};
    }


    protected int[] MapToGenMap(int i , int j,int maxR){
        return new int[]{j-1, maxR - i};
    }
    
    protected int[] MapToGenMap(int [] pos,int maxR){
        return new int[]{pos[1]-1, maxR - pos[0]};
    }

    
    /******************************************************
     *  Gestione della console Log 
     *****************************************************/
    
    
    /**
     * Aggiorna il testo del Log da visualizzare sulla console, incapsulando
     * il testo del messaggio (newLog) sulla base del tipo di questo messaggio
     * Quest'ultimo può essere :
     *   - Log  (informazioni e feedback positivo delle operazioni)
     *   - Error (informazioi relative agli errori in esecu<ione )
     * 
     * @param newLog
     * @param type 
     */

    protected void AppendLogMessage(String newLog, String type){
      String logMessage="";
      if(type.equals("error")){
        logMessage=CreateErrorMessage(newLog);
      }
      else{
        logMessage=CreateLogMessage(newLog);
      }
      log= log + logMessage + "\n";
    }

    
    /**
     * Crea una stringa di LOG
     * @param message
     * @return 
     */

    protected String CreateLogMessage(String message){

      String newString = "[INFO] : ";
      newString += message;
      return newString;
    }

    /**
     * Crea una stringa di ERROR
     * 
     * @param message
     * @return 
     */
    
    protected String CreateErrorMessage(String message){
      String newString = "[ERROR] : ";
      newString += message;
      return newString;
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
    
    

    /******************************************
     * PARTE ASTRATTA
    *******************************************/
    
    
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
    public abstract void SetRobotParams(String state, int x, int y);
    
    /**
     * Carica dal JSON i parametri da settare sul Robot
     * @param json 
     */
  
    public abstract void LoadJsonRobotParams(File json);
    
    /**
     * Salva la configurazione dei parametri del robot sul formato JSON
     * e restiutisce un flag per informare del fallimento-successo dell'operazione
     * 
     * @param json
     * @return 
     */
    
    public abstract boolean SaveJsonRobotParams(File json);
    
    /**
     * Genera il Json delle informazioni della scena contenuta nel file map
     * 
     * @param map 
     */
    
    public abstract void createJsonScene(File map);
    
    /**
     * Genera il Json delle informazioni della history contenuta nel file history
     * 
     * @param history
     * @param jsonMap 
     */
    
    public abstract void createJsonHistory(File history, File jsonMap);
}

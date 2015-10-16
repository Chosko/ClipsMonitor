package org.mapgenerator;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.clipsmonitor.monitor2015.RescueImages;

/*
 * Classe che definisce il concetto di scena con tutti i metodi per accedervi e
 * modificarla
 *
 * @author Tobia Giani, Alessandro Basile
 */
public class MapGeneratorSceneModel {

    //numero di celle sulle x e sulle y
    int num_x, num_y;

    //largezza e altezza celle
    float c_width, c_height;
    //largezza e altezza finestra
    float w_width, w_height;

    //matrice fondamentale rappresentante la scena
    String[][] scene;
    
    String direction;
    int maxduration;
            
    //percentuale della finestra che viene occupata dalla scena
    int perc;

    //array di immagini da disegnare sulla scena sono nello stesso ordine dello
    //schema di valori della scena però traslato di -1 visto che per le celle
    //empty non bisogna disegnare nulla
    //cioè in 0 c'è wall (1), in 1 c'è seat (2), e cosi via
    Map<String,BufferedImage> images;

    public MapGeneratorSceneModel() {

    }

    public MapGeneratorSceneModel(int num_x, int num_y, float w_width, float w_height) {
        //dimensione della finestra
        this.w_width = w_width;
        this.w_height = w_height;
        //imposto la dimensione iniziale della scena
        scene = new String[num_x][num_y];
        //genero la scena della dimensione specificata
        this.resize(num_x, num_y);
        //inizializzo la scena con i valori di default e cioè con i muri su tutto il bordo della scena
        this.initScene(scene);
        //carico tutte le image in ram
        this.loadImages();
    }

    public void loadImages() {
        Map<String,BufferedImage> mapicons = RescueImages.getInstance().getMapImg();
        this.images = mapicons;
        
    }

    public void drawScene(Graphics2D g, float w_width, float w_height) {

        //aggiorno le dimensioni della finestra
        this.w_width = w_width;
        this.w_height = w_height;
        //calcolo la larghezza delle celle
        c_width = (w_width * perc / 100) / num_x;
        c_height = (w_height * perc / 100) / num_y;

        //verifico chi delle due dimensioni é minore e setto quella maggiore uguale a quella minore per rendere le celle quadrate
        if (c_width > c_height) {
            c_width = c_height;
        } else {
            c_height = c_width;
        }

        //calcolo le coordinate di inizio della scena partendo a disegnare
        //dall'angolo in alto a sinistra della nostra scena
        float x0 = (w_width - c_width * num_x) / 2;
        float y0 = (w_height - c_height * num_y) / 2;

        //setto colore delle scritte
        g.setColor(Color.BLACK);

        //doppio ciclo sulla matrice
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                //calcolo la posizione x,y dell'angolo in alto a sinistra della
                //cella corrente
                int x = (int) (x0 + i * c_width);
                int y = (int) (y0 + j * c_height);
                //se la cella non è vuota, allora disegno l'immagine corrispondente
                if (scene[i][j].equals("")) {
                    //disegno l'immagine corretta (usando il numero contenuto nella matrice che definisce la scena) nella posizione calcolata e con la dimensione di cella corretta
                    g.drawImage(images.get(scene[i][j]), x, y, (int) (c_width - 1), (int) (c_height - 1), null);
                }

                //traccio il rettangolo della cella
                g.drawRect(x, y, (int) (c_width - 1), (int) (c_height - 1));
            }
        }
    }

    public void resize(int num_x, int num_y) {
        //creo una scena con la nuova dimensione
        String[][] new_scene = new String[num_x][num_y];
        //percentuale che la scena al massimo o sulle x o sulle y può occupare
        perc = 90;
        //salvo il numero di celle sulle x e sulle y
        this.num_x = num_x;
        this.num_y = num_y;
        //calcolo la larghezza delle celle
        c_width = (w_width * perc / 100) / num_x;
        c_height = (w_height * perc / 100) / num_y;

        if (c_width > c_height) {
            c_width = c_height;
        } else {
            c_height = c_width;
        }

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

    public void initScene(String[][] scene) {

        //imposto i muri sul perimetro
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                if (i == 0 || i == scene.length - 1 || j == 0 || j == scene[0].length - 1) {
                    scene[i][j] = "";
                }
            }
        }
    }

    public String exportHistory() {
        String history = "";
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
    
            
    
    public String exportScene() {
        String map = "(maxduration" + this.maxduration +")\n";
        //variabili per impostare la posizione delle componenti
        int[] pos_agent = new int[2];

        /*
        
        ArrayList<int[]> tavoli = new ArrayList<int[]>();
        ArrayList<int[]> food = new ArrayList<int[]>();
        ArrayList<int[]> drink = new ArrayList<int[]>();
        ArrayList<int[]> recyclable = new ArrayList<int[]>();
        ArrayList<int[]> trash = new ArrayList<int[]>();
        */
        
        
        String s = "";
        //Scansione della matrice di celle
        for (int i = 0; i < scene.length; i++) {
            for (int j = 0; j < scene[i].length; j++) {
                boolean injuredPresence = scene[i][j].contains("_injured");
                if(injuredPresence){
                s += "(real-cell (pos-r " + (scene[i].length - j) + ") (pos-c " + (i + 1) + ") (contains" + scene[i][j].substring(0,scene[i][j].length()-"_injured".length() +1) + ")"
                        + "(injured yes))\n";
                }
                else{
                s += "(real-cell (pos-r " + (scene[i].length - j) + ") (pos-c " + (i + 1) + ") (contains" + scene[i][j] + ")"
                        + "(injured no))\n";
                }
             }
            
        }

        //costuisco la string da salvare sul file
        //1. Posizione del agente all'inizio - Parking
        map += "\n(initial_agentposition (pos-r " + pos_agent[0] + ") (pos-c " + pos_agent[1] + ") (direction" + this.direction + "))\n";

        
        
        /*
        //2. Posizione dei tavoli
        int count = 1;
        for (int[] t : tavoli) {
            map += "(Table (table-id T" + count + ") (pos-r " + t[0] + ") (pos-c " + t[1] + "))\n";
            count++;
        }
        count = 1;

        //3. Posizione dei trash
        for (int[] tr : trash) {
            map += "(TrashBasket (TB-id TB" + count + ") (pos-r " + tr[0] + ") (pos-c " + tr[1] + "))\n";
            count++;
        }
        count = 1;

        //4. Posizione dei Recyclable
        for (int[] rc : recyclable) {
            map += "(RecyclableBasket (RB-id RB" + count + ") (pos-r " + rc[0] + ") (pos-c " + rc[1] + "))\n";
            count++;
        }
        count = 1;

        //5. Posizione dei food
        for (int[] fd : food) {
            map += "(FoodDispenser  (FD-id FD" + count + ") (pos-r " + fd[0] + ") (pos-c " + fd[1] + "))\n";
            count++;
        }
        count = 1;

        //6. Posizione dei drink
        for (int[] dr : drink) {
            map += "(DrinkDispenser  (DD-id DD" + count + ") (pos-r " + dr[0] + ") (pos-c " + dr[1] + "))\n";
            count++;
        }
        */
                
        //concateno con la definizione delle celle;
        map += "\n" + s;
        return map;
    }

    public String click(int x, int y, String state) {
        float x0 = (w_width - c_width * num_x) / 2;
        float y0 = (w_height - c_height * num_y) / 2;
        float cordx = x - x0;
        float cordy = y - y0;
        cordx = cordx / c_width;
        cordy = cordy / c_height;
        int i = (int) cordx;
        int j = (int) cordy;
        String result = "success";
        if (i >= 0 && i < num_x  && j >= 0 && j < num_y) {
            scene[i][j]=state;
            } 
        else {
            result = "Hai cliccato fuori dalla scena.";
        }
        return result;
    }

    public void setNumCelle(int num_x, int num_y) {
        this.num_x = num_x;
        this.num_y = num_y;
        scene = new String[num_x][num_y];
    }

    public void setCella(int x, int y, String value) {
        scene[x][y] = value;
    }

    public void setSizeScreen(float w_width, float w_height) {
        this.w_height = w_height;
        this.w_width = w_width;
    }
    
    public int getNumx(){
      return this.num_x;
    }
    
    public int getNumy(){
      return this.num_y;
    }
            
}

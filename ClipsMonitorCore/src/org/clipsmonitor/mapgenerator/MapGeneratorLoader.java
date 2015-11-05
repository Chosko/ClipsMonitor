package org.clipsmonitor.mapgenerator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clipsmonitor.clips.ClipsConsole;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe per il salvataggio e la lettura delle mappe in JSON
 *
 * @author Alessandro Basile, Tobia Giani
 */ 
public class MapGeneratorLoader {

    
    private MapGeneratorSceneModel mapModel;
    private String[][] map;
    private ClipsConsole console;
    
    public MapGeneratorLoader(){
    
        mapModel = MapGeneratorSceneModel.getInstance();
        map=mapModel.getScene();
        console = ClipsConsole.getInstance();
    }
    
    
    
    public void exportScene(File file) throws JSONException {
        //richiamo l'export della scena il quale mi dará una stringa con tutto il codice clips corrispondente
        String sceneFile = mapModel.exportScene();
        //richiamo l'export della history il quale mi dará una stringa con tutto il codice clips corrispondente
        String historyFile = mapModel.exportHistory();
        String dirpath="";
        String parentpath="";
        
        try{
            dirpath = file.getName();
            parentpath=file.getParent();
             // creazione nuovo file
                
                //scrivo il file della mappa
                Files.write(Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_RealMap.txt"), sceneFile.getBytes());
                console.info("File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_RealMap.txt"));

                if (historyFile.length() > 0) //scrivo il file della history solo se sono
                {                               //sono state aggiunte persone alla scena
                    Files.write(Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_History.txt"), historyFile.getBytes());
                    console.info("File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath +"_History.txt"));
                }
                //scrivo il file json con la mappa scritta
                this.salva_info_mappa(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json");
                console.info("File creato \n" + Paths.get(parentpath + File.separator + dirpath + File.separator + dirpath + "_InfoMappa.json"));

            
               // console.error("Inserire un nome valido");
            
        }
        catch(Exception err){
            err.printStackTrace();
        }
        
                    
       
    }
    
    public boolean salva_info_mappa(String nome) throws JSONException {
        
       
        try {
            //Creo la radice con le informazioni sulla griglia
            JSONObject info = new JSONObject();
            info.put("cell_x", mapModel.getNumx());
            info.put("cell_y", mapModel.getNumy());

            //ciclo sulla matrice degli stati per creare la struttura
            JSONArray ArrayCell = new JSONArray();
            for (int i = 0; i < mapModel.getNumx(); i++) {
                for (int j = 0; j < mapModel.getNumy(); j++) {
                    JSONObject cella = new JSONObject();
                    cella.put("x", i);
                    cella.put("y", j);
                    cella.put("stato",map[i][j]);
                    //salvo solo l'intero dello stato che viene
                    //mappato internamente;
                    ArrayCell.put(cella);
                }
            }
            info.put("celle", ArrayCell);
            //salvo le informazioni in un file JSON della mappa
            Files.write(Paths.get(nome), info.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(MapGeneratorLoader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    
    @SuppressWarnings("UnnecessaryUnboxing")
    public void load_mappa(File jsonFile) throws ParseException {
        //creo una nuova istanza di scena
       
        
        mapModel.init();
       
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
            mapModel.setNumCell(NumCellX, NumCellY);
            mapModel.resize(NumCellX, NumCellY);
            //estraggo il JSONArray dalla radice
            JSONArray arrayCelle = json.getJSONArray("celle");
            for (int i =0 ; i<arrayCelle.length();i++) {
                //ciclo su ogni cella e setto il valore della cella letta nella scena
                JSONObject cell = arrayCelle.getJSONObject(i);
                int x = Integer.parseInt(cell.get("x").toString());
                int y = Integer.parseInt(cell.get("y").toString());;
                String stato = cell.get("stato").toString();
                
                mapModel.setCell (x,y,stato);
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

    
    
}

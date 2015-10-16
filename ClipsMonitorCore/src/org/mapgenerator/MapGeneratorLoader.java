package org.mapgenerator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe per il salvataggio e la lettura delle mappe in JSON
 *
 * @author Alessandro Basile, Tobia Giani
 */
public class MapGeneratorLoader {

    public static boolean salva_info_mappa(MapGeneratorSceneModel mappa, String nome) {
        try {
            //Creo la radice con le informazioni sulla griglia
            JSONObject info = new JSONObject();
            info.put("cell_x", mappa.num_x);
            info.put("cell_y", mappa.num_y);

            //ciclo sulla matrice degli stati per creare la struttura
            JSONArray celle = new JSONArray();
            for (int i = 0; i < mappa.num_x; i++) {
                for (int j = 0; j < mappa.num_y; j++) {
                    JSONObject cella = new JSONObject();
                    cella.put("x", i);
                    cella.put("y", j);
                    cella.put("stato", mappa.scene[i][j]);
                    //salvo solo l'intero dello stato che viene
                    //mappato internamente;
                    celle.add(cella);
                }
            }
            info.put("celle", celle);
            //salvo le informazioni in un file JSON della mappa
            Files.write(Paths.get(nome + "/InfoMappa.json"), info.toJSONString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(MapGeneratorLoader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public static MapGeneratorSceneModel read_mappa(File jsonFile) throws ParseException {
        //creo una nuova istanza di scena
        MapGeneratorSceneModel s = new MapGeneratorSceneModel();
        try {
            //converto il file in un oggetto JSON
            JSONObject json = convertStreamToJson(new FileInputStream(jsonFile));
            //leggo il numero di celle dalla radice del JSON
            int num_x = ((Long) json.get("cell_x")).intValue();
            int num_y = ((Long) json.get("cell_y")).intValue();
            //setto il numero di celle nella scena
            s.setNumCelle(num_x, num_y);
            //estraggo il JSONArray dalla radice
            JSONArray arrayCelle = (JSONArray) json.get("celle");
            for (Object cella : arrayCelle) {
                //ciclo su ogni cella e setto il valore della cella letta nella scena
                JSONObject cell = (JSONObject) cella;
                Long lx = (Long) cell.get("x");
                int x = (lx).intValue();
                s.setCella(x,
                        ((Long) cell.get("y")).intValue(),
                        ((Long) cell.get("stato")).toString());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapGeneratorLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return s;
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

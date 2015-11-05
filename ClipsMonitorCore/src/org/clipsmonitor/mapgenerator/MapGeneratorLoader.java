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
 * @author Alessandro Basile, Tobia Giani, Marco Corona
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
    
  

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.clipsmonitor.clips.ClipsConsole;

/**
 *
 * @author Marco Corona 
 * 
 * Questa classe viene utilizzata come supporto per la visualizzazione sia dell'ambiente 
 * sia come supporto alla creazione delle mappe mediante il generatore.
 * 
 */
public class MonitorImages {
    
    private static MonitorImages instance;

    private Map<String, BufferedImage> map_img;
    private Map<String,BufferedImage> colors;
    private ClipsConsole console;
 
    public int DEFAULT_IMG_SIZE;
    public int MAP_DIMENSION;
    

    /**
     * Private constructor (Singleton)
     */
    private MonitorImages(){}

    public static MonitorImages getInstance(){
        if(instance == null){
            instance = new MonitorImages();
            instance.init();
        }
        return instance;
    }
  
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    private void init(){
        console = ClipsConsole.getInstance();
        map_img = new HashMap<String, BufferedImage>();
        colors = new HashMap<String, BufferedImage>();
    }

    public Map<String, BufferedImage> getMapImg() {
        return map_img;
    }
    
    public Map<String, BufferedImage> getMapColor() {
        return colors;
    }

    public void ClearImg(){
        this.map_img=null;
        this.colors=null;
    }
    
    /*
        Carica le immagini per le mappe e il generatore di mappe 
    */
    
    public void loadImages(String path) {
        try {
            File img_dir = new File(path + File.separator + "img");

            File [] imgs = img_dir.listFiles();

            for(File img : imgs)
            {
                if(img.isFile()){ // escludo la directory dei colori
                    String file_name = img.getName(); // recupero il nome dell'immagine
                    int dot_position = file_name.lastIndexOf(".");  // calcolo la posizione del separatore
                    String img_name = file_name.substring(0,dot_position);
                    map_img.put(img_name, ImageIO.read(new File(path + File.separator + "img" + File.separator + file_name)));
                }
                
             }

        } catch (IOException e) {
            console.error("Load Icons error:");
            console.error(e);
        }
    }
    
    /*
        Load dei colori utilizzati dal generatore di mappe per tracciare il percorso fatto dalle persone
        eseguendo un overlap sulle celle
    */
    
    public void loadGenColors(String path) {
        try {
            String colorPath = path + File.separator + "img" + File.separator + "colors";
            File img_dir = new File(colorPath);

            File [] imgs = img_dir.listFiles();

            for(File img : imgs)
            {

                String file_name = img.getName(); // recupero il nome dell'immagine
                int dot_position = file_name.lastIndexOf(".");  // calcolo la posizione del separatore
                String img_name = file_name.substring(0,dot_position);
                colors.put(img_name, ImageIO.read(new File(colorPath + File.separator + file_name)));
            }

        } catch (IOException e) {
            console.error("Load color error:");
            console.error(e);
        }
    }
    
}

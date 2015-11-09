/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private String [] setKeyMap;
    private String [] setKeyColor;
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
    @SuppressWarnings("empty-statement")
    private void init(){
        console = ClipsConsole.getInstance();
        map_img = new HashMap<String, BufferedImage>();
        colors = new HashMap<String, BufferedImage>();
        setKeyColor = null;
        setKeyMap = null;
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
        this.setKeyColor= null;
        this.setKeyMap = null;
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
            
            Set<String> keys = map_img.keySet();
            setKeyMap= keys.toArray(new String[keys.size()]);
            String setMap = "(";
            for(int i=0; i<setKeyMap.length;i++){
                if(i<setKeyMap.length-1){
                     setMap += setKeyMap[i] + "," ;
                }
                else{
                    setMap += setKeyMap[i] ;
                }
            }
            setMap +=")";
            console.debug("Chiavi icona registrate :" + setMap);
            
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

            Set<String> colorKeys = colors.keySet();
            this.setKeyColor = colorKeys.toArray(new String[colorKeys.size()]);

            String setColor = "(";
            for(int i=0; i<setKeyColor.length;i++){
                if(i<setKeyColor.length-1){
                    setColor += setKeyColor[i] + "," ;
                }
                else{
                    setColor += setKeyColor[i] ;
                }
            }
            setColor +=")";
            console.debug("Chiavi colore registrate :" + setColor);
            
        } catch (IOException e) {
            console.error("Load color error:");
            console.error(e);
        }
    }
    
   public String [] getSetKeyMap(){
   
      return this.setKeyMap;
   }

   public String[] getSetKeyColor(){
   
       return this.setKeyColor;
   }

   
   /**
        * Restituisce l'immagine che è la sovrapposizione fra object e background.
        * La dimensione è quella dell'immagine più piccola
        *
        * @param object
        * @param background
        * @return
        */
        public BufferedImage overlapImages(BufferedImage object, BufferedImage background) {

           BufferedImage combined;
           Graphics g;
           // crea una nuova immagine, la dimensione è quella più grande tra le 2 img
           int w = Math.max(background.getWidth(), object.getWidth());
           int h = Math.max(background.getHeight(), object.getHeight());
           combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

           // SOVRAPPONE le immagini, preservando i canali alpha per le trasparenze (figo eh?)
           g = combined.getGraphics();
           g.drawImage(background, 0, 0, null);
           g.drawImage(object, 0, 0, null);

           return combined;
       }
   
   
}


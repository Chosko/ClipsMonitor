/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import org.clipsmonitor.clips.ClipsConsole;

/**
 *
 * @author Marco Corona 
 * 
 * 
 */
public class MonitorImages {
    
    private static MonitorImages instance;

    private Map<String, BufferedImage> map_img;
    private ClipsConsole console;
 
    public static final int DEFAULT_IMG_SIZE = 170;

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
    }
    
    public Map<String, BufferedImage> getMapImg(){
        return map_img;
    }
    
    /** 
     * Returns an image given its name 
     */
    public BufferedImage getImage(String name){
        BufferedImage img = map_img.get(name);
        
        // Image not found. If 'name' is a color name, create an image with that color and retreive it
        if(img == null){
            Color color = findColorByName(name);
            if(color != null){
                img = createColorImage(name, color);
            }
        }
        
        // Color image not found, put an error image
        if(img == null){
            img = map_img.get("error_image");
            ClipsConsole.getInstance().warn("Image \""+ name +"\" not found, applying a red image instead");
        }
        
        return img;
    }
    
    /**
     * Create a new Buffered image with the given color
     */
    private BufferedImage createColorImage(String name, Color color){
        java.awt.Color c = new java.awt.Color((float)color.getRed(), (float)color.getGreen(), (float)color.getBlue(), (float)color.getOpacity());
        BufferedImage img = new BufferedImage(DEFAULT_IMG_SIZE, DEFAULT_IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        int[] red = new int[DEFAULT_IMG_SIZE * DEFAULT_IMG_SIZE];
        Arrays.fill(red, c.getRGB());
        img.setRGB(0, 0, DEFAULT_IMG_SIZE, DEFAULT_IMG_SIZE, red, 0, 0);
        map_img.put(name, img);
        return img;
    }

    public void ClearImg(){
        this.map_img=null;
    }
    
    public void loadImages(String path) {
        try {
            createColorImage("error_image", new Color(1, 0, 0, 0.4f));
            
            File img_dir = new File(path + File.separator + "img");

            File [] imgs = img_dir.listFiles();

            for(File img : imgs)
            {

                String file_name = img.getName(); // recupero il nome dell'immagine
                int dot_position = file_name.lastIndexOf(".");  // calcolo la posizione del separatore
                String img_name = file_name.substring(0,dot_position);
                map_img.put(img_name, ImageIO.read(new File(path + File.separator + "img" + File.separator + file_name)));
            }            
        } catch (IOException e) {
            console.error(e);
        }
    }

    private Color findColorByName(String name) {
        try{
            return Color.web(name);
        }
        catch(IllegalArgumentException ex){
            return null;
        }
    }
}

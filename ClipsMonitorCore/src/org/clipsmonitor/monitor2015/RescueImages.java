/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorImages;

/**
 *
 * @author Marco Corona 
 */
public class RescueImages extends MonitorImages{
    
    private static RescueImages instance;
    private ClipsConsole console;
    
    public static RescueImages getInstance(){
        if(instance == null){
            instance = new RescueImages();
            instance.init();
        }
        return instance;
    }
    public int DEFAULT_IMG_SIZE;
    public int MAP_DIMENSION;
    
    /**
     * Private constructor (Singleton)
     */
    private RescueImages(){}
    
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    
    
    
    public Map<String, BufferedImage> getMapImg() {
        return map_img;
    }

    public Map<String, BufferedImage> getMapImgRobot() {
        return map_img_robot;
    }

    
    public void ClearImg(){
        this.ClearMapImage();
        this.ClearMapImageRobot();
    }

    @Override
    public void loadImages(String path) {
        try {
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
   
}

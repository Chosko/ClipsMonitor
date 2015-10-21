/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.monitor2015;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.clipsmonitor.clips.ClipsConsole;

/**
 *
 * @author theacid 
 */
public class RescueImages {
    
    private static RescueImages instance;
    private Map<String, BufferedImage> map_img;
    private Map<String, BufferedImage> map_img_robot;
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
    private void init(){
        
        console = ClipsConsole.getInstance();
        
        //Primo campo: coerente con i file di CLIPS
        //Secondo campo: nome del file (a piacere)
        map_img = new HashMap<String, BufferedImage>();

        try {
            map_img.put("wall", ImageIO.read(new File("img" + File.separator + "wall.png")));
            map_img.put("empty", ImageIO.read(new File("img" + File.separator + "empty.png")));
            map_img.put("gate", ImageIO.read(new File("img" + File.separator + "gate.png")));
            map_img.put("outdoor", ImageIO.read(new File("img" + File.separator + "outdoor.png")));
            map_img.put("debris", ImageIO.read(new File("img" + File.separator + "debris.png")));
            map_img.put("debris_injured", ImageIO.read(new File("img" + File.separator + "debris_injured.png")));
            map_img.put("informed", ImageIO.read(new File("img" + File.separator + "informed.png")));
            map_img.put("undiscovered", ImageIO.read(new File("img" + File.separator + "undiscovered.png")));
            map_img.put("agent_east_unloaded", ImageIO.read(new File("img" + File.separator + "agent_east_unloaded.png")));
            map_img.put("agent_west_unloaded", ImageIO.read(new File("img" + File.separator + "agent_west_unloaded.png")));
            map_img.put("agent_north_unloaded", ImageIO.read(new File("img" + File.separator + "agent_north_unloaded.png")));
            map_img.put("agent_south_unloaded", ImageIO.read(new File("img" + File.separator + "agent_south_unloaded.png")));
            map_img.put("agent_east_loaded", ImageIO.read(new File("img" + File.separator + "agent_east_loaded.png")));
            map_img.put("agent_west_loaded", ImageIO.read(new File("img" + File.separator + "agent_west_loaded.png")));
            map_img.put("agent_north_loaded", ImageIO.read(new File("img" + File.separator + "agent_north_loaded.png")));
            map_img.put("agent_south_loaded", ImageIO.read(new File("img" + File.separator + "agent_south_loaded.png")));
            map_img.put("person_rescuer", ImageIO.read(new File("img" + File.separator + "person_rescuer.png")));

        } catch (IOException e) {
            console.error(e);
        }
    
    }
    
    
    public Map<String, BufferedImage> getMapImg() {
        return map_img;
    }

    public Map<String, BufferedImage> getMapImgRobot() {
        return map_img_robot;
    }

    private void ClearMapImage(){
        this.map_img=null;
    }
    
    private void ClearMapImageRobot(){
        this.map_img_robot=null;
    }
    
    public void ClearImg(){
        this.ClearMapImage();
        this.ClearMapImageRobot();
    } 
   
}

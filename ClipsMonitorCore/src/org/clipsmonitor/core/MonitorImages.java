/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.clipsmonitor.clips.ClipsConsole;

/**
 *
 * @author Marco Corona 
 * 
 * 
 */
public abstract class MonitorImages {
    
    protected Map<String, BufferedImage> map_img;
    protected Map<String, BufferedImage> map_img_robot;
    protected ClipsConsole console;
   
 
    public int DEFAULT_IMG_SIZE;
    public int MAP_DIMENSION;
    

  
    protected void init(){
        
        console = ClipsConsole.getInstance();
        
        //Primo campo: coerente con i file di CLIPS
        //Secondo campo: nome del file (a piacere)
        map_img = new HashMap<String, BufferedImage>();
        map_img_robot = new HashMap<String, BufferedImage>();
    }

    
   protected abstract void loadImages(String path);

   
   protected void ClearMapImage(){
        this.map_img=null;
    }
    
    protected void ClearMapImageRobot(){
        this.map_img_robot=null;
    }
    
    public void ClearImg(){
        this.ClearMapImage();
        this.ClearMapImageRobot();
    } 

    
}

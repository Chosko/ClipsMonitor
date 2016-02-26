/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.io.File;
import java.io.IOException;
import org.clipsmonitor.clips.ClipsConsole;

/**
 * 
 * @author Marco Corona 
 */
public class ProjectDirectory {
    
    private File projectDirectory;
    private static ProjectDirectory istance;
    private ClipsConsole console;
    private String envSelected ;
    private String strategySelected;
    
    private ProjectDirectory(File directory){
        projectDirectory=directory;
        console = ClipsConsole.getInstance();
        envSelected = "";
        strategySelected="";
    }
    
    
    public static ProjectDirectory getIstance(File directory){
        if(istance==null){
            istance = new ProjectDirectory(directory);
        }
        return istance;
    }
    
    public static ProjectDirectory getInstance(){
      
      return istance;
    }
    
    public File getProjectDirectory(){
        
        return this.projectDirectory;
    }
    
    public String getDirectoryPath(){
    
      try {
         return this.projectDirectory.getCanonicalPath();
      } 
      catch (IOException ex) {
         console.error(ex.getLocalizedMessage());
         return "";
      }
    }
    
    public void setProjectDirectory(File dir){
        this.projectDirectory=dir;
    }
    
    public String getStrategy(){
      return strategySelected;
    }
    
    public String getEnv(){
      return envSelected;
    }
    
    public void setStrategy(String strategy){
      strategySelected= strategy;
    }
    
    public void setEnv(String env){
      envSelected = env;
    }
    
}

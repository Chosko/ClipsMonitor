/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.io.File;

/**
 *
 * @author Marco Corona 
 */
public class ProjectDirectory {
    
    private File projectDirectory;
    private static ProjectDirectory istance;
    
    
    private ProjectDirectory(File directory){
    
        this.projectDirectory=directory;
    }
    
    
    public static ProjectDirectory getIstance(File directory){
        if(istance==null){
            istance = new ProjectDirectory(directory);
        }
        return istance;
    }
    
    
    public File getProjectDirectory(){
        
        return this.projectDirectory;
    }
    
    
    public void setProjectDirectory(File dir){
        this.projectDirectory=dir;
    }
}

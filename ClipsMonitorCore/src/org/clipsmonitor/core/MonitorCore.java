package org.clipsmonitor.core;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ruben
 */
public class MonitorCore {
    private static MonitorCore instance;
    
    /**
     * Singleton
     */
    public static MonitorCore getInstance(){
        if(instance == null){
            instance = new MonitorCore();
        }
        return instance;
    }
    
    public void resetApplication(){
        MonitorConsole.getInstance().reset();
    }
}

package org.clipsmonitor.core;

import org.clipsmonitor.clips.ClipsConsole;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ruben
 */
public final class MonitorCore {
    private static MonitorCore instance;
    private ClipsConsole console;
    
    /**
     * Private constructor (Singleton)
     */
    private MonitorCore(){}
    
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    private void init(){
        console = ClipsConsole.getInstance();
        resetApplication();
    }
    
    /**
     * get the instance (Singleton)
     */
    public static MonitorCore getInstance(){
        if(instance == null){
            instance = new MonitorCore();
            instance.init();
        }
        return instance;
    }
    
    /**
     * Perform the operations to run at application startup or reset.
     */
    public void resetApplication(){
        ClipsConsole.getInstance().clear();
        ClipsConsole.getInstance().setActive(false);
        console.info("Application started");
    }
}

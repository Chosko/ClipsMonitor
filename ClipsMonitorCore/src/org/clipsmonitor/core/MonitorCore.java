package org.clipsmonitor.core;

import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsCore;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ruben Caliandro
 * 
 */
public final class MonitorCore {
    private static MonitorCore instance;
    private MonitorModel model;
    
    /**
     * Private constructor (Singleton)
     */
    private MonitorCore(){}
    
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    private void init(){
        startApplication();
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
     * Register the model used in this application
     */
    public void registerModel(MonitorModel model){
        this.model = model;
    }
    
    /**
     * Perform the operations to run at application startup or reset.
     */
    public void resetApplication(){
        ClipsCore.clearInstance();
        ClipsConsole.clearInstance();
        model.clear();
        startApplication();
        model.restart();
    }
    
    /**
     * Performs the operations needed when the agent has terminated the execution.
     */
    public void finished(){
        model.finished();
    }

    private void startApplication() {
        ClipsConsole.getInstance().info("Starting application");
        ClipsCore.getInstance();
    }
}

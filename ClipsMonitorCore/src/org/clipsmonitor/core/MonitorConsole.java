/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import org.clipsmonitor.clips.ClipsCore;

/**
 *
 * @author Ruben
 */
public class MonitorConsole extends Observable {
    
    private final int MAX_OUTPUT_LENGTH = 500;
    private static MonitorConsole instance;
    private LinkedList<String> output;
    private boolean active;
    
    // Log levels
    private boolean logDebug;
    private boolean logError;
    private boolean logWarn;
    private boolean logClips;
    private boolean logInfo;

    private MonitorConsole() {
        output = new LinkedList<String>();
        active = false;
        logDebug = true;
        logError = true;
        logClips = true;
        logWarn = true;
        logInfo = true;
    }
    
    /**
     * Singleton
     */
    public static MonitorConsole getInstance(){
        if(instance == null){
            instance = new MonitorConsole();
        }
        return instance;
    }
    
    public void clear(){
        if(output.size() > 0){
            output.clear();
            this.setChanged();
            this.notifyObservers("clear");
        }
    }

    public void reset() {
        this.clear();
        this.setActive(false);
        
    }
    
    public void setActive(boolean value){
        if(active != value){
            active = value;
            this.setChanged();
            this.notifyObservers(active ? "activated" : "deactivated");
        }
    }
    
    public boolean getActive(){
        return active;
    }
    
    public void setLogInfo(boolean value){
        logInfo = value;
    }
    
    public void setLogDebug(boolean value){
        logDebug = value;
    }
    
    public void setLogError(boolean value){
        logError = value;
    }
    
    public void setLogWarn(boolean value){
        logWarn = value;
    }
    
    public void setLogClips(boolean value){
        logClips = value;
    }

    public void debug(Object log) {
        if(logDebug){
            String logString = "[DEBUG] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("debug");
        }
    }

    public void error(Object log) {
        if(logError){
            String logString = "[ERROR] " + log;
            System.out.println(logString);
            this.append(logString);
            this.notifyObservers("error");
        }
    }

    public void warn(Object log) {
        if(logWarn){
            String logString = "[WARN] " + log;
            System.out.println(logString);
            this.append(logString);
            this.notifyObservers("warn");
        }
    }
    
    public void log(Object log){
        if(logInfo){
            String logString = "[INFO] " + log;
            System.out.println(logString);
            this.append(logString);
            this.notifyObservers("info");
        }
    }
    
    public void clips(Object log){
        if(logClips){
            String logString = "[CLIPS] " + log;
            System.out.println(logString);
            this.append(logString);
            this.notifyObservers("clips");
        }
    }
    
    // Internal debug: only System.out.println
    public void internal(Object log){
        System.out.println("[INTERNAL] " + log);
    }
    
    private void append(String log){
        if(output.size() >= MAX_OUTPUT_LENGTH){
            output.removeFirst();
        }
        output.addLast(log);
    }
    
    public String getLastOutput(){
        return output.getLast();
    }
    
    public String getFullOutput(){
        String text = "";
        for(String elem : output){
            text += elem + "\n";
        }
        return text;
    }
}

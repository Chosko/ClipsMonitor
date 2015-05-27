/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.clips;

import java.util.LinkedList;
import java.util.Observable;

/**
 * Implementation of a console for CLIPSJNI
 * 
 * There are 5 log levels: "clips", "info", "debug", "warn" and "error"
 * Every log level is implemented in the same way. For instance, the "debug" level
 * is implemented as follows:
 * 
 *      It can be enabled/disabled calling setLogDebug(true)/setLogDebug(false)
 *      You can log an object with "debug" level calling debug(obj). Then:
 *          - a label "[DEBUG]" is printed in stdout with obj.toString()
 *          - the same string is stored internally ready to be used
 * 
 * Arguments sent with notifyObserver(Object arg):
 * 
 *      "clear": console cleared
 *      "on": console activated
 *      "off": console deactivated
 *      "info on": info log level activated
 *      "info off": info log level deactivated
 *      ... same for all the other log levels (debug, error, warn, clips)
 *      "max changed": maxOutputLength has been changed
 *      "resize": output list resized
 *      "info", "debug", ...: log printed with specific level.
 *      "log": generic log printed
 * 
 * @author Ruben Caliandro
 */
public class ClipsConsole extends Observable {
    
    /**
     * The maximum length of the saved output
     */
    private int maxOutputLength = 500;
    
    /**
     * The instance of ClipsConsole (it's a singleton)
     */
    private static ClipsConsole instance;
    
    /**
     * The instance of ClipsCore
     */
    private ClipsCore clips;
    
    /**
     * A linked list to store the output
     */
    private LinkedList<String> output;
    
    /**
     * Activation status for the console
     */
    private boolean active;
    
    // Log levels
    private boolean logDebug;
    private boolean logError;
    private boolean logWarn;
    private boolean logClips;
    private boolean logInfo;
    
    /**
     * Private constructor (Singleton)
     */
    private ClipsConsole() {}
    
    /**
     * Initialize the instance. Used in a separate function to avoid infinite
     * recursion when initializing singleton classes
     */
    private void init(){
        clips = ClipsCore.getInstance();
        output = new LinkedList<String>();
        active = false;
        logDebug = true;
        logError = true;
        logClips = true;
        logWarn = true;
        logInfo = true;
        append(clips.getBanner());
        internal("ClipsConsole initialized");
    }
    
    /**
     * Get the instance (Singleton)
     */
    public static ClipsConsole getInstance(){
        if(instance == null){
            instance = new ClipsConsole();
            instance.init();
        }
        return instance;
    }
    
    /**
     * Clear the saved output
     */
    public void clear(){
        if(output.size() > 0){
            output.clear();
            append(clips.getBanner());
            this.setChanged();
            this.notifyObservers("clear");
            internal("ClipsConsole cleared");
        }
    }
    
    /**
     * Set the console as active or not active
     * @param value  the new value
     */
    public final void setActive(boolean value){
        if(active != value){
            active = value;
            this.setChanged();
            String act = active ? "on" : "off";
            this.notifyObservers(act);
            internal("ClipsConsole " + act);
        }
    }
    
    /**
     * @return true if the console is active, false otherwise
     */
    public boolean getActive(){
        return active;
    }
    
    /**
     * Activate or deactivate the [INFO] log level
     */
    public final void setLogInfo(boolean value){
        if(logInfo != value){
            logInfo = value;
            this.setChanged();
            String v = value ? "on" : "off";
            this.notifyObservers("info " + v);
            internal("ClipsConsole info log level " + v);
        }
    }
    
    /**
     * Get wether or not the [INFO] log level is active
     * @return true if the [INFO] log level is active
     */
    public boolean getLogInfo(){
        return logInfo;
    }
    
    /**
     * Activate or deactivate the [DEBUG] log level
     */
    public final void setLogDebug(boolean value){
        if(logDebug != value){
            logDebug = value;
            this.setChanged();
            String v = value ? "on" : "off";
            this.notifyObservers("debug " + v);
            internal("ClipsConsole debug log level " + v);
        }
    }
    
    /**
     * Get wether or not the [DEBUG] log level is active
     * @return true if the [DEBUG] log level is active
     */
    public boolean getLogDebug(){
        return logDebug;
    }
        
    /**
     * Activate or deactivate the [ERROR] log level
     */
    public final void setLogError(boolean value){
        if(logError != value){
            logError = value;
            this.setChanged();
            String v = value ? "on" : "off";
            this.notifyObservers("error " + v);
            internal("ClipsConsole error log level " + v);
        }
    }
    
    /**
     * Get wether or not the [ERROR] log level is active
     * @return true if the [ERROR] log level is active
     */
    public boolean getLogError(){
        return logError;
    }
    
    /**
     * Activate or deactivate the [WARN] log level
     */
    public final void setLogWarn(boolean value){
        if(logWarn != value){
            logWarn = value;
            this.setChanged();
            String v = value ? "on" : "off";
            this.notifyObservers("warn " + v);
            internal("ClipsConsole warn log level " + v);
        }
    }
    
    /**
     * Get wether or not the [WARN] log level is active
     * @return true if the [WARN] log level is active
     */
    public boolean getLogWarn(){
        return logWarn;
    }
    
    /**
     * Activate or deactivate the [CLIPS] log level
     */
    public final void setLogClips(boolean value){
        if(logClips != value){
            logClips = value;
            this.setChanged();
            String v = value ? "on" : "off";
            this.notifyObservers("clips " + v);
            internal("ClipsConsole clips log level " + v);
        }
    }
    
    /**
     * Get wether or not the [CLIPS] log level is active
     * @return true if the [CLIPS] log level is active
     */
    public boolean getLogClips(){
        return logClips;
    }
    
    /**
     * Set the max output length for this console.
     * Once the length is reached, the oldest lines are deleted.
     * 
     * @param value the new max output length value. The value must be > 0
     */
    public void setMaxOutputLength(int value){
        if(value > 0 && value != maxOutputLength){
            maxOutputLength = value;
            this.setChanged();
            this.notifyObservers("max changed");
            internal("ClipsConsole max output set to " + value);
            resizeOutput();
        }
    }
    
    /**
     * Get the maximum number of output lines stored
     * @return the maximum number of output lines stored
     */
    public int getMaxOutputLength(){
        return maxOutputLength;
    }
    
    /**
     * Print and store a generic log, without any log level
     * associated (and then without any label).
     * 
     * @param log the object to log.
     */
    public void log(Object log) {
        String logString = log.toString();
        System.out.println(logString);
        this.append(logString);
        this.setChanged();
        this.notifyObservers("log");
    }
    
    /**
     * Print and store a debug level log, prepending the label [DEBUG]
     * 
     * @param log The object to log
     */
    public void debug(Object log) {
        if(logDebug){
            String logString = "[DEBUG] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("debug");
        }
    }

    /**
     * Print and store an error level log, prepending the label [ERROR]
     * 
     * @param log The object to log
     */
    public void error(Object log) {
        if(logError){
            String logString = "[ERROR] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("error");
        }
    }

    /**
     * Print and store a warn level log, prepending the label [WARN]
     * 
     * @param log The object to log
     */
    public void warn(Object log) {
        if(logWarn){
            String logString = "[WARN] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("warn");
        }
    }
    
    /**
     * Print and store an info level log, prepending the label [INFO]
     * 
     * @param log The object to log
     */
    public void info(Object log){
        if(logInfo){
            String logString = "[INFO] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("info");
        }
    }
    
    /**
     * Print and store a clips level log, prepending the label [CLIPS]
     * 
     * @param log The object to log
     */
    public void clips(Object log){
        if(logClips){
            String logString = "[CLIPS] " + log;
            System.out.println(logString);
            this.append(logString);
            this.setChanged();
            this.notifyObservers("clips");
        }
    }
    
    /**
     * Used for package logging only. Print the object only on stdout, using
     * the label [INTERNAL]
     * 
     * @param log The object to log
     */
    final void internal(Object log){
        System.out.println("[INTERNAL] " + log);
    }
    
    /**
     * Append the string to the output list
     * 
     * @param log The string to append
     */
    private void append(String log){
        if(output.size() >= maxOutputLength){
            output.removeFirst();
        }
        output.addLast(log);
    }
    
    /**
     * Get the last saved log
     * 
     * @return the last output saved
     */
    public String getLastOutput(){
        return output.getLast();
    }
    
    /**
     * Get the full output saved so far.
     *
     * @return the full output
     */
    public String getFullOutput(){
        String text = "";
        for(String elem : output){
            text += elem + "\n";
        }
        return text;
    }
    
    /**
     * Return the CLIPS> prompt
     */
    public String getPrompt(){
        return clips.getPrompt();
    }
    
    /**
     * Resize the output list to match maxOutputLength
     */
    private void resizeOutput() {
        int i = 0;
        int exceeding = output.size() - maxOutputLength;
        for (; i < exceeding; i++){
            output.removeFirst();
        }
        if(i > 0){
            this.setChanged();
            this.notifyObservers("resize");
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.core;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Ruben
 */
public class MonitorConsole extends Observable implements Observer{
    
    private final int MAX_OUTPUT_LENGTH = 500;
    private static MonitorConsole instance;
    private LinkedList<String> output;
    private boolean active;

    private MonitorConsole() {
        output = new LinkedList<String>();
        active = false;
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
    
    public void log(Object obj){
        output.addLast(obj.toString());
        this.notifyObservers();
    }
    
    public void clear(){
        if(output.size() > 0){
            output.clear();
            this.notifyObservers();
        }
    }

    public void reset() {
        this.clear();
        this.setActive(false);
        
    }
    
    public void setActive(boolean value){
        if(active != value){
            active = value;
            this.notifyObservers();
        }
    }
    
    public boolean getActive(){
        return active;
    }

    @Override
    public void update(Observable o, Object arg) {
        this.log(arg);
    }

    public void debug(String system_Esecuzione_del_primo_passo_al_fine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void error(String error_Linizializzazione_è_fallita) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

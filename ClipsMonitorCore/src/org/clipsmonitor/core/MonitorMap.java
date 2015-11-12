package org.clipsmonitor.core;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;

/**
 * Questa classe astratta è la parte di view (in un'architettura MVC) e
 * implementa Observer, per osservare il model (i.e. ClipsModel). Le
 * implementazioni di questa classe dovranno implementare i metodi per il
 * mantenimento dell'interfaccia grafica specifica per ogni progetto.
 *
 * @author Piovesan Luca, Verdoja Francesco Edited by: @author Violanti Luca,
 * Varesano Marco, Busso Marco, Cotrino Roberto, Ruben Caliandro
 */
public abstract class MonitorMap extends Observable implements Observer {
    public static final int MAP_DIMENSION = 550;
    protected String[][] map;
    
    @Override
    /**
     * Questo metodo viene invocato ogni volta che è necessario aggiornare
     * l'interfaccia a seguito di modifiche nell'ambiente. Dentro di sè, a
     * seconda della fase di run in cui si trova il model, chiama un metodo
     * corrispondente che implementazioni di questa classe devono implementare.
     *
     */
    public void update(Observable o, Object arg) {
        
        String advice = (String) arg;
        if (advice.equals("setupDone")) {
            onSetup();
        } else if (advice.equals("disposeDone")) {
            onDispose();
        }
        else if(arg == "clearApp"){
            this.clear();
        }
    }
    
    protected void onSetup() {
        this.setChanged();
        this.notifyObservers("initializeMap");
        ClipsConsole.getInstance().info("Setup completato");
    }

    protected void debugMap(){
        for (int i=0; i<map.length; i++) {
            for (int j=0; j<map[i].length; j++) {
                ClipsConsole.getInstance().debug("k-cell " + (i+1) + " " + (j+1) + ": " + map[i][j]);
            }
        }
    }
    
    /**
     * In questo metodo devono essere inseriti gli aggiornamenti
     * dell'interfaccia da svolgersi quando è finita la fase di dispose del
     * model, verrà invocato una volta sola.
     *
     */
    protected abstract void onDispose();
    
    protected abstract void clear();
    
    
    protected abstract void init();
    
    public abstract void initMap() throws CLIPSError;
    
    public abstract void updateMap() throws CLIPSError;
    
    public abstract int[] getSize();
    
    /*
       Questo metodo permette la realizzazione della matrice di icone con cui riempire
       la mappa a seconda dei valori contenuti dalla mappa stessa.
       Viene invocato ad ogni richiesta di repaint della mappa 
    */
    
    public abstract BufferedImage[][] getIconMatrix();
    
    
}

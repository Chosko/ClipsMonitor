package org.clipsmonitor.core;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

/**
 * Questa classe astratta è la parte di view (in un'architettura MVC) e
 * implementa Observer, per osservare il model (i.e. ClipsModel). Le
 * implementazioni di questa classe dovranno implementare i metodi per il
 * mantenimento dell'interfaccia grafica specifica per ogni progetto.
 *
 * @author Piovesan Luca, Verdoja Francesco Edited by: @author Violanti Luca,
 * Varesano Marco, Busso Marco, Cotrino Roberto
 */
public abstract class MonitorMap extends Observable implements Observer {
    public final int MAP_DIMENSION = 550;
    public final int DEFAULT_IMG_SIZE = 85;
    
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
        } else if (advice.equals("actionDone")) {
            onAction();
        } else if (advice.equals("disposeDone")) {
            onDispose();
        }
        else if(arg == "clearApp"){
            this.clear();
        }
    }
    
    
      

    /**
     * In questo metodo devono essere inseriti gli aggiornamenti
     * dell'interfaccia da svolgersi quando è finita la fase di setup del model,
     * verrà invocato una volta sola.
     *
     */
    protected abstract void onSetup();

    /**
     * In questo metodo devono essere inseriti gli aggiornamenti
     * dell'interfaccia da svolgersi quando è finita una delle fasi di action
     * del model, verrà quindi invocato più volte, finchè l'ambiente non ha
     * finito.
     *
     */
    protected abstract void onAction();

    /**
     * In questo metodo devono essere inseriti gli aggiornamenti
     * dell'interfaccia da svolgersi quando è finita la fase di dispose del
     * model, verrà invocato una volta sola.
     *
     */
    protected abstract void onDispose();
    
    protected abstract void clear();
    
    
    protected abstract void init();
    
    public abstract int[] getSize();
    
    /*
       Questo metodo permette la realizzazione della matrice di icone con cui riempire
       la mappa a seconda dei valori contenuti dalla mappa stessa.
       Viene invocato ad ogni richiesta di repaint della mappa 
    */
    
    public abstract BufferedImage[][] getIconMatrix();
    
    
}

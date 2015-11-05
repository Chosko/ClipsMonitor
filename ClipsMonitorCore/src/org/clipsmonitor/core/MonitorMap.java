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
        * Restituisce l'immagine che è la sovrapposizione fra object e background.
        * La dimensione è quella dell'immagine più piccola
        *
        * @param object
        * @param background
        * @return
        */
       
        public BufferedImage overlapImages(BufferedImage object, BufferedImage background) {
           BufferedImage combined;
           Graphics g;
           // crea una nuova immagine, la dimensione è quella più grande tra le 2 img
           int w = Math.max(background.getWidth(), object.getWidth());
           int h = Math.max(background.getHeight(), object.getHeight());
           combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

           // SOVRAPPONE le immagini, preservando i canali alpha per le trasparenze (figo eh?)
           g = combined.getGraphics();
           g.drawImage(background, 0, 0, null);
           g.drawImage(object, 0, 0, null);

           return combined;
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
    
    /*
       Questo metodo permette la realizzazione della matrice di icone con cui riempire
       la mappa a seconda dei valori contenuti dalla mappa stessa.
       Viene invocato ad ogni richiesta di repaint della mappa 
    */
    
    protected abstract BufferedImage[][] makeIconMatrix(String[][] mapString);
    
    
}

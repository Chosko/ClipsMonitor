package org.clipsmonitor.core;

import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;
import net.sf.clipsrules.jni.CLIPSError;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.clips.ClipsCore;
import org.clipsmonitor.monitor2015.RescueModel;

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
    private String[][] oldMap;
    private BufferedImage[][] iconMatrix;
    protected RescueModel model;
    protected MonitorImages images;
    protected ClipsConsole console;
    protected ClipsCore core;
    
    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public MonitorMap() {
        init();
    }

    protected final void init(){
        model = RescueModel.getInstance();
        model.addObserver(this);
        console = ClipsConsole.getInstance();
        images = MonitorImages.getInstance();
        core = ClipsCore.getInstance();
    }
    
    protected final void onSetup() {
        this.setChanged();
        this.notifyObservers("initializeMap");
        ClipsConsole.getInstance().info("Setup completato");
    }

    protected final void debugMap(String prefix){
        for (int i=0; i<map.length; i++) {
            for (int j=0; j<map[i].length; j++) {
                ClipsConsole.getInstance().debug(prefix + " " + (i+1) + " " + (j+1) + ": " + map[i][j]);
            }
        }
    }
    
    /**
     * Update the map
     * @throws CLIPSError 
     */
    public final void updateMap() throws CLIPSError {
        updateOldMap();
        refreshMap();
        updateIconMatrix();
    };
    
    /**
     * Returns the Matrix of icons
     * @return 
     */
    public final BufferedImage[][] getIconMatrix(){
        return iconMatrix;
    }
    
    /**
     * Initialize the map
     * @throws CLIPSError 
     */
    public final void initMap() throws CLIPSError{
        initializeMap();
        int rows = map.length;
        int columns = map[0].length;
        oldMap = new String[rows][columns];
        iconMatrix = new BufferedImage[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                oldMap[i][j] = "initial";
            }
        }
        updateIconMatrix();
    };
    
    public final int[] getSize() {
        int[] size = null;
        if(map == null || map.length == 0 || map[0].length == 0){
            return null;
        }
        else{
            size = new int[2];
            size[0] = map.length;
            size[1] = map[0].length;
        }
        return size;
    }
    
    /**
     * Clear the map instance
     */
    private void clear(){
        this.console = null;
        this.model = null;
        this.images = null;
        this.core = null;
    }
    
    /**
     * Parse the map object and update the icons of the changed cells
     */
    private void updateIconMatrix(){
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if(!map[i][j].equals(oldMap[i][j])){
                    BufferedImage tmpImage;
                    
                    // Split the map string in arguments
                    String[] curCel = map[i][j].split("\\+");

                    // Background image is the first argument
                    iconMatrix[i][j] = images.getImage(curCel[0]);

                    // All the others arguments are overlaps
                    for (int k = 1; k < curCel.length; k++) {
                        String curOverlap = curCel[k];

                        tmpImage = images.getImage(curOverlap);
                        iconMatrix[i][j] = images.overlapImages(tmpImage, iconMatrix[i][j]);
                    }
                }
            }
        }
    }
    
    
    /**
     * Update the old map instance, used to check which cells changed
     */
    private void updateOldMap(){
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                oldMap[i][j] = map[i][j];
            }
        }
    }
    
    
    /**
     * Invoked when the simulation has finished
     */
    protected abstract void onDispose();
    
    /**
     * Overridable part of initialization of the map
     * @throws CLIPSError 
     */
    protected abstract void initializeMap() throws CLIPSError;
    
    /**
     * Overridable part of the update of the map
     * @throws CLIPSError 
     */
    protected abstract void refreshMap() throws CLIPSError;
    
    /**
     * Implementa update di Observable.
     */
    @Override
    public final void update(Observable o, Object arg) {
        String advice = (String) arg;
        
        // Setup done
        if (advice.equals("setupDone")) {
            onSetup();
        }
        
        // Dispose done
        else if (advice.equals("disposeDone")) {
            onDispose();
        }
        
        // App cleared
        else if(arg.equals("clearApp")){
            this.clear();
        }
        
        // Action done
        else if(arg.equals("actionDone")){
            this.setChanged();
            this.notifyObservers("repaint");
        }
    }
}

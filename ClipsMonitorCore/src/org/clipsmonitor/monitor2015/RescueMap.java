package org.clipsmonitor.monitor2015;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Observer;
import org.clipsmonitor.clips.ClipsConsole;
import org.clipsmonitor.core.MonitorMap;

/**
 * L'implementazione della classe ClipsView specifica per il progetto Monitor
 * 2012/2013
 *
 * @author Violanti Luca, Varesano Marco, Busso Marco, Cotrino Roberto Edited
 * by: Enrico Mensa, Matteo Madeddu, Davide Dell'Anna
 */
public class RescueMap extends MonitorMap implements Observer {

    private RescueModel model;
    private ClipsConsole console;
    //private JLabel[][] map;
    public final int MAP_DIMENSION = 550;
    public final int DEFAULT_IMG_SIZE = 85;
    
    // HashMap che attribuisce ad ogni tipo di cella un codice univoco.
    // L'attribuzione è effettuata nel costruttore.
    private Dimension dim;

    /**
     * È il costruttore da chiamare nel main per avviare l'intero sistema, apre
     * una nuova finestra con il controller, pronto per caricare il file .clp
     *
     */
    public RescueMap() {
        init();
    }

    @Override
    protected void onSetup() {
        this.setChanged();
        this.notifyObservers("initializeMap");
        console.info("Setup completato");
    }

    @Override
    protected void onAction() {
        //System.out.println("actionDone");
        try {
            updateMap();
            // updateOutput();
        } catch (IOException ex) {
            console.error(ex);
        }
        console.info("Azione effettuata.");
    }

    @Override
    protected void onDispose() {
        console.debug("Dispose effettuato");
        String result = model.getResult();
        int score = model.getScore();
        @SuppressWarnings("UnusedAssignment")
        String advise = "";
        if (result.equals("disaster")) {
            advise = "DISASTRO\n";
        } else if (model.getTime() == model.getMaxDuration()) {
            advise = "Maxduration has been reached.\n";
        } else {
            advise = "The agent says DONE.\n";
        }
        advise = advise + "Penalties: " + score;
        model.setAdvise(advise);
        this.setChanged();
        this.notifyObservers("advise");
    }

    /**
     * Aggiorna la mappa visualizzata nell'interfaccia per farla allineare alla
     * versione nel modello.
     *
     */
    private void updateMap() throws IOException {
//        Integer step = model.getStep();
//        Integer time = model.getTime();
//        Integer leftTime = model.getMaxDuration() - model.getTime();
//        cp_frame.getTimeTextField().setText(time.toString());
//        cp_frame.getLeftTimeTextField().setText(leftTime.toString());
//        cp_frame.getStepTextField().setText(step.toString());
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//        }
        this.setChanged();
        this.notifyObservers("repaint");
        console.info("Step attuale: " + model.getStep());
    }

//    private BufferedImage imageIcon2Buffered(ImageIcon tempicon) {
//        BufferedImage bi = new BufferedImage(
//                tempicon.getIconWidth(),
//                tempicon.getIconHeight(),
//                BufferedImage.TYPE_INT_RGB);
//        Graphics g = bi.createGraphics();
//        // paint the Icon to the BufferedImage.
//        tempicon.paintIcon(null, g, 0, 0);
//        g.dispose();
//        return bi;
//    }

    // protected void updateOutput() {
    //     //########################### AGGIORNO LA FINESTRA DEI MESSAGGI DI OUTPUT ############################
    //     String[] slots = {"time", "step", "source", "verbosity", "text", "param1", "param2", "param3", "param4", "param5"};
    //     try {
    //         /**
    //          * Ogni fatto viene considerato nella forma: [source] testo (con
    //          * parametri corretti).
    //          *
    //          * È necessario compiere alcune operazioni di processing poiché: 1 -
    //          * le virgolette fanno parte della stringa. 2 - i parametri devono
    //          * essere sostituiti.
    //          */
    //         String[][] matriceFatti = model.findAllFacts("printGUI", "TRUE", slots);

    //         for (String[] fatto : matriceFatti) {
    //             int fact_verbosity = Integer.parseInt(fatto[3]); //Consideriamo la verbosità
    //             String source = removeFistAndLastChar(fatto[2]);
    //             String line = fatto[1] + "\t" + source + "\t" + removeFistAndLastChar(fatto[4]); //prendiamo il testo così com'è
    //             //E applichiamo le sostituzioni, appendendo il risultato alla finestra
    //             String parameters[] = {fatto[5], fatto[6], fatto[7], fatto[8], fatto[9]};
    //             console.clips(source + mapParameters(line, parameters));
    //         }

    //     } catch (Exception ex) {
    //         console.error(ex);
    //     }
    // }

    /**
     * Sostiuisce i parametri nella forma %par<i> con param<i>.
     *
     * @param text
     * @param parameters
     * @return
     */
    protected String mapParameters(String text, String[] parameters) {
        for (int i = 1; i <= parameters.length; i++) {
            text = text.replace("%p" + i, parameters[i - 1]);
        }
        return text;
    }

    /**
     * Rimuove il primo e l'ultimo carattere di una stringa. Nel nostro caso le
     * virgolette di un text.
     *
     * @return
     */
    protected String removeFistAndLastChar(String text) {
        return text.substring(1).replace(text.substring(text.length() - 1), "");
    }

    public String[][] getMap() {
        return model.getEnvMap();
    }

    

    @Override
    protected void clear() {
        this.console = null;
        this.dim = null;
        RescueImages.getInstance().ClearImg();
        this.model = null;
    }

    @Override
    protected void init() {

        
        model = RescueModel.getInstance();
        model.addObserver(this);
    }
}

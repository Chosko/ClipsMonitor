/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clipsmonitor.clips;

import net.sf.clipsrules.jni.Router;

/**
 *
 * @author piovel Edited by: @author Violanti Luca, Varesano Marco, Busso Marco,
 * Cotrino Roberto, Ruben Caliandro , Marco Corona
 */
class RouterDialog extends Router {

    private String stdout;
    private boolean rec;

    public RouterDialog(String name) {
        super(name, 100);
        stdout = "";
        rec = false;
    }

    /**
     * *******
     */
    /* query: */
    /**
     * *******
     */
    @Override
    public synchronized boolean query(String routerName) {
        return routerName.equals("wdisplay") || routerName.equals("wclips") || routerName.equals("werror");
    }
    
    /**
     * *******
     */
    /* print: */
    /**
     * *******
     */
    @Override
    public synchronized void print(String routerName, String printString) {
        if (rec) {
            if(
              routerName.equals("stdout") ||
              routerName.equals("stdin") ||
              routerName.equals("wwarning") ||
              routerName.equals("werror") ||
              routerName.equals("wtrace") ||
              routerName.equals("wdialog") ||
              routerName.equals("wclips") ||
              routerName.equals("wdisplay")
                ){
                stdout = stdout + printString;
            }
        }
    }

    public synchronized String getStdout() {
        return stdout;
    }

    public synchronized void startRec() {
        stdout = "";
        rec = true;
    }

    public synchronized void stopRec() {
        rec = false;
    }
}

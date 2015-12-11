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
      
        boolean wstdout = routerName.equals("stdout");
        boolean wstdin =  routerName.equals("stdin");
        boolean wwarning = routerName.equals("wwarning");
        boolean werror = routerName.equals("werror");
        boolean wtrace = routerName.equals("wtrace");
        boolean wdialog = routerName.equals("wdialog");
        boolean wclips = routerName.equals("wclips");
        boolean wdisplay = routerName.equals("wdisplay");
                
        return  wstdout|| wstdin || wwarning || werror
                || wtrace || wdialog || wclips || wdisplay;
                
        
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
            boolean wstdout = routerName.equals("stdout");
            boolean wstdin =  routerName.equals("stdin");
            boolean wwarning = routerName.equals("wwarning");
            boolean werror = routerName.equals("werror");
            boolean wtrace = routerName.equals("wtrace");
            boolean wdialog = routerName.equals("wdialog");
            boolean wclips = routerName.equals("wclips");
            boolean wdisplay = routerName.equals("wdisplay");

            if(
              wstdout|| wstdin || wwarning || werror
                || wtrace || wdialog || wclips || wdisplay
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

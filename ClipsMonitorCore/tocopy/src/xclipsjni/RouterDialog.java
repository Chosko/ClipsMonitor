package xclipsjni;

import CLIPSJNI.Router;

/**
 *
 * @author piovel Edited by: @author Violanti Luca, Varesano Marco, Busso Marco,
 * Cotrino Roberto
 */
class RouterDialog extends Router {

    private String stdout;
    private boolean rec;

    public RouterDialog(String name) {
        super(name, 100);
        stdout = "";
        rec = false;
    }

    @Override
    public synchronized boolean query(String routerName) {
        return routerName.equals("wdisplay");
    }

    @Override
    public synchronized void print(String routerName, String printString) {
        if (rec) {
            stdout = stdout + printString;
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

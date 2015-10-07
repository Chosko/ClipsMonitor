package xclipsjni;

import java.util.Observable;
import java.util.Observer;
import monitor.DebugFrame;

/**
 * Questa classe astratta è la parte di view (in un'architettura MVC) e
 * implementa Observer, per osservare il model (i.e. ClipsModel). Le
 * implementazioni di questa classe dovranno implementare i metodi per il
 * mantenimento dell'interfaccia grafica specifica per ogni progetto.
 *
 * @author Piovesan Luca, Verdoja Francesco Edited by: @author Violanti Luca,
 * Varesano Marco, Busso Marco, Cotrino Roberto
 */
public abstract class ClipsView implements Observer {

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
        switch (advice) {
            case "setupDone":
                onSetup();
                break;
            case "actionDone":
                onAction();
                break;
            case "disposeDone":
                onDispose();
                break;
            case "resetCore":
                reset();
                break;
            case "updateOutput":
                updateOutput();
                break;
            default:
                DebugFrame.appendText("ECCEZIONE: " + advice);
                break;
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

    /**
     * Questo metodo crea il modulo del pannello di controllo di clips. Da
     * questo pannello sarà possibile caricare il file .clp, lanciare la run e
     * visualizzare agenda e fatti.
     *
     * @param model il modello che dovrà essere controllato dal pannello
     *
     * @return un JPanel contenente il pannello di controllo
     */
    public ControlPanel createControlPanel(ClipsModel model) {
        ControlPanel frame = new ControlPanel(model);
        return frame;
    }

    /**
     * Permette di implementare il tasto reset riavviando solamente la parte
     * necessaria della gui
     *
     */
    protected abstract void reset();

    /**
     * Permette di aggiornare la finestra contenente tutti gli output
     */
    protected abstract void updateOutput();
}

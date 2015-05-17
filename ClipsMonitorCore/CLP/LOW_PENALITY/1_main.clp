; Questo programma contiene il simulatore dell'agente robotico per applicazione NG-CAFE'
;
;
;  Si noti che la parte di funzionamento dell'agente è separata
;  dal particolare problema da risolvere.
;
;  Infatti la definizione del problema in termini di
;         mappa inziale (descritta con istanzazioni di prior_cell)
;         durata massima (maxduration)
;         stato iniziale dell'agente (in termini di initial_agentstatus)
; deve essere contenuta nel file InitMap.txt
;
;  la descrizione di quali eventi avvengono durante l'esecuzione è
;  contenuta nel file history.txt. Questo file conteine anche le informazioni
;  per specificare quali sono i cleinti e quali attività svolgono
;______________________________________________________________________________________________________________________

;// MAIN

;// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯



(defmodule MAIN (export ?ALL))
;// DEFTEMPLATE
(deftemplate exec
  (slot step)   ;// l'environment incrementa il passo
  (slot action  (allowed-values Forward Turnright Turnleft Wait
                                LoadDrink LoadFood DeliveryFood DeliveryDrink
                                CleanTable EmptyFood Release CheckFinish Inform)
                  )
  (slot param1)
  (slot param2)
  (slot param3)
)

(deftemplate msg-to-agent
           (slot request-time)
           (slot step)
           (slot sender)
           (slot type (allowed-values order finish))
           (slot  drink-order)
           (slot food-order))

(deftemplate status (slot step) (slot time) (slot result))      ;//struttura interna

(deftemplate perc-vision        ;// la percezione di visione avviene dopo ogni azione, fornisce informazioni sullo stato del sistema

        (slot step)
    (slot time)
        (slot pos-r)    ;// informazioni sulla posizione del robot (riga)
        (slot pos-c)    ;// (colonna)
        (slot direction)        ;// orientamento del robot
        ;// percezioni sulle celle adiacenti al robot: (il robot é nella 5 e punta sempre verso la 2):


    (slot perc1  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc2  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc3  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc4  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc5  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc6  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc7  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc8  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
    (slot perc9  (allowed-values  Wall Person  Empty Parking Table Seat TrashBasket
                                                      RecyclableBasket DrinkDispenser FoodDispenser))
)

(deftemplate perc-bump          ;// percezione di urto contro persone o ostacoli
    (slot step)
    (slot time)
    (slot pos-r)        ;// la posizione in cui si trova (la stessa in cui era prima dell'urto)
    (slot pos-c)
    (slot direction)
    (slot bump (allowed-values no yes)) ;//restituisce yes se sbatte
)

(deftemplate perc-load
    (slot step)
    (slot time)
    (slot load  (allowed-values yes no))
)

(deftemplate perc-finish
    (slot step)
    (slot time)
    (slot finish (allowed-values no yes))
)

(deftemplate Table (slot table-id) (slot pos-r) (slot pos-c))
(deftemplate TrashBasket (slot TB-id) (slot pos-r) (slot pos-c))
(deftemplate RecyclableBasket (slot  RB-id) (slot pos-r) (slot pos-c))
(deftemplate FoodDispenser  (slot FD-id) (slot pos-r) (slot pos-c))
(deftemplate DrinkDispenser (slot DD-id) (slot pos-r) (slot pos-c))
(deftemplate initial_agentposition (slot pos-r)  (slot pos-c) (slot direction))

(deftemplate prior-cell  (slot pos-r) (slot pos-c)
                         (slot contains (allowed-values Zero Wall Person Empty Parking Table Seat TB RB DD FD)))
(deftemplate counter-non-replane (slot count))
(deftemplate counter-order-performed (slot count))
(deffacts init
        (create)
        (counter-non-replane (count 0))
        (counter-order-performed (count 0))
)


;; regola per inizializzazione
;; legge anche initial map (prior cell), initial agent status e durata simulazione (in numero di passi)
(defrule createworld
  ?f <- (create)
=>
  (load-facts "InitMap.txt")
  (assert (create-map)
  (create-initial-setting)
  (create-history))
  (assert (stop-at-step 256))
  (retract ?f)
  (focus ENV)
)

;// SI PASSA AL MODULO AGENT SE NON  E' ESAURITO IL TEMPO (indicato da maxduration)
(defrule go-on-agent
  (declare (salience 20))
  (maxduration ?d)
  (status (step ?t&:(< ?t ?d))) ;// controllo sul tempo
=>
  ;     (printout t crlf)
  (focus AGENT) ;// passa il focus all'agente, che dopo un'azione lo ripassa al main.
)



;// SI PASSA AL MODULO ENV DOPO CHE AGENTE HA DECISO AZIONE DA FARE

(defrule go-on-env
        (declare (salience 21))
    ?f1<-       (status (step ?t))
        (exec (step ?t))        ;// azione da eseguire al al passo T, viene simulata dall'environment
=>
;       (printout t crlf)
        (focus ENV)
)



;// quando finisce il tempo l'esecuzione si interrompe e vengono stampate le penalitá

(defrule game-over
        (declare (salience 10))
        (maxduration ?d)
        (status (step ?d))
        (penalty ?p)
        (counter-non-replane (count ?nr))
        (counter-order-performed (count ?nc))
=>
        (printout t crlf " TIME OVER - Penalita' accumulate: " ?p crlf crlf)
        (printout t crlf " TIME OVER - Number of not replane: " ?nr crlf crlf)
        (printout t crlf " TIME OVER - Number of order performed: " ?nc crlf crlf)

        (halt)
)

; Per le stampe (interpretabili da CLIPSJNI)
(deftemplate printGUI
  (slot time (default ?NONE))
  (slot step (default ?NONE))
  (slot source (type STRING) (default ?NONE))
  (slot verbosity (type INTEGER) (allowed-integers 0 1 2) (default 0))  ;Tre livelli di verbosità
  (slot text (type STRING))
  (slot param1 (default ""))
  (slot param2 (default ""))
  (slot param3 (default ""))
  (slot param4 (default ""))
  (slot param5 (default ""))
)

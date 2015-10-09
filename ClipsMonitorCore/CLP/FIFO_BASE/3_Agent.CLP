

(defmodule AGENT (import MAIN ?ALL) (export ?ALL))



(deftemplate K-cell

   (slot sound (allowed-values yes no unknown) (default unknown)) 
   (slot pos-r) 
   (slot pos-c) 
   (slot contains (allowed-values empty debris wall gate outdoor person unknown))
   (slot injured (allowed-values yes no unknown)))

(deftemplate K-agent        
        (slot step)
        (slot time)         
        (slot pos-r)         
        (slot pos-c)         
        (slot direction)         
        (slot loaded))

; step dell'ultima percezione esaminata
(deftemplate last-perc (slot step))
(deftemplate last-perc-vision (slot step))
(deftemplate last-perc-drill (slot step))
(deftemplate last-perc-acoustic (slot step))

    
 ; Ci dice se l'inizializzazione dell'agente è conclusa
(deftemplate init-agent (slot done (allowed-values yes no)))   


(deffacts initial-fact-agent
  (last-perc (step -1))
  (last-perc-vision (step -1))
  (last-perc-acoustic (step -1))
  (last-perc-drill (step -1))
)


(defrule  beginagent1

    (declare (salience 11))

    (status (step 0))

    (not (exec (step 0)))
    (init_cell (pos-r ?r) (pos-c ?c) (contains ?x&outdoor|wall|empty|gate)) 

=>

     (assert (K-cell (pos-r ?r) (pos-c ?c) (contains ?x) (injured no))
       (init-agent (done yes)))
)

            

(defrule  beginagent2

    (declare (salience 11))

    (status (step 0))

    (not (exec (step 0)))
    (init_cell (pos-r ?r) (pos-c ?c) (contains ?x&debris|unknown)) 

=>

     (assert (K-cell (pos-r ?r) (pos-c ?c) (contains ?x) (injured unknown)) (init-agent (done yes)))
)



 
(defrule  beginagent3

    (declare (salience 11))

    (status (step 0))

    (not (exec (step 0)))
    (initial_agentposition (pos-r ?r) (pos-c ?c) (direction ?d))
=> 
    (assert (K-agent (step 0) (time 0) (pos-r ?r) (pos-c ?c) (direction ?d)
                              (loaded no))
                              (init-agent (done yes)))
)


(defrule ask_act

 ?f <-   (status (step ?i))

    =>  (printout t crlf crlf)

        (printout t "action to be executed at step:" ?i)

        (printout t crlf crlf)

        (modify ?f (result no)))





(defrule exec_act

    (status (step ?i))

    (exec (step ?i))

 => (focus MAIN))





; nel seguito viene riportata una semplice sequenza di comandi che dovrebbe

; servire a verificare il comportamento del modulo ENV nel dominio descritto 

; nel file precedente



;(assert (exec (action forward) (step 0)))



;(assert (exec (action forward) (step 1)))
;(assert (exec (action inform) (param1 3) (param2 4) (param3 discover) (step 2)))
;(assert (exec (action inform) (param1 4) (param2 4) (param3 checked)(step 3)))

;(assert (exec (action load_debris) (step  4) (param1 4) (param2 4))) 

;(assert (exec (action unload_debris) (step  5) (param1 3) (param2 5)))

;(assert (exec (action turnleft) (step 6)))

;(assert (exec (action forward) (step 7)))

;(assert (exec (action drill) (step 8) (param1 3) (param2 4)))


;(assert (exec (action inform) (param1 3) (param2 4) (param3 discover) (step 9)))
;(assert (exec (action inform) (param1 4) (param2 4) (param3 clear) (step 10)))
;(assert (exec (action inform) (param1 3) (param2 5) (param3 clear) (step 11)))
;(assert (exec (action inform) (param1 4) (param2 8) (param3 checked) (step 12)))
;(assert (exec (action inform) (param1 5) (param2 6) (param3 clear) (step 13)))


; Una seconda sequenza di azioni potenzialmante fate da un agente più smart



;(assert (exec (action forward) (step 0)))

;(assert (exec (action inform) (param1 3) (param2 4) (param3 discover) (step 1)))

;(assert (exec (action forward) (step 2)))
;(assert (exec (action inform) (param1 4) (param2 4) (param3 checked)(step 3)))

;(assert (exec (action load_debris) (step  4) (param1 4) (param2 4)))
;(assert (exec (action turnleft) (step 5)))
;(assert (exec (action forward) (step 6))) 



;(assert (exec (action forward) (step 7)))
;(assert (exec (action unload_debris) (step  8) (param1 3) (param2 3)))
;(assert (exec (action turnright) (step 9)))

;(assert (exec (action forward) (step 10)))
;(assert (exec (action forward) (step 11)))

;(assert (exec (action inform) (param1 6) (param2 2) (param3 checked) (step 12)))
;(assert (exec (action inform) (param1 7) (param2 3) (param3 checked) (step 13)))

;(assert (exec (action load_debris) (step  14) (param1 6) (param2 2)))
;(assert (exec (action turnleft) (step 15)))
;(assert (exec (action forward) (step 16)))
;(assert (exec (action inform) (param1 7) (param2 2) (param3 checked) (step 17)))
;(assert (exec (action unload_debris) (step  18) (param1 6) (param2 1)))


;(assert (exec (action turnleft) (step 19)))
;(assert (exec (action turnleft) (step 20)))
;(assert (exec (action forward) (step 21))) 
;(assert (exec (action turnright) (step 22)))

;(assert (exec (action forward) (step 23)))
;(assert (exec (action forward) (step 24)))
;(assert (exec (action turnleft) (step 25)))
;(assert (exec (action forward) (step 26)))
;(assert (exec (action forward) (step 27)))
;(assert (exec (action forward) (step 28)))
;(assert (exec (action turnleft) (step 29)))
;(assert (exec (action forward) (step 30)))
;(assert (exec (action forward) (step 31)))
;(assert (exec (action inform) (param1 6) (param2 7) (param3 discover) (step 32)))
;(assert (exec (action forward) (step 33)))
;(assert (exec (action turnright) (step 34)))
;(assert (exec (action forward) (step 35)))
;(assert (exec (action drill) (step 36) (param1 7) (param2 8)))
;(assert (exec (action inform) (param1 7) (param2 8) (param3 discover) (step 37)))



;(assert (exec (action turnleft) (step 38)))
;(assert (exec (action forward) (step 39)))
;(assert (exec (action turnright) (step 40)))
;(assert (exec (action forward) (step 41)))
;(assert (exec (action drill) (step 42) (param1 8) (param2 9)))
;(assert (exec (action inform) (param1 8) (param2 9) (param3 discover) (step 43)))

;(assert (exec (action turnleft) (step 44)))
;(assert (exec (action load_debris) (step  45) (param1 9) (param2 8)))
;(assert (exec (action forward) (step 46)))
;(assert (exec (action inform) (param1 9) (param2 9) (param3 checked) (step 47)))
;(assert (exec (action unload_debris) (step  48) (param1 10) (param2 8)))
;(assert (exec (action turnleft) (step 49)))
;(assert (exec (action forward) (step 50)))

;(assert (exec (action done) (step 51)))



;(assert (exec (action inform) (param1 3) (param2 4) (param3 discover) (step 9)))
;(assert (exec (action inform) (param1 4) (param2 4) (param3 clear) (step 10)))
;(assert (exec (action inform) (param1 3) (param2 5) (param3 clear) (step 11)))
;(assert (exec (action inform) (param1 4) (param2 8) (param3 checked) (step 12)))
;(assert (exec (action inform) (param1 5) (param2 6) (param3 clear) (step 13)))
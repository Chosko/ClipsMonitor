

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


(defrule  beginagent_kcell_no_injuried

    (declare (salience 11))

    (status (step 0))
    (not (init-agent (done yes)))
    (not (exec (step 0)))
    (init_cell (pos-r ?r) (pos-c ?c) (contains ?x&outdoor|wall|empty|gate)) 

=>

     (assert (K-cell (pos-r ?r) (pos-c ?c) (contains ?x) (injured no)))
)

            

(defrule  beginagent_kcell_maybe_injuried

    (declare (salience 11))

    (status (step 0))
    (not (init-agent (done yes)))
    (not (exec (step 0)))
    (init_cell (pos-r ?r) (pos-c ?c) (contains ?x&debris|unknown)) 

=>

     (assert (K-cell (pos-r ?r) (pos-c ?c) (contains ?x) (injured unknown)))
)



 
(defrule  beginagent_position

    (declare (salience 10))

    (status (step 0))
    (not (init-agent (done yes)))
    (not (exec (step 0)))
    (initial_agentposition (pos-r ?r) (pos-c ?c) (direction ?d))
=> 
    (assert (K-agent (step 0) (time 0) (pos-r ?r) (pos-c ?c) (direction ?d)
                              (loaded no))
                              (init-agent (done yes)))
)




(defrule go_perception
  (declare (salience 9))
  (status (result no))
  (init-agent (done yes))
  =>  
  (focus PERCEPTION)
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

 => (pop-focus))






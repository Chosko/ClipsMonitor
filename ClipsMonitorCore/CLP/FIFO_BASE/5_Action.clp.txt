;; Modulo per la definizione delle azioni da eseguire

;; MODULO ACTION




(defmodule ACTION (import MAIN ?ALL)(export ?ALL)) 


; (defrule r1
;         ?q <-(K-agent 

;         (step ?step)
;         (time ?time)         
;         (pos-r ?r)         
;         (pos-c ?c)         
;         (direction ?)         
;         (loaded ?))
;         =>
;         (assert (exec (action forward) (step ?step)))
;         (halt)
;         )


(defrule pop-action
  (declare (salience -1))
  =>
  (pop-focus)
)

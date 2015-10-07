;; Modulo per la definizione dei meccanismi di reasoning dell'agente

;; MODULO REASONING




(defmodule REASONING (import MAIN ?ALL)(export ?ALL)) 

(defrule reasoning-finished
  =>
  (focus ACTION)
  )

(defrule pop-reasoning
  (declare (salience -1))
  =>
  (pop-focus)
)

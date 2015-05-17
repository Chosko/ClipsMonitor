(defmodule EMPTY-TRASH (import AGENT ?ALL) (export ?ALL))


(deftemplate et-best-basket (multislot pos) (slot type (allowed-values RB TB)))
(deftemplate et-distance-basket (multislot pos-start) (multislot pos-end) (slot distance) (slot type (allowed-values food drink trash-food trash-drink)))

;
; FASE 0: Trovo i basket servibili.
;
(defrule et-init-tb
  (declare (salience 100))
  (go-to-basket (phase 0))
  (K-cell (pos-r ?r) (pos-c ?c) (contains TB))
=>
  (assert(service-trash TB ?r ?c))
)

(defrule et-init-rb
  (declare (salience 100))
  (go-to-basket (phase 0))
  (K-cell (pos-r ?r) (pos-c ?c) (contains RB))
=>
  (assert(service-trash RB ?r ?c))
)

(defrule et-go-phase1
  (declare (salience 10))
  ?f1<-(go-to-basket (phase 0))
=>
  (modify ?f1 (phase 1))
)

;
; FASE 1: Trovare il cestino più vicino.
;

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun trash basket (Food)
(defrule et-distance-manhattan-tb
  (declare (salience 70))
  (go-to-basket (phase 1))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l_f_waste yes))
  (service-trash TB ?rfo ?cfo)
  =>
  (assert (et-distance-basket (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type trash-food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun recyclable basket (Drink)
(defrule et-distance-manhattan-rb
  (declare (salience 70))
  (go-to-basket (phase 1))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l_d_waste yes))
  (service-trash RB ?rfo ?cfo)
  =>
  (assert (et-distance-basket (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type trash-drink)))
)

;Regola che cerca il cestino più vicino
(defrule et-search-best-basket
  (declare (salience 60))
  (status (step ?current))
  (debug ?level)
  ?f1<-(go-to-basket (phase 1))
  (et-distance-basket (pos-start ?ra ?ca) (pos-end ?rd1 ?cd1) (distance ?d))
  (not (et-distance-basket  (pos-start ?ra ?ca) (pos-end ?rd2 ?cd2) (distance ?dist&:(< ?dist ?d)) ))
  (K-cell (pos-r ?rd1) (pos-c ?cd1) (contains ?c))
=>
  (assert(et-best-basket (pos ?rd1 ?cd1) (type ?c)))
  (modify ?f1 (phase 2))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [ET] [F1:s"?current"] Dispenser/Basket Found: " ?c " in ("?rd1", "?cd1")"  crlf)
    (printout t " [DEBUG] [ET] [F1:s"?current"] Init Phase 2: Pianifica Astar verso dispenser " ?c " in ("?rd1", "?cd1")"  crlf)
  )
)


;
; FASE 2: Pianificare con astar un piano per raggiungere il cestino più vicino. Eseguire il piano.
;

; pulisce le distanze ai cestini
(defrule et-clean-distance-basket
  (declare (salience 80))
  (status (step ?current))
  (go-to-basket (phase 2))
  (et-best-basket (pos ?rd ?cd) (type ?c))
  ?f1<-(et-distance-basket (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d))
=>
  (retract ?f1)
)

;Controlle se esiste un piano per andare al best trash con status OK
(defrule et-existence-plane-3
  (declare (salience 10))
  ?f1<-(go-to-basket (phase 2))
  (et-best-basket (pos ?rd ?cd) (type ?c))
  (K-agent (pos-r ?ra) (pos-c ?ca))
  (plane (plane-id ?pid)(pos-start ?ra ?ca) (pos-end ?rd ?cd) (status ok))
=>
  (assert (plane-exist ?pid))
)
;Se il piano non esiste allora devo avviare astar per cercare un percorso che mi porti a destinazione.
(defrule et-create-plane-3
  (declare (salience 1))
  ?f1<-(go-to-basket (phase 2))
  (et-best-basket (pos ?rd ?cd) (type ?c))
  (not (plane-exist ?))
=>
  (assert (goal-astar ?rd ?cd))
  (focus ASTAR)
)

;Se il piano esiste allo lo eseguo.
(defrule et-execute-plane-3
  (declare (salience 1))
  (go-to-basket (phase 2))
  ?f1<-(plane-exist ?pid)
  (plane  (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (direction ?d) (status ok))
=>
  (assert (run-plane-astar (plane-id ?pid) (pos-start ?ra ?ca ?d) (pos-end ?rd ?cd) (phase 1)))
  (retract ?f1)
  (pop-focus)
)

;Eseguito il piano, il robot si trova vicino al cestino piu vicino.
(defrule et-go-phase3
  (declare (salience 1))
  (status (step ?current))
  (debug ?level)
  (plan-executed (plane-id ?pid) (step ?current) (pos-start ?rs ?cs) (pos-end ?rg ?cg) (result ok))
  ?f1<-(go-to-basket (phase 2))
  (et-best-basket (pos ?rd ?cd) (type ?c))
  (not (plane-exist ?))
=>
  (modify ?f1 (phase 3))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [ET] [F4:s"?current"] Init Phase 4 - Agent in front of best dispenser: "?c" in ("?rd","?cd")" crlf)
  )
)

;Piano fallito, il robot deve ripianificare il percorso per raggiungere il best-trash.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto.
(defrule et-re-execute-phase3
  (declare (salience 20))
  (status (step ?current))
  (debug ?level)
  (plan-executed (plane-id ?pid) (step ?current) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (result fail))
  (et-best-basket (pos ?rd ?cd) (type ?c))
  ?f1<-(plane (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (status ok))
  ?f2<-(go-to-basket (phase 2))
  ?f3<-(K-agent)
=>
  (modify ?f1 (status failure))
  (modify ?f2 (phase 2))
  (modify ?f3)

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [ET] [F3:s"?current"] Init Phase 3: Plane Failed. Re-Plane Astar to dispenser: "?c" in ("?rd","?cd")" crlf)
  )
)

;Se non esiste un percorso per arrivare a destinazione.
;Posso andare a pulire un altro tavolo.
;Posso cercare un altro cestino.
;Insisto col perseguire il fatto di voler arrivare a questo cestino.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto
(defrule et-change-order-in-phase3
  (declare(salience 20))
  (debug ?level)
  (status (step ?current))
  ?f1<-(go-to-basket (phase 2))
  ?f2<-(et-best-basket (type ?c) (pos ?rd ?cd))
  ?f3<-(astar-solution (value no))
  ?f4<-(K-agent)
=>
  (modify ?f1 (phase ND))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [ET] [F3:s"?current"] A-Star not found solution to the basket: "?c" in ("?rd","?cd")" crlf)
  )
)

;
; FASE ND:
;

; non abbiamo una strada per il basket, cerchiamo una finish/delayed disponibile e forziamo l'ordine alla fase 5 (ricerca tavolo da pulire)
; OTTIMIZZAZIONE: introdurre nella ricerca il più vicino.
(defrule et-search-another-finish-order
  (declare (salience 10))
  (best-pen ?pen)
  ?f1<-(go-to-basket (phase ND))
  ?f2<-(exec-order (step ?s)  (table-id ?sen) (time-order ?t) (status delayed|finish) (penality ?p&:(> ?p ?pen)) (phase 0))
  (not (exec-order (step ?s1) (penality ?p2&:(> ?p2 ?p)) (status delayed|finish) (phase 0)))
=>
  (retract ?f1)
  (modify ?f2 (phase 5))
  (pop-focus)
)

; non abbiamo una strada per il basket, cerchiamo un altro cestino e forziamo il go-to-basket
(defrule et-search-another-basket
  (declare (salience 8))
  ?f1<-(go-to-basket (phase ND))
  ?f2<-(service-trash ?type ?r ?c)
  ?f3<-(et-best-basket (type ?type) (pos ?r ?c))
=>
  (modify ?f1 (phase 1))
  (retract ?f2 ?f3)
)

;Cerichiamo l'ultimo ordine di un tavolo COMPLETED e proviamo a vedere se ha terminato di consumare per pulire.
(defrule et-check-finish
  (declare (salience 6))
  (status (step ?current))
  (best-pen ?pen)
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?tid) (clean no))
  ?f1<-(go-to-basket (phase ND))
=>
  (assert (search-order-for-checkfinish))
  (retract ?f1)
  (pop-focus)
)

(defrule et-no-basket-wait
  (declare (salience 5))
  (status (step ?i))
  ?f1<-(go-to-basket (phase ND))
=>
  (assert (exec (step ?i) (action Wait)))
  (modify ?f1 (phase 1))
)

;
; FASE 3: Scarico il trash.
;

; regola per scaricare il cibo
; ===========================
; controllo che ci sia del l_f_waste
; controllo che l'agente possa operare sul trash basket ovvero che sia in una posizione adiacente.
(defrule et-do-EmptyFood
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  (go-to-basket (phase 3))
  (et-best-basket (pos ?rfo ?cfo) (type TB))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l_f_waste yes))
  (or (and (test(= ?ra ?rfo)) (test(= ?ca (+ ?cfo 1))))
      (and (test(= ?ra ?rfo)) (test(= ?ca (- ?cfo 1))))
      (and (test(= ?ra (+ ?rfo 1))) (test(= ?ca ?cfo)))
      (and (test(= ?ra (- ?rfo 1))) (test(= ?ca ?cfo)))
  )
=>
  (assert (exec (step ?ks) (action EmptyFood) (param1 ?rfo) (param2 ?cfo)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [ET] [F4:s"?current"] EmptyFood in TrashBasket: ("?rfo","?cfo")" crlf)
  )
  (pop-focus)
)

; regola per scaricare il drink
; ===========================
; controllo che ci sia del l_d_waste
; controllo che l'agente possa operare sul trash basket ovvero che sia in una posizione adiacente.
(defrule et-do-Release
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  (go-to-basket (phase 3))
  (et-best-basket (pos ?rfo ?cfo) (type RB))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l_d_waste yes))
  (or (and (test(= ?ra ?rfo)) (test(= ?ca (+ ?cfo 1))))
      (and (test(= ?ra ?rfo)) (test(= ?ca (- ?cfo 1))))
      (and (test(= ?ra (+ ?rfo 1))) (test(= ?ca ?cfo)))
      (and (test(= ?ra (- ?rfo 1))) (test(= ?ca ?cfo)))
  )
=>
  (assert (exec (step ?ks) (action Release) (param1 ?rfo) (param2 ?cfo)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [ET] [F4:s"?current"] Release drink in RecyclableBasket: ("?rfo","?cfo")" crlf)
  )
  (pop-focus)
)

; Una volta scaricato rimuovo il fatto best-dispenser.
(defrule et-clean-best-dispenser
        (declare (salience 60))
        ?f1<-(go-to-basket (phase 3))
        ?f2 <- (et-best-basket)
=>
        (retract ?f2)
        (modify ?f1 (phase 4))
)

;
; FASE 4: Controllo se ho ancora immondizia da scaricare o se sono pulito.
;
(defrule empty-trash-completed1
  ?f1<-(go-to-basket (phase 4))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  =>
  (retract ?f1)
  (pop-focus)
)

(defrule empty-trash-completed2-1
  ?f1<-(go-to-basket (phase 4))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste yes))
  =>
  (modify ?f1 (phase 1))
)

(defrule empty-trash-completed2-2
  ?f1<-(go-to-basket (phase 4))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste yes) (l_f_waste no))
  =>
  (modify ?f1 (phase 1))
)

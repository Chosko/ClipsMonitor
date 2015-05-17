;strategia FIFO un tavolo alla volta
; FASE 1, ricerca di un tavolo da servire
; FASE 2, individuare dispenser più vicino
; FASE 3, astar verso il dispenser più vicino ed esecuzione piano
; FASE 4, caricamento food/drink
; FASE 4.5, caricamento food/drink terminato, controllo se c'è ancora food/drink da caricare e c'è ancora spazio.
; FASE 5, A-star verso il tavolo ed esecuzione piano
; FASE 6, controllo action e scarica food/drink o carica trash
;      6.1, controllo di ritorno a fase 2 se c'è ancora roba da servire a quel tavolo
;      6.2, l'ordine è completato, vai alla fase 7
; FASE 7, ordine completato, retract service-table

;Regole per rispondere alla richiesta ordini da parte dei tavoli.
;Attiva quando ricevo un ordine da un tavolo Inform con accepted
(defrule answer-msg-order1
  (declare (salience 150))
  (status (step ?current))
  (msg-to-agent (request-time ?t) (step ?current) (sender ?sen) (type order) (drink-order ?do) (food-order ?fo))
  (K-table (pos-r ?r) (pos-c ?c) (table-id ?sen) (clean yes))
=>
  (assert (exec (step ?current) (action Inform) (param1 ?sen) (param2 ?t) (param3 accepted)))
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Inform) (table-id ?sen) (time-order ?t) (status accepted) (origin-status accepted)  (drink-order ?do) (food-order ?fo) (phase 0) (fail 0) (penality (*(+ ?do ?fo)2))))
  (assert (update-penality))
)

;Attiva quando ricevo un ordine da un tavolo sporco che per specifica assumiamo abbia inviato precedentemente una finish.
;Inform con strategy-return-phase6-to-2_delayed
(defrule answer-msg-order2
  (declare (salience 150))
  (status (step ?current))
  (msg-to-agent (request-time ?t) (step ?current) (sender ?sen) (type order) (drink-order ?do) (food-order ?fo))
  (K-table (table-id ?sen) (clean no))
=>
  (assert (exec (step ?current) (action Inform) (param1 ?sen) (param2 ?t) (param3 delayed)))
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Inform) (table-id ?sen) (time-order ?t) (status delayed) (origin-status delayed) (drink-order ?do) (food-order ?fo) (phase 0) (fail 0) (penality (+ ?do ?fo))))
  (assert (update-penality))
)

;Attiva quando ricevo un 'ordine' di finish da un tavolo sporco.
(defrule answer-msg-order3
  (declare (salience 150))
  (status (step ?current))
  (msg-to-agent (request-time ?t) (step ?current) (sender ?sen) (type finish))
=>
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Finish) (table-id ?sen) (time-order ?t) (status finish) (origin-status finish) (drink-order 0) (food-order 0) (phase 0) (fail 0) (penality 3)))
  (assert (update-penality))
)

(defrule update-penality-1
  (declare (salience 150))
  (status (step ?current))
  ?f1<-(update-penality)
  (exec-order (step ?current) (action Inform) (status accepted) (drink-order ?do) (food-order ?fo) (phase 0) (penality ?pen))
  ?f2<-(qty-order-sum (type accepted) (pen ?pen1) (qty-fo ?sfo) (qty-do ?sdo))
=>
  (retract ?f1)
  (modify ?f2 (pen (+ ?pen1 ?pen)) (qty-fo (+ ?sfo ?fo)) (qty-do (+ ?sdo ?do)))
)

(defrule update-penality-2
  (declare (salience 150))
  (status (step ?current))
  ?f1<-(update-penality)
  (exec-order (step ?current) (action Inform) (status delayed) (drink-order ?do) (food-order ?fo) (phase 0) (penality ?pen))
  ?f2<-(qty-order-sum (type delayed) (pen ?pen1) (qty-fo ?sfo) (qty-do ?sdo))
=>
  (retract ?f1)
  (modify ?f2 (pen (+ ?pen1 ?pen)) (qty-fo (+ ?sfo ?fo)) (qty-do (+ ?sdo ?do)))
)

(defrule update-penality-3
  (declare (salience 150))
  (status (step ?current))
  ?f1<-(update-penality)
  (exec-order (step ?current) (action Finish) (status finish) (drink-order 0) (food-order 0) (phase 0) (penality ?pen))
  ?f2<-(qty-order-sum (type finish) (pen ?pen1))
=>
  (retract ?f1)
  (modify ?f2 (pen (+ ?pen1 ?pen)))
)

;Se non sto facendo nulla prima della wait provo a effettuare una checkfinish su un tavolo che aveva oridinato.
(defrule strategy-check-finish
  (declare (salience 140))
  (debug ?level)
  (status (step ?current))
  (not (exec-order (phase 0|1|2|3|4|4.5|5|6|7)))
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?tid) (clean no))

=>
  (assert (search-order-for-checkfinish))
  (if (> ?level 0)
    then
      (printout t " [INFO] [F0:s"?current":] No job to do." crlf)
  )
)

;per ogni tavolo a cui è stato già consengato tutto il cibo, calcola la distanza dal robot
(defrule search-order-for-checkfinish
  (declare (salience 140))
  (status (step ?current))
  (search-order-for-checkfinish)
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?tid) (clean no) (step-checkfinish ?scf&:(neq ?scf ?current)))
  (K-agent (pos-r ?ra) (pos-c ?ca))
=>
  (assert(table-distance (table-id ?tid) (distance (+ (abs(- ?ra ?rt)) (abs(- ?ca ?ct))))))
)

(defrule found-order-for-checkfinish
  (declare (salience 140))
  (debug ?level)
  (status (step ?current))
  ?f1<-(search-order-for-checkfinish)
  (table-distance (table-id ?tid) (distance ?d))
  (not(table-distance (table-id ?tid2) (distance ?d1&:(< ?d1 ?d))))
=>
  (assert (exec-order (step ?current) (table-id ?tid) (origin-status check-finish) (status check-finish) (food-order 0) (drink-order 0) (penality 0) (fail 0) (phase 5)))
  (retract ?f1)
  (if (> ?level 0)
    then
      (printout t " [INFO] [F0:s"?current":] Checkfinish on table:" ?tid crlf)
  )
  (focus CLEAN-TABLE-DISTANCE)
)

;Se non sto facendo nulla prima della wait provo a scarica immondizia se c'è l'ho
(defrule strategy-empty-trash
  (declare (salience 135))
  (not (exec-order (phase 0|1|2|3|4|4.5|5|6|7)))
  (not (go-to-basket))
  (K-agent (l_d_waste ?ldw) (l_f_waste ?lfw))
  (test(or(=(str-compare ?ldw "yes")0) (=(str-compare ?lfw "yes")0)))
=>
  (assert (go-to-basket (phase 0)))
  (focus EMPTY-TRASH)
)

;
; FASE 1 della Strategia: Ricerca di un tavolo da servire.
;

;L'ordine da servire deve essere un ordine di accepted sum-penality-accepted >= sum-penality-finish + sum-penality-delayed
(defrule evalutation-order-1
  (declare (salience 71))
  (status (step ?current))
  (debug ?level)
  ;La valutazione avviene solo ne caso non stia servendo nessun altro ordine, ovvero non esiste un ordine che è nelle fasi 1,2,3,4,5,6 o 7
  (exec-order (phase 0))
  (not (exec-order (phase 1|2|3|4|4.5|5|6|7)))

  (qty-order-sum (type accepted) (pen ?pen1))
  (qty-order-sum (type delayed) (pen ?pen2))
  (qty-order-sum (type finish) (pen ?pen3))
  (test(>= ?pen1 (+ ?pen2 ?pen3)))
  (K-agent (l-drink ?) (l-food ?) (l_d_waste no) (l_f_waste no))
=>
  (assert (found-order-accepted))
)

;L'ordine da servire deve essere un ordine di delayed/finish sum-penality-accepted < sum-penality-finish + sum-penality-delayed
(defrule evalutation-order-2
  (declare (salience 71))
  (status (step ?current))
  (debug ?level)
  ;La valutazione avviene solo ne caso non stia servendo nessun altro ordine, ovvero non esiste un ordine che è nelle fasi 1,2,3,4,5,6 o 7
  (exec-order (phase 0))
  (not (exec-order (phase 1|2|3|4|4.5|5|6|7)))

  (qty-order-sum (type accepted) (pen ?pen1))
  (qty-order-sum (type delayed) (pen ?pen2))
  (qty-order-sum (type finish) (pen ?pen3))
  (test(< ?pen1 (+ ?pen2 ?pen3)))
  (K-agent (l-drink 0) (l-food 0) (l_d_waste ?) (l_f_waste ?))
=>
  (assert (found-order-finish-delayed))
)

;L'ordine da servire deve essere un ordine di accepted sum-penality-accepted >= sum-penality-finish + sum-penality-delayed ma ho sporco a bordo devo andare al cestino
(defrule evalutation-order-3
  (declare (salience 71))
  (status (step ?current))
  (debug ?level)
  ;La valutazione avviene solo ne caso non stia servendo nessun altro ordine, ovvero non esiste un ordine che è nelle fasi 1,2,3,4,5,6 o 7
  (exec-order (phase 0))
  (not (exec-order (phase 1|2|3|4|4.5|5|6|7)))

  (qty-order-sum (type accepted) (pen ?pen1))
  (qty-order-sum (type delayed) (pen ?pen2))
  (qty-order-sum (type finish) (pen ?pen3))
  (test(>= ?pen1 (+ ?pen2 ?pen3)))
  (K-agent (l-drink 0) (l-food 0) (l_d_waste ?ldw) (l_f_waste ?lfw))
  (test(or(=(str-compare ?ldw "yes")0) (=(str-compare ?lfw "yes")0)))
=>
  (assert (go-to-basket (phase 0)))
  (focus EMPTY-TRASH)
)

;L'ordine da servire deve essere un ordine di delayed/finish sum-penality-accepted < sum-penality-finish + sum-penality-delayed ma ho consumazioni a bordo.
(defrule evalutation-order-4
  (declare (salience 71))
  (status (step ?current))
  (debug ?level)
  ;La valutazione avviene solo ne caso non stia servendo nessun altro ordine, ovvero non esiste un ordine che è nelle fasi 1,2,3,4,5,6 o 7
  (exec-order (phase 0))
  (not (exec-order (phase 1|2|3|4|4.5|5|6|7)))

  (qty-order-sum (type accepted) (pen ?pen1))
  (qty-order-sum (type delayed) (pen ?pen2))
  (qty-order-sum (type finish) (pen ?pen3))
  (test(< ?pen1 (+ ?pen2 ?pen3)))
  (K-agent (l-drink ?ld) (l-food ?lf) (l_d_waste no) (l_f_waste no))
  (test(or (> ?ld 0) (> ?lf 0)))
=>
  (assert (force-delivery (min 1000)))
)

;Ricerca dell'ordine accepted da servire con penalità + alta.
(defrule strategy-search-order-accepted-1
  (declare (salience 70))
  (status (step ?current))
  (best-pen ?pen)
  (debug ?level)
  ?f1<-(found-order-accepted)
  ?f2<-(exec-order (step ?s&:(< ?s ?current)) (origin-order-step ?step) (table-id ?sen) (time-order ?t) (status accepted) (penality ?p&:(> ?p ?pen)) (phase 0))
  ;(not (exec-order (step ?s1&:(<= ?s1 ?s)) (penality ?p2&:(>= ?p2 ?p)) (status accepted) (phase 0) (time-order ?t1&:(< ?t1 ?t))))
  (not (exec-order (penality ?p2&:(> ?p2 ?p)) (status accepted) (phase 0)))
=>
  (retract ?f1)
  (modify ?f2 (phase 1))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Init Phase 1 - table: " ?sen ". Step of order is:" ?step crlf)
  )
)

;Ricerca dell'ordine accepted da servire che minimizzi le consegne.
;Obiettivo è arrivare ad avere il robot con 0 food e 0 drink a bordo perchè devo passare alla fase di pulizia dei tavoli.
;Caso 1 il robot ha a bordo sia food che drink
(defrule strategy-search-order-accepted-2-caso1
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)
  (K-agent (l-drink ?ld) (l-food ?lf))
  ?f1<-(force-delivery (min ?min))
  (exec-order (step ?s) (origin-order-step ?step) (food-order ?fo) (drink-order ?do)  (table-id ?sen) (status accepted) (phase 0))
  (test(< (+ (- ?lf ?fo ) (- ?ld ?do )) ?min))
  (test(> ?lf 0))
  (test(> ?ld 0))
=>
  (modify ?f1 (min =(+ (- ?lf ?fo ) (- ?ld ?do ))) (step ?s) (table-id ?sen))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Init Phase 1 - table: " ?sen ". Step of order is:" ?step crlf)
  )
)

;Ricerca dell'ordine accepted da servire che minimizzi le consegne.
;Obiettivo è arrivare ad avere il robot con 0 food e 0 drink a bordo perchè devo passare alla fase di pulizia dei tavoli.
;Caso 2 il robot ha a bordo solo food.
(defrule strategy-search-order-accepted-2-caso2
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)
  (K-agent (l-drink ?ld) (l-food ?lf))
  ?f1<-(force-delivery (min ?min))
  (exec-order (step ?s) (origin-order-step ?step) (food-order ?fo) (table-id ?sen) (status accepted) (phase 0))
  (test(< (- ?lf ?fo ) ?min))
  (test(> ?lf 0))
  (test(= ?ld 0))
=>
  (modify ?f1 (min =(- ?lf ?fo)) (step ?s) (table-id ?sen))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Init Phase 1 - table: " ?sen ". Step of order is:" ?step crlf)
  )
)

;Ricerca dell'ordine accepted da servire che minimizzi le consegne.
;Obiettivo è arrivare ad avere il robot con 0 food e 0 drink a bordo perchè devo passare alla fase di pulizia dei tavoli.
;Caso 2 il robot ha a bordo solo drink.
(defrule strategy-search-order-accepted-2-caso3
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)
  (K-agent (l-drink ?ld) (l-food ?lf))
  ?f1<-(force-delivery (min ?min))
  (exec-order (step ?s) (origin-order-step ?step) (drink-order ?do)  (table-id ?sen) (status accepted) (phase 0))
  (test(< (- ?ld ?do ) ?min))
  (test(= ?lf 0))
  (test(> ?ld 0))
=>
  (modify ?f1 (min =(- ?ld ?do )) (step ?s) (table-id ?sen))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Init Phase 1 - table: " ?sen ". Step of order is:" ?step crlf)
  )
)



;Trovato l'ordine che minimizza il numero di consegne, vado a consegnare al tavolo.
(defrule strategy-search-order-accepted-2bis
  (declare (salience 65))
  ?f1<-(force-delivery (min ?min) (step ?step) (table-id ?sen))
  ?f2<-(exec-order (step ?step) (table-id ?sen) (status accepted) (phase 0))
 =>
   (retract ?f1)
   (modify ?f2 (phase 5))
)

;Ricerca dell'ordine delayed/finish da servire con penalità + alta.
(defrule strategy-search-order-finish-delayed
  (declare (salience 70))
  (status (step ?current))
  (best-pen ?pen)
  (debug ?level)
  ?f1<-(found-order-finish-delayed)
  ?f2<-(exec-order (step ?s&:(< ?s ?current)) (origin-order-step ?step)  (table-id ?sen) (time-order ?t) (status delayed|finish) (penality ?p&:(> ?p ?pen)) (phase 0))
  ;(not (exec-order (step ?s1&:(<= ?s1 ?s)) (penality ?p2&:(>= ?p2 ?p)) (status delayed|finish) (phase 0) (time-order ?t1&:(< ?t1 ?t))))
  (not (exec-order (penality ?p2&:(> ?p2 ?p)) (status accepted) (phase 0)))
=>
  (retract ?f1)
  (modify ?f2 (phase 1))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Init Phase 1 - table: " ?sen ". Step of order is:" ?step crlf)
  )
)

;Trovato l'ordine eseguo la fase di competenza
(defrule strategy-complete-phase1
  (declare (salience 60))
  (status (step ?s1))
  ?f1 <- (exec-order (step ?s2) (drink-order ?do) (food-order ?fo) (table-id ?id) (status ?status)  (phase 1) )
  (K-table (table-id ?id) (clean ?clean))
  (K-agent (l-drink ?ld) (l-food ?lf) (l_d_waste ?ldw) (l_f_waste ?lfw))
  ?f2<-(qty-order-sum (type accepted) (pen ?pen1) (qty-fo ?sfo1) (qty-do ?sdo1))
  ?f3<-(qty-order-sum (type delayed) (pen ?pen2) (qty-fo ?sfo2) (qty-do ?sdo2))
=>
  ; vado alla fase 2 se l'ordine è accettato, ovvero posso cercare già il dispenser più vicino
  (if (=(str-compare ?status "accepted") 0)
  then
    (modify ?f1 (table-id ?id) (phase 2))
  )
  ; se l'ordine è delayed vado alla fase 5
  (if (= (str-compare ?status "delayed") 0)
  then
    (modify ?f1 (table-id ?id) (phase 5))
  )
  ; se ho ricevuto una finish e non ho cibo caricato vado a pulire il tavolo
  (if (= (str-compare ?status "finish") 0)
  then
    (modify ?f1 (table-id ?id) (phase 5))
  )
)

;
; FASE 2 della Strategia: Individuare il dispenser/cestino più vicino.
;

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun food-dispenser
(defrule distance-manhattan-fo
  (declare (salience 70))
  (exec-order (food-order ?fo) (table-id ?id) (phase 2) (status accepted))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld))
  (test (> ?fo 0))
  (test (< ?lf ?fo))
  (test (< (+ ?lf ?ld) 4))

  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains FD))
  =>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun drink-dispenser
(defrule distance-manhattan-do
  (declare (salience 70))
  (exec-order (drink-order ?do) (table-id ?id) (phase 2) (status accepted))
  (K-agent (pos-r ?ra)(pos-c ?ca) (l-food ?lf) (l-drink ?ld))
  (test (> ?do 0))
  (test (< ?ld ?do))
  (test (< (+ ?lf ?ld) 4))

  (K-cell (pos-r ?rdo) (pos-c ?cdo) (contains DD))
=>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance (+ (abs(- ?ra ?rdo)) (abs(- ?ca ?cdo)))) (type drink)))
)

;Regola che cerca il dispenser/cestino più vicino
(defrule search-best-dispenser
  (declare (salience 60))
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (phase 2))
  (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rd1 ?cd1) (distance ?d))
  (not (strategy-distance-dispenser  (pos-start ?ra ?ca) (pos-end ?rd2 ?cd2) (distance ?dist&:(< ?dist ?d)) ))
  (K-cell (pos-r ?rd1) (pos-c ?cd1) (contains ?c))
=>
  (assert(strategy-best-dispenser (pos-dispenser ?rd1 ?cd1) (type ?c)))
  (modify ?f1 (phase 3))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F2:s"?current":"?id"] Dispenser Found: " ?c " in ("?rd1", "?cd1")"  crlf)
  )
)

;Se ho gia l'ordinazione a bordo vado a consegnarla
(defrule strategy-all-loaded-go-phase5
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (phase 2) (food-order ?fo) (drink-order ?do) (status accepted))
  (not (strategy-distance-dispenser (type ?type)))
=>
  (modify ?f1 (phase 5))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F2:s"?current":"?id"] Go to the table: " ?id  crlf)
  )
)


;
; FASE 3 della Strategia: Pianificare con astar un piano per raggiungere il dispenser/cestino più vicino. Eseguire il piano.
;

; pulisce le distanze ai dispensers/cestini
(defrule clean-distance-dispenser
  (declare (salience 80))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 3) )
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  ?f1 <- (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d))
=>
  (retract ?f1)
)

;Controlle se esiste un piano per andare al best dispenser/trash con status OK
(defrule strategy-existence-plane-3
  (declare (salience 10))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 3) )
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  (K-agent (pos-r ?ra) (pos-c ?ca))
  (plane (plane-id ?pid)(pos-start ?ra ?ca) (pos-end ?rd ?cd) (status ok))
=>
  (assert (plane-exist ?pid))
  (assert (add-counter-n-replane))
  (printout t " [INFO] [F3:s"?current":"?id"] Exist a plane for go to the dispenser." crlf)
)

;Se il piano non esiste allora devo avviare astar per cercare un percorso che mi porti a destinazione.
(defrule strategy-create-plane-3
  (declare (salience 1))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 3) )
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  (not (plane-exist ?))
=>
  (assert (start-astar (pos-r ?rd) (pos-c ?cd)))
  (assert (less-counter-n-replane))
  (printout t " [INFO] [F3:s"?current":"?id"] Not exist a valid plane. Run Astar to: "?rd ","?cd crlf)
)

;Se il piano esiste allo lo eseguo.
(defrule strategy-execute-plane-3
  (declare (salience 1))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 3) )
  ?f1<-(plane-exist ?pid)
  (plane  (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (direction ?d) (status ok))
=>
  (assert (run-plane-astar (plane-id ?pid) (pos-start ?ra ?ca ?d) (pos-end ?rd ?cd) (phase 1)))
  (printout t " [INFO] [F3:s"?current":"?id"] Exec plane: "?pid crlf)
  (retract ?f1)
)

;Eseguito il piano, il robot si trova vicino al dispenser/cestino piu vicino.
(defrule strategy-go-phase4
  (declare (salience 11))
  (status (step ?current))
  (debug ?level)
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  (plan-executed (plane-id ?pid) (step ?current) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (result ok))
  ?f1<-(exec-order (table-id ?id) (phase 3) )
  (not (plane-exist ?))
=>
  (modify ?f1 (phase 4) (fail 0))
  (assert(set-plane-in-position ?rd ?cd))
  (focus SET-PLANE-AT-OK)
  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F4:s"?current":"?id"] Init Phase 4 - Agent in front of best dispenser: "?c" in ("?rd","?cd")" crlf)
  )
)

;Piano fallito, il robot deve ripianificare il percorso per raggiungere il best-dispenser.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto.
(defrule strategy-re-execute-phase3
  (declare (salience 20))
  (status (step ?current))
  (debug ?level)
  (plan-executed (plane-id ?pid) (step ?current) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (result fail))
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  ?f1<-(plane (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (status ok))
  ?f2<-(exec-order (table-id ?id) (phase 3) (fail ?f))
  ?f3<-(K-agent)
=>
  (modify ?f1 (status failure))
  (modify ?f2 (phase 3) (fail (+ ?f 1)))
  (modify ?f3)

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F3:s"?current":"?id"] Init Phase 3: Plane Failed. Re-Plane Astar to dispenser: "?c" in ("?rd","?cd")" crlf)
  )
)

;Se non esiste un percorso per arrivare a destinazione, l'ordine viene inserito al fondo.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto
(defrule strategy-change-order-in-phase3
  (declare(salience 20))
  (debug ?level)
  (status (step ?current))
  ?f1<-(exec-order (table-id ?id) (step ?s2) (phase 3))
  ?f2<-(strategy-best-dispenser (type ?c) (pos-dispenser ?rd ?cd))
  ?f3<-(astar-solution (value no))
  ?f4<-(K-agent)
=>
  (modify ?f1 (step ?current) (phase 0))
  (modify ?f4)
  (retract ?f2 ?f3)
  (assert (add-counter-n-replane))
  (assert(set-plane-in-position ?rd ?cd))
  (focus SET-PLANE-AT-OK)
  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F3:s"?current":"?id"] A-Star not found solution to the dispenser: "?c" in ("?rd","?cd")" crlf)
    (printout t " [DEBUG] [F3:s"?current":"?id"] Order moved to the bottom." crlf)
  )
)

;
;  FASE 4 della Strategia: Il robot arrivato al dispenser/cestino carica/scarica.
;

; regola per caricare il cibo
; ===========================


(defrule strategy-do-LoadFood
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 4) (food-order ?fo))
  ?f2<-(qty-order-sum (type accepted) (qty-fo ?sfo))
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type FD))        ;
  (test (> ?fo 0))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (test (< (+ ?lf ?ld) 4))
  (test (< ?lf ?sfo))

=>
  (assert (exec (step ?ks) (action LoadFood) (param1 ?rd) (param2 ?cd)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F4:s"?current":"?id"] Loading food in dipsenser FD: ("?rd","?cd")" crlf)
  )
)

; regola per caricare il drink
; ===========================
; medesime situazioni del food
(defrule strategy-do-LoadDrink
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 4) (drink-order ?do))
  ?f2<-(qty-order-sum (type accepted) (qty-do ?sdo))
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type DD))
  (test (> ?do 0))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (test (< (+ ?lf ?ld) 4))
  (test (< ?ld ?sdo))
=>
  (assert (exec (step ?ks) (action LoadDrink) (param1 ?rd) (param2 ?cd)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F4:s"?current":"?id"] Loading drink in dispenser DD: ("?rd","?cd")" crlf)
  )
)

; Una volta caricato o scaricato rimuovo il fatto best-dispenser.
; Nel caso del carico controllo che non abbia ancora drink o food di quell'ordine da caricare
(defrule strategy-clean-best-dispenser
        (declare (salience 60))
        ?f1<-(exec-order (drink-order ?do) (food-order ?fo) (phase 4))
        ?f2 <- (strategy-best-dispenser)
=>
        (retract ?f2)
        (modify ?f1 (phase 4.5))
)

;
; FASE 4.5 della Strategia:
;

;Controllo se deve caricare altra roba.
(defrule strategy-return-phase2_order
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (drink-order ?do) (food-order ?fo) (phase 4.5) (status accepted))
  (K-agent (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (test (< (+ ?lf ?ld) 4))
  (test (or (>(- ?fo ?lf)0) (>(- ?do ?ld)0)))
=>
  (modify ?f1 (phase 2))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F4.5:s"?current":"?id"] Agent has space available, return to Phase 2" crlf)
  )
)

; L'agente ha caricato tutti i food o drink per quell'ordine o è arrivato alla capienza max trasportabile, possiamo andare alla fase 5, cioè cercare il piano per arrivare al tavolo
(defrule strategy-go-phase5
  (status (step ?current))
  (debug ?level)

  ?f1 <- (exec-order (table-id ?id) (drink-order ?do) (food-order ?fo) (phase 4.5) (status ?a))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (or (test (= (+ ?lf ?ld) 4))
      (test (or (<=(- ?fo ?lf)0) (<=(- ?do ?ld)0)))
  )
=>
  (modify ?f1 (phase 5))

  ;debug
  (if (> ?level 0)
  then
   (printout t " [DEBUG] [F5:s"?current":"?id"] Go to the table: " ?id crlf)
  )
)

;
; FASE 5 della Strategia: Esecuzione di astar per determinare il piano per arrivare al tavolo ed esecuzione del piano.
;

;Controlle se esiste un piano per andare al tavolo con status OK
(defrule strategy-existence-plane-5
  (declare (salience 10))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 5) )
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  (K-agent (pos-r ?ra) (pos-c ?ca))
  (plane (plane-id ?pid)(pos-start ?ra ?ca) (pos-end ?rt ?ct) (status ok))
=>
  (assert (add-counter-n-replane))
  (assert (plane-exist ?pid))
  (printout t " [INFO] [F5:s"?current":"?id"] Exist a plane for go to the table." crlf)
)

;Se il piano non esiste allora devo avviare astar per cercare un percorso che mi porti a destinazione.
(defrule strategy-create-plane-5
  (declare (salience 1))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 5) )
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  (not (plane-exist ?))
=>
  (assert (less-counter-n-replane))
  (assert (start-astar (pos-r ?rt) (pos-c ?ct)))
  (printout t " [INFO] [F5:s"?current":"?id"] Run Astar to: "?rt ","?ct crlf)
)

;Se il piano esiste allo lo eseguo.
(defrule strategy-execute-plane-5
  (declare (salience 1))
  (exec-order (table-id ?id) (phase 5) )
  ?f1<-(plane-exist ?pid)
  (plane  (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rt ?ct) (direction ?d) (status ok))
=>
  (assert (run-plane-astar (plane-id ?pid) (pos-start ?ra ?ca ?d) (pos-end ?rt ?ct) (phase 1)))
  (retract ?f1)
)

;Eseguito il piano, il robot si trova vicino al tavolo.
(defrule strategy-go-phase6
  (declare (salience 11))
  (status (step ?current))
  (debug ?level)
  (K-table (table-id ?id) (pos-r ?rt) (pos-c ?ct))
  ?f2<-(exec-order (table-id ?id) (phase 5) (drink-order ?do) (food-order ?fo) (status ?a))
  (plan-executed (step ?current) (pos-start ?rs ?cs) (pos-end ?rt ?ct) (result ok))
  (not (plane-exist ?))
=>
  (modify ?f2 (phase 6) (fail 0))
  (assert(set-plane-in-position ?rt ?ct))
  (focus SET-PLANE-AT-OK)
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Init Phase 6, Agent in front of table " ?id ", order (food: "?fo", drink: "?do") action ("?a")" crlf)
  )
)

;Se il piano è fallito, il robot deve ripianificare per arrivare al tavolo. Cio significa rieseguire la fase 5.
(defrule strategy-re-execute-phase5
  (declare (salience 20))
  (status (step ?current))
  (debug ?level)
  (plan-executed (plane-id ?pid)(step ?current) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (result fail))
  ?f1<-(plane (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (status ok))
  ?f2<-(exec-order (table-id ?id) (phase 5)  (fail ?f))
  ?f3<-(K-agent)
=>
  (modify ?f1 (status failure))
  (modify ?f2 (phase 5) (fail (+ ?f 1)))
  (modify ?f3)

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F5:s"?current":"?id"] Init Phase 5: Plane Failed. Re-Plane Astar to table: "?id crlf)
  )
)

;Se non esiste un percorso per arrivare a destinazione, l'ordine viene inserito al fondo.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto
(defrule strategy-change-order-in-phase5
  (declare (salience 20))
  (debug ?level)
  (status (step ?current))
  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 5) (status finish|delayed|accepted))
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  ?f2<-(astar-solution (value no))
  ?f3<-(K-agent)
=>
  (modify ?f1 (step ?current) (phase 0))
  (retract ?f2)
  (modify ?f3)
  (assert (add-counter-n-replane))
  (assert(set-plane-in-position ?rt ?ct))
  (focus SET-PLANE-AT-OK)

  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F5:s"?current":"?id"] A-Star not found solution to the table: "?id crlf)
    (printout t " [DEBUG] [F5:s"?current":"?id"] Order moved to the bottom." crlf)
  )
)

;Se non esiste un percorso per arrivare a destinazione, l'ordine di checkfinish viene buttato.
;lo step che indica quando si voleva fare una checkfinish viene aggiornato.
;Devo modificare K-agent altrimenti la regola S0 di astar non parte perche attivata più volte dal medesimo fatto
(defrule strategy-remove-order-checkfinish
  (declare (salience 20))
  (debug ?level)
  (status (step ?current))
  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 5) (status check-finish))
  ?f4<-(K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  ?f2<-(astar-solution (value no))
  ?f3<-(K-agent)
=>
  (retract ?f1 ?f2)
  (modify ?f3)
  (modify ?f4 (step-checkfinish ?current))
  (assert(set-plane-in-position ?rt ?ct))
  (focus SET-PLANE-AT-OK)

  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F5:s"?current":"?id"] A-Star not found solution to the table: "?id crlf)
    (printout t " [DEBUG] [F5:s"?current":"?id"] Order checkfinish is removed." crlf)
  )
)
;
; FASE 6 della Strategia: il robot è arrivato al tavolo e deve scaricare.
;

;Regola per scaricare il food al tavolo
(defrule strategy-do-DeliveryFood
  (declare(salience 10))
  (status (step ?current))
  (debug ?level)

  (exec-order (step ?s2) (table-id ?id) (phase 6) (status accepted) (food-order ?fo))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf))
  (Table (table-id ?id) (pos-r ?rfo) (pos-c ?cfo))
  (and (test(> ?fo 0)) (test(> ?lf 0)))
=>
  (assert (exec (step ?ks) (action DeliveryFood) (param1 ?rfo) (param2 ?cfo)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"-SERVE] Delivery Food" crlf)
  )
)

;Regola per scaricare i drink al tavolo
(defrule strategy-do-DeliveryDrink
  (declare(salience 10))
  (status (step ?current))
  (debug ?level)

  (exec-order (step ?s2) (table-id ?id) (phase 6) (status accepted) (drink-order ?do))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-drink ?ld))
  (Table (table-id ?id) (pos-r ?rfo) (pos-c ?cfo))
  (and (test(> ?do 0)) (test(> ?ld 0)))
=>
  (assert (exec (step ?ks) (action DeliveryDrink) (param1 ?rfo) (param2 ?cfo)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"-SERVE] Delivery Drink" crlf)
  )
)

;regola per controllare se le consumazioni al tavolo sono state consumate e che non si già arrivato un ordine di cleantable.
(defrule strategy-do-CheckFinish
  (declare(salience 10))
  (status (step ?current))
  (debug ?level)
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-drink ?ld) (l-food ?lf))
  (K-table (table-id ?id) (pos-r ?rt) (pos-c ?ct) (clean no))
  (exec-order (table-id ?id) (phase 6) (status  check-finish))
  (not(exec-order (table-id ?id) (phase 0) (status finish)))
  ; controlla che l'agente sia scarico
  (test (= (+ ?ld ?lf) 0))
=>
  (assert (exec (step ?ks) (action CheckFinish) (param1 ?rt) (param2 ?ct)))
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"-CLEAN] CheckFinish" crlf)
  )
)

;regola per pulire il tavolo se l'ordine era delayed o finish.
(defrule strategy-do-CleanTable
  (declare(salience 10))
  (status (step ?current))
  (debug ?level)
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-drink ?ld) (l-food ?lf))
  (K-table (table-id ?id) (pos-r ?rt) (pos-c ?ct) (clean no))
  (exec-order (table-id ?id) (phase 6) (status  ?status))
  (test(or(=(str-compare ?status "delayed")0) (=(str-compare ?status "finish")0)))
  ;controllo che l'agente posso operare sul tavolo.
  (or (and (test(= ?ra ?rt)) (test(= ?ca (+ ?ct 1))))
      (and (test(= ?ra ?rt)) (test(= ?ca (- ?ct 1))))
      (and (test(= ?ra (+ ?rt 1))) (test(= ?ca ?ct)))
      (and (test(= ?ra (- ?rt 1))) (test(= ?ca ?ct)))
  )
  ; controlla che l'agente sia scarico
  (test (= (+ ?ld ?lf) 0))
=>
  (assert (exec (step ?ks) (action CleanTable) (param1 ?rt) (param2 ?ct)))
  (assert (complete-order ?status))
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"-CLEAN] CleanTable" crlf)
  )
)
;Regola che cancella gli ordini di finish precedenti all'ordine che sto servendo (in questo caso sto servendo un ordine delayed)
;Se servo prima un ordine delayed di un ordin finish, quando pulisco l'ordine finish diventa completato
(defrule strategy-complete-previous-order-finish
  (declare(salience 7))
  ?f1<-(complete-order delayed)
  (debug ?level)
  (status (step ?current))
  (exec-order (table-id ?id) (step ?ds) (phase 6) (status delayed))
  ?f2<-(exec-order (table-id ?id) (step ?fs&:(< ?fs ?ds)) (status finish) (phase 0))
  ?f3<-(qty-order-sum (type finish) (pen ?pen) (qty-fo ?sfo) (qty-do ?sdo))
=>
  (retract ?f1)
  (modify ?f2 (phase COMPLETED))
  (modify ?f3 (pen =(- ?pen 3)))
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step " ?fs " of table: " ?id " is completed" crlf)
  )
)

;Regola che imposta a Accepted gli ordini delayed successivi alla finish. (in questo caso sto servendo una finish)
(defrule strategy-set-as-accepted-next-delayed-orders
  (declare(salience 7))
  ?f1<-(complete-order finish)
  (exec-order (table-id ?id) (step ?fs) (phase 6) (status finish))
  ?f2<-(exec-order (table-id ?id) (step ?ds&:(>= ?ds ?fs)) (status delayed) (phase 0) (drink-order ?do) (food-order ?fo))
  ?f3<-(qty-order-sum (type accepted) (pen ?pen1) (qty-fo ?sfo1) (qty-do ?sdo1))
  ?f4<-(qty-order-sum (type delayed) (pen ?pen2) (qty-fo ?sfo2) (qty-do ?sdo2))
=>
  (retract ?f1)
  (modify ?f2 (status accepted))
  (modify ?f3 (pen(+ ?pen1 (+ ?do ?fo))) (qty-fo(+ ?sfo1 ?fo))  (qty-do(+ ?sdo1 ?do))) ; la penalità rimane quella di delayed perché per env la penalità è quella
  (modify ?f4 (pen (- ?pen2 (+ ?do ?fo))) (qty-fo(- ?sfo2 ?fo)) (qty-do(- ?sdo2 ?do)))
)

; aggiorno lo status delayed ad accepted perché ho appena pulito il tavolo e devo servirlo
(defrule strategy-update-current-order-delayed-to-accepted
  (declare(salience 5))
  ?f1<-(update-current-order-table-cleaned)
  ?f2<-(qty-order-sum (type accepted) (pen ?pen1) (qty-fo ?sfo1) (qty-do ?sdo1))
  ?f3<-(qty-order-sum (type delayed) (pen ?pen2) (qty-fo ?sfo2) (qty-do ?sdo2))
  ?f4<-(exec-order (table-id ?id) (phase 6) (status delayed) (drink-order ?do) (food-order ?fo))
=>
  (retract ?f1)
  (modify ?f2 (pen(+ ?pen1 (+ ?do ?fo))) (qty-fo(+ ?sfo1 ?fo))  (qty-do(+ ?sdo1 ?do)))
  (modify ?f3 (pen (- ?pen2 (+ ?do ?fo))) (qty-fo(- ?sfo2 ?fo)) (qty-do(- ?sdo2 ?do)))
  (modify ?f4 (status accepted) (phase 0)) ; deve riiniziare la fase
)

; aggiorno lo status finish a completed perché ho appena pulito il tavolo (ho appena servito un ordine di tipo finish)
; Attenzione nel caso sia una check-finish questa regola non deve esser considerata (per questo aggiungiamo origin-status)
(defrule strategy-complete-current-order-finish
  (declare(salience 5))
  (status (step ?current))
  (debug ?level)
  ?f1<-(update-current-order-table-cleaned)
  ?f2<-(qty-order-sum (type finish) (pen ?pen) (qty-fo ?sfo) (qty-do ?sdo))
  ?f3<-(exec-order (table-id ?id) (phase 6) (status finish) (step ?fs) (origin-status finish))
=>
  (retract ?f1)
  (modify ?f2 (pen =(- ?pen 3)))
  (modify ?f3 (phase COMPLETED))

  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step " ?fs " of table: " ?id " is completed" crlf)
  )
)


;Se non ho ne da scaricare cibo, ne da scaricare drink ne da pulire il tavolo vado alla fase 7.
(defrule go-phase7
  (declare(salience 4))
  (debug ?level)
  (status (step ?current))
  ?f3<-(exec-order (table-id ?id) (phase 6))
  =>
  (modify ?f3 (phase 7))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F7:s"?current":"?id"] Init Phase 7" crlf)
  )
)
;
; FASE 7 della Strategia: Controllo se l'ordine è stato evaso.
;

;Devo ancora consegnare della roba al tavolo. L'ordine aggiornato torna nella lista degli ordini da servire
(defrule strategy-update-order
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (phase 7) (status accepted) (food-order ?fo) (drink-order ?do))
  ; ho scaricato tutta la roba
  (test (> (+ ?fo ?do) 0))
=>
  (modify ?f1 (phase 0) )

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F7:s"?current":"?id"-SERVE] Order not completed, search order with penality more high" crlf)
  )
)

;Ordine completato, retract fatto service table. Devo trovare il nuovo ordine da evadare.
;Ordine completato se ho scaricato tutta la roba e  l'agente non ha niente (attenzione giusto nella logica di servire un tavolo alla volta)
(defrule strategy-order-completed
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (origin-order-step ?step) (origin-status accepted|delayed|finish) (phase 7) (food-order 0) (drink-order 0))
  ;(K-agent (l-drink 0) (l-food 0))
=>
  (modify ?f1 (phase COMPLETED))
  
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step " ?step " of table: " ?id " is completed" crlf)
  )
)

;Ordine completato, retract fatto service table. Devo trovare il nuovo ordine da evadare.
;Ordine completato se ho scaricato tutta la roba e  l'agente non ha niente (attenzione giusto nella logica di servire un tavolo alla volta)
(defrule strategy-order-completed2
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (origin-order-step ?step) (origin-status check-finish) (phase 7) (food-order 0) (drink-order 0))
  ;(K-agent (l-drink 0) (l-food 0))
=>
  (modify ?f1 (phase COMPLETED))

  
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step " ?step " of table: " ?id " is completed" crlf)
  )
)


(defrule go-to-empty-trash
  (declare (salience 77))
  (go-to-basket)
=>
  (focus EMPTY-TRASH)
)



(defrule update-counter-add
  (declare (salience 150))
  ?f1 <- (add-counter-n-replane)
  ?f2 <- (counter-non-replane (count ?nr))
  =>
  (modify ?f2 (count =(+ ?nr 1)))
  (retract ?f1)
)

(defrule update-counter-less
  (declare (salience 150))
  ?f1 <- (less-counter-n-replane)
  ?f2 <- (counter-non-replane (count ?nr))
  =>
  (modify ?f2 (count =(- ?nr 1)))
  (retract ?f1)
)



(defmodule SET-PLANE-AT-OK (import AGENT ?ALL) (export ?ALL))

; Imposto il piano a ok
(defrule set-plane
  (declare(salience 10))
  (set-plane-in-position ?rd ?cd)
  ?f1<-(plane  (plane-id ?pid) (pos-end ?rd ?cd) (status failure))
=>
  (modify ?f1 (status ok))
)

(defrule set-plane-2
  (declare(salience 10))
  ?f1<-(set-plane-in-position ?rd ?cd)

=>
  (retract ?f1)
  (pop-focus)
)

(defmodule CLEAN-TABLE-DISTANCE (import AGENT ?ALL) (export ?ALL))

(defrule clean-table-distance
  ?f1<-(table-distance)
=>
  (retract ?f1)
)

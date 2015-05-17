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
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Inform) (table-id ?sen) (time-order ?t) (status accepted) (origin-status accepted) (drink-order ?do) (food-order ?fo) (phase 0) (fail 0) (penality (*(+ ?do ?fo)2))))
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
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Inform) (table-id ?sen) (time-order ?t) (status delayed) (origin-status delayed) (drink-order ?do) (food-order ?fo) (clean no) (phase 0) (fail 0) (penality (+ ?do ?fo))))
)

;Attiva quando ricevo un 'ordine' di finish da un tavolo sporco.
(defrule answer-msg-order3
  (declare (salience 150))
  (status (step ?current))
  (msg-to-agent (request-time ?t) (step ?current) (sender ?sen) (type finish))
=>
  (assert (exec-order (step ?current) (origin-order-step ?current) (action Finish) (table-id ?sen) (time-order ?t) (status finish) (origin-status finish) (drink-order 0) (food-order 0) (clean no) (phase 0) (fail 0) (penality 3)))
)

;
; FASE 1 della Strategia: Ricerca di un tavolo da servire.
;

;Ricerca dell'ordine da servire. La ricerca avviene sia sulle Inform che sulle Finish. Si ricerca l'ordine più vecchio non ancora servito.
(defrule strategy-go-phase1
  (declare (salience 70))
  (status (step ?current))
  (best-pen ?pen)
  (debug ?level)
  ;?f1 <- (last-intention (step ?last) (time ?time))
  ; cerca una exec di tipo inform
  ?f2<-(exec-order (step ?s&:(< ?s ?current)) (action Inform|Finish) (table-id ?sen) (time-order ?t) (status ?status) (penality ?p&:(> ?p ?pen)) (phase 0))
  (not (exec-order (step ?s1&:(<= ?s1 ?s)) (penality ?p2&:(>= ?p2 ?p)) (action Inform|Finish) (phase 0) (time-order ?t1&:(< ?t1 ?t))))

  ; @TODO cambiare per gestire più tavoli
  ;La ricerca avviene solo ne caso non stia servendo nessun altro ordine, ovvero non esiste un ordine che è nelle fasi 1,2,3,4,5,6 o 7
  (not (exec-order (phase 1|2|3|4|4.5|5|6|7)))
=>
  ;(modify ?f1 (step ?next) (time ?t))
  (modify ?f2 (phase 1))

  ;debug
  (if (> ?level 0)
    then
      (printout t " [DEBUG] [F0:s"?current":"-1"] Inizializza Fase 1 - target tavolo: " ?sen crlf)
  )
)

;Trovato l'ordine eseguo la fase di competenza
(defrule strategy-complete-phase1
  (declare (salience 70))
  (status (step ?s1))
  ?f1 <- (exec-order (step ?s2) (drink-order ?do) (food-order ?fo) (table-id ?id) (status ?status)  (phase 1) )
  (K-table (table-id ?id) (clean ?clean))
  (K-agent (l-drink ?ld) (l-food ?lf) (l_d_waste ?ldw) (l_f_waste ?lfw))
=>
  ; se l'ordine è accepted e non ho sporco posso servirlo
  (if (and(=(str-compare ?status "accepted") 0) (= (str-compare ?ldw "no")0 ) (= (str-compare ?lfw "no")0))
  then
    (modify ?f1 (table-id ?id) (phase 2))
  )
  ; se l'ordine è accepted e ho sporco non posso servirlo,inserisco questo ordine al fondo.
  (if (and(=(str-compare ?status "accepted") 0) (or(= (str-compare ?ldw "yes")0 ) (= (str-compare ?lfw "yes")0)))
  then
     (modify ?f1 (step ?s1) (phase 0))
  )
  ; se l'ordine è delayed, non ho cibo caricato e il tavolo è sporco vado a pulire il tavolo
  (if (and (= (str-compare ?status "delayed") 0) (= ?lf 0) (= ?ld 0) (=(str-compare ?clean "no")0))
  then
    (modify ?f1 (table-id ?id) (phase 5))
  )
  ; se l'ordine è delayed ma ho del cibo caricato inserisco questo ordine al fondo.
  (if (and(= (str-compare ?status "delayed") 0) (or (> ?lf 0) (> ?ld 0)))
  then
    (modify ?f1 (step ?s1) (phase 0))
  )
  ; se l'ordine è delayed il tavolo è gia pulito ma ho sporco a bordo vado al cestino
  (if (and(= (str-compare ?status "delayed") 0) (=(str-compare ?clean "yes")0) (or(= (str-compare ?ldw "yes")0 ) (= (str-compare ?lfw "yes")0)))
  then
    (modify ?f1 (table-id ?id) (phase 2))
  )
  ; se ho ricevuto una finish e non ho cibo caricato vado a pulire il tavolo
  (if (and(= (str-compare ?status "finish") 0)  (= ?lf 0) (= ?ld 0) (=(str-compare ?clean "no")0) )
  then
    (modify ?f1 (table-id ?id) (phase 5))
  )
  ; se ho ricevuto una finish ma ho del cibo caricato inserisco questo ordine al fondo.
  (if (and(= (str-compare ?status "finish") 0) (or (> ?lf 0) (> ?ld 0)))
  then
    (modify ?f1 (step ?s1) (phase 0))
  )
  ;l'ordine da servire è una finish il tavolo è gia pulito ma ho sporco a bordo vado al cestino
  (if (and(= (str-compare ?status "finish") 0) (=(str-compare ?clean "yes")0) (or(= (str-compare ?ldw "yes")0 ) (= (str-compare ?lfw "yes")0)))
  then
    (modify ?f1 (table-id ?id) (phase 2))
  )
)

;
; FASE 2 della Strategia: Individuare il dispenser/cestino più vicino.
;

; Initializza la fase 2
; =====================

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

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun trash basket (Food)
(defrule distance-manhattan-tb
  (declare (salience 70))
  (exec-order (table-id ?id) (phase 2) (status delayed|finish))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l_f_waste yes))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains TB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type trash-food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun recyclable basket (Drink)
(defrule distance-manhattan-rb
  (declare (salience 70))
  (exec-order (table-id ?id) (phase 2) (status delayed|finish))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l_d_waste yes))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains RB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type trash-drink)))
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
    (printout t " [DEBUG] [F2:s"?current":"?id"] Dispenser/Basket Found: " ?c " in ("?rd1", "?cd1")"  crlf)
    (printout t " [DEBUG] [F3:s"?current":"?id"] Init Phase 3: Pianifica Astar verso dispenser " ?c " in ("?rd1", "?cd1")"  crlf)
  )
)

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
    (printout t " [DEBUG] [F2:s"?current":"?id"] Agent hasn't space available. Useless found dispenser."  crlf)
    (printout t " [DEBUG] [F5:s"?current":"?id"] Init Phase 5, a-star towards table "?id", order (food: "?fo", drink: "?do")" crlf)
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
  (printout t " [INFO] [F3:s"?current":"?id"] Exist a plane for go to the dispenser." crlf)
  (assert (add-counter-n-replane))
)

;Se il piano non esiste allora devo avviare astar per cercare un percorso che mi porti a destinazione.
(defrule strategy-create-plane-3
  (declare (salience 1))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 3) )
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
  (not (plane-exist))
=>
  (assert (less-counter-n-replane))
  (assert (start-astar (pos-r ?rd) (pos-c ?cd)))
  (printout t " [INFO] [F3:s"?current":"?id"] Run Astar to: "?rd ","?cd crlf)
)

;Se il piano esiste allo lo eseguo.
(defrule strategy-execute-plane-3
  (declare (salience 1))
  (exec-order (table-id ?id) (phase 3) )
  ?f1<-(plane-exist ?pid)
  (plane  (plane-id ?pid) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (direction ?d) (status ok))
=>
  (assert (run-plane-astar (plane-id ?pid) (pos-start ?ra ?ca ?d) (pos-end ?rd ?cd) (phase 1)))
  (retract ?f1)
)

;Eseguito il piano, il robot si trova vicino al dispenser/cestino piu vicino.
(defrule strategy-go-phase4
  (declare (salience 1))
  (status (step ?current))
  (debug ?level)
  (plan-executed (plane-id ?pid) (step ?current) (pos-start ?rs ?cs) (pos-end ?rg ?cg) (result ok))
  ?f1<-(exec-order (table-id ?id) (phase 3) )
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type ?c))
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
  (assert (set-plane-in-position ?rd ?cd))
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
; controlla che ci sia ancora del food da caricare
; controlla che non ci sia waste
; controlla che il truckload non sia pieno
; scatena azione di load-food verso dispenser
; scatena diminuzione fl in strategy-service-table
(defrule strategy-do-LoadFood
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 4) (food-order ?fo))
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type FD))        ; posizione del food dispenser
  (test (> ?fo 0))                                                   ; food to load > 0
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (test (< (+ ?lf ?ld) 4))
  (test (> ?fo ?lf))
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
  (strategy-best-dispenser (pos-dispenser ?rd ?cd) (type DD))
  (test (> ?do 0)) ; ci sono ancora drink da caricare
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste no) (l_f_waste no))
  (test (< (+ ?lf ?ld) 4))
  (test (> ?do ?ld))
=>
  (assert (exec (step ?ks) (action LoadDrink) (param1 ?rd) (param2 ?cd)))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F4:s"?current":"?id"] Loading drink in dispenser DD: ("?rd","?cd")" crlf)
  )
)

; regola per scaricare il cibo
; ===========================
; controllo che ci sia del l_f_waste
; controllo che l'agente possa operare sul trash basket ovvero che sia in una posizione adiacente.
(defrule strategy-do-EmptyFood
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  (exec-order (step ?s2) (table-id ?id) (phase 4))
  (strategy-best-dispenser (pos-dispenser ?rfo ?cfo) (type TB))
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
  (printout t " [DEBUG] [F4:s"?current":"?id"] EmptyFood in TrashBasket: ("?rfo","?cfo")" crlf)
  )
)

; regola per scaricare il drink
; ===========================
; controllo che ci sia del l_d_waste
; controllo che l'agente possa operare sul trash basket ovvero che sia in una posizione adiacente.
(defrule strategy-do-Release
  (declare (salience 70))
  (status (step ?current))
  (debug ?level)

  (exec-order (step ?s2) (table-id ?id) (phase 4))
  (strategy-best-dispenser (pos-dispenser ?rfo ?cfo) (type RB))
  ;controllo che l'agente possa operare sul disp.
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
  (printout t " [DEBUG] [F4:s"?current":"?id"] Release drink in RecyclableBasket: ("?rfo","?cfo")" crlf)
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
; FASE 4.5 della Strategia: Controllo se ritornare alla fase 2 sia nel caso debba caricare altra roba, oppure se non ha finito di scaricare lo sporco. Altrimenti vado alla fase 5.
;

;Controllo se deve caricare altra roba
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

;Controllo se ho altro sporco da scaricare.
(defrule strategy-return-phase2_clean
  (status (step ?current))
  (debug ?level)

  ?f1<-(exec-order (table-id ?id) (drink-order ?do) (food-order ?fo) (phase 4.5) (status delayed))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste ?ldw) (l_f_waste ?lfw))
=>
  (if (or (= (str-compare ?ldw "yes") 0) (= (str-compare ?lfw "yes") 0))
  then
    (modify ?f1 (phase 2))

    ;debug
    (if (> ?level 0)
    then
    (printout t " [DEBUG] [F4.5:s"?current":"?id"] Agent has trash, return to Phase 2: agent trash (food: "?lfw", drink: "?ldw")" crlf)
    )
  else
    (modify ?f1 (phase 2) (status accepted))

    ;debug
    (if (> ?level 0)
    then
    (printout t " [DEBUG] [F4.5:s"?current":"?id"] Agent has finished trashing, starting serving table" ?id crlf)
    )
  )

)
; Se era un ordine di finish e non ho sporco a bordo ho finito di pulire e vado alla fase 6. Altrimenti se ho ancora sporco vado alla 2.
(defrule strategy-return-phase2_finish
  (status (step ?current))
  (debug ?level)
  ?f1 <- (exec-order (table-id ?id) (drink-order ?do) (food-order ?fo) (phase 4.5) (status finish))
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld) (l_d_waste ?ldw) (l_f_waste ?lfw))
=>
  (if (or (= (str-compare ?ldw "yes") 0) (= (str-compare ?lfw "yes") 0))
  then
    (modify ?f1 (phase 2))
    ;debug
    (if (> ?level 0)
    then
    (printout t " [DEBUG] [F4.5:s"?current":"?id"] (FINISH) Agent has trash, return to Phase 2: agent trash (food: "?lfw", drink: "?ldw")" crlf)
    )
  else
    (modify ?f1 (phase 6))
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
  (printout t " [DEBUG] [F5:s"?current":"?id"] Init Phase 5, a-star towards table "?id", order (food: "?fo", drink: "?do") action "?a crlf)
  )
)

;
; FASE 5 della Strategia: Esecuzione di astar per determinare il piano per arrivare al tavolo ed esecuzione del piano.
;

;Controlle se esiste un piano per andare al best dispenser/trash con status OK
(defrule strategy-existence-plane-5
  (declare (salience 10))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 5) )
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  (K-agent (pos-r ?ra) (pos-c ?ca))
  (plane (plane-id ?pid)(pos-start ?ra ?ca) (pos-end ?rt ?ct) (status ok))
=>
  (assert (plane-exist ?pid))
  (printout t " [INFO] [F5:s"?current":"?id"] Exist a plane for go to the table." crlf)
  (assert (add-counter-n-replane))
)

;Se il piano non esiste allora devo avviare astar per cercare un percorso che mi porti a destinazione.
(defrule strategy-create-plane-5
  (declare (salience 1))
  (status (step ?current))
  (exec-order (table-id ?id) (phase 5) )
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
  (not (plane-exist))
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
  (declare (salience 1))
  (status (step ?current))
  (debug ?level)
  (plan-executed (step ?current) (pos-start ?rs ?cs) (pos-end ?rt ?ct) (result ok))
  ?f2<-(exec-order (table-id ?id) (phase 5) (drink-order ?do) (food-order ?fo) (status ?a))
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
  (K-table (pos-r ?rt) (pos-c ?ct) (table-id ?id))
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
  ?f1<-(exec-order (step ?s2) (table-id ?id) (phase 5))
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

;regola per pulire il tavolo se l'ordine era delayed o finish.
(defrule strategy-do-CleanTable
  (declare(salience 10))
  (status (step ?current))
  (debug ?level)
  (K-agent (step ?ks) (pos-r ?ra) (pos-c ?ca) (l-drink ?ld) (l-food ?lf))
  (K-table (table-id ?id) (pos-r ?rt) (pos-c ?ct) (clean no))
  (exec-order (table-id ?id) (phase 6) (status ?status))
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
  (if (=(str-compare ?status "finish")0)
    then
    (assert (complete-order ?status))
  )
  (if (=(str-compare ?status "delayed")0)
    then
    (assert (complete-order ?status))
  )
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"-CLEAN] CleanTable" crlf)
  )
)

;Regola che cancella gli ordini di finish precedenti all'ordine che sto servendo. Se servo prima un ordine delayed di un ordin finish, quando pulisco l'ordine finish diventa completato
(defrule strategy-delete-order-finish
  (declare(salience 7))
  ?f1<-(complete-order delayed)
  (exec-order (table-id ?id) (step ?s) (phase 6) (status delayed))
  ?f2<-(exec-order (table-id ?id) (step ?step&:(< ?step ?s)) (status finish) (phase 0))
=>
  (retract ?f1)
  (modify ?f2 (phase COMPLETED))
)

(defrule strategy-set-as-accepted-next-delayed-orders
  (declare(salience 7))
  ?f1<-(complete-order finish)
  (exec-order (table-id ?id) (step ?fs) (phase 6) (status finish))
  ?f2<-(exec-order (table-id ?id) (step ?ds&:(>= ?ds ?fs)) (status delayed) (phase 0) (drink-order ?do) (food-order ?fo))
=>
  (retract ?f1)
  (modify ?f2 (status accepted))
)

;Se non ho altre consumazioni da scaricare vado alla fase 7.
(defrule go-phase7-1
  (declare(salience 5))
  (debug ?level)
  (status (step ?current))
  ?f3<-(exec-order (table-id ?id) (phase 6) (status accepted))
  =>
  (modify ?f3 (phase 7))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F7:s"?current":"?id"] Init Phase 7" crlf)
  )
)

;Pulito il tavolo vado alla fase 7.
(defrule go-phase7-2
  (declare(salience 5))
  (debug ?level)
  (status (step ?current))
  ?f3<-(exec-order (table-id ?id) (phase 6) (status delayed|finish))
  =>
  (modify ?f3 (phase 7) (clean yes))

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
(defrule strategy-return-search-order
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (phase 7) (status accepted) (food-order ?fo) (drink-order ?do))
  ; ho scaricato tutta la roba
  (test (> (+ ?fo ?do) 0))
=>
  (modify ?f1 (phase 0))

  ;debug
  (if (> ?level 0)
    then
    (printout t " [DEBUG] [F7:s"?current":"?id"-SERVE] Order not completed, search order with penality more high" crlf)
  )
)

;Devo ancora buttare lo sporco. Devo ricercare il cestino più vicino (FASE 2)
(defrule strategy-return-phase7-to-2_delayed
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (phase 7) (status delayed|finish))
  (K-agent (l_d_waste ?ldw) (l_f_waste ?lfw))
  (test (or (= (str-compare ?ldw "yes") 0) (= (str-compare ?lfw "yes") 0)))
=>
  (modify ?f1 (phase 2))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F7:s"?current":"?id"-CLEAN] CleanTable, sono pieno di trash, return to phase 2" crlf)
  )
)

;Se ho completato un ordine finish/delayed, setto a completed tutti gli ordini finish o delayed di cui avevo solo pulito il tavolo.
(defrule strategy-order-finish-delayed-completed
  (status (step ?current))
  (debug ?level)
  (exec-order (table-id ?id) (step ?step) (phase 7) (food-order 0) (drink-order 0))
  ?f1<-(exec-order (table-id ?id2) (step ?step2) (phase 0) (food-order 0) (drink-order 0) (clean yes))
=>
  (modify ?f1 (phase COMPLETED))

  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step" ?step2 " of table:" ?id2 " is completed" crlf)
  )
)

;Ordine completato se ho scaricato tutta la roba e  l'agente non ha niente (attenzione giusto nella logica di servire un tavolo alla volta)
;Ordine completato, devo trovare il nuovo ordine da evadare.
(defrule strategy-order-completed
  (status (step ?current))
  (debug ?level)
  ?f1<-(exec-order (table-id ?id) (step ?step) (phase 7) (food-order 0) (drink-order 0))
=>
  (modify ?f1 (phase COMPLETED))
  ;debug
  (if (> ?level 0)
  then
  (printout t " [DEBUG] [F6:s"?current":"?id"] Phase 7: Order at step" ?step " of table:" ?id " is completed" crlf)
  )
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

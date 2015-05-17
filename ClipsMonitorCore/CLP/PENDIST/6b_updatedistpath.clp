(defmodule UPDATE-DISTPATH (import AGENT ?ALL) (export ?ALL))

(defrule init-ud
(declare (salience 70))
(update-order-distpath ?table ?step 0)
(not(min-distpath ?x))
=>
(assert (min-distpath 1000))
)

; ===========================================
; MANHATTAN RECYCLED FASE 0/3
; ===========================================
;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun food-dispenser
(defrule distance-manhattan-fo-1
  (declare (salience 70))
  (update-order-distpath ?table ?step 0)
  (exec-order (food-order ?fo) (table-id ?table) (phase 0) (status accepted))
  (K-agent (pos-r ?ra) (pos-c ?ca) (l-food ?lf) (l-drink ?ld))
  (test (> ?fo 0))
  (test (< ?lf ?fo))
  (test (< (+ ?lf ?ld) 4))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains FD))
  =>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?ra ?rfo)) (abs(- ?ca ?cfo)))) (type food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun drink-dispenser
(defrule distance-manhattan-do-1
  (declare (salience 70))
  (update-order-distpath ?table ?step 0)
  (exec-order (drink-order ?do) (table-id ?table) (phase 0) (status accepted))
  (K-agent (pos-r ?ra)(pos-c ?ca) (l-food ?lf) (l-drink ?ld))
  (test (> ?do 0))
  (test (< ?ld ?do))
  (test (< (+ ?lf ?ld) 4))

  (K-cell (pos-r ?rdo) (pos-c ?cdo) (contains DD))
=>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance (+ (abs(- ?ra ?rdo)) (abs(- ?ca ?cdo)))) (type drink)))
)

; calcola distanza robot-tavolo
; se ordine è di tipo delayed!finish
(defrule distance-manhattan-robot-table-1
  (declare (salience 70))
  (update-order-distpath ?table ?step 0)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (K-agent (pos-r ?ra) (pos-c ?ca))
  (K-table (table-id ?table) (pos-r ?rt) (pos-c ?ct))
  =>
  (assert (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rt ?ct) (distance (+ (abs(- ?ra ?rt)) (abs(- ?ca ?ct))))))
)

;Regola che cerca il dispenser/tavolo più vicino
(defrule search-best-dispenser-1
  (declare (salience 60))
  (update-order-distpath ?table ?step 0)
  (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rd1 ?cd1) (distance ?d))
  (not (strategy-distance-dispenser  (pos-start ?ra ?ca) (pos-end ?rd2 ?cd2) (distance ?dist&:(< ?dist ?d)) ))
  (K-cell (pos-r ?rd1) (pos-c ?cd1) (contains ?c))
=>
  (assert (best-distpath (id 0) (distance ?d) (pos-dispenser ?rd1 ?cd1) (type ?c)))
)

; pulisce le distanze ai dispensers/tavoli
(defrule clean-distance-dispenser-1
  (declare (salience 80))
  (update-order-distpath ?table ?step 0)
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  ?f1 <- (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d))
=>
  (retract ?f1)
)

; cambia fase, dopo che ho pulito le distance ai dispenser/tavoli
(defrule ud-go-to-phase1
  (declare (salience 80))
  ?f1<-(update-order-distpath ?table ?step 0)
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (not(strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d)))
=>
  (assert(update-order-distpath ?table ?step 1))
  (retract ?f1)
)

; ===========================================
; MANHATTAN RECYCLED FASE 1/3
; cerca la distanza tra bestdispenser e altrodispenser
; ===========================================
(defrule distance-manhattan-fo-2
  (declare (salience 70))
  (update-order-distpath ?table ?step 1)
  (exec-order (food-order ?fo) (table-id ?table) (phase 0) (status accepted))
  (K-agent (l-food ?lf) (l-drink ?ld))
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (test (> ?fo 0))
  (test (< ?lf ?fo))
  (test (< (+ ?lf ?ld) 4))
  ; se ho già trovato un best dispenser non lo cerco di nuovo
  (test (neq ?c FD))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains FD))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rd ?rfo)) (abs(- ?cd ?cfo)))) (type food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun drink-dispenser
(defrule distance-manhattan-do-2
  (declare (salience 70))
  (update-order-distpath ?table ?step 1)
  (exec-order (drink-order ?do) (table-id ?table) (phase 0) (status accepted))
  (K-agent (l-food ?lf) (l-drink ?ld))
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (test (> ?do 0))
  (test (< ?ld ?do))
  (test (< (+ ?lf ?ld) 4))
  ; se ho già trovato un best dispenser non lo cerco di nuovo
  (test (neq ?c DD))
  (K-cell (pos-r ?rdo) (pos-c ?cdo) (contains DD))
=>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rdo ?cdo) (distance (+ (abs(- ?rd ?rdo)) (abs(- ?cd ?cdo)))) (type drink)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun trash basket (Food)
(defrule distance-manhattan-tb-2
  (declare (salience 70))
  (update-order-distpath ?table ?step 1)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 0) (pos-dispenser ?rt ?ct) (type ?c))
  (K-table (table-id ?table) (l-food ?f&:(> ?f 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains TB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  ;(test (~ ?c TB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rt ?ct) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rt ?rfo)) (abs(- ?ct ?cfo)))) (type trash-food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun recyclable basket (Drink)
(defrule distance-manhattan-rb-2
  (declare (salience 70))
  (update-order-distpath ?table ?step 1)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 0) (pos-dispenser ?rt ?ct) (type ?c))
  (K-table (table-id ?table) (l-drink ?d&:(> ?d 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains RB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  ;(test (~ ?c RB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rt ?ct) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rt ?rfo)) (abs(- ?ct ?cfo)))) (type trash-drink)))
)

;Regola che cerca il dispenser/cestino più vicino
(defrule search-best-dispenser-2
  (declare (salience 60))
  (status (step ?current))
  (update-order-distpath ?table ?step 1)
  (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rd1 ?cd1) (distance ?d))
  (not (strategy-distance-dispenser  (pos-start ?ra ?ca) (pos-end ?rd2 ?cd2) (distance ?dist&:(< ?dist ?d)) ))
  (K-cell (pos-r ?rd1) (pos-c ?cd1) (contains ?c))
=>
  (assert (best-distpath (id 1) (distance ?d) (pos-dispenser ?rd1 ?cd1) (type ?c)))
)

; pulisce le distanze ai dispensers/cestini
(defrule clean-distance-dispenser-2
  (declare (salience 80))
  (status (step ?current))
  (update-order-distpath ?table ?step 1)
  (best-distpath (id 1) (pos-dispenser ?rd ?cd) (type ?c))
  ?f1 <- (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d))
=>
  (retract ?f1)
)

; Nel caso ci sia un (best-distpath (id 1)) vado alla fase 2
(defrule ud-go-to-phase2-caso1
  (declare (salience 80))
  ?f1<-(update-order-distpath ?table ?step 1)
  (best-distpath (id 1) (pos-dispenser ?rd ?cd) (type ?c))
  (not(strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d)))
=>
  (assert(update-order-distpath ?table ?step 2))
  (retract ?f1)
)

; Nel caso non ci sia un (best-distpath (id 1)) vado alla fase 2
(defrule ud-go-to-phase2-caso2
  (declare (salience 50))
  ?f1<-(update-order-distpath ?table ?step 1)
  (not(best-distpath (id 1) (pos-dispenser ?rd1 ?cd1) (type ?c)))
  =>
  (assert (update-order-distpath ?table ?step 2))
  (retract ?f1)
)

; ===========================================
; MANHATTAN RECYCLED FASE 2/3
; cerca la distanza tra l'ultimo dispencer trovato e il tavolo
; ===========================================
(defrule distance-manhattan-bestdispenser-table-3-caso1
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (food-order ?fo) (table-id ?table) (phase 0) (status accepted))
  (K-table (table-id ?table) (pos-r ?rt) (pos-c ?ct))
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (not(best-distpath (id 1)))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rt ?ct) (distance (+ (abs(- ?rd ?rt)) (abs(- ?cd ?ct))))))
)

(defrule distance-manhattan-bestdispenser-table-3-caso2
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (food-order ?fo) (table-id ?table) (phase 0) (status accepted))
  (K-table (table-id ?table) (pos-r ?rt) (pos-c ?ct))
  (best-distpath (id 1) (pos-dispenser ?rd ?cd) (type ?c))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rt ?ct) (distance (+ (abs(- ?rd ?rt)) (abs(- ?cd ?ct))))))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun trash basket (Food)
(defrule distance-manhattan-tb-3-caso1
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (not(best-distpath (id 1)))
  (K-table (table-id ?table) (l-food ?f&:(> ?f 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains TB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  (test (neq ?c TB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rd ?rfo)) (abs(- ?cd ?cfo)))) (type trash-food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun trash basket (Food)
(defrule distance-manhattan-tb-3-caso2
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 1) (pos-dispenser ?rd ?cd) (type ?c))
  (K-table (table-id ?table) (l-food ?f&:(> ?f 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains TB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  (test (neq ?c TB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rd ?rfo)) (abs(- ?cd ?cfo)))) (type trash-food)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun recyclable basket (Drink)
(defrule distance-manhattan-rb-3-caso1
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 0) (pos-dispenser ?rd ?cd) (type ?c))
  (not(best-distpath (id 1)))
  (K-table (table-id ?table) (l-drink ?d&:(> ?d 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains RB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  (test (neq ?c RB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rd ?rfo)) (abs(- ?cd ?cfo)))) (type trash-drink)))
)

;Regola che calcola la distanza di manhattan dalla posizione corrente del robot a ciascun recyclable basket (Drink)
(defrule distance-manhattan-rb-3-caso2
  (declare (salience 70))
  (update-order-distpath ?table ?step 2)
  (exec-order (table-id ?table) (phase 0) (status delayed|finish))
  (best-distpath (id 1) (pos-dispenser ?rd ?cd) (type ?c))
  (K-table (table-id ?table) (l-drink ?d&:(> ?d 0)))
  (K-cell (pos-r ?rfo) (pos-c ?cfo) (contains RB))
  ; se ho già trovato un best dispenser di questo tipo non lo cerco di nuovo
  (test (neq ?c RB))
  =>
  (assert (strategy-distance-dispenser (pos-start ?rd ?cd) (pos-end ?rfo ?cfo) (distance (+ (abs(- ?rd ?rfo)) (abs(- ?cd ?cfo)))) (type trash-drink)))
)

;Regola che cerca il dispenser/cestino più vicino
(defrule search-best-dispenser-3
  (declare (salience 60))
  (status (step ?current))
  (update-order-distpath ?table ?step 2)
  (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rd1 ?cd1) (distance ?d))
  (not (strategy-distance-dispenser  (pos-start ?ra ?ca) (pos-end ?rd2 ?cd2) (distance ?dist&:(< ?dist ?d)) ))
  (K-cell (pos-r ?rd1) (pos-c ?cd1) (contains ?c))
=>
  (assert (best-distpath (id 2) (distance ?d) (pos-dispenser ?rd1 ?cd1) (type ?c)))
)

; pulisce le distanze ai dispensers/cestini
(defrule clean-distance-dispenser-3
  (declare (salience 80))
  (status (step ?current))
  (update-order-distpath ?table ?step 2)
  (best-distpath (id 2) (pos-dispenser ?rd ?cd) (type ?c))
  ?f1 <- (strategy-distance-dispenser (pos-start ?ra ?ca) (pos-end ?rdo ?cdo) (distance ?d))
=>
  (retract ?f1)
)

(defrule ud-go-to-distpath-phase4-caso1
  (declare (salience 50))
  ?f1<-(update-order-distpath ?table ?step 2)
  (best-distpath (id 2) (pos-dispenser ?rd1 ?cd1) (type ?c))
  =>
  (assert (update-order-distpath ?table ?step 3))
  (retract ?f1)
)

(defrule ud-go-to-distpath-phase4-caso2
  (declare (salience 50))
  ?f1<-(update-order-distpath ?table ?step 2)
  (best-distpath (id 1) (pos-dispenser ?rd1 ?cd1) (type ?c))
  (not(best-distpath (id 2)))
  =>
  (assert (update-order-distpath ?table ?step 3))
  (retract ?f1)
)


; ===========================================
; UPDATE DISTANCE 3/3
; Aggiorne le distanze
; ===========================================
;

(defrule ud-update-order-distance-caso1
  (declare (salience 50))
  ?f0<-(update-order-distpath ?table ?step 3)
  ?f1<-(exec-order (food-order ?fo) (table-id ?table) (phase 0)
    )
  ?f2<-(best-distpath (id 0) (distance ?dist2) (pos-dispenser ?rd1 ?cd1) (type ?c1))
  ?f3<-(best-distpath (id 1) (distance ?dist3) (pos-dispenser ?rd2 ?cd2) (type ?c2))
  ?f4<-(best-distpath (id 2) (distance ?dist4) (pos-dispenser ?rd3 ?cd3) (type ?c3))
  ?f5<-(min-distpath ?md)
  =>
  (if (< (+ ?dist2 ?dist3 ?dist4 ) ?md)
    then
      (retract ?f5)
      (assert (min-distpath (+ ?dist2 ?dist3 ?dist4) ))
  )

  (modify ?f1 (distpath =(+ ?dist2 ?dist3 ?dist4) ))
  (retract ?f0)
  (retract ?f2)
  (retract ?f3)
  (retract ?f4)
)

(defrule ud-update-order-distance-caso2
  (declare (salience 50))
  ?f0<-(update-order-distpath ?table ?step 3)
  ?f1<-(exec-order (food-order ?fo) (table-id ?table) (phase 0))
  ?f2<-(best-distpath (id 0) (distance ?dist2) (pos-dispenser ?rd1 ?cd1) (type ?c1))
  ?f4<-(best-distpath (id 2) (distance ?dist4) (pos-dispenser ?rd2 ?cd2) (type ?c2))
  ?f5<-(min-distpath ?md)
  =>
  (if (< (+ ?dist2 ?dist4 ) ?md)
    then
      (retract ?f5)
      (assert (min-distpath (+ ?dist2 ?dist4) ))
  )
  (modify ?f1 (distpath =(+ ?dist2 ?dist4) ))
  (retract ?f0)
  (retract ?f2)
  (retract ?f4)
)

(defrule ud-update-order-distance-caso3
  (declare (salience 50))
  ?f0<-(update-order-distpath ?table ?step 3)
  ?f1<-(exec-order (food-order ?fo) (table-id ?table) (phase 0))
  ?f2<-(best-distpath (id 0) (distance ?dist2) (pos-dispenser ?rd1 ?cd1) (type ?c1))
  ?f4<-(best-distpath (id 1) (distance ?dist3) (pos-dispenser ?rd2 ?cd2) (type ?c2))
  ?f5<-(min-distpath ?md)
  =>
  (if (< (+ ?dist2 ?dist3 ) ?md)
    then
      (retract ?f5)
      (assert (min-distpath (+ ?dist2 ?dist3) ))
  )
  (modify ?f1 (distpath =(+ ?dist2 ?dist3) ))
  (retract ?f0)
  (retract ?f2)
  (retract ?f4)
)

(defrule ud-finish
  (declare (salience 1))
  =>
  ;(halt)
  (pop-focus)
)
;(defrule ud-update-priority-value
;  (declare (salience 50))
;  ?f1<-(exec-order (food-order ?fo) (table-id ?table) (distpath ?dp) (penality ?pen) (phase 0) (status accepted))
;  (test (> ?dp 0))
;  (min-distpath ?md)
;  =>
;  ;(modify ?f1 (priority =(* ?pen (* (/ 1 ?dp) 100) )))
;  (modify ?f1 (priority =(* ?pen (+ (* (/ ?md ?dp) 0.5) 0.5) )))
;  (halt)
;  (pop-focus)
;)
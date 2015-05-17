(defmodule ASTAR (import AGENT ?ALL) (export ?ALL))

(deftemplate node (slot ident) (slot gcost) (slot fcost) (slot father) (slot pos-r)
                  (slot pos-c) (slot direction) (slot open))

(deftemplate newnode  (slot ident) (slot gcost) (slot fcost) (slot father) (slot pos-r)
                      (slot pos-c) (slot direction)
)

(defrule S0
  (K-agent (pos-r ?r) (pos-c ?c) (direction ?d))
=>
        (assert (node (ident 0) (gcost 0) (fcost 0) (father NA) (pos-r ?r) (pos-c ?c) (direction ?d) (open yes)) )
        (assert (start ?r ?c ?d))
        (assert (current 0))
        (assert (lastnode 0))
        (assert (open-worse 0))
        (assert (open-better 0))
        (assert (alreadyclosed 0))
        (assert (numberofnodes 0))
)

;Definiamo i goal. Sono di due tipi.
;Se la cella di destinazione è vuota il goal è rappresentato da (goal-astar ?r ?c)
;Se la cella di destianzione è un tavolo,un dispenser o un cestino il goal è rappresentato dalle 4 celle adiacenti al tavolo.
(defrule S0-goal-table
        (goal-astar ?r ?c)
        (K-cell (pos-r ?r) (pos-c ?c) (contains Table|DD|FD|TB|RB))
=>
  (assert (end-astar (- ?r 1) ?c))
  (assert (end-astar (+ ?r 1) ?c))
  (assert (end-astar ?r (- ?c 1)))
  (assert (end-astar ?r (+ ?c 1)))
)

(defrule S0-goal-empty
        (goal-astar ?r ?c)
  (K-cell (pos-r ?r) (pos-c ?c) (contains Empty))
=>
  (assert (end-astar ?r ?c))
)

(defrule achieved-goal
  (declare (salience 100))
  (debug ?level)
  (current ?id)
  (goal-astar ?gr ?gc)
  (end-astar ?r ?c)
  (node (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?) (gcost ?g))
  (K-cell (pos-r ?r) (pos-c ?c) (contains Empty))
=>
  (assert (stampa ?id))
  (assert (cost-solution ?g))

  ;debug
  (if (> ?level 2)
    then
    (printout t " [DEBUG] [ASTAR] Esiste soluzione per goal (" ?gr "," ?gc ") in (" ?r "," ?c ")  con costo "  ?g crlf)
  )

  (focus PRINT)
)

;
; FORWARD
;

(defrule forward-apply-north
  (declare (salience 50))
  (current ?curr)
  (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction north) (open yes))
  (K-cell (pos-r =(+ ?r 1)) (pos-c ?c) (contains Empty))
=>
  (assert (apply ?curr forward ?r ?c north (+ ?r 1) ?c))
)

(defrule forward-apply-south
  (declare (salience 50))
  (current ?curr)
  (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction south) (open yes))
  (K-cell (pos-r =(- ?r 1)) (pos-c ?c) (contains Empty))
=>
  (assert (apply ?curr forward ?r ?c south (- ?r 1) ?c))
)

(defrule forward-apply-east
  (declare (salience 50))
  (current ?curr)
  (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction east) (open yes))
  (K-cell (pos-r ?r) (pos-c =(+ ?c 1)) (contains Empty))
=>
  (assert (apply ?curr forward ?r ?c east ?r (+ ?c 1)))
)

(defrule forward-apply-west
  (declare (salience 50))
  (current ?curr)
  (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction west) (open yes))
  (K-cell (pos-r ?r) (pos-c =(- ?c 1)) (contains Empty))
=>
  (assert (apply ?curr forward ?r ?c west ?r (- ?c 1)))
)

;Controllare costo distanza di manhattan

(defrule forward-exec-astar
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr forward ?r ?c ?d ?r1 ?c1)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)
=>
  (assert (exec-astar ?curr (+ ?n 1) Forward ?d ?r ?c)
  (newnode (ident (+ ?n 1)) (pos-r ?r1) (pos-c ?c1) (direction ?d)
  (gcost (+ ?g 2)) (fcost (+ (abs (- ?x ?r1)) (abs (- ?y ?c1)) ?g 1))
  (father ?curr)))
  (retract ?f1)
  (focus NEW)
)

;
; TURNLEFT
;

(defrule turnleft-apply-south
  (declare (salience 50))
  (current ?curr)
  (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction south) (open yes))
=>
  (assert (apply ?curr turnleft-south ?r ?c south))
)

(defrule turnleft-exec-astar-south
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnleft-south ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 2) Turnleft ?d ?r ?c)
              (newnode (ident (+ ?n 2)) (pos-r ?r) (pos-c ?c) (direction east)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

(defrule turnleft-apply-west
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction west) (open yes))


        => (assert (apply ?curr turnleft-west ?r ?c west))
)

(defrule turnleft-exec-astar-west
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnleft-west ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 3) Turnleft ?d ?r ?c)
              (newnode (ident (+ ?n 3)) (pos-r ?r) (pos-c ?c) (direction south)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
    (retract ?f1)
    (focus NEW)
)

(defrule turnleft-apply-north
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction north) (open yes))


        => (assert (apply ?curr turnleft-north ?r ?c north))
)

(defrule turnleft-exec-astar-north
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnleft-north ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 4) Turnleft ?d ?r ?c)
              (newnode (ident (+ ?n 4)) (pos-r ?r) (pos-c ?c) (direction west)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

(defrule turnleft-apply-east
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction east) (open yes))


        => (assert (apply ?curr turnleft-east ?r ?c east))
)

(defrule turnleft-exec-astar-east
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnleft-east ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 5) Turnleft ?d ?r ?c)
              (newnode (ident (+ ?n 5)) (pos-r ?r) (pos-c ?c) (direction north)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

;
; TURNRIGHT
;

(defrule turnright-apply-south
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction south) (open yes))


        => (assert (apply ?curr turnright-south ?r ?c south))
)

(defrule turnright-exec-astar-south
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnright-south ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 6) Turnright ?d ?r ?c)
              (newnode (ident (+ ?n 6)) (pos-r ?r) (pos-c ?c) (direction west)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

(defrule turnright-apply-west
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction west) (open yes))


        => (assert (apply ?curr turnright-west ?r ?c west))
)

(defrule turnright-exec-astar-west
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnright-west ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 7) Turnright ?d ?r ?c)
              (newnode (ident (+ ?n 7)) (pos-r ?r) (pos-c ?c) (direction north)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

(defrule turnright-apply-north
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction north) (open yes))


        => (assert (apply ?curr turnright-north ?r ?c north))
)

(defrule turnright-exec-astar-north
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnright-north ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 8) Turnright ?d ?r ?c)
              (newnode (ident (+ ?n 8)) (pos-r ?r) (pos-c ?c) (direction east)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)

(defrule turnright-apply-east
        (declare (salience 50))
        (current ?curr)
        (node (ident ?curr) (pos-r ?r) (pos-c ?c) (direction east) (open yes))


        => (assert (apply ?curr turnright-east ?r ?c east))
)

(defrule turnright-exec-astar-east
        (declare (salience 50))
        (current ?curr)
        (lastnode ?n)
 ?f1<-  (apply ?curr turnright-east ?r ?c ?d)
        (node (ident ?curr) (gcost ?g))
        (goal-astar ?x ?y)

   => (assert (exec-astar ?curr (+ ?n 9) Turnright ?d ?r ?c)
              (newnode (ident (+ ?n 9)) (pos-r ?r) (pos-c ?c) (direction south)
                       (gcost (+ ?g 1)) (fcost (+ (abs (- ?x ?r)) (abs (- ?y ?c)) ?g 1))

        (father ?curr)))
        (retract ?f1)
        (focus NEW)
)


(defrule change-current
 (declare (salience 49))
  ?f1<-(current ?curr)
  ?f2<-(node (ident ?curr))
  (node (ident ?best&:(neq ?best ?curr)) (fcost ?bestcost) (open yes))
  (not (node (ident ?id&:(neq ?id ?curr)) (fcost ?gg&:(< ?gg ?bestcost)) (open yes)))
  ?f3<-(lastnode ?last)
=>    
  (assert (current ?best) (lastnode (+ ?last 10)))
  (retract ?f1 ?f3)
  (modify ?f2 (open no))
)

(defrule close-empty
  (declare (salience 49))
  (debug ?level)
  ?f1<-(current ?curr)
  ?f2<-(node (ident ?curr))
  (not (node (ident ?id&:(neq ?id ?curr))  (open yes)))
=>
  (retract ?f1)
  (modify ?f2 (open no))
  (assert (astar-solution (value no)))

  ;debug
  (if (> ?level 2)
    then
    (printout t " [DEBUG] [ASTAR] fail (last  node expanded " ?curr ")" crlf)
  )

  (focus CLEAN)
)

(defmodule NEW (import ASTAR ?ALL) (export ?ALL))

(defrule check-closed
(declare (salience 50))
 ?f1 <-    (newnode (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d))
           (node (ident ?old) (pos-r ?r) (pos-c ?c) (open no) (direction ?d))
 ?f2 <-    (alreadyclosed ?a)
    =>
           (assert (alreadyclosed (+ ?a 1)))
           (retract ?f1)
           (retract ?f2)
           (pop-focus))

(defrule check-open-worse
(declare (salience 50))
 ?f1 <-    (newnode (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g) (father ?anc))
           (node (ident ?old) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g-old) (open yes))
           (test (or (> ?g ?g-old) (= ?g-old ?g)))
 ?f2 <-    (open-worse ?a)
    =>
           (assert (open-worse (+ ?a 1)))
           (retract ?f1)
           (retract ?f2)
           (pop-focus))

(defrule check-open-better
(declare (salience 50))
 ?f1 <-    (newnode (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g) (fcost ?f) (father ?anc))
 ?f2 <-    (node (ident ?old) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g-old) (open yes))
           (test (<  ?g ?g-old))
 ?f3 <-    (open-better ?a)
    =>     (assert (node (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g) (fcost ?f) (father ?anc) (open yes))
                   )
           (assert (open-better (+ ?a 1)))
           (retract ?f1 ?f2 ?f3)
           (pop-focus))

(defrule add-open
       (declare (salience 49))
 ?f1 <-    (newnode (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g) (fcost ?f)(father ?anc))
 ?f2 <-    (numberofnodes ?a)
    =>     (assert (node (ident ?id) (pos-r ?r) (pos-c ?c) (direction ?d) (gcost ?g) (fcost ?f)(father ?anc) (open yes))
                   )
           (assert (numberofnodes (+ ?a 1)))
           (retract ?f1 ?f2)
           (pop-focus))

(defmodule PRINT (import ASTAR ?ALL) (export ?ALL))

(defrule print0
    (declare (salience 4))
    (stampa ?id)
    (exec-astar ?anc ?id $?)
    (next-plane-id ?plane-id)
    (start ?rs ?cs ?d1)
    (goal-astar ?rg ?cg)
    (cost-solution ?g)
=>
    (assert (stampa ?anc))
    (assert (plane (plane-id ?plane-id) (pos-start ?rs ?cs) (pos-end ?rg ?cg) (direction ?d1) (cost ?g) (status ok)))
)


(defrule stampaSol1
  (declare (salience 3))
  (debug ?level)
  (next-plane-id ?plane-id)
  ?f<-(stampa ?id)
  (node (ident ?id) (father ?anc&~NA))
  (exec-astar ?anc ?id ?oper ?d ?r ?c)
=>
  (assert (step-plane  (plane-id ?plane-id) (action ?oper) (direction ?d) (pos-start ?r ?c) (father ?anc) (child ?id)))
  (retract ?f)

  ;debug
  (if (> ?level 2)
    then
    (printout t " [DEBUG] [ASTAR] Eseguo azione " ?oper " direzione " ?d " da stato (" ?r "," ?c ") " crlf)
  )
)


;regola per generare un fatto di tipo plane, quando deve essere eseguito un piano a costo 0
(defrule stampaSolZeroCost
  (declare (salience 2))
  ?f<-(stampa ?id)
  (next-plane-id ?plane-id)
  (start ?rs ?cs ?d1)
  (goal-astar ?rg ?cg)
  (cost-solution 0)
  
=>
  (assert (plane (plane-id ?plane-id) (pos-start ?rs ?cs) (pos-end ?rg ?cg) (cost 0) (status ok)))
)

(defrule stampa-fine
    (declare (salience 1))
    (stampa ?id)
    (node (ident ?id) (father ?anc&NA))
    (open-worse ?worse)
    (open-better ?better)
    (alreadyclosed ?closed)
    (numberofnodes ?n )
    ?f1<-(next-plane-id ?plane-id)
=>
;   (printout t " stati espansi " ?n crlf)
;   (printout t " stati generati gia' in closed " ?closed crlf)
;   (printout t " stati generati gia' in open (open-worse) " ?worse crlf)
;   (printout t " stati generati gia' in open (open-better) " ?better crlf)
    (retract ?f1)
    (assert (next-plane-id (+ ?plane-id 1)))
    (focus CLEAN)
)

(defmodule CLEAN (import ASTAR ?ALL) (export ?ALL))

(defrule clean-node
  (declare (salience 1))
  ?fn <- (node)
=>
  (retract ?fn)
)

(defrule clean-exec
  (declare (salience 1))
  ?fe <- (exec-astar $?)
=>
  (retract ?fe)
)

(defrule clean-goal
  (declare (salience 1))
  ?fe <- (end-astar $?)
=>
  (retract ?fe)
)

(defrule clean
  (declare(salience 1))
  ?fg <- (goal-astar $?)
  ?fs <- (start $?)
=>
  (retract ?fg)
  (retract ?fs)
)
; da rimuovere quando in clips risolvono bug
(defrule clean-init
(declare(salience 1))
  ?f2<-(lastnode ?)
  ?f3<-(open-worse ?)
  ?f4<-(open-better ?)
  ?f5<-(alreadyclosed ?)
  ?f6<-(numberofnodes ?)
=>
  (retract ?f2 ?f3 ?f4 ?f5 ?f6)
)

(defrule clean-cost-solution
  (declare(salience 1))
  ?f1<-(cost-solution ?)
=>
  (retract ?f1)
)

(defrule clean-current
  (declare(salience 1))
  ?f1<-(current ?)
=>
  (retract ?f1)
)

(defmodule PERCEPTION (import MAIN ?ALL) (import AGENT ?ALL) (export ?ALL))

; aggiorna la conoscenza sul mondo usando le percezioni visive
(defrule incoming-percepts-north
        (status (step ?s))
        ?last-perc-v <- (last-perc-vision (step ?old-s))
        ?fs <- (last-perc (step ?))         
        (test (> ?s ?old-s))
        ?f1 <- (perc-vision (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction north) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9))
                                                   
        ?f2 <-   (K-cell (pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) )
        ?f3 <-   (K-cell (pos-r =(+ ?r 1)) (pos-c ?c)  )
        ?f4 <-   (K-cell (pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)))
        ?f5 <-   (K-cell (pos-r ?r)  (pos-c =(- ?c 1)) )
        ?f6 <-   (K-cell (pos-r ?r)  (pos-c ?c)  )
        ?f7 <-   (K-cell (pos-r ?r)  (pos-c =(+ ?c 1)) )
        ?f8 <-   (K-cell (pos-r =(- ?r 1)) (pos-c =(- ?c 1)) )
        ?f9 <-   (K-cell (pos-r =(- ?r 1)) (pos-c ?c)  )
        ?f10 <-  (K-cell (pos-r =(- ?r 1)) (pos-c =(+ ?c 1)) )
        ?f11 <- (K-agent (step ?ks ) (time ?kt) (pos-r ?kr) (pos-c ?kc) (direction ?kd) (loaded ?kl))   
        
                =>
        (modify ?f11 (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction north) (loaded ?kl))
        (modify ?f2 (contains ?x1))
        (modify ?f3 (contains ?x2))
        (modify ?f4 (contains ?x3))
        (modify ?f5  (contains ?x4))
        (modify ?f6  (contains ?x5))
        (modify ?f7  (contains ?x6))
        (modify ?f8 (contains ?x7))
        (modify ?f9 (contains ?x8))
        (modify ?f10 (contains ?x9))
        (modify ?fs (step ?s))
        (modify ?last-perc-v (step ?s))
        (retract ?f1)
)

(defrule incoming-percepts-sud

      (status (step ?s))
      ?last-perc-v <- (last-perc-vision (step ?old-s))
      ?fs <- (last-perc (step ?))         
      (test (> ?s ?old-s))
      ?f1 <- (perc-vision (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction south) 
             (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)
             (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)
             (perc7 ?x7) (perc8 ?x8) (perc9 ?x9) )
                                                   
      ?f2 <-(K-cell (pos-r =(- ?r 1)) (pos-c =(+ ?c 1)))
      ?f3 <- (K-cell (pos-r =(- ?r 1)) (pos-c ?c)  )
      ?f4 <- (K-cell (pos-r =(- ?r 1)) (pos-c =(- ?c 1)) )
      ?f5 <-  (K-cell (pos-r ?r)  (pos-c =(+ ?c 1)) )
      ?f6 <-  (K-cell (pos-r ?r)  (pos-c ?c)  )
      ?f7 <-(K-cell (pos-r ?r)  (pos-c =(- ?c 1)) )
      ?f8 <-  (K-cell (pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)))
      ?f9 <-   (K-cell (pos-r =(+ ?r 1)) (pos-c ?c) )
      ?f10 <-  (K-cell (pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) )
      ?f11 <- (K-agent (step ?ks ) (time ?kt) (pos-r ?kr) (pos-c ?kc) (direction ?kd) (loaded ?kl))
                
                =>
      (modify ?f11 (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction south) (loaded ?kl))
      (modify ?f2 (contains ?x1))
      (modify ?f3 (contains ?x2))
      (modify ?f4 (contains ?x3))
      (modify ?f5  (contains ?x4))
      (modify ?f6  (contains ?x5))
      (modify ?f7  (contains ?x6))
      (modify ?f8 (contains ?x7))
      (modify ?f9 (contains ?x8))
      (modify ?f10 (contains ?x9))
      (modify ?fs (step ?s))
      (modify ?last-perc-v (step ?s))        
      (retract ?f1)
)

(defrule incoming-percepts-west
        (status (step ?s))
        ?last-perc-v <- (last-perc-vision (step ?old-s))
        ?fs <- (last-perc (step ?))         
        (test (> ?s ?old-s))
        ?f1 <- (perc-vision (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction west) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                       (perc7 ?x7) (perc8 ?x8) (perc9 ?x9))
        ?f2 <- (K-cell (pos-r =(- ?r 1)) (pos-c =(- ?c 1)))
        ?f3 <- (K-cell (pos-r ?r)(pos-c =(- ?c 1)))
        ?f4     <- (K-cell (pos-r =(+ ?r 1)) (pos-c =(- ?c 1)))
        ?f5<-(K-cell (pos-r =(- ?r 1)) (pos-c ?c))
        ?f6<-(K-cell (pos-r ?r)  (pos-c ?c))
        ?f7<-(K-cell (pos-r =(+ ?r 1)) (pos-c ?c))
        ?f8<-(K-cell (pos-r =(- ?r 1)) (pos-c =(+ ?c 1)))
        ?f9<-(K-cell (pos-r ?r)  (pos-c =(+ ?c 1)))
        ?f10<-(K-cell (pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)))
        ?f11 <- (K-agent (step ?ks ) (time ?kt) (pos-r ?kr) (pos-c ?kc) (direction ?kd) (loaded ?kl))
                =>
        (modify ?f11 (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction west) (loaded ?kl))
        (modify ?f2 (contains ?x1))
        (modify ?f3 (contains ?x2))
        (modify ?f4 (contains ?x3))
        (modify ?f5  (contains ?x4))
        (modify ?f6  (contains ?x5))
        (modify ?f7  (contains ?x6))
        (modify ?f8 (contains ?x7))
        (modify ?f9 (contains ?x8))
        (modify ?f10 (contains ?x9))
        (modify ?fs (step ?s))
        (modify ?last-perc-v (step ?s))  
        (retract ?f1)
)

(defrule incoming-percepts-east

        (status (step ?s))
        ?last-perc-v <- (last-perc-vision (step ?old-s))
        ?fs <- (last-perc (step ?))         
        (test (> ?s ?old-s))
        ?f1 <- (perc-vision (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction east) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9))
                                                   
        ?f2 <-(K-cell (pos-r =(+ ?r 1)) (pos-c =(+ ?c 1))) 
        ?f3 <-(K-cell (pos-r ?r)  (pos-c =(+ ?c 1)))
        ?f4 <-(K-cell (pos-r =(- ?r 1)) (pos-c =(+ ?c 1)))
        ?f5 <-(K-cell (pos-r =(+ ?r 1)) (pos-c ?c))
        ?f6 <-(K-cell (pos-r ?r)  (pos-c ?c))
        ?f7 <-(K-cell (pos-r =(- ?r 1)) (pos-c ?c))
        ?f8 <-(K-cell (pos-r =(+ ?r 1)) (pos-c =(- ?c 1)))
        ?f9 <-(K-cell (pos-r ?r)  (pos-c =(- ?c 1)))
        ?f10 <-(K-cell (pos-r =(- ?r 1)) (pos-c =(- ?c 1)))
        ?f11 <- (K-agent (step ?ks ) (time ?kt) (pos-r ?kr) (pos-c ?kc) (direction ?kd) (loaded ?kl))
                
                =>
        (modify ?f11 (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (direction east) (loaded ?kl))
        (modify ?f2 (contains ?x1))
        (modify ?f3 (contains ?x2))
        (modify ?f4 (contains ?x3))
        (modify ?f5  (contains ?x4))
        (modify ?f6  (contains ?x5))
        (modify ?f7  (contains ?x6))
        (modify ?f8 (contains ?x7))
        (modify ?f9 (contains ?x8))
        (modify ?f10 (contains ?x9))
        (modify ?fs (step ?s))
        (modify ?last-perc-v (step ?s))        
        (retract ?f1)
)


; ;; Percezioni per l'operazione di drill

(defrule drill-percepts-yes
  
  (status (step ?s))
  ?last-perc-d <- (last-perc-drill (step ?old-s))
  ?fs <- (last-perc (step ?))         
  (test (> ?s ?old-s))
  ?f1 <-(perc-drill (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (result yes))
  ?f2 <-(K-cell (pos-r ?r) (pos-c ?c)) 


  =>
  (modify ?last-perc-d (step ?s))
  (modify ?fs (step ?s))
  (modify ?f2 (injured yes))
  (retract ?f1)

)

(defrule drill-percepts-no
  
  (status (step ?s))
  ?last-perc-d <- (last-perc-drill (step ?old-s))
  ?fs <- (last-perc (step ?))         
  (test (> ?s ?old-s))
  ?f1 <-(perc-drill (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (result no))
  ?f2 <-(K-cell (pos-r ?r) (pos-c ?c)) 

  =>
  (modify ?last-perc-d (step ?s))
  (modify ?fs (step ?s))
  (modify ?f2 (injured no))
  (retract ?f1)
)

(defrule drill-percepts-fail
  
  (status (step ?s))
  ?last-perc-d <- (last-perc-drill (step ?old-s))
  ?fs <- (last-perc (step ?))         
  (test (> ?s ?old-s))
  ?f1 <-(perc-drill (step ?s)(time ?t) (pos-r ?r) (pos-c ?c) (result fail))
  ?f2 <-(K-cell (pos-r ?r) (pos-c ?c)) 

  =>
  (modify ?last-perc-d (step ?s))
  (modify ?fs (step ?s))
  (modify ?f2 (injured unknown))
  (retract ?f1)
)


;; percezioni acustiche


(defrule acoustic-percepts

  (status (step ?s))
  ?last-perc-a <- (last-perc-acoustic (step ?old-s))
  ?fs <- (last-perc (step ?))         
  (test (> ?s ?old-s))
  ?f1 <- (perc-acoust (step ?s)(time ?t)(pos-r ?r)(pos-c ?c)(ac ?percAcoustic)) ;no o yes
  ?f2 <- (K-cell (pos-r ?r)(pos-c ?c))
  =>
  (modify ?last-perc-a (step ?s))
  (modify ?fs (step ?s))
  (modify ?f2 (sound ?percAcoustic))
  (retract ?f1)
  ; (printout t "percezione acustica" ?percAcoustic)
)

( defrule begin-kcell-perception
 

 (init-agent (done yes))
 (initial_agentposition (pos-r ?r) (pos-c ?c) (direction ?d))
 ?f1 <- (init-cell (pos-r ?r) (pos-c ?c) )
)

; (defrule perception-finished
;   (declare (salience -1))
;   =>
;   (focus REASONING)
;   )

; (defrule pop-perception
;   (declare (salience -2))
;   =>
;   (pop-focus)
; )
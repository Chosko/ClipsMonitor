
;; *************************************

;; *************************************

;; MODULO ENV

;;

;; *************************************







(defmodule ENV (import MAIN ?ALL)(export ?ALL))



(deftemplate real_cell
        (slot pos-r)

        (slot pos-c)

        (slot contains (allowed-values empty debris wall gate 
                                                 person outdoor))
        (slot injured (allowed-values yes no)))

(deftemplate cell
        (slot step)
        (slot time) 

        (slot pos-r)

        (slot pos-c)

        (slot contains (allowed-values robot empty debris wall gate 
                                                 person outdoor))
        (slot injured (allowed-values yes no))
        (slot previous)
        (slot discovered)
        (slot checked)
        (slot clear)
        )





(deftemplate agentstatus 

	(slot step)
        (slot time) 

	(slot pos-r)

	(slot pos-c) 

	(slot direction)

	(slot loaded))



(deftemplate personstatus 	;// informazioni sulla posizione delle persone

	(slot step)
        (slot time)

	(slot ident)

	(slot pos-r)

	(slot pos-c)

	(slot activity)   ;// activity out se soccorritore è outdoor, stand se in piedi, oppure path  		
        (slot move)			

)



(deftemplate personmove		;// modella i movimenti delle persone. l'environment deve tenere conto dell'interazione di tanti agenti. Il mondo cambia sia per le azioni del robot, si per le azioni degli operatori. Il modulo environment deve gestire le interazioni. 

	(slot step)

	(slot ident)

	(slot path-id)

) 

                         

(defrule create1
    (declare (salience 27)) 

    ?f<-   (create-map) =>
           (load-facts "RealMap.txt")

           (assert (create-real-map) (create-init-map) (create-initial-setting)

                   (create-history))  

           (retract ?f)

           )





(defrule  create2

    (declare (salience 26))

    (create-real-map)
    (real_cell (pos-r ?r) (pos-c ?c) (contains ?x) (injured ?z)) 

=>

     (assert (cell (step 0) (time 0) (pos-r ?r) (pos-c ?c) 
                   (contains ?x) (injured ?z)
                   (discovered no)
                   (checked no)
                   (clear no)
                   (previous ?x)))
)

            


(defrule  create3-1

    (declare (salience 25))

    (create-init-map)
    (real_cell (pos-r ?r) (pos-c ?c) (contains outdoor)) 

=>

     (assert (init_cell (pos-r ?r) (pos-c ?c) (contains outdoor)))
)

(defrule  create3-2

    (declare (salience 25))

    (create-init-map)
    (real_cell (pos-r ?r) (pos-c ?c) (contains ?x&wall|gate|debris))
    (or (real_cell (pos-r =(+ ?r 1)) (pos-c ?c) (contains outdoor))
        (real_cell (pos-r =(- ?r 1)) (pos-c ?c) (contains outdoor))
        (real_cell (pos-r ?r) (pos-c =(+ ?c 1)) (contains outdoor))
        (real_cell (pos-r ?r) (pos-c =(- ?c 1)) (contains outdoor))) 

=>

     (assert (init_cell (pos-r ?r) (pos-c ?c) (contains ?x)))
)

; Questa regola inzializza le celle empty. Per costruzione del mondo iniziale le celle 
; empty sono solo celle interne all'ambiente, se fossero vicino all'esterno sarebbero 
; di tipo gate

(defrule  create3-3

    (declare (salience 25))

    (create-init-map)
    (real_cell (pos-r ?r) (pos-c ?c) (contains empty))
=>

     (assert (init_cell (pos-r ?r) (pos-c ?c) (contains unknown)))
)

(defrule  create3-4

    (declare (salience 24))

    (create-init-map)
    (real_cell (pos-r ?r) (pos-c ?c) (contains ?x&wall|gate|debris))
    (not (init_cell (pos-r ?r) (pos-c ?c))) 
=>

     (assert (init_cell (pos-r ?r) (pos-c ?c) (contains unknown)))
)

(defrule creation4	

	(declare (salience 23))

?f1<-	(create-history) 

=>

   	(load-facts "history.txt")
        (retract ?f1))


(defrule creation5
         (declare (salience 21))
?f1 <-   (create-initial-setting)
?f2 <-   (create-real-map)
?f4 <-   (create-init-map)
         (initial_agentposition (pos-r ?r) (pos-c ?c) (direction ?d))
?f3 <-   (cell (step 0) (pos-r ?r) (pos-c ?c) (contains ?xx))
=> 
         (modify ?f3 (contains robot) (previous ?xx))
         (assert (agentstatus (step 0) (time 0) (pos-r ?r) (pos-c ?c) (direction ?d)
                              (loaded no))
                 (status (step 0) (time 0) (result no))
                 (penalty 0))
         (retract ?f1 ?f2 ?f4)
)


;;*****************************************************

;;*****************************************************

;;      AZIONI DI MOVIMENTO

;;





(defrule move-north-ok1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded no))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c) (contains ?xx&empty|gate))

         

       =>  (modify  ?f1 (pos-r (+ ?r 1)) (step (+ ?i 1))(time (+ ?t 1)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 1)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 1)) (contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))



(defrule move-north-ok2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded yes))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c) (contains ?xx&empty|gate))

        

       =>  (modify  ?f1 (pos-r (+ ?r 1)) (step (+ ?i 1))(time (+ ?t 2)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 2)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 1)) (contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-north-bump1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north))

  ?f3<- (cell (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c) 
              (contains wall|debris|outdoor))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction north)

                              (bump yes))

                   (penalty (+ ?p 1000000))

           )

           (retract ?f4))



(defrule move-north-bump2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north))

  ?f3<- (cell (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c) 
              (contains person))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction north)

                              (bump yes))

                   (penalty (+ ?p 10000000))

           )

           (retract ?f4))

        

(defrule move-south-ok1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded no))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r =(- ?r 1)) (pos-c ?c) (contains ?xx&empty|gate))

         

       =>  (modify  ?f1 (pos-r (- ?r 1)) (step (+ ?i 1))(time (+ ?t 1)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 1)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 1))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-south-ok2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded yes))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r =(- ?r 1)) (pos-c ?c) (contains ?xx&empty|gate))

        

       =>  (modify  ?f1 (pos-r (- ?r 1)) (step (+ ?i 1))(time (+ ?t 2)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 2)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 2))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-south-bump1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south))

  ?f3<- (cell (step ?i) (pos-r =(- ?r 1)) (pos-c ?c) 
              (contains wall|debris|outdoor))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction south)

                              (bump yes))

                   (penalty (+ ?p 1000000))

           )

           (retract ?f4))



(defrule move-south-bump2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south))

  ?f3<- (cell (step ?i) (pos-r =(- ?r 1)) (pos-c ?c) 
              (contains person))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction south)

                              (bump yes))

                   (penalty (+ ?p 10000000))

           )

           (retract ?f4))





(defrule move-west-ok1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded no))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(- ?c 1)) (contains ?xx&empty|gate))

         

       =>  (modify  ?f1 (pos-c (- ?c 1)) (step (+ ?i 1))(time (+ ?t 1)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 1)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 1))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-west-ok2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded yes))
  ?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(- ?c 1)) (contains ?xx&empty|gate))

        

       =>  (modify  ?f1 (pos-c (- ?c 1)) (step (+ ?i 1))(time (+ ?t 2)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 2)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 2))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))



(defrule move-west-bump1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(- ?c 1))
              (contains wall|debris|outdoor))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction west)

                              (bump yes))

                   (penalty (+ ?p 1000000))

           )

           (retract ?f4))



(defrule move-west-bump2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(- ?c 1)) 
              (contains person))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction west)

                              (bump yes))

                   (penalty (+ ?p 10000000))

           )

           (retract ?f4))




(defrule move-east-ok1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded no))
?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(+ ?c 1)) (contains ?xx&empty|gate))

         

       =>  (modify  ?f1 (pos-c (+ ?c 1)) (step (+ ?i 1))(time (+ ?t 1)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 1)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 1))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-east-ok2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded yes))
?f4<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous ?zz))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(+ ?c 1)) (contains ?xx&empty|gate))

        

       =>  (modify  ?f1 (pos-c (+ ?c 1)) (step (+ ?i 1))(time (+ ?t 2)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 2)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 2))(contains robot) (previous ?xx))
           (modify  ?f4 (step (+ ?i 1))(time (+ ?t 1)) (contains ?zz)))




(defrule move-east-bump1 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(+ ?c 1))
              (contains wall|debris|outdoor))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction east)

                              (bump yes))

                   (penalty (+ ?p 1000000))

           )

           (retract ?f4))



(defrule move-east-bump2 

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  forward))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c =(+ ?c 1)) 
              (contains person))

  ?f4<- (penalty ?p)

        => (modify  ?f1 (step (+ ?i 1))(time (+ ?t 10)))

           (modify  ?f2 (step (+ ?i 1))(time (+ ?t 10)))
           (modify  ?f3 (step (+ ?i 1))(time (+ ?t 10)))

           (assert (perc-bump (step (+ ?i 1))(time (+ ?t 10))
                              (pos-r ?r) (pos-c ?c) (direction east)

                              (bump yes))

                   (penalty (+ ?p 10000000))

           )

           (retract ?f4))




(defrule turnleft11

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction south) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnleft12

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction south) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)

(defrule turnleft21

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction east) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnleft22

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction east) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)



(defrule turnleft31

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction north) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnleft32

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction north) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)




(defrule turnleft41

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction west) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnleft42

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnleft))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction west) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)



(defrule turnright11

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction north) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnright12

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction west) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction north) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)



(defrule turnright21

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction west) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnright22

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction south) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction west) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)


(defrule turnright31

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction south) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnright32

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction east) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction south) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)




(defrule turnright41

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded no))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction east) (step (+ ?i 1)) (time (+ ?t 2)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 2)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 2)))		

)



(defrule turnright42

	(declare (salience 20))      

?f2<-	(status (step ?i) (time ?t)) 

	(exec (step ?i) (action  turnright))

?f1<-	(agentstatus (step ?i) (pos-r ?r) (pos-c ?c)(direction north) (loaded yes))

?f3<-	(cell (step ?i) (pos-r ?r) (pos-c ?c))

=>	

	(modify ?f1 (direction east) (step (+ ?i 1)) (time (+ ?t 3)) )

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 3)) )
        (modify ?f3 (step (+ ?i 1))(time (+ ?t 3)))		

)




;;*****************************************************

;;

;;       DRILL

;;





(defrule drill-north-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured ?p))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                               (pos-r ?x)(pos-c ?y) (result ?p)))

           )




(defrule drill-north-ko1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains ~debris))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?r 1)) (pos-c ?c))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                                (pos-r ?r)(pos-c ?c) (result fail))
                   (penalty (+ ?p 1000000))

                )

           (retract ?f4)

           )
  





(defrule drill-south-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains debris) (injured ?p))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                               (pos-r ?r)(pos-c ?c) (result ?p)))

           )



(defrule drill-south-ko1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains ~debris))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?r 1)) (pos-c ?c))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                                (pos-r ?r)(pos-c ?c) (result fail))
                   (penalty (+ ?p 1000000))

                )

           (retract ?f4)

           )





(defrule drill-east-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains debris) (injured ?p))

  ?f1<- (agentstatus (step ?i) (pos-c =(+ ?c 1)) (pos-r ?r))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                               (pos-r ?r)(pos-c ?c) (result ?p)))

           )



(defrule drill-east-ko1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains ~debris))

  ?f1<- (agentstatus (step ?i) (pos-c =(+ ?c 1)) (pos-r ?r))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                                (pos-r ?r)(pos-c ?c) (result fail))
                   (penalty (+ ?p 1000000))

                )

           (retract ?f4)

           )





(defrule drill-west-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains debris) (injured ?p))

  ?f1<- (agentstatus (step ?i) (pos-c =(- ?c 1)) (pos-r ?r))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                               (pos-r ?r)(pos-c ?c) (result ?p)))

           )



(defrule drill-west-ko1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains ~debris))

  ?f1<- (agentstatus (step ?i) (pos-c =(- ?c 1)) (pos-r ?r))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                                (pos-r ?r)(pos-c ?c) (result fail))
                   (penalty (+ ?p 1000000))

                )

           (retract ?f4)

           )



(defrule drill_KO_too_far

   (declare (salience 19))  

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  drill) (param1 ?r) (param2 ?c))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 60)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 60)))
           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 60)))

           (assert (perc-drill  (step (+ ?i 1)) (time (+ ?t 60)) 
                                (pos-r ?r)(pos-c ?c) (result fail))
                   (penalty (+ ?p 1000000))

                )

           (retract ?f4)

           )

    

 

;;;;******************************

;;;;******************************

;;;;          LOAD_DEBRIS







(defrule load_OK_north

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded no))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains empty))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded  yes)))

           )




(defrule load_injured_north

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y))  

        => 

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)) (result disaster))

           (printout t crlf crlf)

           (printout t "disaster at time " ?i)

           (printout t crlf crlf)

           (printout t "exec of load_debris with person in (" ?x ","  ?y ")")

           (printout t crlf crlf)

           (focus MAIN)

           )


(defrule load_KO_Person_north

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person) )

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded ?l))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded ?l))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )




 

(defrule load_OK_south2

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y) (loaded no))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains empty))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded  yes)))

           )

(defrule load_injured_south

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y))  

        => 

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)) (result disaster))

           (printout t crlf crlf)

           (printout t "disaster at time " ?i)

           (printout t crlf crlf)

           (printout t "exec of load_debris with person in (" ?x ","  ?y ")")

           (printout t crlf crlf)

           (focus MAIN)

           )


(defrule load_KO_Person_south

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y)(loaded ?l))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded ?l))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )


 



(defrule load_OK_east2

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)) (loaded no))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains empty))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1)) 
                                 (loaded  yes)))

           )

(defrule load_injured_east

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)))  

        => 

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)) (result disaster))

           (printout t crlf crlf)

           (printout t "disaster at time " ?i)

           (printout t crlf crlf)

           (printout t "exec of load_debris with person in (" ?x ","  ?y ")")

           (printout t crlf crlf)

           (focus MAIN)

           )


(defrule load_KO_Person_east

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1))(loaded ?l))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1))  
                                 (loaded ?l))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )








(defrule load_OK_west2

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded no))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains empty))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1)) 
                                 (loaded  yes)))

           )

(defrule load_injured_west

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)))  

        => 

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)) (result disaster))

           (printout t crlf crlf)

           (printout t "disaster at time " ?i)

           (printout t crlf crlf)

           (printout t "exec of load_debris with person in (" ?x ","  ?y ")")

           (printout t crlf crlf)

           (focus MAIN)

           )


(defrule load_KO_Person_west

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded ?l))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1))  
                                 (loaded ?l))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f3)

           )
      



(defrule load_KO

   (declare (salience 19))    

   ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  load_debris) (param1 ?x) (param2 ?y))
  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c) (loaded ?l))
  ?f4<- (penalty ?p)   



        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 20)) (loaded ?l))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 20)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 20)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 20)) 
                                 (robotpos-r ?r) (robotpos-c ?c)  
                                 (loaded ?l))
                    (penalty (+ ?p 1000000))

           )

           (retract ?f4)

           )





;;;;******************************

;;;;******************************

;;;;          UNLOAD_DEBRIS





(defrule unload_OK_north_outdoor

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains outdoor))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 10)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 10)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 10)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 10)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded  no)))

           )




(defrule unload_OK_north_empty_noclear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear no))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)) (contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded  no)))

           )


(defrule unload_north_empty_clear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear yes))

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded  no))
                   (penalty (+ ?p 100000)))
           (retract ?f4)

           )



(defrule unload_KO_Person_north

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person) )

  ?f1<- (agentstatus (step ?i) (pos-r =(- ?x 1)) (pos-c ?y) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(- ?x 1)) (robotpos-c ?y) 
                                 (loaded yes))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )




 

(defrule unload_OK_south_outdoor

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains outdoor))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 10)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 10)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 10)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 10)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded  no)))

           )




(defrule unload_OK_south_empty_noclear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear no))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded  no)))

           )


(defrule unload_south_empty_clear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear yes))

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded  no))
                   (penalty (+ ?p 100000)))
           (retract ?f4)

           )



(defrule unload_KO_Person_south

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person) )

  ?f1<- (agentstatus (step ?i) (pos-r =(+ ?x 1)) (pos-c ?y) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r =(+ ?x 1)) (robotpos-c ?y) 
                                 (loaded yes))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )




 

(defrule unload_OK_east_outdoor

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains outdoor))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 10)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 10)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 10)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 10)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1)) 
                                 (loaded  no)))

           )




(defrule unload_OK_east_empty_noclear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear no))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1))
                                 (loaded  no)))

           )


(defrule unload_east_empty_clear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear yes))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1))
                                 (loaded  no))
                   (penalty (+ ?p 100000)))
           (retract ?f4)

           )



(defrule unload_KO_Person_east

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person) )

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(- ?y 1)) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(- ?y 1))
                                 (loaded yes))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )



 

(defrule unload_OK_west_outdoor

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains outdoor))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 10)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 10)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 10)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 10)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1)) 
                                 (loaded  no)))

           )




(defrule unload_OK_west_empty_noclear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear no))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded yes))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1))
                                 (loaded  no)))

           )


(defrule unload_west_empty_clear

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty) (clear yes))

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded no))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16))(contains debris)(injured no))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1))
                                 (loaded  no))
                   (penalty (+ ?p 100000)))
           (retract ?f4)

           )



(defrule unload_KO_Person_west

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains person) )

  ?f1<- (agentstatus (step ?i) (pos-r ?x) (pos-c =(+ ?y 1)) (loaded yes))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded yes))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)) (contains person))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?x) (robotpos-c =(+ ?y 1))
                                 (loaded yes))
                    (penalty (+ ?p 10000000))

           )

           (retract ?f4)

           )



      



(defrule unload_KO

   (declare (salience 19))    

   ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  unload_debris) (param1 ?x) (param2 ?y))
  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y))

  ?f1<- (agentstatus (step ?i) (pos-r ?r) (pos-c ?c) (loaded ?l))
  ?f4<- (penalty ?p)   



        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 16)) (loaded ?l))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 16)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 16)))

           (assert (perc-loaded  (step (+ ?i 1)) (time (+ ?t 16)) 
                                 (robotpos-r ?r) (robotpos-c ?c)  
                                 (loaded ?l))
                    (penalty (+ ?p 1000000))

           )

           (retract ?f4)

           )







;;;;******************************

;;;;******************************

;;;;          INFORM


(defrule inform-discover-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 discover))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes)
              (discovered no))

  ?f1<- (agentstatus (step ?i))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)) (discovered yes))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           )




(defrule inform-discover-useless

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 discover))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes)
              (discovered yes))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 25000)))

           (retract ?f4)

           )



(defrule inform-discover-KO1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 discover))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 20000000)))

           (retract ?f4)

           )


(defrule inform-discover-KO2

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 discover))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains ~debris))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 20000000)))

           (retract ?f4)

           )


(defrule inform-checked-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 checked))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no)
              (checked no))

  ?f1<- (agentstatus (step ?i))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)) (checked yes))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           )




(defrule inform-checked-useless

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 checked))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured no)
              (checked yes))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 25000)))

           (retract ?f4)

           )



(defrule inform-checked-KO1

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 checked))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 50000000)))

           (retract ?f4)

           )


(defrule inform-checked-KO2

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 checked))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains ~debris))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 20000000)))

           (retract ?f4)
           )

(defrule inform-clear-ok

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 clear))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains empty)
              (clear no))

  ?f1<- (agentstatus (step ?i))

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)) (clear yes))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           )



(defrule inform-clear-useless

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 clear))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains debris) 
              (clear yes))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 25000)))

           (retract ?f4)

           )



(defrule inform-clear-KO

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t))

        (exec (step ?i) (action  inform) (param1 ?x) (param2 ?y) (param3 clear))

  ?f3<- (cell (step ?i) (pos-r ?x) (pos-c ?y) (contains ~empty))

  ?f1<- (agentstatus (step ?i))
  ?f4<- (penalty ?p)

        => (modify ?f1  (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f3 (step (+ ?i 1)) (time (+ ?t 1)))

           (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)))

           (assert (penalty (+ ?p 200000)))

           (retract ?f4)

           )

           

;;;;******************************

;;;;******************************

;;;;          DONE





(defrule done-undiscovered

   (declare (salience 21))    

        (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  done))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains debris) 
            (injured yes) (discovered no))

  ?f1<- (penalty ?p)

        => (assert (penalty (+ ?p 2000000)))

           (modify ?f3 (discovered done))
           (retract ?f1)

           )



(defrule done-unchecked

   (declare (salience 21))    

        (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  done))

  ?f3<- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains debris) 
            (injured no) (checked no))

  ?f1<- (penalty ?p)

        => (assert (penalty (+ ?p 200000)))

           (modify ?f3 (checked done))
           (retract ?f1)

           )



(defrule done-no-gate

   (declare (salience 20))    

  ?f2<- (status (step ?i) (time ?t)) 

        (exec (step ?i) (action  done))

        (agentstatus (step ?i) (pos-r ?r) (pos-c ?c))

        (cell (step ?i)(pos-r ?r) (pos-c ?c) (previous ~gate))

  ?f1<- (penalty ?p)

        => (assert (penalty (+ ?p 100000)))

           (retract ?f1)

          (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)) (result done))

           (focus MAIN)

)







(defrule done-in-gate
   (declare (salience 20))
  ?f2<- (status (step ?i) (time ?t))
        (exec (step ?i) (action  done))
        (agentstatus (step ?i) (pos-r ?r) (pos-c ?c))
        (cell (step ?i) (pos-r ?r) (pos-c ?c) (previous gate))
        =>
          (modify ?f2 (step (+ ?i 1)) (time (+ ?t 1)) (result done))
           (focus MAIN)
)




;;;;******************************

;;;;******************************

;;;;          WAIT





(defrule wait

	(declare (salience 20))
?f2<-	(status (time ?t) (step ?i)) 

	(exec (step ?i) (action wait))

?f1<-	(agentstatus (step ?i) (time ?t))   
=> 

	(modify ?f2 (step (+ ?i 1)) (time (+ ?t 5)))

	(modify ?f1 (step (+ ?i 1)) (time (+ ?t 5)))

)


;;;;******************************************

;;;;******************************************

;;;;          GENERAZIONE EVOLUZIONE TEMPORALE E PENALTY

;// per ogni istante di tempo che una persona injured resta sotto le macerie fino
;// non viene discovered prende 50 punti




(defrule Evolution-debris-injured-not-disc       

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes) (discovered no)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

?f2<-	(penalty ?p)

=> 

	(modify ?f1 (time ?t) (step ?i))

	(assert (penalty (+ ?p (* (- ?t ?tt) 50))))

	(retract ?f2)	

)


(defrule Evolution-debris-injured-disc       

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains debris) (injured yes) (discovered yes)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

=> 

	(modify ?f1 (time ?t) (step ?i))

)



;// per ogni istante di tempo una cella debris senza persona intrappolata prende 4 punti 
;// fino a quando non viene dichiarata checked




(defrule Evolution-debris-no-injured-no-check       

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains debris) (injured no) (checked no)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

?f2<-	(penalty ?p)

=> 

	(modify ?f1 (time ?t) (step ?i))

	(assert (penalty (+ ?p (* (- ?t ?tt) 4))))

	(retract ?f2)	

)


(defrule Evolution-debris-no-injured-checked            

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains debris) (injured no) (checked yes)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

=> 

	(modify ?f1 (time ?t) (step ?i))
)


;// per ogni istante di tempo  una cella empty prende 1 punto fino a quando
;// non viene dichiarata clear




(defrule Evolution-empty-no-clear       

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains empty) (clear no)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

?f2<-	(penalty ?p)

=> 

	(modify ?f1 (time ?t) (step ?i))

	(assert (penalty (+ ?p (* (- ?t ?tt) 1))))

	(retract ?f2)	

)


(defrule Evolution-empty-clear            

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) (contains empty) (clear yes)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

=> 

	(modify ?f1 (time ?t) (step ?i))
)


;// evoluzione per altre celle




(defrule Evolution-generic-cell            

	(declare (salience 15))

	(status (time ?t) (step ?i))

?f1<-	(cell (step = (- ?i 1)) (time ?tt) (pos-r ?x) (pos-c ?y) 
              (contains wall|gate|person|outdoor|robot)) 

	(not (cell (step ?i) (pos-r ?x) (pos-c ?y)))

=> 

	(modify ?f1 (time ?t) (step ?i))
)

;// __________________________________________________________________________________________

;// GENERA MOVIMENTI PERSONE                    

;// ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

;// Persona ferma non arriva comando di muoversi

(defrule MovePerson1		

	(declare (salience 9))    

	(status (step ?i) (time ?t)) 

?f1<-	(personstatus (step =(- ?i 1)) (ident ?id) (activity out|stand))

	(not (personmove (step ?i) (ident ?id)))

=> 

	(modify ?f1 (time ?t) (step ?i))

)         

         

;//;//Persona ferma ma arriva comando di muoversi         

(defrule MovePerson2

   (declare (salience 10))    

        (status (step ?i) (time ?t))  

 ?f1 <- (personstatus (step =(- ?i 1)) (ident ?id) (activity out|stand))

 ?f2 <- (personmove (step  ?i) (ident ?id) (path-id ?m))

        => (modify  ?f1 (time ?t) (step ?i) (activity ?m) (move 0))

           (retract ?f2)

)           

  


;// La cella in cui deve  andare la persona è libera. Persona si muove. 

;//

(defrule MovePerson3

   (declare (salience 10))    

        (status (step ?i) (time ?t))   

 ?f1 <- (personstatus (step =(- ?i 1)) (ident ?id) (pos-r ?x) (pos-c ?y) 

                      (activity ?m&~out&~stand) (move ?s))

 ?f4 <- (cell (step ?i) (pos-r ?x) (pos-c ?y) (previous ?w))

 ?f3 <- (move-path ?m =(+ ?s 1) ?id ?r ?c)

 ?f2 <- (cell (step ?i) (pos-r ?r) (pos-c ?c) (contains ?xx&empty|gate|outdoor))

        => (modify  ?f1  (step ?i) (time ?t) (pos-r ?r) (pos-c ?c) (move (+ ?s 1)))

           (modify ?f2 (contains person) (previous ?xx))
           (modify ?f4 (contains ?w))

           (retract ?f3)		

)



;// La cella in cui deve  andare la persona è occupata dal robot. Persona non si muove           

(defrule MovePerson_wait1

	(declare (salience 10))    

	(status (step ?i) (time ?t))

?f1<-	(personstatus (step =(- ?i 1)) (time ?tt) (ident ?id) (activity ?m&~out&~stand) (move ?s))

	(move-path ?m =(+ ?s 1) ?id ?r ?c)

	(cell (step ?i) (pos-r ?r) (pos-c ?c) (contains robot))

?f2<-	(penalty ?p)

=> 

	(modify  ?f1 (time ?t) (step ?i))

	(assert (penalty (+ ?p (* (- ?t ?tt) 5))))

	(retract ?f2)

;	(printout t " - penalità aumentate" ?id " attende che il robot si sposti)" crlf)

)

                  


;// La cella in cui deve  andare la persona non è libera (ma non è occupata da robot). Persona non si muove           

(defrule MovePerson_wait2

	(declare (salience 10))    

	(status (step ?i) (time ?t))

?f1<-	(personstatus (step =(- ?i 1)) (time ?tt) (ident ?id) (activity ?m&~out&~stand) (move ?s))

	(move-path ?m =(+ ?s 1) ?id ?r ?c)
        (cell (step ?i)(pos-r ?r) (pos-c ?c) (contains wall|debris|person))
=>

	(modify  ?f1 (time ?t) (step ?i))



)


;//La serie di mosse è stata esaurita, la persona rimane ferma dove si trova

 (defrule MovePerson_end

   (declare (salience 9))    

        (status (step ?i) (time ?t)) 

?f1<-	(personstatus (step =(- ?i 1)) (time ?tt) (ident ?id) (activity ?m&~outd&~stand) (move ?s))

	(not (move-path ?m =(+ ?s 1) ?id ?r ?c))

        => (modify  ?f1  (time ?t) (step ?i) (activity stand) (move NA)) 

        )

           



           










;;;;******************************************

;;;;******************************************

;;;;          GENERAZIONE PERCEZIONI ACUSTICHE





(defrule percept-acoust-yes

(declare (salience 8))

     (status (step ?i) (time ?t&:(> ?t 0)))
     (agentstatus (pos-r ?r) (pos-c ?c) (step ?i))

     (or (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c ?c) (contains debris) (injured yes))

         (cell (step ?i)(pos-r =(- ?r 1)) (pos-c ?c) (contains debris)(injured yes))

         (cell (step ?i)(pos-r ?r) (pos-c =(+ ?c 1)) (contains debris)(injured yes))

         (cell (step ?i)(pos-r ?r) (pos-c =(- ?c 1)) (contains debris)(injured yes))

      )

     => (assert (perc-acoust (step ?i) (time ?t) (pos-r ?r) (pos-c ?c) (ac yes)))

        )





(defrule percept-acoust-no

(declare (salience 7))

     (status (step ?i) (time ?t&:(> ?t 0)))
     (agentstatus (pos-r ?r) (pos-c ?c) (step ?i))

     (not (perc-acoust (time ?t)))

     => (assert (perc-acoust (step ?i) (time ?t) (pos-r ?r) (pos-c ?c) (ac no)))

        )











;;;;******************************************

;;;;******************************************

;;;;          GENERAZIONE PERCEZIONI VISIVE





(defrule percept-north

(declare (salience 5))

  ?f1<- (agentstatus (step ?i) (time ?t&:(> ?t 0)) (pos-r ?r) (pos-c ?c) (direction north)) 

        
        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) (contains ?x1))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c ?c)  (contains ?x2))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)) (contains ?x3))

        (cell (step ?i)(pos-r ?r)  (pos-c =(- ?c 1)) (contains ?x4))

        (cell (step ?i)(pos-r ?r)  (pos-c ?c)  (contains ?x5))

        (cell (step ?i)(pos-r ?r)  (pos-c =(+ ?c 1)) (contains ?x6))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(- ?c 1)) (contains ?x7))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c ?c)  (contains ?x8))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(+ ?c 1)) (contains ?x9))

      => 

        (assert (perc-vision (step ?i)(time ?t) 
                           (pos-r ?r) (pos-c ?c) (direction north) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9)))

        (focus MAIN)

       )





(defrule percept-south

(declare (salience 5))

  ?f1<- (agentstatus (step ?i)(time ?t&:(> ?t 0)) (pos-r ?r) (pos-c ?c) (direction south)) 

        
        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(+ ?c 1)) (contains ?x1))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c ?c)  (contains ?x2))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(- ?c 1)) (contains ?x3))

        (cell (step ?i)(pos-r ?r)  (pos-c =(+ ?c 1)) (contains ?x4))

        (cell (step ?i)(pos-r ?r)  (pos-c ?c)  (contains ?x5))

        (cell (step ?i)(pos-r ?r)  (pos-c =(- ?c 1)) (contains ?x6))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)) (contains ?x7))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c ?c)  (contains ?x8))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) (contains ?x9))

        

     => 

        (assert (perc-vision (step ?i) (time ?t) 
                           (pos-r ?r) (pos-c ?c) (direction south) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9)))

        (focus MAIN)

       )









(defrule percept-east

(declare (salience 5))

  ?f1<- (agentstatus (step ?i)(time ?t&:(> ?t 0)) (pos-r ?r) (pos-c ?c) (direction east)) 

        
        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)) (contains ?x1))

        (cell (step ?i)(pos-r ?r)  (pos-c =(+ ?c 1)) (contains ?x2))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(+ ?c 1)) (contains ?x3))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c ?c)  (contains ?x4))

        (cell (step ?i)(pos-r ?r)  (pos-c ?c)  (contains ?x5))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c ?c)  (contains ?x6))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) (contains ?x7))

        (cell (step ?i)(pos-r ?r)  (pos-c =(- ?c 1)) (contains ?x8))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(- ?c 1)) (contains ?x9))

     => 

        (assert (perc-vision (step ?i)(time ?t) 
                           (pos-r ?r) (pos-c ?c) (direction east) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9)))

        (focus MAIN)

       )









(defrule percept-west

(declare (salience 5))

  ?f1<- (agentstatus (step ?i)(time ?t&:(> ?t 0)) (pos-r ?r) (pos-c ?c) (direction west)) 

        
        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(- ?c 1)) (contains ?x1))

        (cell (step ?i)(pos-r ?r)  (pos-c =(- ?c 1)) (contains ?x2))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(- ?c 1)) (contains ?x3))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c ?c)  (contains ?x4))

        (cell (step ?i)(pos-r ?r)  (pos-c ?c)  (contains ?x5))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c ?c)  (contains ?x6))

        (cell (step ?i)(pos-r =(- ?r 1)) (pos-c =(+ ?c 1)) (contains ?x7))

        (cell (step ?i)(pos-r ?r)  (pos-c =(+ ?c 1)) (contains ?x8))

        (cell (step ?i)(pos-r =(+ ?r 1)) (pos-c =(+ ?c 1)) (contains ?x9))

     => 

        (assert (perc-vision (step ?i)(time ?t) 
                           (pos-r ?r) (pos-c ?c) (direction west) 

                           (perc1 ?x1) (perc2 ?x2) (perc3 ?x3)

                           (perc4 ?x4) (perc5 ?x5) (perc6 ?x6)

                           (perc7 ?x7) (perc8 ?x8) (perc9 ?x9)))

        (focus MAIN)

       )
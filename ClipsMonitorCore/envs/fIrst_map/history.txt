(maxduration 300)
(initial_agentposition (pos-r 2) (pos-c 5) (direction north))

(personstatus 	
	(step 0)
        (time 0)
	(ident C1)
	(pos-r 1)
	(pos-c 9)
	(activity out))

(personstatus 	
	(step 0)
        (time 0)
	(ident C2)
	(pos-r 10)
	(pos-c 5)
	(activity out))


		


(personmove (step 5) (ident C1) (path-id P1))

(personmove (step 19) (ident C1) (path-id P2))

(move-path P1 1 C1  2 9)
(move-path P1 2 C1  3 9)
(move-path P1 3 C1  3 8)
(move-path P1 4 C1  3 7)
(move-path P1 5 C1  3 6)


(move-path P2 1 C1  4 6)
(move-path P2 2 C1  4 5)
(move-path P2 3 C1  3 5)
(move-path P2 4 C1  2 5)
(move-path P2 5 C1  1 5)
(move-path P2 6 C1  1 4)





(personstatus
        (step 0)
        (time 0)
        (ident C1)
        (pos-r 19)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C2)
        (pos-r 18)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C3)
        (pos-r 14)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C4)
        (pos-r 13)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C5)
        (pos-r 12)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C6)
        (pos-r 2)
        (pos-c 2)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C7)
        (pos-r 19)
        (pos-c 3)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C8)
        (pos-r 7)
        (pos-c 3)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C9)
        (pos-r 5)
        (pos-c 3)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C10)
        (pos-r 19)
        (pos-c 4)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C11)
        (pos-r 6)
        (pos-c 4)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C12)
        (pos-r 5)
        (pos-c 8)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C13)
        (pos-r 2)
        (pos-c 8)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C14)
        (pos-r 19)
        (pos-c 9)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C15)
        (pos-r 16)
        (pos-c 10)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C16)
        (pos-r 5)
        (pos-c 10)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C17)
        (pos-r 2)
        (pos-c 10)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C18)
        (pos-r 16)
        (pos-c 12)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C19)
        (pos-r 5)
        (pos-c 12)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C20)
        (pos-r 2)
        (pos-c 12)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C21)
        (pos-r 19)
        (pos-c 13)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C22)
        (pos-r 5)
        (pos-c 14)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C23)
        (pos-r 2)
        (pos-c 14)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C24)
        (pos-r 19)
        (pos-c 17)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C25)
        (pos-r 6)
        (pos-c 17)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C26)
        (pos-r 19)
        (pos-c 18)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C27)
        (pos-r 7)
        (pos-c 18)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C28)
        (pos-r 5)
        (pos-c 18)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C29)
        (pos-r 19)
        (pos-c 19)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C30)
        (pos-r 18)
        (pos-c 19)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C31)
        (pos-r 14)
        (pos-c 19)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C32)
        (pos-r 13)
        (pos-c 19)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C33)
        (pos-r 12)
        (pos-c 19)
        (activity seated)
)

(personstatus
        (step 0)
        (time 0)
        (ident C34)
        (pos-r 2)
        (pos-c 19)
        (activity seated)
)

(event (step 1) (type request) (source T6) (food 0) (drink 1))
(event (step 10) (type request) (source T14) (food 2) (drink 0))
(event (step 45) (type finish) (source T6))
(event (step 70) (type request) (source T4) (food 1) (drink 0))
(event (step 130) (type request) (source T18) (food 2) (drink 0))
(event (step 150) (type finish) (source T14))

(personmove (step 2) (ident C6) (path-id P1))
(personmove (step 150) (ident C18) (path-id P2))
(personmove (step 200) (ident C18) (path-id P3))

(move-path P1 1 C6  2 3)
(move-path P1 2 C6  2 4)
(move-path P1 3 C6  3 4)
(move-path P1 4 C6  4 4)
(move-path P1 5 C6  4 5)
(move-path P1 6 C6  4 6)
(move-path P1 7 C6  4 7)
(move-path P1 8 C6  4 8)
(move-path P1 9 C6  4 9)

(move-path P2 1 C18  15 12)
(move-path P2 2 C18  14 12)
(move-path P2 3 C18  14 13)
(move-path P2 4 C18  14 14)
(move-path P2 5 C18  15 14)

(move-path P3 1 C18  15 15)
(move-path P3 2 C18  15 16)
(move-path P3 3 C18  15 17)
(move-path P3 4 C18  15 18)
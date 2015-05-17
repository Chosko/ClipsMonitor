(defmodule CLEAN-DISTPATH (import AGENT ?ALL) (export ?ALL))

;(update-order-distpath ?table ?step 0)

(defrule clean-order-distpath
  (declare (salience 50))
  (clean-order-distpath)
  ?f1<-(exec-order (distpath -1) (phase 0))
  =>
  (modify ?f1 (distpath 0))
)


(defrule clean-order-distpath-focuspop
  (declare (salience 40))
  ?f1<-(clean-order-distpath)
  =>
  (retract ?f1)
  (pop-focus)
)

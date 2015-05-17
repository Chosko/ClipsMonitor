(defmodule CLEAN-PLANE (import AGENT ?ALL) (export ?ALL))

(defrule plane-to-deleted
	(declare (salience 10))
	(plan-executed (step ?current) (pos-start ?ra ?ca) (pos-end ?rd ?cd))
	(plane  (plane-id ?id) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (direction ?d))
	(plane  (plane-id ?id2) (pos-start ?ra ?ca) (pos-end ?rd ?cd) (direction ?d))
	(test (and(neq ?id ?id2) (< ?id2 ?id)))
=>
	(assert (delete-plane-id ?id))
)

(defrule remove-plane
	(declare (salience 20))
	(delete-plane-id ?id)
	?f1<-(plane (plane-id ?id))
=>
	(retract ?f1)
)

(defrule remove-step-plane
	(declare (salience 20))
	(delete-plane-id ?id)
	?f1<-(step-plane  (plane-id ?id))
=>
	(retract ?f1)
)

(defrule remove-delete-plane-id
	(declare (salience 15))
	?f1<-(delete-plane-id ?id)
=>
	(retract ?f1)
)
package com.game.pavuk

import com.game.pavuk.objects.Card

class Pathfinder(private val deck: Deck) {

    class Helper(var from: Int, var to: Int, var size: Int, var solverStage: Int,
                 var ignoreChecks: Int, var priority: Int, var final: Boolean) {

        override fun toString(): String {
            return ">->->->->->->->->->-> \n" +
                    "from column - $from \n to column - $to \n " +
                    "size of the pack - $size \n" +
                    "solution found at solver's stage - $solverStage \n" +
                    "ignored first $ignoreChecks checks \n " +
                    "sequence priority - $priority \n" +
                    "has key pattern - $final \n" +
                    "<-<-<-<-<-<-<-<-<-<-<"
        }

    }

    private var bestSeq = listOf<Helper>()
    private var sequence = mutableListOf<Helper>()

    private fun findSeq() {

        val test = deck
        val prohibitedColumns = mutableSetOf<Int>()
        val solver = OneSolver(test)

        val startStage = solver.step(0, prohibitedColumns)
        var firstStepIgnore = if (startStage < 2) 0 else startStage - 1

        // Расчет первого хода отдельно от цикла
        // >>>>>>>>>>>>>>>>
        sequence.add(Helper(solver.from, solver.to,
                solver.size, startStage, firstStepIgnore, solver.stepPriority, solver.solution))
        if (sequence[0].size == -1) return

        if (Logic(test).hasCards(sequence[0].from) &&
                !Logic(test).lastCard(sequence[0].from)!!.isOpened)
            prohibitedColumns.add(sequence[0].from)
        // <<<<<<<<<<<<<<<<

        while (firstStepIgnore < 4) {

            val stage = if (sequence.isEmpty()) solver.step(firstStepIgnore, prohibitedColumns)
            else solver.step(sequence.last().ignoreChecks, prohibitedColumns)

            if (sequence.isNotEmpty()) sequence.last().solverStage = stage

            if (!solver.nothingHappened) {

                val from = solver.from
                val to = solver.to
                val size = solver.size
                val priority = if (sequence.isNotEmpty())
                    sequence.last().priority + solver.stepPriority
                else solver.stepPriority


                if (Logic(test).hasCards(from) && !Logic(test).lastCard(from)!!.isOpened)
                    prohibitedColumns.add(from)
                sequence.add(Helper(from, to, size, stage, 0, priority, solver.solution))
                if (solver.solution) solver.solution = false
            } else {
                if (sequence.isEmpty()) break

                if (bestSeq.isEmpty() || sequence.last().priority > bestSeq.last().priority)
                    bestSeq = sequence

                solver.backStep(sequence.last().from, sequence.last().to, sequence.last().size)

                prohibitedColumns.remove(sequence.last().from)

                // В любых других вариациях начинает бесконечно наращивать последовательность
                sequence = sequence.dropLast(1).toMutableList()

                if (sequence.isEmpty()) firstStepIgnore++
                else sequence.last().ignoreChecks = sequence.last().solverStage

                solver.nothingHappened = false
            }
            if (sequence.isNotEmpty() && sequence.last().final) {
                bestSeq = sequence
                while (sequence.isNotEmpty()) {
                    solver.backStep(sequence.last().from, sequence.last().to, sequence.last().size)
                    prohibitedColumns.remove(sequence.last().from)
                    sequence = sequence.dropLast(1).toMutableList()
                }
                break
            }
        }
    }

    fun makeMove() {
        if (bestSeq.isEmpty()) {
            sequence.clear()
            findSeq()
        }
        if (bestSeq.isEmpty()) {
            Dynamics(deck).new()
            return
        }
        val pack = mutableListOf<Card>()
        val parameters = bestSeq.first()
        var line = if (Logic(deck).hasCards(parameters.to))
            Logic(deck).lastCard(parameters.to)!!.line + 1 else 0
        for (i in 1..parameters.size) {
            if (Logic(deck).hasCards(parameters.from)) pack.add(Logic(deck).lastCard(parameters.from)!!)
            pack.last().column = parameters.to
        }
        for (elem in pack.reversed()) {
            elem.line = line
            line++
        }
        bestSeq = bestSeq.drop(1)
    }
}
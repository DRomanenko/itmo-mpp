import kotlin.random.Random

class SkipListSet<E : Comparable<E>>(
    infinityLeft: E, infinityRight: E
) {
    private val head: Node<E>

    init {
        head = Node(infinityLeft, MAX_LEVEL)
        val tail = Node(infinityRight, MAX_LEVEL)
        for (i in head.next.indices) head.next[i] = tail
    }

    /**
     * Adds the specified [element] to this set if it is not already present.
     * Returns `true` if the [element] was not present, `false` otherwise.
     */
    fun add(element: E): Boolean {
        val topLevel = randomLevel()
        val w = findWindow(element)
        if (w.found) return false
        val newNode = Node(element, topLevel)
        for (level in 0..topLevel) newNode.next[level] = w.succs[level]
        for (level in 0..topLevel) w.preds[level]!!.next[level] = newNode
        return true
    }


    /**
     * Removes the specified [element] from this set.
     * Returns `true` if the [element] was presented in this set,
     * `false` otherwise.
     */
    fun remove(element: E): Boolean {
        val w = findWindow(element)
        if (!w.found) return false
        for (level in w.levelFound downTo 0) {
            w.preds[level]!!.next[level] = w.succs[level]!!.next[level]
        }
        return true
    }

    /**
     * Returns `true` if this set contains the specified [element].
     */
    fun contains(element: E): Boolean {
        val w = findWindow(element)
        return w.found
    }

    /**
     * Returns the [Window], where
     * `preds[l].x < x <= succs[l].x`
     * for every level `l`
     */
    private fun findWindow(element: E): Window<E> {
        val w = Window<E>()
        var pred: Node<E> = head
        for (level in MAX_LEVEL downTo 0) {
            var succ = pred.next[level]
            while (succ!!.element < element) {
                pred = succ
                succ = pred.next[level]
            }
            if (!w.found && element == succ.element)
                w.levelFound = level
            w.preds[level] = pred
            w.succs[level] = succ
        }
        return w
    }
}

private class Node<E>(
    val element: E,
    topLevel: Int
) {
    val next = arrayOfNulls<Node<E>?>(topLevel + 1)
}

private class Window<E> {
    var levelFound = -1 // -1 if not found
    var preds = arrayOfNulls<Node<E>>(MAX_LEVEL + 1)
    var succs = arrayOfNulls<Node<E>>(MAX_LEVEL + 1)

    val found get() = levelFound != -1
}

private fun randomLevel(): Int = Random.nextInt(MAX_LEVEL)

private const val MAX_LEVEL = 30

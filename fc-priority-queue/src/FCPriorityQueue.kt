import java.util.*
import kotlinx.atomicfu.*
import java.util.concurrent.*

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()

    inner class Query<E>(val op: () -> E?) {
        var appliedOp: E? = null
        var finished = false
        fun apply() {
            appliedOp = op()
            finished = true
        }
    }

    private val locked = atomic(false)
    private val queries = atomicArrayOfNulls<Query<E>>(QTY_THREADS)

    private fun apply(op: () -> E?): E? {
        val query = Query(op)
        var index: Int
        do index = ThreadLocalRandom.current().nextInt(0, QTY_THREADS)
        while (!queries[index].compareAndSet(null, query))
        do if (locked.compareAndSet(false, true)) {
            (0 until QTY_THREADS).asSequence()
                .mapNotNull { queries[it].value }
                .filterNot { it.finished }
                .forEach { it.apply() }
            locked.compareAndSet(true, false)
            break
        } while (!query.finished)
        queries[index].getAndSet(null)
        return query.appliedOp
    }

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        return apply { q.poll() }
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        return apply { q.peek() }
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        apply {
            q.add(element)
            null
        }
    }
}

private val QTY_THREADS = Runtime.getRuntime().availableProcessors()
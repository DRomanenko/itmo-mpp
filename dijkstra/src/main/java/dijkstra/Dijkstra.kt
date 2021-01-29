package dijkstra

import java.util.*
import java.util.concurrent.Phaser
import kotlin.Comparator
import kotlin.concurrent.thread
import kotlinx.atomicfu.atomic

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> o1!!.distance.compareTo(o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    // val q = PriorityQueue(workers, NODE_DISTANCE_COMPARATOR) // replace me with a multi-queue based PQ!
    val q = PriorityMultiQueue(workers, NODE_DISTANCE_COMPARATOR)
    q.add(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while (true) {
                // Write the required algorithm here,
                // break from this loop when there is no more node to process.
                //  Be careful, "empty queue" != "all nodes are processed".
                val cur: Node = q.poll() ?: if (q.isEmpty()) break else continue
                for (e in cur.outgoingEdges) {
                    do {
                        val oldDist = e.to.distance
                        val newDist = cur.distance + e.weight
                        if (oldDist > newDist && e.to.casDistance(oldDist, newDist)) {
                            q.add(e.to)
                            break
                        }
                    } while (oldDist > newDist)
                }
                q.decrement()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

private class PriorityMultiQueue(val workers: Int, comparator: Comparator<Node>) {
    val size = atomic(0)
    val random = Random(0)
    val q: MutableList<PriorityQueue<Node>> = Collections.nCopies(workers, PriorityQueue(comparator))

    fun poll(): Node? {
        val ind1 = random.nextInt(workers)
        val ind2 = (ind1 + 1) % workers
        synchronized(q[ind1]) {
            synchronized(q[ind2]) {
                if (q[ind1].peek() == null)
                    return q[ind2].peek()
                if (q[ind2].peek() == null)
                    return q[ind1].peek()
                return if (q[ind1].peek().distance < q[ind2].peek().distance) q[ind1].poll() else q[ind2].poll()
            }
        }
    }

    fun add(element: Node) {
        val randomIndex = random.nextInt(workers)
        synchronized(q[randomIndex]) {
            q[randomIndex].add(element)
        }
        size.incrementAndGet()
    }

    fun isEmpty(): Boolean {
        return size.compareAndSet(0, 0)
    }

    fun decrement() {
        size.decrementAndGet()
    }
}
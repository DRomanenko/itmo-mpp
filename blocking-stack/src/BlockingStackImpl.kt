import kotlinx.atomicfu.*
import kotlin.coroutines.*
import java.util.concurrent.atomic.*

@Suppress("UNCHECKED_CAST")
class BlockingStackImpl<E> : BlockingStack<E> {

    // ==========================
    // Segment Queue Synchronizer
    // ==========================
    private class Container<E>(
        val continuation: Continuation<E>? = null,
        val next: AtomicReference<Container<E>> = AtomicReference<Container<E>>(null)
    )

    private val common = Container<E>(null)
    private val enqIdx = AtomicReference(common)
    private val deqIdx = AtomicReference(common)

    private suspend fun suspend(): E = suspendCoroutine { continuation ->
        val container = Container(continuation)
        while (true) {
            val deqIdx = deqIdx.get()
            if (deqIdx.next.compareAndSet(null, container)) {
                this.deqIdx.compareAndSet(deqIdx, container)
                break
            }
        }
    }

    private fun resume(element: E) {
        while (true) {
            val enqIdx = enqIdx.get()
            if (enqIdx != deqIdx.get()) {
                val next = enqIdx.next.get() ?: continue
                if (this.enqIdx.compareAndSet(enqIdx, next)) {
                    next.continuation!!.resume(element)
                    return
                }
            }
        }
    }

    // ==============
    // Blocking Stack
    // ==============


    private val head = atomic<Node<E>?>(null)
    private val elements = atomic(0)

    override fun push(element: E) {
        val elements = this.elements.getAndIncrement()
        if (elements >= 0) {
            // push the element to the top of the stack
            while (true) {
                val pred = head.value
                if (pred != null && pred.element == SUSPENDED && head.compareAndSet(pred, pred.next.get())) {
                    break
                } else if (head.compareAndSet(pred, Node(element, pred))) {
                    return
                }
            }
        }
        // resume the next waiting receiver
        resume(element)
    }

    override suspend fun pop(): E {
        val elements = this.elements.getAndDecrement()
        if (elements > 0) {
            // remove the top element from the stack
            while (true) {
                val pred = head.value
                if (pred != null) {
                    if (head.compareAndSet(pred, pred.next.get())) return pred.element as E
                } else if (head.compareAndSet(pred, Node(SUSPENDED))) {
                    break
                }
            }
        }
        return suspend()
    }
}

private class Node<E>(val element: Any? = null, next: Node<E>? = null) {
    val next = AtomicReference(next)
}

private val SUSPENDED = Any() //
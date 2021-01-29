import java.util.concurrent.atomic.*

class Solution(private val env: Environment) : Lock<Solution.Node> {
    // todo: необходимые поля (val, используем AtomicReference)
    private val tail = AtomicReference<Node>()

    override fun lock(): Node {
        val my = Node() // сделали узел
        // todo: алгоритм
        my.locked.value = true
        val pred = tail.getAndSet(my)
        if (pred != null) {
            pred.next.value = my
            while (my.locked.value) {
                env.park()
            }
        }
        return my // вернули узел
    }

    override fun unlock(node: Node) {
        // todo: алгоритм
        if (node.next.get() == null) {
            if (tail.compareAndSet(node, null)) {
                return
            } else {
                while (node.next.get() == null);
            }
        }
        node.next.value.locked.value = false
        env.unpark(node.next.value.thread)
    }

    class Node {
        val thread: Thread = Thread.currentThread() // запоминаем поток, которые создал узел
        // todo: необходимые поля (val, используем AtomicReference)
        val next = AtomicReference<Node>(null)
        val locked = AtomicReference<Boolean>(false)
    }
}
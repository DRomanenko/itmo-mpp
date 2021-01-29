/**
 * @author :TODO: Romanenko Demian
 */
class Solution : AtomicCounter {
    private val node: Node = Node(0)
    private val last: ThreadLocal<Node> = ThreadLocal.withInitial { node }

    override fun getAndAdd(x: Int): Int {
        while (true) {
            val old = last.get().arg
            val node = Node(old + x)
            last.set(last.get().next.decide(node))
            if (last.get() == node) return old
        }
    }

    private class Node(val arg: Int, val next: Consensus<Node> = Consensus())
}

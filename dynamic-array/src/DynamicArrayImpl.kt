import kotlinx.atomicfu.*

@Suppress("UNCHECKED_CAST")
class DynamicArrayImpl<E> : DynamicArray<E> {
    private val realSize = atomic(0)
    private val core = atomic(Core<E>(INITIAL_CAPACITY))

    override fun get(index: Int): E {
        if (index >= size) throw IllegalArgumentException()
        while (true) {
            val core = core.value
            val value = core.array[index].value
            if (isNeedToMove(core, value) && value is NotNeedToMove<*>) {
                return value.value as E
            }
        }
    }

    override fun put(index: Int, element: E) {
        if (index >= size) throw IllegalArgumentException()
        while (true) {
            val core = core.value
            val value = core.array[index].value
            if (isNeedToMove(core, value) && isNotNeedToMove(index, core, element, value)) {
                return
            }
        }
    }

    override fun pushBack(element: E) {
        while (true) {
            val size = size
            val core = core.value
            if (core.capacity <= size) {
                move(core)
            } else if (isNotNeedToMove(size, core, element)) {
                realSize.incrementAndGet()
                return
            }
        }
    }

    override val size: Int get() = realSize.value

    private fun isNotNeedToMove(num : Int, core: Core<E>, value: Any?, expect: Any? = null): Boolean =
        core.array[num].compareAndSet(expect, NotNeedToMove(value))

    private fun isNeedToMove(core: Core<E>, value: Any?): Boolean =
        if (value is NeedToMove<*>) {
            move(core)
            false
        } else true

    private fun move(core: Core<E>) {
        core.next.value ?: core.next.compareAndSet(null, Core(2 * core.capacity))
        val next = core.next.value ?: return
        (0 until core.capacity).forEach { num ->
            var value: Any?

            do value = core.array[num].value
            while (value is NotNeedToMove<*> && !core.array[num].compareAndSet(value, NeedToMove(value.value)))

            if (value is NeedToMove<*>) isNotNeedToMove(num, next, value.value)
            if (value is NotNeedToMove<*>) isNotNeedToMove(num, next, value.value)
        }
        this.core.compareAndSet(core, next)
    }
}

private class NeedToMove<E>(val value: E)
private class NotNeedToMove<E>(val value: E)

private class Core<E>(
    val capacity: Int,
) {
    val array = atomicArrayOfNulls<Any>(capacity)
    val next: AtomicRef<Core<E>?> = atomic(null)
}

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME
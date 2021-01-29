import org.jetbrains.kotlinx.lincheck.LoggingLevel.INFO
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.annotations.Param
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.paramgen.IntGen
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.jetbrains.kotlinx.lincheck.verifier.VerifierState
import org.junit.Test

@Param(name = "element", gen = IntGen::class, conf = "1:10")
class SkipListSetTest {
    private val set = SkipListSet(infinityLeft = Int.MIN_VALUE, infinityRight = Int.MAX_VALUE)

    @Operation
    fun add(@Param(name = "element") element: Int): Boolean = set.add(element)

    @Operation
    fun remove(@Param(name = "element") element: Int): Boolean = set.remove(element)

    @Operation
    fun contains(@Param(name = "element") element: Int): Boolean = set.contains(element)

    @Test
    fun runTest() = StressOptions()
        .iterations(100)
        .invocationsPerIteration(50_000)
        .actorsBefore(2)
        .threads(3)
        .actorsPerThread(5)
        .sequentialSpecification(SkipListSetSequential::class.java)
        .logLevel(INFO)
        .check(this::class.java)
}

class SkipListSetSequential : VerifierState() {
    private val set = HashSet<Int>()

    fun add(@Param(name = "element") element: Int): Boolean = set.add(element)
    fun remove(@Param(name = "element") element: Int): Boolean = set.remove(element)
    fun contains(@Param(name = "element") element: Int): Boolean = set.contains(element)

    override fun extractState() = set
}
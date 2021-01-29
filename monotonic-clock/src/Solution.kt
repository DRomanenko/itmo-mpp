/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :TODO: Romanenko Demian
 */
class Solution : MonotonicClock {
    private var c1 by RegularInt(0)
    private var c2 by RegularInt(0)
    private var c3 by RegularInt(0)

    private var c1Old by RegularInt(0)
    private var c2Old by RegularInt(0)

    override fun write(time: Time) {
        c1Old = time.d1
        c2Old = time.d2
        // write right-to-left
        c3 = time.d3
        c2 = time.d2
        c1 = time.d1
    }

    override fun read(): Time {
        // read left-to-right
        val d1 = c1
        val d2 = c2
        val d3 = c3

        val d2New = c2Old
        val d1New = c1Old
        return Time(d1New, if (d1 == d1New) d2New else 0, if (d1 == d1New && d2 == d2New) d3 else 0)
    }
}
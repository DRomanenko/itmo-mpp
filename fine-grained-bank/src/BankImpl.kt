import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Bank implementation.
 *
 * :TODO: This implementation has to be made thread-safe.
 *
 * @author :TODO: Romanenko Demian
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val numberOfAccounts: Int
        get() = accounts.size

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override fun getAmount(index: Int): Long {
        return accounts[index].getRealCurrentAmount()
    }

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override val totalAmount: Long
        get() {
            accounts.forEach { it.lock() }
            val sum: Long = accounts.sumOf { it.amount }
            accounts.forEach { it.unlock() }
            return sum
        }

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override fun deposit(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        account.lock.withLock {
            check(!(amount > Bank.MAX_AMOUNT || account.amount + amount > Bank.MAX_AMOUNT)) {
                "Overflow"
            }
            account.amount += amount
            return account.amount
        }
    }

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override fun withdraw(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        val account = accounts[index]
        account.lock.withLock {
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            return account.amount
        }
    }

    /**
     * :TODO: This method has to be made thread-safe.
     */
    override fun transfer(fromIndex: Int, toIndex: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromIndex != toIndex) { "fromIndex == toIndex" }
        val to: Account?
        val from: Account?
        if (fromIndex < toIndex) {
            from = accounts[fromIndex]
            from.lock()
            to = accounts[toIndex]
            to.lock()
        } else {
            to = accounts[toIndex]
            to.lock()
            from = accounts[fromIndex]
            from.lock()
        }
        check(amount <= from.amount) {
            from.unlock()
            to.unlock()
            "Underflow"
        }
        check(!(amount > Bank.MAX_AMOUNT || to.amount + amount > Bank.MAX_AMOUNT)) {
            from.unlock()
            to.unlock()
            "Overflow"
        }
        from.amount -= amount
        from.unlock()
        to.amount += amount
        to.unlock()
    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        var amount: Long = 0

        var lock = ReentrantLock()

        fun getRealCurrentAmount() = lock.withLock {
            amount
        }

        fun lock() = lock.lock()

        fun unlock() = lock.unlock()
    }
}
import java.math.BigInteger
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.reflect.KFunction2


class RSA {
    private val random = Random(123)

    private val p = getRandomPrime()
    private val q = getRandomPrime()
    private val n = p * q
    private val phiEuler = (p - 1uL) * (q - 1uL)

    private val publicKey = generatePublicKey()
    private val privateKey = generatePrivateKey()

    fun encryptULong(message: ULong): ULong = fastPow(message, publicKey, n)
    fun decryptULong(message: ULong): ULong = fastPow(message, privateKey, n)

    init {
        println("private -- $privateKey public -- $publicKey")
    }

    fun rsa(src: ByteArray, action: KFunction2<RSA, ULong, ULong>): ByteArray {
        var length = if (src.size % 8 == 0) {
            src.size
        } else {
            src.size + (8 - src.size % 8)
        }

        var i = 0
        var res = listOf<Byte>()
        while (i < length) {
            val curSrc = src.copyOfRange(i, if (i + 8 > src.size) src.size else i + 8)
            val block64 = byteArrayToLong(curSrc)

            val c = action(this, block64)

            longToByteArray(c).forEach {
                res = res.plus(it)
            }

            i += 8
        }

        return res.toByteArray()
    }

    private fun generatePublicKey(): ULong {
        var curGcd: ULong = 0u
        var curVal: ULong = 0u

        while (curGcd != 1uL) {
            curVal = random.nextULong(ULongRange(START, STOP))
            curGcd = gcd(curVal, phiEuler)
        }

        return curVal
    }

    private fun generatePrivateKey(): ULong {
        return inverseByExtendedEuclideanAlgorithm(publicKey, phiEuler)
    }

    private fun fastPow(x: ULong, pow: ULong, mod: ULong): ULong {
        var res: BigInteger = BigInteger.valueOf(1)
        var t: BigInteger = BigInteger.valueOf(x.toLong())
        var powRest = pow

        while (powRest > 0uL) {
            if (powRest.mod(2uL) != 0uL) {
                res = (res * t).mod(BigInteger.valueOf(mod.toLong()))
            }

            t = (t * t).mod(BigInteger.valueOf(mod.toLong()))
            powRest /= 2uL
        }

        return res.toLong().toULong()
    }

    private fun gcd(x1_: ULong, x2_: ULong): ULong {
        var x1 = x1_
        var x2 = x2_

        while (x1 != x2) {
            if (x1 > x2) {
                x1 -= x2
            }
            else {
                x2 -= x1
            }
        }

        return x1
    }

    // Returns number n from (n * fNumber) mod sNumber == 1
    private fun inverseByExtendedEuclideanAlgorithm(cofactor: ULong, modNumber: ULong): ULong {
        var outT = 0L
        var currentR = modNumber.toLong()

        var newT = 1L
        var newR = cofactor.toLong()

        var quotient: Long
        while (newR != 0L) {
            quotient = currentR / newR

            newT = (outT - quotient * newT).also { outT = newT }
            newR = (currentR - quotient * newR).also { currentR = newR }
        }

        if (currentR > 1L)
            throw Exception("No such inverse value")

        if (outT < 0)
            outT += modNumber.toLong()

        return outT.toULong()
    }

    private fun getRandomPrime(): ULong {
        var num = random.nextULong(ULongRange(START, STOP))
        while (!isPrime(num)) {
            num = random.nextULong(ULongRange(START, STOP))
        }

        return num
    }

    private fun isPrime(x: ULong): Boolean {
        for (i in ULongRange(2u, x - 1u)) {
            if (x.mod(i) == 0uL) {
                return false
            }
        }

        return true
    }

    private fun byteArrayToLong(byteArray: ByteArray) : ULong {
        var res: ULong = 0u
        for (i in 0 until 8) {
            val c: Byte = if (i < byteArray.size) {
                byteArray[i]
            }
            else {
                0.toByte()
            }

            res = (res shl 8) or c.toUByte().toULong()
        }

        return res
    }

    private fun longToByteArray(block64b: ULong): ByteArray {
        var res = MutableList<Byte>(8) { 0 }
        for (i in 0 until 8) {
            val b: Byte = ((block64b shr (56 - i * 8)) and 255u).toByte()
            res[i] = b
        }

        return res.toByteArray()
    }

    companion object {
        private val START: ULong = 1000u
        private val STOP: ULong = 10000u
    }
}
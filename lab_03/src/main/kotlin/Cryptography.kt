class Cryptography {
    fun DES(cryptographyAction: CryptographyAction, src: ByteArray, key: String): ByteArray {
        var length = if (src.size % 8 == 0) {
            src.size
        } else {
            src.size + (8 - src.size % 8)
        }

        val keys = keyExpansion(stringBlockToLong(key))

        var i = 0
        var res = listOf<Byte>()
        while (i < length) {
            // val curSrc = src.substring(i until if (i + 8 > src.length) src.length else i + 8)
            val curSrc = src.copyOfRange(i, if (i + 8 > src.size) src.size else i + 8)
            val test: ULong = byteArrayToLong(curSrc)
            val test1 = longToByteArray(test)
            val block64 = initialPermutation(byteArrayToLong(curSrc))
            val splitedBlock = split64(block64)

            val feistel = join64(feistelCipher(cryptographyAction, splitedBlock, keys))

            longToByteArray(finalPermutation(feistel)).forEach {
                res = res.plus(it)
            }

            i += 8
        }

        return res.toByteArray()
    }

    private fun feistelCipher(cryptographyAction: CryptographyAction, parts: Pair<UInt, UInt>, keys: List<ULong>): Pair<UInt, UInt> {
        var res = parts

        if (cryptographyAction == CryptographyAction.ENCRYPTION) {
            keys.forEach {
                res = roundFeistelCipher(res, it)
            }
        }
        else if (cryptographyAction == CryptographyAction.DECRYPTION) {
            keys.reversed().forEach {
                res = roundFeistelCipher(res, it)
            }
        }

        return Pair(res.second, res.first)
    }

    private fun roundFeistelCipher(parts: Pair<UInt, UInt>, key: ULong): Pair<UInt, UInt> {
        val temp: UInt = parts.second
        val part2 = feistelFunc(parts.second, key) xor parts.first

        return Pair(temp, part2)
    }

    private fun feistelFunc(block32: UInt, key48b: ULong): UInt {
        // expansion permutation
        var block48: ULong = 0u
        for (i in 0 until 48) {
            block48 = block48 or (((block32 shr (32 - EP[i])) and 1u).toULong() shl (63 - i))
        }

        // xor
        block48 = block48 xor key48b

        // substitution
        val block32 = substitutions(block48)

        // permutation
        var res: UInt = 0u
        for (i in 0 until 32) {
            res = res or ((block32 shr (32 - P[i]) and 1u) shl (31 - i))
        }

        return res
    }

    private fun substitutions(block48: ULong): UInt {
        val blocks6b = MutableList<UByte>(8) { 0.toUByte() }
        for (i in 0 until 8) {
            blocks6b[i] = ((block48 shr (58 - (i * 6))).toUInt()).shl(2).toUByte()
        }

        var res: UInt = 0u

        for (i in 0 until 8) {
            val extreme = ((blocks6b[i].toUInt() shr 6) and 2u).toUByte() or ((blocks6b[i].toUInt() shr 2) and 1u).toUByte()
            val middle = ((blocks6b[i].toUInt() shr 3) and 0xFu).toUByte()
            res = res or (SBOX[i][extreme.toInt()][middle.toInt()].toUInt() shl (i * 4))
        }

        return res
    }

    private fun keyExpansion(key: ULong): List<ULong> {
        var block28b_1: UInt = 0.toUInt()
        var block28b_2: UInt = 0.toUInt()

        // split
        for (i in 0 until 28) {
            block28b_1 = block28b_1 or (((key shr (64 - KP1[i])) and 1u).toUInt() shl (31 - i))
            block28b_2 = block28b_2 or (((key shr (64 - KP2[i])) and 1u).toUInt() shl (31 - i))
        }

        var res = MutableList<ULong>(16) { 0.toULong() }
        for (i in 0..15) {
            val n = when(i) {
                0, 1, 8, 15 -> 1
                else -> 2
            }

            // shift
            block28b_1 = (block28b_1 shl n) or (block28b_1 shr (28 - n))
            block28b_2 = (block28b_2 shl n) or (block28b_2 shr (28 - n))

            // join
            var block56b: ULong = (block28b_1 shr 4).toULong()
            block56b = ((block56b shl 32) or block28b_2.toULong()) shl 4

            // permutation
            var block48b: ULong = 0.toULong()
            for (i in 0 until 48) {
                block48b = block48b or (((block56b shr (64 - CP[i])) and 1u) shl (63 - i))
            }

            res[i] = block48b
        }

        return res
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

    private fun stringBlockToLong(strBlock: String) : ULong {
        var res: ULong = 0u
        for (i in 0 until 8) {
            val c: UByte = if (i < strBlock.length) {
                strBlock[i].code.toUByte()
            }
            else {
                0.toUByte()
            }

            res = (res shl 8) or c.toULong()
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

    private fun longToString(block64b: ULong): String {
        var res: String = String()
        for (i in 0 until 8) {
            val c: Char = ((block64b shr (56 - i * 8)) and 255u).toInt().toChar()
            res = res.plus(c)
        }

        return res
    }

    private fun initialPermutation(block64b: ULong): ULong {
        var res: ULong = 0.toULong()
        for (i in 0 until 64) {
            res = res or (((block64b shr (64 - IP[i])) and 1.toULong()) shl (63 - i))
        }

        return res
    }

    private fun finalPermutation(block64b: ULong): ULong {
        var res: ULong = 0.toULong()
        for (i in 0 until 64) {
            res = res or (((block64b shr (64 - FP[i])) and 1.toULong()) shl (63 - i))
        }

        return res
    }

    private fun split64(block64b: ULong): Pair<UInt, UInt> {
        val part1: UInt = (block64b shr 32).toUInt()
        val part2: UInt = block64b.toUInt()

        return Pair(part1, part2)
    }

    private fun join64(pair: Pair<UInt, UInt>): ULong {
        var res: ULong = pair.first.toULong()
        res = (res shl 32) or pair.second.toULong()

        return res
    }

    companion object {
        private val KP1 = listOf(    57, 49, 41, 33, 25, 17, 9 , 1 , 58, 50, 42, 34, 26, 18,
            10, 2 , 59, 51, 43, 35, 27, 19, 11, 3 , 60, 52, 44, 36)
        private val KP2 = listOf(63, 55, 47, 39, 31, 23, 15, 7 , 62, 54, 46, 38, 30, 22,
            14, 6 , 61, 53, 45, 37, 29, 21, 13, 5 , 28, 20, 12, 4)
        private val CP = listOf(14, 17, 11, 24, 1 , 5 , 3 , 28, 15, 6 , 21, 10,
            23, 19, 12, 4 , 26, 8 , 16, 7 , 27, 20, 13, 2 ,
            41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32)
        private val IP = listOf(58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9 , 1, 59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7)
        private val FP = listOf(40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9 , 49, 17, 57, 25)
        private val EP = listOf(32, 1 , 2 , 3 , 4 , 5 , 4 , 5 , 6 , 7 , 8 , 9 ,
            8 , 9 , 10, 11, 12, 13, 12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1)
        private val P = listOf(16, 7 , 20, 21, 29, 12, 28, 17, 1 , 15, 23, 26, 5 , 18, 31, 10,
            2 , 8 , 24, 14, 32, 27, 3 , 9 , 19, 13, 30, 6 , 22, 11, 4 , 25)

        private val SBOX = listOf(
            listOf(
                listOf(14, 4 , 13, 1 , 2 , 15, 11, 8 , 3 , 10, 6 , 12, 5 , 9 , 0 , 7),
                listOf(0 , 15, 7 , 4 , 14, 2 , 13, 1 , 10, 6 , 12, 11, 9 , 5 , 3 , 8),
                listOf(4 , 1 , 14, 8 , 13, 6 , 2 , 11, 15, 12, 9 , 7 , 3 , 10, 5 , 0),
                listOf(15, 12, 8 , 2 , 4 , 9 , 1 , 7 , 5 , 11, 3 , 14, 10, 0 , 6 , 13)
            ),
            listOf(
                listOf(15, 1 , 8 , 14, 6 , 11, 3 , 4 , 9 , 7 , 2 , 13, 12, 0 , 5 , 10),
                listOf(3 , 13, 4 , 7 , 15, 2 , 8 , 14, 12, 0 , 1 , 10, 6 , 9 , 11, 5),
                listOf(0 , 14, 7 , 11, 10, 4 , 13, 1 , 5 , 8 , 12, 6 , 9 , 3 , 2 , 15),
                listOf(13, 8 , 10, 1 , 3 , 15, 4 , 2 , 11, 6 , 7 , 12, 0 , 5 , 14, 9)
            ),
            listOf(
                listOf(10, 0 , 9 , 14, 6 , 3 , 15, 5 , 1 , 13, 12, 7 , 11, 4 , 2 , 8),
                listOf(13, 7 , 0 , 9 , 3 , 4 , 6 , 10, 2 , 8 , 5 , 14, 12, 11, 15, 1),
                listOf(13, 6 , 4 , 9 , 8 , 15, 3 , 0 , 11, 1 , 2 , 12, 5 , 10, 14, 7),
                listOf(1 , 10, 13, 0 , 6 , 9 , 8 , 7 , 4 , 15, 14, 3 , 11, 5 , 2 , 12)
            ),
            listOf(
                listOf(7 , 13, 14, 3 , 0 , 6 , 9 , 10, 1 , 2 , 8 , 5 , 11, 12, 4 , 15),
                listOf(13, 8 , 11, 5 , 6 , 15, 0 , 3 , 4 , 7 , 2 , 12, 1 , 10, 14, 9),
                listOf(10, 6 , 9 , 0 , 12, 11, 7 , 13, 15, 1 , 3 , 14, 5 , 2 , 8 , 4),
                listOf(3 , 15, 0 , 6 , 10, 1 , 13, 8 , 9 , 4 , 5 , 11, 12, 7 , 2 , 14)
            ),
            listOf(
                listOf(2 , 12, 4 , 1 , 7 , 10, 11, 6 , 8 , 5 , 3 , 15, 13, 0 , 14, 9),
                listOf(14, 11, 2 , 12, 4 , 7 , 13, 1 , 5 , 0 , 15, 10, 3 , 9 , 8 , 6),
                listOf(4 , 2 , 1 , 11, 10, 13, 7 , 8 , 15, 9 , 12, 5 , 6 , 3 , 0 , 14),
                listOf(11, 8 , 12, 7 , 1 , 14, 2 , 13, 6 , 15, 0 , 9 , 10, 4 , 5 , 3)
            ),
            listOf(
                listOf(12, 1 , 10, 15, 9 , 2 , 6 , 8 , 0 , 13, 3 , 4 , 14, 7 , 5 , 11),
                listOf(10, 15, 4 , 2 , 7 , 12, 9 , 5 , 6 , 1 , 13, 14, 0 , 11, 3 , 8),
                listOf(9 , 14, 15, 5 , 2 , 8 , 12, 3 , 7 , 0 , 4 , 10, 1 , 13, 11, 6),
                listOf(4 , 3 , 2 , 12, 9 , 5 , 15, 10, 11, 14, 1 , 7 , 6 , 0 , 8 , 13)
            ),
            listOf(
                listOf(4 , 11, 2 , 14, 15, 0 , 8 , 13, 3 , 12, 9 , 7 , 5 , 10, 6 , 1),
                listOf(13, 0 , 11, 7 , 4 , 9 , 1 , 10, 14, 3 , 5 , 12, 2 , 15, 8 , 6),
                listOf(1 , 4 , 11, 13, 12, 3 , 7 , 14, 10, 15, 6 , 8 , 0 , 5 , 9 , 2),
                listOf(6 , 11, 13, 8 , 1 , 4 , 10, 7 , 9 , 5 , 0 , 15, 14, 2 , 3 , 12)
            ),
            listOf(
                listOf(13, 2 , 8 , 4 , 6 , 15, 11, 1 , 10, 9 , 3 , 14, 5 , 0 , 12, 7),
                listOf(1 , 15, 13, 8 , 10, 3 , 7 , 4 , 12, 5 , 6 , 11, 0 , 14, 9 , 2),
                listOf(7 , 11, 4 , 1 , 9 , 12, 14, 2 , 0 , 6 , 10, 13, 15, 3 , 5 , 8),
                listOf(2 , 1 , 14, 7 , 4 , 10, 8 , 13, 15, 12, 9 , 0 , 3 , 5 , 6 , 11)
            )
        )
    }
}
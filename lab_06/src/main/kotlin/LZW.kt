import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.*

class ProcessorLZW(val filename: String) {
    val dictionary = DictionaryLZW()

    private fun getFileBytes(): ByteArray {
        return File(filename).readBytes()
    }

    private fun getFileShorts(): ShortArray {
        val outList = mutableListOf<Short>()

        val stream = DataInputStream(File(filename).inputStream())

        while (stream.available() >= Short.SIZE_BYTES) {
            outList.add(stream.readShort())
        }

//        println("Read: ")
//        println(outList)

        return outList.toShortArray()
    }

    fun compressFile() {
        dictionary.initDictionary()

        val bytesToCompress = getFileBytes()
//        println("Read bytes: ")
//        bytesToCompress.forEach { print("$it ") }
//        println()
        val compressedInts = mutableListOf<Short>()

        var currentWord = shortArrayOf()

        for (currentByte in bytesToCompress) {
            if (dictionary.dictionary.filterKeys { it.contentEquals(currentWord + currentByte.toShort()) }
                    .isNotEmpty()) {
                currentWord += currentByte.toShort()
            } else {
                dictionary.dictionary[currentWord + currentByte.toShort()] = dictionary.dictionary.size.toShort()
                compressedInts.add(dictionary.dictionary.filterKeys { it.contentEquals(currentWord) }.values.first())
                currentWord = shortArrayOf(currentByte.toShort())
            }
        }

        compressedInts.add(dictionary.dictionary.filterKeys { it.contentEquals(currentWord) }.values.first())
//
//        for (currentByte in bytesToCompress.slice(1 until bytesToCompress.size)) {
//            val currentWordWithByteAdded = currentWord.toIntArray() + currentByte.toInt()
//
//            if (dictionary.dictionary.filterKeys { it.contentEquals(currentWordWithByteAdded) }.isNotEmpty()) {
//                currentWord = currentWordWithByteAdded.toMutableList()
//            } else {
//                compressedInts.add(
//                    dictionary.dictionary.filterKeys { it.contentEquals(currentWord.toIntArray()) }.values.first()
//                )
//                dictionary.dictionary[currentWordWithByteAdded] = dictionary.dictionary.size
//                currentWord = mutableListOf(currentByte.toInt())
//            }
//        }
//
//        compressedInts.add(
//            dictionary.dictionary.filterKeys { it.contentEquals(currentWord.toIntArray()) }.values.first()
//        )
//
//        dictionary.dictionary.forEach { it.key.forEach { print("$it ") }; print("value: ${it.value}"); println() }
//        dictionary.dictionary.keys.forEach { println(); it.forEach { print("$it ") } }
//
//        println("Uncompressed: ${bytesToCompress.toMutableList()}")
//        println("Compressed: $compressedInts")

        val file = File("compressed$filename")
        file.createNewFile()
        val stream = DataOutputStream(file.outputStream())
        compressedInts.forEach { stream.writeShort(it.toInt()) }
    }

    fun decompressFile() {
        dictionary.initDictionary()

        val intsToDecompress = getFileShorts()
        val decompressedBytes = mutableListOf<Byte>()

        var old = intsToDecompress.first()
        dictionary.dictionary.filterValues { it == old }.keys.first()
            .forEach { decompressedBytes.add(it.toByte()) }

        for (currentInt in intsToDecompress.slice(1 until intsToDecompress.size)) {
            if (dictionary.dictionary.filterValues { it == currentInt }.isNotEmpty()) {
                val translation = dictionary.dictionary.filterValues { it == currentInt }.keys.first()
                translation.forEach { decompressedBytes.add(it.toByte()) }
                val translationOld = dictionary.dictionary.filterValues { it == old }.keys.first()
                dictionary.dictionary[translationOld + translation.first()] = dictionary.dictionary.size.toShort()
                old = currentInt
            } else {
                val translation = dictionary.dictionary.filterValues { it == old }.keys.first()
                val add = translation + translation.first()
                add.forEach { decompressedBytes.add(it.toByte()) }
                dictionary.dictionary[add] = dictionary.dictionary.size.toShort()
                old = currentInt
            }
        }

        println("Decompressed: ")
        decompressedBytes.forEach { print("$it ") }
        File("decompressed$filename").writeBytes(decompressedBytes.toByteArray())
    }
}
import java.io.File

fun main(args: Array<String>) {
    val inputFile = "/home/max/repos/information-security/lab_03/build/classes/kotlin/main/heh.rar"
    val key = "zxcvbnmk"

    val file = File(inputFile)
    val fileData = file.readBytes()

    // val input = "qwertyuiopzxcvbnmlkjhgfdsa"

    // val input = listOf<Byte>(-1, 2, 3, 4, 5, 127, -28, 37, 34, 86, 12, 45)

    println(fileData.forEach { print("$it,") })

    val enc = Cryptography().DES(CryptographyAction.ENCRYPTION, fileData, key)

    // println(enc)
    // println(enc.toByteArray().forEach { print("$it,") })
    enc.forEach { print("$it, ") }

    val dec = Cryptography().DES(CryptographyAction.DECRYPTION, enc, key).copyOfRange(0, fileData.size)

    println()
    dec.forEach { print("$it, ") }

    // println(dec)
    println(dec.forEach { print("$it,") })


    File("/home/max/repos/information-security/lab_03/build/classes/kotlin/main/res.rar").writeBytes(dec)
}
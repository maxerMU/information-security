import java.io.File

fun main(args: Array<String>) {
    val rsa = RSA()
    val inputFile = "/home/max/repos/information-security/lab_04/build/classes/kotlin/main/heh.rar"
    val encryptedFile = "/home/max/repos/information-security/lab_04/build/classes/kotlin/main/enc.rar"
    val resFile = "/home/max/repos/information-security/lab_04/build/classes/kotlin/main/res.rar"

    encryptFile(rsa, inputFile, encryptedFile)
    decryptFile(rsa, encryptedFile, resFile)

   // val x: ULong = 5936151270347178240uL
   // val x: ULong = 24uL

   // val c = rsa.encryptULong(x)
   // println("encrypted: $c")

   // val m = rsa.decryptULong(c)
   // println("decrypted: $m")

   //  val inputFile = "/home/max/repos/information-security/lab_04/build/classes/kotlin/main/heh.rar"

   //  val file = File(inputFile)
   //  val fileData = file.readBytes()

    // // val input = "qwertyuiopzxcvbnmlkjhgfdsa"

    // // val input = listOf<Byte>(-1, 2, 3, 4, 5, 127, -28, 37, 34, 86, 12, 45)

    // println(fileData.forEach { print("$it,") })

    // val enc = rsa.rsa(fileData, RSA::encryptULong)

    // // println(enc)
    // // println(enc.toByteArray().forEach { print("$it,") })
    // enc.forEach { print("$it, ") }

    // val dec = rsa.rsa(enc, RSA::decryptULong).copyOfRange(0, fileData.size)

    // println()
    // dec.forEach { print("$it, ") }

    // // println(dec)
    // println(dec.forEach { print("$it,") })


    // File("/home/max/repos/information-security/lab_04/build/classes/kotlin/main/res.rar").writeBytes(dec)
}
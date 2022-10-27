import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File

fun encryptFile(rsa: RSA, src: String, dst: String) {
    val inputStream = DataInputStream(File(src).inputStream())
    val outputStream = DataOutputStream(File(dst).outputStream())

    while (inputStream.available() > 0) {
        val enc = rsa.encryptULong(inputStream.readUnsignedByte().toULong())
        outputStream.writeLong(enc.toLong())
    }

    outputStream.close()
}

fun decryptFile(rsa: RSA, src: String, dst: String) {
    val inputStream = DataInputStream(File(src).inputStream())
    val outputStream = DataOutputStream(File(dst).outputStream())

    while (inputStream.available() > 0) {
        val enc = rsa.decryptULong(inputStream.readLong().toULong())
        outputStream.writeByte(enc.toInt())
    }

    outputStream.close()
}

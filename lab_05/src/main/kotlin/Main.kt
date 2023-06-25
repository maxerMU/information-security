import java.io.File
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

const val PRIVATE_KEY_FILENAME = "private_key.key"
const val PUBLIC_KEY_FILENAME = "public_key.pub"
const val SIGNATURE_FILENAME = "signature"

object KeyHelper {
    private val keyFactory by lazy { KeyFactory.getInstance("RSA") }

    fun getPublicKey(encodedPublic: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(encodedPublic)
        return keyFactory.generatePublic(keySpec)
    }

    fun getPrivateKey(encodedPrivate: ByteArray): PrivateKey {
        val keySpec = PKCS8EncodedKeySpec(encodedPrivate)
        return keyFactory.generatePrivate(keySpec)
    }
}

fun getPublicKey(): PublicKey {
    val encodedPublic = File(PUBLIC_KEY_FILENAME).readBytes()
    return KeyHelper.getPublicKey(encodedPublic)
}

fun getPrivateKey(): PrivateKey {
    val encodedPrivate = File(PRIVATE_KEY_FILENAME).readBytes()
    return KeyHelper.getPrivateKey(encodedPrivate)
}

fun generateKeys() {
    val generator = KeyPairGenerator.getInstance("RSA")
    val keys: KeyPair = generator.generateKeyPair()

    File(PUBLIC_KEY_FILENAME).writeBytes(keys.public.encoded)
    File(PRIVATE_KEY_FILENAME).writeBytes(keys.private.encoded)
}

fun signFile(filename: String) {
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initSign(getPrivateKey())
    signature.update(File(filename).readBytes())
    File(SIGNATURE_FILENAME).writeBytes(signature.sign())
}

fun verifyDigitalSignature(filename: String) {
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initVerify(getPublicKey())
    signature.update(File(filename).readBytes())

    if (signature.verify(File(SIGNATURE_FILENAME).readBytes()))
        println("It's ok")
    else
        println("File corrupted, don't open it o.o")
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(
            "Required arguments:\n" +
                    "1. Mode [keygen, sign, verify];" +
                    "2. File name (not used in key generation)"
        )
    }

    val mode = args[0]
    if (mode !in listOf("keygen", "sign", "verify")) {
        println("Invalid mode, check required arguments.")

        return
    }

    if (mode != "keygen" && args.size < 2) {
        println("File name to be signed or verified is needed.")

        return
    }

    when (mode) {
        "keygen" -> generateKeys()
        "sign" -> signFile(args[1])
        "verify" -> verifyDigitalSignature(args[1])
    }
}
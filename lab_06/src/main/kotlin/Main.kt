fun main(args: Array<String>) {
    if (args.size < 2) {
        println(
            "Usage:\n" +
                    "1. Mode [compress, decompress]\n" +
                    "2. Name of file to work with"
        )

        return
    }

    val processorLZW = ProcessorLZW(args[1])

    when (args[0]) {
        "compress" -> processorLZW.compressFile()
        "decompress" -> processorLZW.decompressFile()
        else -> {
            println("Invalid mode passed")

            return
        }
    }
}
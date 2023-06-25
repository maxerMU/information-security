class DictionaryLZW {
    val dictionary: HashMap<ShortArray, Short> = hashMapOf()

    fun initDictionary() {
        dictionary.clear()

        for (currentByte in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            dictionary[shortArrayOf(currentByte.toShort())] = currentByte.toShort()
        }
    }
}
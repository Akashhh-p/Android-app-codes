package com.example.phonebook

object SmsUtils {
    fun getMessageKey(message: SmsMessage): String {
        // Normalize address: remove non-alphanumeric if it's a phone number, but keep it simple
        val normalizedAddress = message.address.filter { it.isLetterOrDigit() }.lowercase()
        return "${normalizedAddress}|${message.body}|${message.timestamp}"
    }

    fun getUniqueNewMessages(
        imported: List<SmsMessage>,
        existing: List<SmsMessage>
    ): List<SmsMessage> {
        val existingKeys = existing.map { getMessageKey(it) }.toSet()
        val uniqueImported = mutableListOf<SmsMessage>()
        val seenInCurrentBatch = mutableSetOf<String>()

        for (message in imported) {
            val key = getMessageKey(message)
            if (!existingKeys.contains(key) && !seenInCurrentBatch.contains(key)) {
                uniqueImported.add(message)
                seenInCurrentBatch.add(key)
            }
        }
        return uniqueImported
    }
}

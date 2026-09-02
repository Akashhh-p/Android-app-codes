package com.example.phonebook

object ContactUtils {
    /**
     * Normalizes a name for comparison: trimmed and lowercase.
     */
    fun normalizeName(name: String): String {
        return name.trim().lowercase()
    }

    /**
     * Normalizes a phone number for comparison: removes all non-digit characters.
     */
    fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    /**
     * Generates a unique key for a contact based on normalized name and phone number.
     */
    fun getContactKey(name: String, phone: String): String {
        return "${normalizeName(name)}|${normalizePhone(phone)}"
    }

    /**
     * Filters a list of imported contacts, returning only those that do not already exist
     * in the existing list and are unique within the imported list itself.
     */
    fun getUniqueNewContacts(
        imported: List<Contact>,
        existing: List<Contact>
    ): List<Contact> {
        val existingKeys = existing.map { getContactKey(it.name, it.phoneNumber) }.toSet()
        val uniqueImported = mutableListOf<Contact>()
        val seenInCurrentBatch = mutableSetOf<String>()

        for (contact in imported) {
            val key = getContactKey(contact.name, contact.phoneNumber)
            if (!existingKeys.contains(key) && !seenInCurrentBatch.contains(key)) {
                uniqueImported.add(contact)
                seenInCurrentBatch.add(key)
            }
        }
        return uniqueImported
    }

    /**
     * Checks if a contact already exists in a list based on name and phone number.
     */
    fun isDuplicate(name: String, phone: String, existing: List<Contact>): Boolean {
        val key = getContactKey(name, phone)
        return existing.any { getContactKey(it.name, it.phoneNumber) == key }
    }
}

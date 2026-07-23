package com.example.myapplication.ui.call

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor() : CallRepository{
    private val contacts = mutableListOf<Contact>(
        Contact(id = 1L, name = "엄마", phoneNumber = "010-0000-0001"),
        Contact(id = 2L, name = "동생", phoneNumber = "010-0000-0002")
    )

    override suspend fun getContacts(): List<Contact> {
        return contacts
    }

    override suspend fun addContact(contact: Contact) {
        val newContact = contact.copy(
            id=(contacts.maxOfOrNull {it.id}?:0L)+1L
        )
        contacts.add(newContact)
    }
}
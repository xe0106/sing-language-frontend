package com.example.myapplication.ui.call


interface CallRepository {
    suspend fun getContacts():List<Contact>

    suspend fun addContact(contact: Contact)
}
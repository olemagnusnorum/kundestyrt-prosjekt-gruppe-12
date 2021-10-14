package com.backend.plugins

class Inbox {

    private var inbox: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val allowedResources= listOf<String>()


    fun addToInbox(key: String, data: String): Unit{
        if (inbox.containsKey(key)){
            inbox[key]?.add(data)
        } else {
            var newList = mutableListOf<String>(data)
            inbox[key] = newList
        }
    }


    fun deleteFromInbox(key: String, index: Int): Unit{
        if (inbox.containsKey(key)){
            inbox[key]?.removeAt(index)
        } else {
          throw Exception("No index or key in inbox!")
        }
    }

    fun getInbox(): MutableMap<String, MutableList<String>> {
        return inbox
    }

}
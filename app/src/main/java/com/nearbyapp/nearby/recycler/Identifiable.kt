package com.nearbyapp.nearby.recycler

import com.databinding.databinding.IData

interface Identifiable : IData {

    fun id(): String
    
}
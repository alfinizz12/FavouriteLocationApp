package com.example.favouritelocationnotesapp.Model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class LocationNote(
    // 'id' akan diisi manual setelah mengambil dari Firestore
    // agar tidak ikut tersimpan di firebase
    @get:Exclude var id: String = "",

    // fields yg disimpan di firebase
    var userId: String? = null,
    var title: String = "",
    var description: String = "",
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,

    @ServerTimestamp
    val createdAt: Date? = null
) {
    constructor() : this("", null, "", "", 0.0, 0.0, null)
}
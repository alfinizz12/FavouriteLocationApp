package com.example.favouritelocationnotesapp

import com.google.firebase.firestore.Exclude // Opsional tapi bagus
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class LocationNote(
    // 'id' akan kita isi manual setelah mengambil dari Firestore
    // @Exclude agar tidak ikut tersimpan kembali ke Firestore
    @get:Exclude var id: String = "",

    // Field-field yang kita simpan di Firestore
    var userId: String? = null,
    var title: String = "",
    var description: String = "",
    var latitude: Double? = 0.0,  // <-- WAJIB ADA NILAI DEFAULT
    var longitude: Double? = 0.0, // <-- WAJIB ADA NILAI DEFAULT

    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Constructor kosong ini sekarang diperlukan oleh Firestore
    // karena kita punya constructor utama dengan argumen.
    // ATAU, jika semua properti di atas punya default, ini tidak wajib
    // tapi lebih aman ditambahkan.
    constructor() : this("", null, "", "", 0.0, 0.0, null)
}
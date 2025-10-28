package com.example.favouritelocationnotesapp.Activites

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.favouritelocationnotesapp.Model.LocationNote
import com.example.favouritelocationnotesapp.R
import com.example.favouritelocationnotesapp.adapter.LocationNotesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import androidx.core.net.toUri

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: TextView
    private lateinit var logoutBtn: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: LocationNotesAdapter
    private val itemList = ArrayList<LocationNote>()
    private lateinit var fabAddLocation: FloatingActionButton

    private val locationPermissionCode = 1001
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        db = Firebase.firestore
        userEmail = findViewById(R.id.userEmail)
        logoutBtn = findViewById(R.id.logoutBtn)
        recyclerView = findViewById(R.id.rvLocation)
        fabAddLocation = findViewById(R.id.addFavLocationButton)

        userEmail.text = auth.currentUser?.email

        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        fabAddLocation.setOnClickListener {
            showAddLocationDialog()
        }

        adapter = LocationNotesAdapter(
            itemList,
            onItemClick = { locationNote ->
                // open maps
                if (locationNote.latitude != null && locationNote.longitude != null) {
                    val uri = "https://www.google.com/maps/search/?api=1&query=${locationNote.latitude},${locationNote.longitude}"
                    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            },

            onItemLongClick = { locationNote ->
                showUpdateDialog(locationNote)
            },

            onDeleteClick = { locationNote ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Lokasi")
                    .setMessage("Anda yakin ingin menghapus '${locationNote.title}'?")
                    .setPositiveButton("Hapus") { _, _ ->
                        // Logika Hapus
                        val userId = auth.currentUser?.uid ?: return@setPositiveButton
                        if (locationNote.id.isEmpty()) {
                            Toast.makeText(this, "Error: ID Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        db.collection("users").document(userId)
                            .collection("locations")
                            .document(locationNote.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Lokasi dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Request permission
        requestLocationPermission()

        // Load data user dan listen live changes
        listenUserLocations()
    }

    private fun logout() {
        auth.signOut()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    // pengambilan lokasi saat ini
    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val androidLocation: android.location.Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            androidLocation?.let { location ->
                currentLatitude = location.latitude
                currentLongitude = location.longitude
            }
        }
    }

    // fungsi menampilkan dialog
    private fun showUpdateDialog(locationNote: LocationNote) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_location, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.etTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.etDescription)

        titleInput.setText(locationNote.title)
        descInput.setText(locationNote.description)

        AlertDialog.Builder(this)
            .setTitle("Update Favorite Location")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newTitle = titleInput.text.toString()
                val newDesc = descInput.text.toString()

                updateLocationInFirestore(locationNote, newTitle, newDesc)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // fungsi untuk update
    private fun updateLocationInFirestore(locationNote: LocationNote, newTitle: String, newDescription: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Error: Pengguna tidak login", Toast.LENGTH_SHORT).show()
            return
        }

        if (locationNote.id.isEmpty()) {
            Toast.makeText(this, "Error: ID Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "title" to newTitle,
            "description" to newDescription
        )

        // logic update firestore
        db.collection("users").document(userId)
            .collection("locations")
            .document(locationNote.id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Lokasi berhasil di-update!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal meng-update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddLocationDialog() {
        getCurrentLocation()

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_location, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.etTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(this)
            .setTitle("Add Favorite Location")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString()
                val desc = descInput.text.toString()
                val lat = currentLatitude
                val lon = currentLongitude
                saveLocationToFirestore(title, desc, lat, lon)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveLocationToFirestore(title: String, desc: String, lat: Double?, lon: Double?) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Error: Pengguna tidak login", Toast.LENGTH_SHORT).show()
            Log.e("FirestoreSave", "Gagal menyimpan: User ID null")
            return
        }

        val locationData = hashMapOf(
            "title" to title,
            "description" to desc,
            "latitude" to lat,
            "longitude" to lon,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId)
            .collection("locations")
            .add(locationData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                Log.d("FirestoreSave", "Dokumen disimpan dengan ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.w("FirestoreSave", "Error adding document", e)
            }
    }

    private fun listenUserLocations() {
        val userId = auth.currentUser?.uid ?: return

        listenerRegistration = db.collection("users").document(userId)
            .collection("locations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("FirestoreLoad", "Listen failed.", e)
                    return@addSnapshotListener
                }

                itemList.clear()
                snapshots?.forEach { doc ->
                    val locationNote = doc.toObject(LocationNote::class.java)
                    locationNote.id = doc.id

                    itemList.add(locationNote)
                }
                // Beritahu ke adapter bahwa data berubah
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // hapus listener ketika aplikasi close
        listenerRegistration?.remove()
    }
}

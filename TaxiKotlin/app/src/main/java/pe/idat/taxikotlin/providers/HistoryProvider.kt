package pe.idat.taxikotlin.providers

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pe.idat.taxikotlin.models.Booking
import pe.idat.taxikotlin.models.History

class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()

    fun create(history: History): Task<DocumentReference> {
        return db.add(history).addOnFailureListener{
            Log.d("FIRESTORE", "Error: ${it.message}")
        }
    }

    fun gethistoryById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }

    fun getLastHistory():Query{ //compuesta
        return db.whereEqualTo("idClient", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun getHistories():Query{ //compuesta
        return db.whereEqualTo("idClient", authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun updateCalificationToDriver(id: String, calification: Float): Task<Void> {
        return db.document(id).update("calificationToDriver", calification).addOnFailureListener{ exception ->
            Log.d("FIRESTORE", "ERROR: ${exception.message}")
        }
    }
}
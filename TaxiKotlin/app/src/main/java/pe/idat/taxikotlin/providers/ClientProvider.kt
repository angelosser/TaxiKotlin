package pe.idat.taxikotlin.providers

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import pe.idat.taxikotlin.models.Client
import java.io.File

class ClientProvider {

    val db = Firebase.firestore.collection("Clients")
    var storage = FirebaseStorage.getInstance().reference.child("profile")

    fun create(client: Client): Task<Void>{
        return db.document(client.id!!).set(client)
    }

    fun getClientById(id: String): Task<DocumentSnapshot>{
        return db.document(id).get()
    }

    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromFile)

        return uploadTask.addOnFailureListener {
            Log.d("STORAGE", "ERROR: ${it.message}")
        }
    }

    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun update(client: Client): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["name"] = client?.name!!
        map["lastName"] = client?.lastName!!
        map["phone"] = client?.phone!!
        map["image"] = client?.image!!

        return db.document(client?.id!!).update(map)
    }
}
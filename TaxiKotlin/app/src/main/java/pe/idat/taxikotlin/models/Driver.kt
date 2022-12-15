package pe.idat.taxikotlin.models

// To parse the JSON, install Klaxon and do:
//
//   val client = Client.fromJson(jsonString)

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Driver (
    val id: String? = null,
    val name: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    var image: String? = null,
    val plateNumber: String? = null,
    val colorCar: String? = null,
    val brandCar: String? = null
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Client>(json)
    }
}


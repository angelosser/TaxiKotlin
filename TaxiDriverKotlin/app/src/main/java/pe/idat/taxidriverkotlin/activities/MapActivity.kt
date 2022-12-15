package pe.idat.taxidriverkotlin.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ListenerRegistration
import pe.idat.taxidriverkotlin.R
import pe.idat.taxidriverkotlin.databinding.ActivityMapBinding
import pe.idat.taxidriverkotlin.fragments.ModalBottomSheetBooking
import pe.idat.taxidriverkotlin.fragments.ModalBottomSheetMenu
import pe.idat.taxidriverkotlin.models.Booking
import pe.idat.taxidriverkotlin.providers.AuthProvider
import pe.idat.taxidriverkotlin.providers.BookingProvider
import pe.idat.taxidriverkotlin.providers.GeoProvider

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private var bookingListener: ListenerRegistration? = null
    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()

    private val modalBooking = ModalBottomSheetBooking()
    private val modalMenu = ModalBottomSheetMenu()

    val timer = object: CountDownTimer(20000, 1000){
        override fun onTick(counter: Long) {
            Log.d("TIMER", "Counter: $counter")
        }

        override fun onFinish() {
            Log.d("TIMER", "On finish")
            modalBooking.dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)
        locationPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        listenerBooking()

        binding.btnConnect.setOnClickListener{connectDriver()}
        binding.btnDisconnect.setOnClickListener{disconnectDriver()}
        binding.imageViewMenu.setOnClickListener { showModalMenu() }

    }

    val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permission ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ->{
                    Log.d("Localizacion", "Permiso concedido")
                    //easyWayLocation?.startLocation();
                    checkIfDriverIsConnected()
                }
                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) ->{
                    Log.d("Localizacion", "Permiso concedido con limitacion")
                    //easyWayLocation?.startLocation();
                    checkIfDriverIsConnected()
                }
                else ->{
                    Log.d("Localizacion", "Permiso no concedido")

                }
            }
        }
    }

    private fun showModalMenu(){
        modalMenu.show(supportFragmentManager, ModalBottomSheetMenu.TAG)
    }


    private fun showModalBooking(booking: Booking){

        val bundle = Bundle()
        bundle.putString("booking", booking.toJson())
        modalBooking.arguments = bundle
        modalBooking.isCancelable = false // no se puede salir del modal si le damos click afuera
        modalBooking.show(supportFragmentManager, ModalBottomSheetBooking.TAG)
        timer.start()
    }

    private fun listenerBooking(){
        bookingListener = bookingProvider.getBooking().addSnapshotListener{snapshot, e ->
            if (e!= null){
                Log.d("FIRESTORE", "ERROR: ${e.message}")
                return@addSnapshotListener
            }
            if (snapshot != null){
                if (snapshot.documents.size > 0){
                    val booking = snapshot.documents[0].toObject(Booking::class.java)
                    if (booking?.status == "create"){
                        showModalBooking(booking!!)
                    }
                }
            }
        }
    }


    fun checkIfDriverIsConnected(){
        geoProvider.getLoccation(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                if (document.contains("l")){
                    connectDriver()
                }else{
                    showButtonConnect()
                }
            }else{
                showButtonConnect()
            }
        }
    }

    private fun saveLocation(){
        if (myLocationLatLng != null){
            geoProvider.saveLocation(authProvider.getId(), myLocationLatLng!!)
        }
    }

    private fun disconnectDriver(){
        easyWayLocation?.endUpdates()
        if (myLocationLatLng != null){
            geoProvider.removeLocation(authProvider.getId())
            showButtonConnect()
        }
    }

    private fun connectDriver(){
        easyWayLocation?.endUpdates()
        easyWayLocation?.startLocation()
        showButtonDisconnect()
    }

    private fun showButtonConnect(){
        binding.btnDisconnect.visibility = View.GONE
        binding.btnConnect.visibility = View.VISIBLE
    }

    private fun showButtonDisconnect(){
        binding.btnDisconnect.visibility = View.VISIBLE
        binding.btnConnect.visibility = View.GONE
    }

    private fun addMarker(){
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.taxi)
        val markerIcon = getMarkerFromDrawable(drawable!!)
        if (markerDriver != null){
            markerDriver?.remove()
        }
        markerDriver = googleMap?.addMarker(
            MarkerOptions()
                .position(myLocationLatLng!!)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(markerIcon)
        )
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            200,
            200,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,200,200)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
        bookingListener?.remove()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //easyWayLocation?.startLocation();
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap?.isMyLocationEnabled = false

        try {
            val succes = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )
            if(!succes!!){
                Log.d("MAPAS", "No se pudo encontrar el estilo.")
            }

        }catch (e: Resources.NotFoundException){
            Log.d("MAPAS", "Error: ${e.toString()}")
        }
    }

    override fun locationOn() {
    }

    override fun currentLocation(location: Location) {
        myLocationLatLng = LatLng(location.latitude, location.longitude) // posicion actual
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
        ))
        addMarker()
        saveLocation()
    }

    override fun locationCancelled() {
    }
}
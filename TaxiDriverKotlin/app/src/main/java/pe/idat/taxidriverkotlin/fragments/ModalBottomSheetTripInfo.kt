package pe.idat.taxidriverkotlin.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.hdodenhof.circleimageview.CircleImageView
import pe.idat.taxidriverkotlin.R
import pe.idat.taxidriverkotlin.activities.HistoriesActivity
import pe.idat.taxidriverkotlin.activities.MainActivity
import pe.idat.taxidriverkotlin.activities.ProfileActivity
import pe.idat.taxidriverkotlin.models.Booking
import pe.idat.taxidriverkotlin.models.Client
import pe.idat.taxidriverkotlin.models.Driver
import pe.idat.taxidriverkotlin.providers.AuthProvider
import pe.idat.taxidriverkotlin.providers.ClientProvider
import pe.idat.taxidriverkotlin.providers.DriverProvider

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    private var client: Client? = null
    private lateinit var booking: Booking
    val authProvider = AuthProvider()
    val clientprovider = ClientProvider()

    var textViewClientName: TextView? = null
    var textViewOrigin: TextView? = null
    var textViewDestination: TextView? = null
    var imageViewPhone: ImageView? = null
    var circleImage: CircleImageView? = null

    val REQUEST_PHONE_CALL = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info, container, false)

        textViewClientName = view.findViewById(R.id.textViewClientName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        imageViewPhone = view.findViewById(R.id.imageViewPhone)
        circleImage = view.findViewById(R.id.circleImageClient)

//        getDriver()
        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!

        textViewOrigin?.text = booking.origin
        textViewDestination?.text = booking.destination
        imageViewPhone?.setOnClickListener {
            if (client?.phone != null){

                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
                }
                call(client?.phone!!)
            }
        }

        getClientInfo()
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PHONE_CALL){
            if (client?.phone != null){
                call(client?.phone!!)
            }
        }
    }

    private fun call(phone: String){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:$phone")

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }

        requireActivity().startActivity(i)
    }

    private fun getClientInfo(){
        clientprovider.getClientById(booking.idClient!!).addOnSuccessListener { document ->
            if (document.exists()){
                client = document.toObject(Client::class.java)
                textViewClientName?.text = "${client?.name} ${client?.lastName}"

                if (client?.image != null){
                    if (client?.image != ""){
                        Glide.with(requireActivity()).load(client?.image).into(circleImage!!)
                    }
                }
//                textViewUsername?.text = "${driver?.name} ${driver?.lastName}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }

}
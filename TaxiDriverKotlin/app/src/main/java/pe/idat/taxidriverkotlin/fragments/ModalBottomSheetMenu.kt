package pe.idat.taxidriverkotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pe.idat.taxidriverkotlin.R
import pe.idat.taxidriverkotlin.activities.MainActivity
import pe.idat.taxidriverkotlin.activities.ProfileActivity
import pe.idat.taxidriverkotlin.models.Driver
import pe.idat.taxidriverkotlin.providers.AuthProvider
import pe.idat.taxidriverkotlin.providers.DriverProvider

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()

    var textViewUsername: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu, container, false)

        textViewUsername = view.findViewById(R.id.textViewUserName)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)


        getDriver()
        linearLayoutLogout?.setOnClickListener { goToMain() }
        linearLayoutProfile?.setOnClickListener { goToProfile() }
        return view
    }

    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                textViewUsername?.text = "${driver?.name} ${driver?.lastName}"
            }
        }
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

    companion object{
        const val TAG = "ModalBottomSheetMenu"
    }

}
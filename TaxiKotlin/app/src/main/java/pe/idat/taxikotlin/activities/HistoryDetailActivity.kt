package pe.idat.taxikotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.toObject
import pe.idat.taxikotlin.databinding.ActivityHistoryDetailBinding
import pe.idat.taxikotlin.models.Client
import pe.idat.taxikotlin.models.History
import pe.idat.taxikotlin.providers.ClientProvider
import pe.idat.taxikotlin.providers.DriverProvider
import pe.idat.taxikotlin.providers.HistoryProvider
import pe.idat.taxikotlin.utils.RelativeTime

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var driverProvider = DriverProvider()
    private var extraId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        extraId = intent.getStringExtra("id")!!
        gethistory()

        binding.imageViewBack.setOnClickListener { finish() }
    }

    private fun gethistory(){
        historyProvider.gethistoryById(extraId).addOnSuccessListener { document ->
            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoryDetailActivity)
                binding.textViewPrice.text = "S/.${String.format("%.2f", history?.price)}"
                binding.textViewMyCalification.text = "${history?.calificationToDriver}"
                binding.textViewClientCalification.text = "${history?.calificationToClient}"
                binding.textViewTimeAndDistance.text = "${history?.time} min - ${String.format("%.1f", history?.km)} km"

                getDriverInfo(history?.idDriver!!)
            }
        }
    }

    private fun getDriverInfo(id: String){
        driverProvider.getDriver(id).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Client::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textViewName.text = "${driver?.name} ${driver?.lastName}"
                if (driver?.image != null){
                    if (driver?.image != ""){
                        Glide.with(this).load(driver?.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

}
package pe.idat.taxidriverkotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.toObject
import pe.idat.taxidriverkotlin.databinding.ActivityHistoryDetailBinding
import pe.idat.taxidriverkotlin.models.Client
import pe.idat.taxidriverkotlin.models.History
import pe.idat.taxidriverkotlin.providers.ClientProvider
import pe.idat.taxidriverkotlin.providers.HistoryProvider
import pe.idat.taxidriverkotlin.utils.RelativeTime

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var clientProvider = ClientProvider()
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

                getClientInfo(history?.idClient!!)
            }
        }
    }

    private fun getClientInfo(id: String){
        clientProvider.getClientById(id).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                binding.textViewEmail.text = client?.email
                binding.textViewName.text = "${client?.name} ${client?.lastName}"
                if (client?.image != null){
                    if (client?.image != ""){
                        Glide.with(this).load(client?.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

}
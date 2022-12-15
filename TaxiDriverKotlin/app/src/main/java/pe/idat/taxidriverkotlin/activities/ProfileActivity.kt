package pe.idat.taxidriverkotlin.activities

import android.app.Activity
import android.app.Instrumentation
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.github.dhaval2404.imagepicker.ImagePicker
import pe.idat.taxidriverkotlin.databinding.ActivityProfileBinding
import pe.idat.taxidriverkotlin.models.Driver
import pe.idat.taxidriverkotlin.providers.AuthProvider
import pe.idat.taxidriverkotlin.providers.DriverProvider
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        getDriver()
        binding.imageViewBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener{selectImage()}
    }

    private fun updateInfo(){
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val carBrand = binding.textFieldCarBrand.text.toString()
        val carColor = binding.textFieldCarColor.text.toString()
        val carPlate = binding.textFieldCarPlate.text.toString()

        val driver = Driver(
            id =  authProvider.getId(),
            name = name,
            lastName = lastName,
            phone = phone,
            colorCar = carColor,
            brandCar = carBrand,
            plateNumber = carPlate
            )

        if (imageFile != null){
            driverProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot ->
                val url = taskSnapshot.storage.downloadUrl.toString()
                Log.d("STORAGE", "URL: ${url}")
            }
        }else{
            driverProvider.update(driver).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos Actualizados Correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la informacion", Toast.LENGTH_SHORT).show()
                }
            }
        }



    }

    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textFieldName.setText(driver?.name)
                binding.textFieldLastName.setText(driver?.lastName)
                binding.textFieldPhone.setText(driver?.phone)
                binding.textFieldCarBrand.setText(driver?.brandCar)
                binding.textFieldCarColor.setText(driver?.colorCar)
                binding.textFieldCarPlate.setText(driver?.plateNumber)
            }
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)
        }else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()

        }else{
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }
}
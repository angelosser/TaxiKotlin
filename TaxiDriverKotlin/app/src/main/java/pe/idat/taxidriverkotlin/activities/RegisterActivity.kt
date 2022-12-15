package pe.idat.taxidriverkotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import pe.idat.taxidriverkotlin.databinding.ActivityRegisterBinding
import pe.idat.taxidriverkotlin.models.Driver
import pe.idat.taxidriverkotlin.providers.AuthProvider
import pe.idat.taxidriverkotlin.providers.DriverProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnGoToLogin.setOnClickListener{goToLogin()}
        binding.btnRegistrar.setOnClickListener{register()}

    }

    private fun goToMap() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun register(){
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.text.toString()

        if (isValidForm(name, lastName, phone, email, password, confirmPassword)){
            authProvider.register(email, password).addOnCompleteListener{
                if (it.isSuccessful){
                    val driver = Driver(
                        id = authProvider.getId(),
                        name = name,
                        lastName = lastName,
                        phone = phone,
                        email = email
                    )
                    driverProvider.create(driver).addOnCompleteListener{
                        if (it.isSuccessful){
                            Toast.makeText(this@RegisterActivity, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                            goToMap()
                        }else{
                            Toast.makeText(this, "Error almacenando los datos del usuario ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                            Log.d("fb", "error: ${it.exception.toString()}")
                        }
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Registro Fallido ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                    Log.d("fb", "error: ${it.exception.toString()}")
                }
            }
        }

    }

    private fun isValidForm(name: String, lastName: String, phone: String, email: String, password: String, confirmPassword: String): Boolean{

        if (name.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu nombre.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (lastName.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu apelllido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu telefono.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu email.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()){
            Toast.makeText(this, "Debes ingresar tu contraseña.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (confirmPassword.isEmpty()){
            Toast.makeText(this, "Debes confirmar tu contraseña.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword){
            Toast.makeText(this, "Las contraseñas deben coincidir.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6){
            Toast.makeText(this, "La contraseña debe tener al menos 6 digitos.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun goToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}
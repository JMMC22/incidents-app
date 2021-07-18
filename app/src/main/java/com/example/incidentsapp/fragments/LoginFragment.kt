package com.example.incidentsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.MapsActivity
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.utils.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.shobhitpuri.custombuttons.GoogleSignInButton
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener{


    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Configuración google sign
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(),gso)
        auth = Firebase.auth


        return inflater.inflate(R.layout.fragment_login,container,false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val google_button : GoogleSignInButton = view.findViewById(R.id.google_button)

        entrar_button.setOnClickListener(this)
        google_button.setOnClickListener(this)
        register_button.setOnClickListener(this)

        email_etext.addTextChangedListener(loginTextWatcher)
        pass_etext.addTextChangedListener(loginTextWatcher)
    }

    // una vez se rellenen los campos, se habilita el boton de entrar
    private val loginTextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var email = email_etext.text.toString()
            var password = pass_etext.text.toString()
            entrar_button.isEnabled = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !password.isEmpty())
        }

    }


    /*
    Definición de la función para llamar a la actividad de login con google
     */
    private fun signIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /*
    Comprobamos si el usuario se ha logueado correctamente o si ha fallado.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Snackbar.make(requireView(), "Google Failed.", Snackbar.LENGTH_SHORT).show()
                // ...
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

    }


    /*
    Función para comprobar autenticación con firebase y si las credenciales son correctas o no.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Si el login es correcto, redirigir al mapa
                    Log.d(TAG, "signInWithCredential:success")
                    var user = auth.currentUser

                    requireActivity().finish()
                    startActivity(Intent(activity,MapsActivity::class.java))
                } else {
                    // Si falla, mostrar mensaje a usuario
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //val view = binding.mainLayout
                    Snackbar.make(requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }

            }
    }


    companion object{
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginFragment"
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.google_button -> signIn()
            R.id.entrar_button -> signInWithEmail(email_etext.text.toString(), pass_etext.text.toString())
            R.id.register_button -> requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_main_activity,RegisterFragment()).addToBackStack(null).commit()
        }
    }

    fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful){
                    requireActivity().finish()
                    startActivity(Intent(activity,MapsActivity::class.java))
                } else {
                    Snackbar.make(requireView(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                }

            }
    }


}
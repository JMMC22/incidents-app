package com.example.incidentsapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.MapsActivity
import com.example.incidentsapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.shobhitpuri.custombuttons.GoogleSignInButton
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.email_etext
import kotlinx.android.synthetic.main.fragment_register.pass_etext

class RegisterFragment : Fragment(R.layout.fragment_register), View.OnClickListener{

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        return inflater.inflate(R.layout.fragment_register,container,false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entrar_reg_button.setOnClickListener(this)
        email_etext.addTextChangedListener(loginTextWatcher)
        pass_etext.addTextChangedListener(loginTextWatcher)
    }


    fun createUserWithEmail(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful){
                    Snackbar.make(requireView(), "Authentication success.", Snackbar.LENGTH_SHORT).show()
                    // FirebaseData().createUser(auth.currentUser!!.uid)
                    auth.signOut()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Snackbar.make(requireView(), "Authentication failed.", Snackbar.LENGTH_SHORT).show()
                }
            }
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

            entrar_reg_button.isEnabled = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !password.isEmpty() && password.length > 5)

        }

    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object{
        private const val TAG = "RegisterFragment"
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.entrar_reg_button -> {
                createUserWithEmail(email_etext.text.toString(), pass_etext.text.toString())
                requireContext().hideKeyboard(this.requireView())
            }

        }
    }


}
package ru.netology.nmedia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.NewPostFragment.Companion.isNewPost
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth

    /*
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging
     */

    private val viewModel: AuthViewModel by viewModels()

    private val postViewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }
                    .show()
                return@let
            }
            findNavController(R.id.navigation).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArg = text
                    isNewPost = true
                }
            )
        }
        /*
        checkGoogleApiAvailability()

        requestNotificationsPermission()
         */

        //val viewModel by viewModels<AuthViewModel>()

        var oldMenuProvider: MenuProvider? = null
        viewModel.data.observe(this) {
            oldMenuProvider?.let(::removeMenuProvider)

            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    val authorized = viewModel.isAuthorized
                    if (authorized) {
                        menu.setGroupVisible(R.id.authorized, true)
                        menu.setGroupVisible(R.id.unauthorized, false)
                    } else {
                        menu.setGroupVisible(R.id.authorized, false)
                        menu.setGroupVisible(R.id.unauthorized, true)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.auth -> {
                            //AppAuth.getInstance().setToken(Token(5L, "x-token"))
                            findNavController(R.id.navigation).navigate(R.id.authFragment)
                            true
                        }
                        R.id.register -> {
                            findNavController(R.id.navigation).navigate(R.id.registrationFragment)
                            true
                        }
                        R.id.logout -> {
                            appAuth.clearAuth()
                            postViewModel.refresh()
                            true
                        }
                        else -> {
                            false
                        }
                    }
            }.apply {
                oldMenuProvider = this
            }, this)
        }
    }

    /*
    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }
     */
}


package ru.netology.nmedia.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.ui.NewPostFragment.Companion.isNewPost
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.ui.PostAttachmentFragment.Companion.typeArg
import ru.netology.nmedia.ui.PostFragment.Companion.idArg
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.WallViewModel
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
    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
    private val wallViewModel: WallViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_24);
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                        android.R.id.home -> {
                            binding.drawer.openDrawer(GravityCompat.START)
                            true
                        }

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

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.posts -> {
                    //Toast.makeText(this@AppActivity, "Posts", Toast.LENGTH_SHORT).show()
                    findNavController(R.id.navigation).navigate(
                        R.id.feedFragment
                        /*,
                        Bundle().apply {
                        idArg = 0
                        typeArg = "POSTS"
                    }
                    */
                    )
                }

                R.id.users -> {
                    //Toast.makeText(this@AppActivity, "Users", Toast.LENGTH_SHORT).show()
                    findNavController(R.id.navigation).navigate(
                        R.id.usersFragment,
                        Bundle().apply {
                            idArg = -1
                            typeArg = "all"
                        })
                }

                R.id.events -> {
                    //Toast.makeText(this@AppActivity, "Events", Toast.LENGTH_SHORT).show()
                    findNavController(R.id.navigation).navigate(R.id.eventsFeedFragment)
                }

                R.id.profile -> {
                    if (viewModel.isAuthorized) {
                        //userViewModel.getUserById(viewModel.data.value!!.id)
                        profileViewModel.setPostSource(PostsSource(viewModel.data.value!!.id, SourceType.MYWALL))
                        findNavController(R.id.navigation).navigate(
                            R.id.profileFragment
                            /*,
                        Bundle().apply {
                            idArg = viewModel.data.value?.id
                            type = "MYWALL"
                            userViewModel.getUserById(idArg!!)
                        }
                        */
                        )


                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.authorization_required),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(R.string.login) {
                                findNavController(R.id.navigation).navigate(
                                    R.id.authFragment
                                )
                            }
                            .show()
                    }
                    //Toast.makeText(this@AppActivity, "Profile", Toast.LENGTH_SHORT).show()
                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }

    }

    /*
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.posts_menu, menu)
    }
    */

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

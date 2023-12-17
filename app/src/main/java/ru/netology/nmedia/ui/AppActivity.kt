package ru.netology.nmedia.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.EventViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_24)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                            findNavController(R.id.navigation).navigate(R.id.authFragment)
                            true
                        }

                        R.id.register -> {
                            findNavController(R.id.navigation).navigate(R.id.registrationFragment)
                            true
                        }

                        R.id.logout -> {
                            appAuth.clearAuth()
                            postViewModel.refreshList()
                            eventViewModel.refreshList()
                            findNavController(R.id.navigation).navigate(R.id.feedFragment)
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
                    findNavController(R.id.navigation).navigate(R.id.feedFragment)
                }

                R.id.users -> {
                    userViewModel.setForSelection(getString(R.string.users), false, "Users")
                    findNavController(R.id.navigation).navigate(R.id.usersFragment)
                }

                R.id.events -> {
                    findNavController(R.id.navigation).navigate(R.id.eventsFeedFragment)
                }

                R.id.profile -> {
                    if (viewModel.isAuthorized) {
                        profileViewModel.setPostSource(
                            PostsSource(
                                viewModel.data.value!!.id,
                                SourceType.MYWALL
                            )
                        )
                        findNavController(R.id.navigation).navigate(
                            R.id.profileFragment
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
                }

                R.id.about -> {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder
                        .setPositiveButton(R.string.ok) { _, _ ->
                        }
                        .setView(layoutInflater.inflate(R.layout.about_dialog, null))
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        }
    }

}


package ru.maltsev.dogs.view

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import ru.maltsev.dogs.R
import ru.maltsev.dogs.databinding.FragmentDetailBinding
import ru.maltsev.dogs.databinding.SendSmsDialogBinding
import ru.maltsev.dogs.model.DogBreed
import ru.maltsev.dogs.model.DogPalette
import ru.maltsev.dogs.model.SmsInfo
import ru.maltsev.dogs.viewmodel.DetailViewModel


class DetailFragment : Fragment() {

    private lateinit var viewModel: DetailViewModel

    private lateinit var dataBinding: FragmentDetailBinding

    private var dogUuid = 0

    private var sendSmsStarted = false

    private var currentDog: DogBreed? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createMenu()
        arguments?.let {
            dogUuid = DetailFragmentArgs.fromBundle(it).dogUuid
        }
        viewModel = ViewModelProviders.of(this)[DetailViewModel::class.java]
        viewModel.fetchById(dogUuid)



        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.dog.observe(viewLifecycleOwner) { dog ->
            currentDog = dog
            dataBinding.dog = dog

            dog.imageUrl?.let {
                setupBackgroundColor(it)
            }
        }
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this dog breed")
                        intent.putExtra(Intent.EXTRA_TEXT, "${currentDog?.dogBreed} breed for ${currentDog?.bredFor}")
                        intent.putExtra(Intent.EXTRA_STREAM, currentDog?.imageUrl)
                        startActivity(Intent.createChooser(intent, "Share with"))

                        true
                    }
                    R.id.action_send_sms -> {
                        sendSmsStarted = true
                        (activity as MainActivity).checkSmsPermission()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun onPermissionResult(permissionGranted: Boolean) {
        if (sendSmsStarted && permissionGranted) {
            context?.let {
                val smsInfo = SmsInfo("", "${currentDog?.dogBreed}", "${currentDog?.imageUrl}")
                val dialogBinding = DataBindingUtil.inflate<SendSmsDialogBinding>(LayoutInflater.from(it), R.layout.send_sms_dialog, null, false)
                AlertDialog.Builder(it)
                    .setView(dialogBinding.root)
                    .setPositiveButton("Send sms") { _, _ ->
                        if (!dialogBinding.smsDestination.text.isNullOrEmpty()) {
                            smsInfo.to = dialogBinding.smsDestination.text.toString()
                            sendSms(smsInfo)
                        }
                    }
                    .setNegativeButton("Cancel") {
                        _, _ ->
                    }.show()
                dialogBinding.smsInfo = smsInfo
            }
        }
    }

    private fun sendSms(smsInfo: SmsInfo) {
        val intent = Intent(context, MainActivity::class.java)
        val p1 = PendingIntent.getActivity(context, 0 ,intent, PendingIntent.FLAG_IMMUTABLE)
        val sms = context?.getSystemService(SmsManager::class.java)
        sms?.sendTextMessage(smsInfo.to, null, smsInfo.text, p1, null)
    }

    private fun setupBackgroundColor(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource)
                        .generate {
                            val dogPalette = DogPalette(it?.vibrantSwatch?.rgb ?: 0)
                            dataBinding.palette = dogPalette
                        }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }
}
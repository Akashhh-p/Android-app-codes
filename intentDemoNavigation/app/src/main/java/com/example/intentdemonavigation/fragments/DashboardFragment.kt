package com.example.intentdemonavigation.fragments

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.intentdemonavigation.AuthManager
import com.example.intentdemonavigation.R
import com.example.intentdemonavigation.databinding.FragmentDashboardBinding
import com.example.intentdemonavigation.service.DemoService
import com.example.intentdemonavigation.service.MediaPlayerService
import com.example.intentdemonavigation.worker.DownloadWorker
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.dashboardScrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top
            )
            insets
        }

        binding.btnBattery.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_batteryFragment)
        }

        binding.btnContacts.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            startActivity(intent)
        }

        binding.btnDialer.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9876543210"))
            startActivity(intent)
        }

        binding.btnCamera.setOnClickListener {
            try {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "Camera application not available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnBrowser.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            startActivity(intent)
        }

        binding.btnAkash.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9912169094"))
            startActivity(intent)
        }

        binding.btnStartWorker.setOnClickListener {
            startDownloadWorker()
        }

        binding.btnStartMusic.setOnClickListener {
            startMusic()
        }

        binding.btnStopMusic.setOnClickListener {
            stopMusic()
        }

        binding.btnStartService.setOnClickListener {
            val intent = Intent(requireContext(), DemoService::class.java)
            requireContext().startService(intent)
        }

        binding.btnStopService.setOnClickListener {
            val intent = Intent(requireContext(), DemoService::class.java)
            requireContext().stopService(intent)
        }
    }

    private fun startDownloadWorker() {
        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>().build()
        val workManager = WorkManager.getInstance(requireContext())

        workManager.enqueueUniqueWork(
            "background_image_download",
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        workManager.getWorkInfoByIdLiveData(workRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> {
                            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val uri = workInfo.outputData.getString("uri")
                            Toast.makeText(
                                context,
                                "Download successful!\nSaved to Downloads:\nbackground_download.webp",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        WorkInfo.State.FAILED -> {
                            val error = workInfo.outputData.getString("error") ?: "Unknown error"
                            Toast.makeText(
                                context,
                                "Download unsuccessful:\n$error",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {}
                    }
                }
            }
    }

    private fun startMusic() {
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), MediaPlayerService::class.java))
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            val controller = controllerFuture?.get()
            controller?.play()
        }, MoreExecutors.directExecutor())
    }

    private fun stopMusic() {
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), MediaPlayerService::class.java))
        val future = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        future.addListener({
            val controller = future.get()
            controller.pause()
            controller.release()
            val intent = Intent(requireContext(), MediaPlayerService::class.java)
            requireContext().stopService(intent)
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
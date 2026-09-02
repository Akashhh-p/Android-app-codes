package com.example.phonebook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<MaterialCardView>(R.id.cardAppContacts).setOnClickListener {
            navigateTo(AppContactsFragment())
        }

        view.findViewById<MaterialCardView>(R.id.cardDeviceContacts).setOnClickListener {
            navigateTo(DeviceContactsFragment())
        }

        view.findViewById<MaterialCardView>(R.id.cardSms).setOnClickListener {
            navigateTo(SmsFragment())
        }

        return view
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Main Menu"
    }
}

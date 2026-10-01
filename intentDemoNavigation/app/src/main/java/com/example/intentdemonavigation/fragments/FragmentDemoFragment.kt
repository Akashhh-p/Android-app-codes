package com.example.intentdemonavigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.intentdemonavigation.R
import com.example.intentdemonavigation.databinding.FragmentFragmentDemoBinding

class FragmentDemoFragment : Fragment() {
    private var _binding: FragmentFragmentDemoBinding? = null
    private val binding get() = _binding!!

    private var fragmentCount = 0
    private var fragmentTags = mutableListOf<String>()
    private var currentTag: String? = null
    private var isInternalSelection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            fragmentCount = savedInstanceState.getInt("fragmentCount")
            fragmentTags = savedInstanceState.getStringArrayList("fragmentTags")?.toMutableList() ?: mutableListOf()
            currentTag = savedInstanceState.getString("currentTag")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFragmentDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        updateUIState()

        binding.btnAddFragment.setOnClickListener {
            addFragment()
        }

        binding.btnBackToDashboard.setOnClickListener {
            findNavController().popBackStack()
        }

        childFragmentManager.addOnBackStackChangedListener {
            updateUIState()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 1) {
                    childFragmentManager.popBackStack()
                } else if (childFragmentManager.backStackEntryCount == 1) {
                    // If popping the last one, we might want to clear the container or just let it stay.
                    // The prompt implies multiple fragments. 
                    childFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fragmentTags)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFragments.adapter = adapter

        binding.spinnerFragments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInternalSelection) {
                    isInternalSelection = false
                    return
                }
                val tag = fragmentTags[position]
                if (tag != currentTag) {
                    showFragment(tag)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun addFragment() {
        fragmentCount++
        val tag = fragmentCount.toString()
        fragmentTags.add(tag)
        
        (binding.spinnerFragments.adapter as ArrayAdapter<String>).notifyDataSetChanged()
        
        val fragment = DemoChildFragment.newInstance(fragmentCount)
        childFragmentManager.beginTransaction()
            .replace(R.id.demo_child_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
        
        currentTag = tag
        updateSpinnerSelection(tag)
        binding.tvStatus.text = "Status: Fragment $tag added"
    }

    private fun showFragment(tag: String) {
        val fragment = childFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.demo_child_container, fragment, tag)
                .addToBackStack(tag) // Add to backstack as per "preserve backstack demonstration behavior"
                .commit()
            currentTag = tag
            binding.tvStatus.text = "Status: Showing Fragment $tag"
        }
    }

    private fun updateUIState() {
        val count = childFragmentManager.backStackEntryCount
        binding.tvBackstackCount.text = getString(R.string.backstack_count, count)
        
        if (count > 0) {
            val topEntry = childFragmentManager.getBackStackEntryAt(count - 1)
            val tag = topEntry.name
            if (tag != null && tag != currentTag) {
                currentTag = tag
                updateSpinnerSelection(tag)
                binding.tvStatus.text = "Status: Showing Fragment $tag"
            }
        } else {
            currentTag = null
            binding.tvStatus.text = getString(R.string.status_no_fragments)
        }
    }

    private fun updateSpinnerSelection(tag: String) {
        val index = fragmentTags.indexOf(tag)
        if (index != -1) {
            isInternalSelection = true
            binding.spinnerFragments.setSelection(index)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("fragmentCount", fragmentCount)
        outState.putStringArrayList("fragmentTags", ArrayList(fragmentTags))
        outState.putString("currentTag", currentTag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
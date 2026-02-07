package com.example.emergencyresponder.modules.dashboard.ui

import AddEmergencyContactUseCase
import ObserveEmergencyContactsUseCase
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.R
import com.example.emergencyresponder.databinding.FragmentEmergencyContactBinding
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.data.datasource.EmergencyContactRemoteDataSource
import com.example.emergencyresponder.modules.dashboard.data.repositoryImpl.EmergencyContactRepositoryImpl
import com.example.emergencyresponder.modules.dashboard.domain.adapters.EmergencyContactsAdapter
import com.example.emergencyresponder.modules.dashboard.domain.viewModelFactory.EmergencyContactViewModelFactory
import com.example.emergencyresponder.modules.dashboard.domain.viewmodel.EmergencyContactViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EmergencyContactFragment : Fragment() {

    private var _binding: FragmentEmergencyContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmergencyContactsAdapter
    private val contactsList = mutableListOf<EmergencyContact>()

    private lateinit var viewModel: EmergencyContactViewModel
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()

        val firestore = FirebaseFirestore.getInstance()
        val remoteDataSource = EmergencyContactRemoteDataSource(firestore)
        val repository = EmergencyContactRepositoryImpl(remoteDataSource)
        val observeUseCase = ObserveEmergencyContactsUseCase(repository)
        val addUseCase = AddEmergencyContactUseCase(repository)

        val factory = EmergencyContactViewModelFactory(observeUseCase, addUseCase)
        viewModel = ViewModelProvider(this, factory).get(EmergencyContactViewModel::class.java)

        val uid = auth.currentUser?.uid ?: return

        viewModel.observeContacts(uid)
        viewModel.contacts.observe(viewLifecycleOwner) { list ->
            contactsList.clear()
            contactsList.addAll(list)
            adapter.notifyDataSetChanged()
        }

        binding.addMoreContacts.setOnClickListener {
            showAddDialog(uid)
        }
    }

    private fun showAddDialog(uid: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val nameEt = dialogView.findViewById<EditText>(R.id.contactName)
        val phoneEt = dialogView.findViewById<EditText>(R.id.emergency_Contact_Phone)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey))

        positiveButton.setOnClickListener {
            val nameText = nameEt.text.toString().trim()
            val phoneText = phoneEt.text.toString().trim()
            var isValid = true

            if (nameText.isEmpty()) {
                nameEt.error = "Name is required"
                isValid = false
            }

            if (phoneText.isEmpty()) {
                phoneEt.error = "Phone is required"
                isValid = false
            } else if (!com.example.emergencyresponder.core.utils.ValidationUtils.isPhoneValid(phoneText, "PK")) {
                phoneEt.error = "Invalid phone format"
                isValid = false
            }

            if (isValid) {
                val contact = EmergencyContact(
                    name = nameText,
                    phone = phoneText
                )
                viewModel.addContact(uid, contact)

                dialog.dismiss()
            }
        }
    }
    private fun setupRecycler() {
        adapter = EmergencyContactsAdapter(contactsList)
        binding.emergencyContactsList.layoutManager =
            LinearLayoutManager(requireContext())
        binding.emergencyContactsList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.emergencyresponder.modules.dashboard.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emergencyresponder.R
import com.example.emergencyresponder.core.utils.SOSUtils
import com.example.emergencyresponder.core.utils.ValidationUtils
import com.example.emergencyresponder.databinding.FragmentEmergencyContactBinding
import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.dashboard.ui.adapters.EmergencyContactsAdapter
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.EmergencyContactViewModel
import com.example.emergencyresponder.modules.dashboard.ui.viewmodel.EmergencyError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmergencyContactFragment : Fragment() {

    private var _binding: FragmentEmergencyContactBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmergencyContactsAdapter

    private val viewModel: EmergencyContactViewModel by viewModels()

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
        setupObservers()

        binding.addMoreContacts.setOnClickListener { showAddDialog() }

    }

    private fun setupObservers() {
        viewModel.contacts.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorType ->
                val messageRes = when (errorType) {
                    EmergencyError.FAILED_TO_ADD -> R.string.err_failed_add
                    EmergencyError.FAILED_TO_DELETE -> R.string.err_failed_delete
                    EmergencyError.NETWORK_ERROR -> R.string.err_network
                    else -> R.string.err_generic
                }
                Toast.makeText(context, getString(messageRes), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val nameEt = dialogView.findViewById<EditText>(R.id.contactName)
        val phoneEt = dialogView.findViewById<EditText>(R.id.emergency_Contact_Phone)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_more_contacts)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_add, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        positiveButton.setOnClickListener {
            val nameText = nameEt.text.toString().trim()
            val phoneText = phoneEt.text.toString().trim()

            if (nameText.isEmpty()) {
                nameEt.error = getString(R.string.err_name_required)
                return@setOnClickListener
            }

            if (phoneText.isEmpty()) {
                phoneEt.error = getString(R.string.err_phone_required)
                return@setOnClickListener
            }

            if (!ValidationUtils.isPhoneValid(phoneText, "PK")) {
                phoneEt.error = getString(R.string.err_invalid_phone)
                return@setOnClickListener
            }

            viewModel.addContact(EmergencyContact(nameText, phoneText))
            dialog.dismiss()
        }
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey))

    }

    private fun setupRecycler() {
        adapter = EmergencyContactsAdapter(
            onSosClick = { contact ->
                SOSUtils.sendSOSToSpecificPerson(requireContext(), contact.phone)
            },
            onItemLongClick = { contact, position ->
                showDeleteDialog(contact)
            }
        )


        binding.emergencyContactsList.layoutManager = LinearLayoutManager(requireContext())
        binding.emergencyContactsList.adapter = adapter
    }

    private fun showDeleteDialog(contact: EmergencyContact) {
        val message = getString(R.string.dialog_delete_msg, contact.name)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(message)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                viewModel.deleteContact(contact)

                Toast.makeText(requireContext(), R.string.msg_contact_deleted, Toast.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.show()

        val deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
        cancelButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

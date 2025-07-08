package com.example.twofactorauthapp

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.twofactorauthapp.data.AccountEntity
import com.example.twofactorauthapp.data.AppDatabase
import com.example.twofactorauthapp.data.LogEntity
import com.example.twofactorauthapp.databinding.FragmentAddAccountBinding
import com.example.twofactorauthapp.util.EncryptionHelper
import com.example.twofactorauthapp.util.QRCodeGenerator
import com.example.twofactorauthapp.util.QRCodeParser
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { scanned ->
            if (scanned.startsWith("otpauth://")) {
                val parsed = QRCodeParser.parseOtpAuthUri(scanned)
                parsed?.let {
                    binding.etSecretKey.setText(it.secret)
                    binding.etAccountName.setText(it.label)
                } ?: run {
                    Toast.makeText(requireContext(), "Invalid QR code", Toast.LENGTH_SHORT).show()
                }
            } else {
                // ჩვეულებრივი ტექსტური QR
                binding.etSecretKey.setText(scanned)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Generate QR
        binding.btnGenerateQR.setOnClickListener {
            val secret = binding.etSecretKey.text.toString()
            if (secret.isNotEmpty()) {
                val qrBitmap = QRCodeGenerator.generate(secret)
                binding.ivQRCode.setImageBitmap(qrBitmap)
            } else {
                Toast.makeText(requireContext(), "Enter a secret key", Toast.LENGTH_SHORT).show()
            }
        }

        // Scan QR
        binding.btnScanQR.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan a QR code")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            qrScanLauncher.launch(options)
        }

        // Save Account
        binding.btnSaveAccount.setOnClickListener {
            val secret = binding.etSecretKey.text.toString()
            val name = binding.etAccountName.text.toString().ifEmpty {
                "Account_${System.currentTimeMillis()}"
            }

            if (secret.isNotEmpty()) {
                val encryptedKey = EncryptionHelper.encrypt(secret)

                val account = AccountEntity(
                    id = 0,
                    name = name,
                    secretKey = encryptedKey
                )

                val log = LogEntity(
                    id = 0,
                    accountName = name,
                    timestamp = System.currentTimeMillis(),
                    status = "Account Created"
                )

                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(requireContext())
                    db.accountDao().insertAccount(account)
                    db.logDao().insertLog(log)

                    Toast.makeText(requireContext(), "Account saved", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a secret key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.accounts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.settingsFragment)
                true
            }
            R.id.action_logs -> {
                findNavController().navigate(R.id.logFragment)
                true
            }
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

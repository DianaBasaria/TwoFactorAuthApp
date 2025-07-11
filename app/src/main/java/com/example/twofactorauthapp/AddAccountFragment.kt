package com.example.twofactorauthapp

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.twofactorauthapp.data.AccountEntity
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
                binding.etSecretKey.setText(scanned)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGenerateQR.setOnClickListener {
            val secret = binding.etSecretKey.text.toString()
            if (secret.isNotEmpty()) {
                val qrBitmap = QRCodeGenerator.generate(secret)
                binding.ivQRCode.setImageBitmap(qrBitmap)
            } else {
                Toast.makeText(requireContext(), "Enter a secret key", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnScanQR.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan a QR code")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            qrScanLauncher.launch(options)
        }

        binding.btnSaveAccount.setOnClickListener {
            val secret = binding.etSecretKey.text.toString()
            val name = binding.etAccountName.text.toString().ifEmpty {
                "Account_${System.currentTimeMillis()}"
            }

            if (secret.isNotEmpty()) {
                val encryptedKey = EncryptionHelper.encrypt(secret)

                val account = AccountEntity(0, name, encryptedKey)
                val log = LogEntity(0, name, System.currentTimeMillis(), "Account Created")

                lifecycleScope.launch {
                    TwoFactorAuthApp.database.accountDao().insertAccount(account)
                    TwoFactorAuthApp.database.logDao().insertLog(log)

                    Toast.makeText(requireContext(), "Account saved", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a secret key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

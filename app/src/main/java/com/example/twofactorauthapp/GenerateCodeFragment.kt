package com.example.twofactorauthapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.twofactorauthapp.databinding.FragmentGenerateCodeBinding
import com.example.twofactorauthapp.util.EncryptionHelper
import com.example.twofactorauthapp.util.TotpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateCodeFragment : Fragment() {

    private var _binding: FragmentGenerateCodeBinding? = null
    private val binding get() = _binding!!

    private val args: GenerateCodeFragmentArgs by navArgs()

    private var generateJob: Job? = null
    private var countdownJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerateCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            val account = TwoFactorAuthApp.database.accountDao().getAccountById(args.accountId)

            if (account == null) {
                withContext(Dispatchers.Main) {
                    findNavController().navigateUp()
                }
                return@launch
            }

            val decryptedKey = try {
                EncryptionHelper.decrypt(account.secretKey)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textViewCode.text = "--"
                    binding.textViewTimer.text = "Decryption failed"
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                binding.textViewAccountName.text = account.name
            }

            startGeneratingCodes(decryptedKey)
        }
    }

    private fun startGeneratingCodes(secretKey: String) {
        generateJob?.cancel()
        countdownJob?.cancel()

        generateJob = lifecycleScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val currentTimeSeconds = now / 1000

                val code = TotpUtils.generateTOTP(secretKey, currentTimeSeconds)

                withContext(Dispatchers.Main) {
                    binding.textViewCode.text = code
                }

                val nextInterval = ((now / 30000) + 1) * 30000
                val delayMillis = nextInterval - now

                delay(delayMillis)
            }
        }

        countdownJob = lifecycleScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val secondsRemaining = 30 - ((now / 1000) % 30).toInt()

                withContext(Dispatchers.Main) {
                    binding.textViewTimer.text = "Expires in: $secondsRemaining s"
                }

                delay(1000L)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        generateJob?.cancel()
        countdownJob?.cancel()
        _binding = null
    }
}

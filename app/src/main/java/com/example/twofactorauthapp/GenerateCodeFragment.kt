package com.example.twofactorauthapp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.twofactorauthapp.databinding.FragmentGenerateCodeBinding
import com.example.twofactorauthapp.util.EncryptionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateCodeFragment : Fragment() {

    private var _binding: FragmentGenerateCodeBinding? = null
    private val binding get() = _binding!!

    private val args: GenerateCodeFragmentArgs by navArgs()
    private var timer: CountDownTimer? = null

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


        // ანგარიშის მოძებნა Room DB-დან
        lifecycleScope.launch {
            val account = withContext(Dispatchers.IO) {
                TwoFactorAuthApp.database.accountDao().getAccountByIdSync(args.accountId)
            }

            if (account != null) {
                binding.textViewAccountName.text = account.name

                try {
                    // Secret Key-ის გაშიფვრა
                    val decryptedKey = EncryptionHelper.decrypt(account.secretKey)
                    startGeneratingCodes(decryptedKey)
                } catch (e: Exception) {
                    binding.textViewCode.text = "--"
                    binding.textViewTimer.text = "Decryption failed"
                    e.printStackTrace()
                }

            } else {
                binding.textViewAccountName.text = "Account not found"
                binding.textViewCode.text = "--"
                binding.textViewTimer.text = ""
            }
        }
    }

    private fun startGeneratingCodes(secretKey: String) {
        generateCode(secretKey)
    }

    private fun generateCode(secretKey: String) {
        val totp = TOTPGenerator(secretKey)
        val code = totp.generateCurrentCode()
        val secondsRemaining = totp.secondsUntilNextCode()

        binding.textViewCode.text = code
        binding.textViewTimer.text = "Expires in: ${secondsRemaining}s"

        timer?.cancel()
        timer = object : CountDownTimer(secondsRemaining * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished / 1000
                binding.textViewTimer.text = "Expires in: ${remaining}s"
            }

            override fun onFinish() {
                generateCode(secretKey)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}

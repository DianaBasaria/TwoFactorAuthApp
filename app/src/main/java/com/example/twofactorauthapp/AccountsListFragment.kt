package com.example.twofactorauthapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.twofactorauthapp.data.AccountEntity
import com.example.twofactorauthapp.data.AppDatabase
import com.example.twofactorauthapp.data.LogEntity
import com.example.twofactorauthapp.databinding.FragmentAccountsListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class AccountsListFragment : Fragment() {

    private var _binding: FragmentAccountsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = AccountsAdapter(
            onItemClick = { account ->
                val action = AccountsListFragmentDirections
                    .actionAccountsListFragmentToGenerateCodeFragment(account.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { account ->
                confirmAndDelete(account)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.fabAddAccount.setOnClickListener {
            findNavController().navigate(R.id.addAccountFragment)
        }

        setupMenu()
        loadAccounts()
    }

    override fun onResume() {
        super.onResume()
        loadAccounts()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.accounts_menu, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_accountsListFragment_to_settingsFragment)
                        true
                    }
                    R.id.action_logs -> {
                        findNavController().navigate(R.id.action_accountsListFragment_to_logsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            val accounts = AppDatabase.getInstance(requireContext())
                .accountDao()
                .getAllAccounts()
            Log.d("DEBUG_ACCOUNTS", "Accounts loaded: $accounts")
            adapter.submitList(accounts)
        }
    }

    private fun confirmAndDelete(account: AccountEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete \"${account.name}\"?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(requireContext())

                    // 1. წაშლა Account ცხრილიდან
                    db.accountDao().deleteAccount(account)

                    // 2. ლოგის შენახვა
                    val log = LogEntity(
                        accountName = account.name,
                        timestamp = System.currentTimeMillis(),
                        status = "Account deleted"
                    )
                    db.logDao().insertLog(log)

                    // 3. განახლება UI-ში
                    loadAccounts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

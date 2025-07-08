package com.example.twofactorauthapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.twofactorauthapp.data.AccountEntity
import com.example.twofactorauthapp.databinding.ItemAccountBinding

class AccountsAdapter(
    private val onItemClick: (AccountEntity) -> Unit,
    private val onDeleteClick: (AccountEntity) -> Unit
) : ListAdapter<AccountEntity, AccountsAdapter.AccountViewHolder>(AccountDiffCallback()) {

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: AccountEntity) {
            binding.tvAccountName.text = account.name

            binding.root.setOnClickListener {
                onItemClick(account)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(account)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<AccountEntity>() {
        override fun areItemsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem == newItem
        }
    }
}

package com.example.telegram.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.telegram.R
import com.example.telegram.models.CommonModel
import com.example.telegram.utilits.*
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.contact_item.view.*
import kotlinx.android.synthetic.main.fragment_contact.*

class ContactFragment : BaseFragment(R.layout.fragment_contact) {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: FirebaseRecyclerAdapter<CommonModel, ContactHolder>
    private lateinit var mRefContacts: DatabaseReference
    private lateinit var mRefUsers: DatabaseReference
    private lateinit var mRefUserListener: AppValueEventListener
    private var mapListener = hashMapOf<DatabaseReference, AppValueEventListener>()
    override fun onResume() {
        super.onResume()
        APP_ACTIVITY.title = getString(R.string.contact_title)
        initRecView()
    }

    //инициализация ресайклер вью
    private fun initRecView() {
        mRecyclerView = contacts_rec_view
        mRefContacts = REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS)
            .child(CURRENT_UID)//пробираемся в ноду контактов
        val options = FirebaseRecyclerOptions.Builder<CommonModel>()
            .setQuery(mRefContacts, CommonModel::class.java)
            .build()
        mAdapter = object : FirebaseRecyclerAdapter<CommonModel, ContactHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.contact_item, parent, false)
                return ContactHolder(view)
            }

            override fun onBindViewHolder(
                holder: ContactHolder,
                position: Int,
                model: CommonModel
            ) {
                mRefUsers = REF_DATABASE_ROOT.child(NODE_USERS)
                    .child(model.id)//пробираемся по айдишке в номер телефона
                mRefUserListener = AppValueEventListener {
                    val contact = it.getCommonModel()
                    holder.name.text = contact.fullname
                    holder.status.text = contact.state
                    holder.photo.downloadAndSetImage(contact.photoURL)
                    holder.itemView.setOnClickListener{
                       replaceFragment(SingleChatFragment(contact))
                    }
                }
                mRefUsers.addValueEventListener(mRefUserListener)
                mapListener[mRefUsers] = mRefUserListener
            }

        }
        mRecyclerView.adapter = mAdapter
        mAdapter.startListening()

    }

    //холдер для захвата Вьюгруп
    class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.contact_full_name
        val status: TextView = itemView.contact_status
        val photo: CircleImageView = itemView.contact_photo

    }

    override fun onPause() {
        super.onPause()
        mAdapter.stopListening()
        mapListener.forEach {
            it.key.removeEventListener(it.value)
        }
    }

}



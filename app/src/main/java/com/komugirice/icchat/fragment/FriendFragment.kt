package com.komugirice.icchat.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.FriendAdapter
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.User
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class FriendFragment : Fragment() {

    private val customAdapter by lazy { FriendAdapter(context) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initLayout()
        initData()
    }

    private fun initLayout() {
        initClick()
        initRecyclerView()
        initSwipeRefreshLayout()
    }

    private fun initClick() {

    }

    private fun initRecyclerView() {
        recyclerView.apply {
            adapter = customAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            initData()
        }
    }
    private fun initData() {
        FirebaseFirestore.getInstance()
            .collection("user")
            .get()
            .addOnCompleteListener {
                swipeRefreshLayout.isRefreshing = false
                if (!it.isSuccessful)
                    return@addOnCompleteListener
                it.result?.toObjects(User::class.java)?.also { users ->
                    customAdapter.refresh(users)
                }
            }
    }
}

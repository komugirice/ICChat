package com.komugirice.icchat.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.User
import kotlinx.android.synthetic.main.fragment_debug.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DebugFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug, container, false)
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
    }

    private fun initClick() {
        buttonAddDebugUser.setOnClickListener {
            var list: MutableList<User> = mutableListOf()
            for (i in 0..9) {
                list.add(
                    User().apply {
                        userId = "00000" + i.toString()
                        name = "ユーザ" + i.toString()
                        val birthSthring = ("199" + i.toString() + "/" + (i + 1).toString()
                                + "/" + (i+1).toString())
                        birthDay = birthSthring.toDate("yyyy/MM/dd")
                    }
                )
            }
            list.forEach{
            FirebaseFirestore.getInstance()
                .collection("user")
                .add(it)
            }
        }
    }

    private fun initData() {
        var adapter: ArrayAdapter<CharSequence>
        FirebaseFirestore.getInstance()
            .collection("user")
            .orderBy(User::userId.name)
            .limit(10)
            .get()
            .addOnCompleteListener {

                it.result?.toObjects(User::class.java)?.also { users ->
                    val userIdArray = users.map { it.userId }.toMutableList().toTypedArray()
                    context?.also{
                        adapter = ArrayAdapter<CharSequence>(it, R.layout.row_spinner,
                        userIdArray)
                        SpinnerUsers.adapter = adapter
                    }
                }
            }

    }
}

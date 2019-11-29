package com.komugirice.icchat.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.User
import kotlinx.android.synthetic.main.fragment_debug.*

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
                        birthDay = ("199" + i.toString() + "/" + (i + 1).toString()
                                + "/" + (i+1).toString()).toDate()
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
}

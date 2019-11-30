package com.komugirice.icchat.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.qiitaapplication.extension.getIdFromEmail
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.ui.login.LoginActivity
import com.komugirice.icchat.ui.login.LoginViewModel
import com.komugirice.icchat.util.FireStoreUtil
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
        initSpinner()
    }

    private fun initLayout() {
        initClick()
    }

    private fun initClick() {
        // デバッグユーザ追加
        buttonAddDebugUser.setOnClickListener {
            var list: MutableList<User> = mutableListOf()
            for (i in 0..9) {
                list.add(
                    User().apply {
                        userId = "00000" + i.toString()
                        name = "ユーザ" + i.toString()
                        val birthString = ("199" + i.toString() + "/" + (i + 1).toString()
                                + "/" + (i+1).toString())
                        birthDay = birthString.toDate("yyyy/MM/dd")
                    }
                )
            }
            list.forEach{
            FirebaseFirestore.getInstance()
                .collection("user")
                .add(it).addOnFailureListener {
                    return@addOnFailureListener
                }
            }
            Toast.makeText(
                context,
                "デバッグユーザ登録が完了しました。",
                Toast.LENGTH_LONG
            ).show()
        }

        // 友だち追加
        buttonAddDebugFriend.setOnClickListener {
            val friendId: String = SpinnerUsers.selectedItem.toString()

            FirebaseFirestore.getInstance()
                .collection("friend")
                .add(Friend().apply{
                    userId = FireStoreUtil.getLoginUserId()
                    this.friendId = friendId
                }).addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "友だち登録が完了しました。ID:$friendId",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
    }

    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        FirebaseFirestore.getInstance()
            .collection("user")
            .whereGreaterThan("userId", FireStoreUtil.getLoginUserId())
            .whereLessThan("userId", FireStoreUtil.getLoginUserId())
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

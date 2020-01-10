package com.komugirice.icchat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.komugirice.icchat.interfaces.Update

/**
 * A simple [Fragment] subclass.
 */
class InterestFragment : Fragment(), Update {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_interest, container, false)
    }

    /**
     * 遷移先のActivityから戻ってきた場合にリロードする
     */
    override fun update() {

    }


}

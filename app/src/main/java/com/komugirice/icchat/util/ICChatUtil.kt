package com.komugirice.icchat.util

import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.gson.Gson
import com.komugirice.icchat.R
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.extension.*
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.Room
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.util.*


object ICChatUtil
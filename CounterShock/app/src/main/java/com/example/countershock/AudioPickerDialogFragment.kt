package com.example.countershock

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AudioPickerDialogFragment : DialogFragment(), AudioPickerAdapter.Callback {

    lateinit var preferences: SharedPreferences
    lateinit var editor: Editor

    lateinit var adapter: AudioPickerAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireContext().getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_media_picker, container, false)

        val items = AudioStorer(requireContext()).getAllAudios()

        adapter = AudioPickerAdapter(items, this)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        return view
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)

    }

    override fun itemSelected(item: AudioModel) {
        editor.putInt(getString(R.string.key_audio_id), item.id)
        editor.commit()
        dismiss()
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(ShockUtils.MEDIA_UPDATED_ACTION))
    }
}
package net.comelite.kurdistanborsa.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.comelite.kurdistanborsa.BuildConfig
import net.comelite.kurdistanborsa.R

class ShareBottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var playStoreBTN: Button
    lateinit var appStoreBTN: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // To provide rounded corners in Bottom Sheet.
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_share_bottom_sheet, container, false)

        playStoreBTN = view.findViewById(R.id.playStoreBTN)
        appStoreBTN = view.findViewById(R.id.appStoreBTN)

        playStoreBTN.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            //sendIntent.putExtra(Intent.EXTRA_TITLE, "Iraqi Borsa")
            // text to share
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            startActivity(Intent.createChooser(sendIntent, null))
        }
        appStoreBTN.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            //sendIntent.putExtra(Intent.EXTRA_TITLE, "Iraqi Borsa")
            // text to share
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://apps.apple.com/app/id1175422834")
            startActivity(Intent.createChooser(sendIntent, null))
        }
        return view
    }
    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
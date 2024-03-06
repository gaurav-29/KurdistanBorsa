package net.comelite.kurdistanborsa.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import net.comelite.kurdistanborsa.R
import net.comelite.kurdistanborsa.utils.Constants

class ImagePopUpDialogFragment: DialogFragment(){

    lateinit var imageView: ImageView
    lateinit var closeImg: ImageView
    var imageUrl: String?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.custom_image_dialog, container, false)
        imageView = view.findViewById(R.id.ivImageDialogPopup)
        closeImg = view.findViewById(R.id.ivImageCloseDialogPopup)

        if (arguments != null)
            imageUrl = arguments?.getString("imageUrl")

        closeImg.setOnClickListener {
            dissmissDialog()
        }
        return view
    }

    private fun dissmissDialog() {
        dialog?.dismiss()
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(Constants.getDeviceWidth(requireContext()) - 50,Constants.getDeviceWidth(requireContext()) - 50)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.CENTER)

        val option: RequestOptions = RequestOptions()
            .placeholder(R.mipmap.ic_action_logo)
            .error(R.mipmap.ic_action_logo)
        Glide.with(requireContext()).load(imageUrl).apply(option).centerInside().into(imageView)
    }
}
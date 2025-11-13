package `in`.amankumar110.madenewsapp.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.material.R.style.Theme_MaterialComponents_Dialog
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentUploadImageBinding

class UploadImageFragment : DialogFragment() {

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private var onImageResultCallback: ((selectedUri: Uri?) -> Unit)? = null

    companion object {
        private const val TAG = "UploadImageFragment"
        private var uploadImageFragmentInstance: UploadImageFragment? = null
        private const val KEY_SELECTED_URI = "selectedImageUri"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, onImageResult: (selectedUri: Uri?) -> Unit) {
            uploadImageFragmentInstance?.dismissAllowingStateLoss() // Dismiss any old instance
            uploadImageFragmentInstance = null

            val fragment = UploadImageFragment()
            fragment.onImageResultCallback = onImageResult
            uploadImageFragmentInstance = fragment
            fragment.show(fragmentManager, TAG)
        }

        @JvmStatic
        fun hide() {
            uploadImageFragmentInstance?.dismissAllowingStateLoss()
            uploadImageFragmentInstance = null
        }

        @JvmStatic
        fun isShowing(): Boolean {
            return uploadImageFragmentInstance?.dialog?.isShowing == true &&
                    uploadImageFragmentInstance?.isAdded == true &&
                    uploadImageFragmentInstance?.isVisible == true &&
                    uploadImageFragmentInstance?.view != null
        }
    }

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                updateUiWithSelectedImage()
            } else {
                // If user cancels, we might not want to clear a previously selected image
                // unless explicitly desired. For now, just show a toast if nothing new is picked.
                Toast.makeText(requireContext(), "No new image selected", Toast.LENGTH_SHORT).show()
                // If you want to clear previous selection on cancel:
                // selectedImageUri = null
                // updateUiWithSelectedImage() // This would hide preview and reset button
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, Theme_MaterialComponents_Dialog)
        isCancelable = true

        savedInstanceState?.getParcelable<Uri>(KEY_SELECTED_URI)?.let {
            selectedImageUri = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUiWithSelectedImage() // Update UI based on current selectedImageUri

        binding.btnSelectImage.setOnClickListener { // Assuming your "Yes, Please" button ID is btnSelectImage
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnUploadImage.setOnClickListener { // Assuming your "Confirm Image" button ID is btnUploadImage
            // This button's role is now primarily to confirm the selection
            if (selectedImageUri != null) {
                onImageResultCallback?.invoke(selectedImageUri)
                hide()
            } else {
                // If no image is selected, perhaps prompt to select one or launch picker
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
                // pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        binding.btnCancelUpload.setOnClickListener {
            onImageResultCallback?.invoke(null)
            hide()
        }
    }

    private fun updateUiWithSelectedImage() {
        if (_binding == null) return // Ensure binding is available

        if (selectedImageUri != null) {
            binding.ivStoryImagePreview.visibility = View.VISIBLE
            Glide.with(this@UploadImageFragment)
                .load(selectedImageUri)
                .into(binding.ivStoryImagePreview)
            binding.btnSelectImage.text = getString(R.string.change_image) // e.g., "Change Image"
            binding.btnUploadImage.visibility = View.VISIBLE // Show confirm button
        } else {
            binding.ivStoryImagePreview.visibility = View.GONE
            binding.btnSelectImage.text = getString(R.string.select_image) // e.g., "Select Image"
            binding.btnUploadImage.visibility = View.GONE // Hide confirm button if no image
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let {
            outState.putParcelable(KEY_SELECTED_URI, it)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (uploadImageFragmentInstance == this) {
            uploadImageFragmentInstance = null
        }
    }

}

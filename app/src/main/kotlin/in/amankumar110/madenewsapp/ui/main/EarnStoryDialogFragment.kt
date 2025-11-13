package `in`.amankumar110.madenewsapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment // Changed from Fragment to DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.R.style.Theme_MaterialComponents_Dialog
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentEarnStoryDialogBinding
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.viewmodel.ad.AdViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.math.floor

// It's good practice to set a dialog theme if you want full-screen or custom styling
// import com.google.android.material.R as MaterialR

class EarnStoryDialogFragment : DialogFragment() { // Changed to DialogFragment

    private val adViewModel: AdViewModel by activityViewModels()
    private lateinit var fragmentEarnStoryDialogBinding: FragmentEarnStoryDialogBinding;
    private lateinit var adSource: AdViewModel.AdSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: If you want it to look like a full-screen dialog similar to NewsArticleFragment
        // setStyle(STYLE_NORMAL, MaterialR.style.Theme_MaterialComponents_DayNight_NoActionBar_Bridge) // Example theme
        setStyle(STYLE_NORMAL, Theme_MaterialComponents_Dialog)
        isCancelable = true // Default is true for DialogFragment, but good to be explicit


        savedInstanceState?.getString(ARG_AD_SOURCE)?.let {
            adSource = AdViewModel.AdSource.valueOf(it)
        }

        // If adSource is not initialized from savedInstanceState, get it from arguments
        if (!::adSource.isInitialized) {
            val adSourceString = arguments?.getString(ARG_AD_SOURCE)
                ?: throw IllegalArgumentException("Ad Source must be provided via arguments or savedInstanceState")
            adSource = AdViewModel.AdSource.valueOf(adSourceString)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentEarnStoryDialogBinding = FragmentEarnStoryDialogBinding.inflate(inflater,container,false)
        return fragmentEarnStoryDialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add any view initializations here if needed

        val titles = resources.getStringArray(R.array.free_story_generation_messages)
        val randomIndex = floor(Math.random() * titles.size).toInt()
        val selectedTitle = titles[randomIndex]
        fragmentEarnStoryDialogBinding.tvEarnStoryTitle.text = selectedTitle

        fragmentEarnStoryDialogBinding.btnWatchAd.setOnClickListener {
            adViewModel.showAd(requireActivity(),adSource)
        }

        setupAdEventsListener()
    }

    private fun setupAdEventsListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                adViewModel.adEvents.collect {
                    when(it) {
                        is AdViewModel.AdEvent.DismissEarnStoryDialog -> hide()
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adViewModel.isLoading.collect {
                    if(it)
                        LoadingFragment.show(childFragmentManager)
                    else
                        LoadingFragment.hide(childFragmentManager)
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the static reference if this is the instance being shown
        if (earnStoryDialogFragmentInstance == this) {
            earnStoryDialogFragmentInstance = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::adSource.isInitialized) { // Ensure adSource has been initialized
            outState.putString(ARG_AD_SOURCE, adSource.name)
        }
    }

    companion object {
        private const val TAG = "EarnStoryDialogFragment" // Tag for showing the dialog
        private const val ARG_AD_SOURCE = "ad_source"


        // Static instance variable to keep track of the currently shown dialog
        private var earnStoryDialogFragmentInstance: EarnStoryDialogFragment? = null


        @JvmStatic
        fun show(fragmentManager: FragmentManager,adSource: AdViewModel.AdSource) {
            // Dismiss any previously shown instance to prevent multiple dialogs
            earnStoryDialogFragmentInstance?.dismissAllowingStateLoss()
            earnStoryDialogFragmentInstance = null

            val fragment = EarnStoryDialogFragment()
            // You can pass arguments here if needed using a Bundle:
             val args = Bundle()
             args.putString(ARG_AD_SOURCE, adSource.toString())
             fragment.arguments = args

            earnStoryDialogFragmentInstance = fragment
            fragment.show(fragmentManager, TAG)
        }

        @JvmStatic
        fun hide() {
            earnStoryDialogFragmentInstance?.dismissAllowingStateLoss() // Use dismissAllowingStateLoss for safety
            earnStoryDialogFragmentInstance = null
        }

        @JvmStatic
        fun isShowing(): Boolean {
            return earnStoryDialogFragmentInstance?.dialog?.isShowing == true &&
                    earnStoryDialogFragmentInstance?.isAdded == true &&
                    earnStoryDialogFragmentInstance?.isVisible == true &&
                    earnStoryDialogFragmentInstance?.view != null // Ensure view is created
        }


    }
}

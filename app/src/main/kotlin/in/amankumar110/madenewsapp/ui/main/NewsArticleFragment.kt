package `in`.amankumar110.madenewsapp.ui.main

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentNewsArticleBinding
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.ui.customviews.ExpandableChipGroup
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.utils.SatireStyle
import `in`.amankumar110.madenewsapp.utils.TTSManager
import `in`.amankumar110.madenewsapp.viewmodel.ad.AdViewModel
import `in`.amankumar110.madenewsapp.viewmodel.news.NewsViewModel
import `in`.amankumar110.madenewsapp.viewmodel.story.StoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class NewsArticleFragment : DialogFragment() {

    private val SELECTED_SATIRE_TYPE_ID_KEY =
        "selected_satire_type_id_key" // For onSaveInstanceState

    private lateinit var binding: FragmentNewsArticleBinding
    private lateinit var article: Article // Initialized in onCreate
    private val storyViewModel: StoryViewModel by viewModels()
    private val newsViewModel: NewsViewModel by viewModels()
    private val adViewModel: AdViewModel by activityViewModels()
    private var currentSelectedSatireTypeId: String? = null
    private var prompt: String? = null

    @Inject
    lateinit var ttsManager: TTSManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_Dialog)
        isCancelable = true

        var articleJson: String? = null

        if (savedInstanceState != null) {
            articleJson = savedInstanceState.getString(ARG_ARTICLE_JSON)
            prompt = savedInstanceState.getString(ARG_PROMPT)
            currentSelectedSatireTypeId = savedInstanceState.getString(SELECTED_SATIRE_TYPE_ID_KEY)
        }

        if (articleJson == null) {
            articleJson = arguments?.getString(ARG_ARTICLE_JSON)
                ?: throw IllegalArgumentException("Article JSON must be provided via arguments or savedInstanceState")
        }

        if (prompt == null) {
            prompt = arguments?.getString(ARG_PROMPT)
        }
        article = Gson().fromJson(articleJson, Article::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsArticleBinding.inflate(inflater, container, false)
        showArticle(article) // article is now guaranteed to be initialized
        return binding.root
    }

    private fun showArticle(article: Article) {
        // Update basic article info
        binding.tvNewsTitle.text = article.title
        binding.tvNewsDescription.text = article.content

        if (!article.appGenerated) {
            binding.ecgSatireStyles.setChips(SatireStyle.entries.map {
                ExpandableChipGroup.ExpandableInfo(it.id, it.displayName, it.colorResId)
            })

            if (currentSelectedSatireTypeId == null && article.satireStyle != null) {
                currentSelectedSatireTypeId = article.satireStyle
            }

            val initialSelectedInfo = currentSelectedSatireTypeId?.let { styleId ->
                SatireStyle.fromId(styleId)?.let {
                    ExpandableChipGroup.ExpandableInfo(it.id, it.displayName, it.colorResId)
                }
            }

            binding.ecgSatireStyles.setSelectedInfo(initialSelectedInfo)
            updateRemixButtonVisibility()


            binding.ecgSatireStyles.setOnChipCLickListener { expandableInfo ->
                currentSelectedSatireTypeId = expandableInfo?.label
                updateRemixButtonVisibility()
                binding.ecgSatireStyles.post {
                    binding.scrollViewNewsArticle.requestLayout()
                    // Or force CardView to recalculate
                    binding.cardView.requestLayout()
                }
            }

            binding.btnPublish.visibility = View.VISIBLE
            binding.ecgSatireStyles.visibility = View.VISIBLE
            // updateRemixButtonVisibility will handle btnRemix visibility
        } else {
            binding.btnPublish.visibility = View.GONE
            binding.ecgSatireStyles.visibility = View.GONE
            binding.btnRemix.visibility = View.GONE
        }

        // Ensure shimmer is stopped if not actively loading
        if (!newsViewModel.articleRemixLoading.value) {
            stopDescriptionShimmer()
        }
    }

    private fun updateRemixButtonVisibility() {
        val showRemix =
            !article.appGenerated && currentSelectedSatireTypeId != null && currentSelectedSatireTypeId != article.satireStyle

        binding.btnRemix.visibility = if (showRemix) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SELECTED_SATIRE_TYPE_ID_KEY, currentSelectedSatireTypeId)
        outState.putString(ARG_PROMPT, prompt)
        if (::article.isInitialized) {
            outState.putString(ARG_ARTICLE_JSON, Gson().toJson(article))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNewsTitle.setOnClickListener {
            binding.frameProgressView.playWithTTS(article.content, ttsManager)
        }

        binding.btnPublish.setOnClickListener {
            UploadImageFragment.show(parentFragmentManager) { uri ->
                storyViewModel.saveStory(article.title, article.content, uri?.toString())
            }
        }

        binding.btnRemix.setOnClickListener {
            ttsManager.stop()
            binding.frameProgressView.clearProgress()

            if (!EarnStoryDialogFragment.isShowing()) {
                EarnStoryDialogFragment.show(
                    childFragmentManager, AdViewModel.AdSource.NEWS_ARTICLE
                )
            }
            // Note: The actual remix logic is now primarily in the ad event listener
            // The scroll and focus logic can remain here or be moved if preferred.
            activity?.currentFocus?.clearFocus()
            binding.scrollViewNewsArticle.post {
                binding.scrollViewNewsArticle.smoothScrollTo(0, 0)
            }
        }

        observeRemixFlows()
        observeViewModel()
        setupAdEventsListener()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun observeRemixFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    newsViewModel.newsEvents.collect {
                        if (it is NewsViewModel.NewsEvent.ArticleRemixed) {
                            article = it.article
                            currentSelectedSatireTypeId = it.satireStyle.id
                            showArticle(article)
                        }
                    }
                }

                launch {
                    newsViewModel.articleRemixLoading.collect { isLoading ->
                        if (isLoading) {
                            startDescriptionShimmer()
                            binding.ecgSatireStyles.isClickable = false
                            binding.ecgSatireStyles.isEnabled = false
                            binding.btnPublish.isClickable = false
                            binding.btnPublish.isEnabled = false
                            binding.btnRemix.isClickable = false
                            binding.btnRemix.isEnabled = false
                        } else {
                            stopDescriptionShimmer()
                            binding.ecgSatireStyles.isClickable = true
                            binding.ecgSatireStyles.isEnabled = true
                            binding.btnPublish.isClickable = true
                            binding.btnPublish.isEnabled = true
                            binding.btnRemix.isClickable = true
                            binding.btnRemix.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    storyViewModel.isLoading.collect { isLoadingValue ->
                        if (isLoadingValue && !LoadingFragment.isShowing(childFragmentManager)) {
                            LoadingFragment.show(childFragmentManager)
                        } else if (!isLoadingValue && LoadingFragment.isShowing(childFragmentManager)) {
                            LoadingFragment.hide(childFragmentManager)
                        }
                    }
                }

                launch {
                    storyViewModel.storyEvents.collect { event ->
                        when (event) {
                            is StoryViewModel.StoryEvent.Error -> handleStoryPublishError(event.message)
                            is StoryViewModel.StoryEvent.SaveSuccess -> handleStoryPublishSuccess(
                                event.message
                            )

                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    fun setupAdEventsListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adViewModel.adEvents.collect { event ->
                    when (event) {
                        is AdViewModel.AdEvent.RewardEarned -> {

                            if (event.adSource != AdViewModel.AdSource.NEWS_ARTICLE) {
                                return@collect
                            }

                            binding.ecgSatireStyles.getSelectedInfo()?.label?.let { selectedLabel ->
                                SatireStyle.fromId(selectedLabel)?.let { selectedSatireStyle ->

                                    newsViewModel.remixArticleWithSatireStyle(
                                        prompt ?: article.title, selectedSatireStyle
                                    )
                                } ?: run {
                                    showMessage("Invalid satire style selected for remix after ad.")
                                }
                            } ?: run {
                                showMessage("No satire style selected for remix after ad.")
                            }
                        }

                        is AdViewModel.AdEvent.Error -> {

                            if (event.adSource != AdViewModel.AdSource.NEWS_ARTICLE) return@collect

                            showMessage(event.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun handleStoryPublishError(message: String) {
        showMessage(message)
    }

    private fun handleStoryPublishSuccess(message: String) {
        showMessage(message)
        dismissAllowingStateLoss()
    }

    private fun showMessage(messageData: Any) {
        if (!isAdded) return
        val text = when (messageData) {
            is Int -> getString(messageData)
            is String -> messageData
            else -> return
        }
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (Companion.newsArticleFragmentInstance == this) {
            Companion.newsArticleFragmentInstance = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (Companion.newsArticleFragmentInstance == this) {
            Companion.newsArticleFragmentInstance = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.stop()
        binding.frameProgressView.clearProgress()
    }

    companion object {
        private const val FRAGMENT_TAG = "NewsArticleFragment_TAG"
        private const val ARG_ARTICLE_JSON =
            "article_json" // Ensure this is consistent or defined once globally
        private const val ARG_PROMPT = "prompt"
        internal var newsArticleFragmentInstance: NewsArticleFragment? = null
        private var lastUsedFragmentManagerRef: WeakReference<FragmentManager>? = null

        private fun newInstance(article: Article, prompt: String): NewsArticleFragment {
            return NewsArticleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROMPT, prompt)
                    putString(ARG_ARTICLE_JSON, Gson().toJson(article))
                }
            }
        }

        @JvmStatic
        fun show(article: Article, prompt: String, fragmentManager: FragmentManager) {
            if (fragmentManager.isStateSaved || fragmentManager.isDestroyed) {
                Log.w(FRAGMENT_TAG, "FragmentManager is not in a valid state.")
                return
            }
            (fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment)?.dismissAllowingStateLoss()
            val newFragment = newInstance(article, prompt)
            try {
                newFragment.show(fragmentManager, FRAGMENT_TAG)
                newsArticleFragmentInstance = newFragment
                lastUsedFragmentManagerRef = WeakReference(fragmentManager)
            } catch (e: IllegalStateException) {
                Log.e(FRAGMENT_TAG, "Error showing DialogFragment: ${e.message}", e)
            }
        }

        @JvmStatic
        fun hide() {
            val fm = lastUsedFragmentManagerRef?.get()
            var dismissedByFmTag = false
            if (fm != null && !fm.isStateSaved && !fm.isDestroyed) {
                (fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment)?.let {
                    try {
                        it.dismissAllowingStateLoss()
                        dismissedByFmTag = true
                    } catch (e: IllegalStateException) {
                        Log.w(FRAGMENT_TAG, "Error dismissing fragment by tag: ${e.message}", e)
                    }
                }
            }
            if (!dismissedByFmTag) {
                try {
                    newsArticleFragmentInstance?.dismissAllowingStateLoss()
                } catch (e: IllegalStateException) {
                    Log.w(FRAGMENT_TAG, "Error dismissing fragment by static ref: ${e.message}", e)
                }
            }
            newsArticleFragmentInstance = null
        }

        @JvmStatic
        fun isShowing(): Boolean {
            val fm = lastUsedFragmentManagerRef?.get()
            if (fm != null && !fm.isStateSaved && !fm.isDestroyed) {
                return (fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment)?.isVisible == true
            }
            return newsArticleFragmentInstance?.let { it.isAdded && it.isVisible } == true
        }
    }

    fun getDefaultImageUri(context: Context, drawableResId: Int): Uri {
        val drawable = ContextCompat.getDrawable(context, drawableResId)!!
        val bitmap = (drawable as BitmapDrawable).bitmap
        val file = File(context.cacheDir, "default_image.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun startDescriptionShimmer() {
        binding.tvNewsDescription.visibility = View.GONE
        // Correctly access the ShimmerFrameLayout from the <include> tag
        val shimmerLayout = binding.shimmerViewContainerDescription.root
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
    }

    private fun stopDescriptionShimmer() {
        val shimmerLayout = binding.shimmerViewContainerDescription.root
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        binding.tvNewsDescription.visibility = View.VISIBLE
    }
}
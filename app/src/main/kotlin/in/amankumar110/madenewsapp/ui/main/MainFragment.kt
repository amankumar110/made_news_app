package `in`.amankumar110.madenewsapp.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.FragmentMainBinding
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import `in`.amankumar110.madenewsapp.ui.userprofile.UserProfileFragment.Companion.ARG_USER_JSON
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.utils.SpacingItemDecoration
import `in`.amankumar110.madenewsapp.viewmodel.ad.AdViewModel
import `in`.amankumar110.madenewsapp.viewmodel.auth.EmailVerificationViewModel
import `in`.amankumar110.madenewsapp.viewmodel.news.NewsViewModel
import `in`.amankumar110.madenewsapp.viewmodel.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val emailVerificationViewModel: EmailVerificationViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private val newsViewModel: NewsViewModel by viewModels()
    private val adViewModel: AdViewModel by activityViewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var newsCategoryAdapter: NewsCategoryAdapter
    private var pendingArticleQuery: String? = null
    private lateinit var loadingMessages: List<String>
    private lateinit var generateTitleLoadingMessages: List<String>

    companion object {
        private const val KEY_PENDING_QUERY = "pending_query"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            pendingArticleQuery = savedInstanceState.getString(KEY_PENDING_QUERY)
        }

        loadingMessages = resources.getStringArray(R.array.loading_messages).toList()
        generateTitleLoadingMessages =
            resources.getStringArray(R.array.generate_news_loading_messages).toList()

        // Force hide any existing loading fragment first
        if (LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.hide(childFragmentManager)
        }

        setupLoadingObserver()
        collectNewsViewModelFlows()
        collectUserSearchFlows()
        setupAdEventsListener()
        setupUI()
        newsViewModel.getWeeklyArticles()
    }

    private fun setupLoadingObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Combine all loading states from different ViewModels
                combine(
                    emailVerificationViewModel.isLoading,
                    newsViewModel.isLoading,
                    userViewModel.isLoading

                ) { emailLoading, newsLoading, userLoading ->
                    when {
                        emailLoading -> Pair(true, emptyList<String>())
                        newsLoading -> Pair(true, generateTitleLoadingMessages)
                        userLoading -> Pair(true, emptyList<String>())
                        else -> Pair(false, emptyList<String>())
                    }
                }.collect { loadingState ->
                    handleLoadingState(loadingState.first, loadingState.second)
                }
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean, messages: List<String>) {
        if (isLoading && !LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.show(childFragmentManager, messages)
        } else if (!isLoading && LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.hide(childFragmentManager)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_QUERY, pendingArticleQuery)
    }

    override fun onStop() {
        super.onStop()
        // Hide loading fragment when fragment stops to prevent it from staying visible
        if (LoadingFragment.isShowing(childFragmentManager)) {
            LoadingFragment.hide(childFragmentManager)
        }
    }

    fun setupAdEventsListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                adViewModel.adEvents.collect { event ->
                    when (event) {
                        is AdViewModel.AdEvent.RewardEarned -> {

                            if (event.adSource != AdViewModel.AdSource.MAIN) {
                                return@collect
                            }

                            pendingArticleQuery?.let { query ->
                                newsViewModel.getArticleByTitle(query)
                            }

                        }

                        is AdViewModel.AdEvent.Error -> {

                            if (event.adSource != AdViewModel.AdSource.MAIN)
                                return@collect

                            showToast(event.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupUI() {

        newsCategoryAdapter = NewsCategoryAdapter(this)
        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_m)
        val spacingPx = spacing.toDp()

        binding.rvNewsCategories.addItemDecoration(
            SpacingItemDecoration(requireContext(), spacingPx, false)
        )
        binding.rvNewsCategories.adapter = newsCategoryAdapter

        binding.searchBarItemLayout.chatInput.doAfterTextChanged { text ->
            val cleanedText = text.toString().replace("\n", "")
            if (cleanedText != text.toString()) {
                binding.searchBarItemLayout.chatInput.setText(cleanedText)
                binding.searchBarItemLayout.chatInput.setSelection(cleanedText.length)
            }
        }

        binding.searchBarItemLayout.btnSearch.setOnClickListener {
            val query = binding.searchBarItemLayout.chatInput.text.toString().trim()
            if (query.isNotEmpty()) {
                if (query.startsWith("@")) {
                    // Extract text after @ symbol
                    val userQuery = query.substring(1).trim()
                    if (userQuery.isNotEmpty()) {
                        lifecycleScope.launch {
                            userViewModel.getUserByUserName(userQuery)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a username after @",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    pendingArticleQuery = query // Store the query
                    if (!EarnStoryDialogFragment.isShowing()) EarnStoryDialogFragment.show(
                        childFragmentManager,
                        AdViewModel.AdSource.MAIN
                    )
                }
            } else {
                Toast.makeText(requireContext(), R.string.empty_search_query, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun collectNewsViewModelFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    newsViewModel.newsEvents.collect {
                        when (it) {
                            is NewsViewModel.NewsEvent.ArticleGenerated -> {

                                NewsArticleFragment.show(it.article,pendingArticleQuery?:it.article.title, childFragmentManager)
                                pendingArticleQuery = null // setting null after usage
                            }

                            is NewsViewModel.NewsEvent.ShowMessage -> showToast(it.message)
                            else -> Unit // Ensure 'when' is exhaustive if NewsEvent is sealed
                        }
                    }
                }

                launch {
                    newsViewModel.weeklyArticlesState.collect {
                        if (it is NewsViewModel.WeeklyArticlesState.Success) {
                            newsCategoryAdapter.setCategoryArticles(it.articles)
                        }
                    }
                }
            }
        }
    }

    fun collectUserSearchFlows() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.userEvents.collect {
                    when (it) { // Ensure 'when' is exhaustive if UserEvents is sealed
                        is UserViewModel.UserEvents.Success -> showUser(it.user)
                        is UserViewModel.UserEvents.Error -> {
                            showToast(it.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    fun showUser(user: User) {
        val bundle = Bundle().apply {
            putString(ARG_USER_JSON, Gson().toJson(user))
        }
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.mainFragment) {
            navController.navigate(R.id.action_mainFragment_to_userProfileFragment, bundle)
        }
    }

    private fun showToast(message: Any) {
        if (!isAdded) return // Prevent Toast if fragment not attached
        val text = when (message) {
            is Int -> getString(message)
            is String -> message
            else -> return
        }
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun Int.toDp(): Int {
        return (this / Resources.getSystem().displayMetrics.density).toInt()
    }
}
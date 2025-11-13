package `in`.amankumar110.madenewsapp.ui.userprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.databinding.FragmentUserProfileBinding
import `in`.amankumar110.madenewsapp.domain.models.auth.User
import `in`.amankumar110.madenewsapp.domain.models.story.Story
import `in`.amankumar110.madenewsapp.ui.utils.LoadingFragment
import `in`.amankumar110.madenewsapp.viewmodel.story.StoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    companion object {
        const val ARG_USER_JSON = "user_json"
    }

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var userStoriesAdapter: UserStoriesAdapter
    private val userPublishedStories = mutableListOf<Story>()
    private val storyViewModel : StoryViewModel by viewModels()
    private lateinit var user: User;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Attempt to get user from savedInstanceState first, then arguments.
        // This order is more robust for recreation scenarios.
        val userJson = savedInstanceState?.getString(ARG_USER_JSON)
            ?: arguments?.getString(ARG_USER_JSON)

        if (userJson != null) {
            user = Gson().fromJson(userJson, User::class.java)
        } else {
            // Handle the case where user data is not available,
            // e.g., navigate back or show an error.
            // For now, creating a default User or throwing an exception.
            // This depends on how critical user data is for this fragment.
            // For dummy data purposes, we might let it proceed or create a placeholder.
            // If user is lateinit and not initialized, it will crash.
            // Let's assume for now the user will be passed.
            // Consider adding error handling or a fallback if user can be null.
             throw IllegalStateException("User data is missing in UserProfileFragment")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensure user is initialized before accessing its properties
        if(::user.isInitialized) {
            binding.tvUserName.text = user.username
            val joinedAt = "Joined ${formatDate(user.joinedAt)}"
            binding.tvUserJoinedDate.text = joinedAt
        } else {
            // Handle cases where user might not have been initialized in onCreate
            // (e.g., if we remove the throw IllegalStateException)
             binding.tvUserName.text = "User Name"
             binding.tvUserJoinedDate.text = "Joined date unknown"
        }

        setupUserPublishedStories()
        observeStoryFetchFlows()
        storyViewModel.fetchStoriesByUser(user.id)
    }

    private fun formatDate(createdAt: Any): String {
        val dateString = createdAt.toString()

        // Handle up to 6 fractional digits
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        return try {
            val date: Date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            // Fallback for different date formats or parsing errors
            "Date N/A"
        }
    }

    private fun observeStoryFetchFlows() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {

                launch {
                    storyViewModel.storyEvents.collect {
                        when (it) {
                            is StoryViewModel.StoryEvent.Error -> {
                                showMessage(it.message)
                                Log.v("UserProfileFragment", "Error: "+it.message)
                            }
                            is StoryViewModel.StoryEvent.StoryDisliked -> TODO()
                            is StoryViewModel.StoryEvent.StoryLiked -> TODO()
                            else -> Unit
                        }
                    }
                }

                launch {
                    storyViewModel.stories.collect {
                        this@UserProfileFragment.userStoriesAdapter.setStories(it)
                    }
                }

                launch {
                    storyViewModel.isLoading.collect {
                        if (it && !LoadingFragment.isShowing(childFragmentManager)) {
                            LoadingFragment.show(childFragmentManager)
                        } else if (!it) {
                            LoadingFragment.hide(childFragmentManager)
                        }
                    }
                }

            }
        }

    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setupUserPublishedStories() {

        binding.rvUserPublishedStories.apply {
            isNestedScrollingEnabled = false
            // Consider using a more robust layout manager if grid-like appearance is desired
             layoutManager = GridLayoutManager(context, 2) // Example for a 2-column grid
            userStoriesAdapter = UserStoriesAdapter(this@UserProfileFragment)
            adapter = userStoriesAdapter
            // No ItemDecoration is added here as item margins will handle spacing
        }
        userStoriesAdapter.setStories(userPublishedStories) // Pass the (possibly empty) list
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::user.isInitialized) {
            outState.putString(ARG_USER_JSON, Gson().toJson(user))
        }
    }
}

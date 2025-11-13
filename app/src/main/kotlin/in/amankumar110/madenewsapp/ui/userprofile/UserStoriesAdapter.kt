package `in`.amankumar110.madenewsapp.ui.userprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import `in`.amankumar110.madenewsapp.databinding.EmptyStoriesItemLayoutBinding
import `in`.amankumar110.madenewsapp.databinding.UserPublishedStoryItemLayoutBinding
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.story.Story
import `in`.amankumar110.madenewsapp.ui.main.NewsArticleFragment

class UserStoriesAdapter(private val fragment : Fragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STORY = 0
        const val VIEW_TYPE_EMPTY = 1
    }

    private var userStories = mutableListOf<Story>()

    override fun getItemViewType(position: Int): Int {
        return if(userStories.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_STORY
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {


        if(viewType == VIEW_TYPE_EMPTY) {
            return EmptyStoryViewHolder(EmptyStoriesItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ))
        }

        return StoryViewHolder(UserPublishedStoryItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if(holder is StoryViewHolder) {
            val story = userStories[position]
            holder.storyItemLayoutBinding.tvStoryTitle.text = story.title

            holder.storyItemLayoutBinding.root.setOnClickListener {
                if(!NewsArticleFragment.isShowing())
                    NewsArticleFragment.show(Article(title = story.title, content = story.content,story.createdAt,true),story.title,fragment.childFragmentManager)
            }

            Glide.with(holder.itemView.context).load(story.imageUrl).into(holder.storyItemLayoutBinding.ivStoryImage)
        } else if(holder is EmptyStoryViewHolder) {
            holder.emptyStoryItemLayoutBinding.tvEmptyMessage.text = "No stories published yet!"
        }
    }

    fun setStories(list: List<Story>) {
        userStories.clear()
        userStories.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if(userStories.isEmpty()) 1 else userStories.size
    }

    data class StoryViewHolder(val storyItemLayoutBinding: UserPublishedStoryItemLayoutBinding)
        : RecyclerView.ViewHolder(storyItemLayoutBinding.root)

    data class EmptyStoryViewHolder(val emptyStoryItemLayoutBinding: EmptyStoriesItemLayoutBinding)
        : RecyclerView.ViewHolder(emptyStoryItemLayoutBinding.root)
}
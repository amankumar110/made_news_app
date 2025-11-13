package `in`.amankumar110.madenewsapp.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import `in`.amankumar110.madenewsapp.databinding.NewsArticleItemLayoutBinding
import `in`.amankumar110.madenewsapp.domain.models.news.Article
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.utils.NewsCategory

class NewsArticlesAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<NewsArticlesAdapter.NewsArticlesViewHolder>() {

    private val articles = mutableListOf<CategorizedArticle>()

    fun setArticles(newArticles: List<CategorizedArticle>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticlesViewHolder {
        val binding = NewsArticleItemLayoutBinding.inflate(
            LayoutInflater.from(fragment.context), parent, false
        )
        return NewsArticlesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsArticlesViewHolder, position: Int) {
        holder.bind(articles[position],position)
    }

    override fun getItemCount(): Int = articles.size

    inner class NewsArticlesViewHolder(
        private val binding: NewsArticleItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: CategorizedArticle,position: Int) {

            // remove the top padding for the first item
            if(position==0)
                binding.root.setPadding(0)

            // bind article data to views
            binding.tvDescription.text = article.title
            binding.tvSubText.text = article.createdAt

            // Only override color if available
            val colorId = NewsCategory.getByName(article.category)?.backgroundColorId
            Log.v("NewsArticlesAdapter", "Category: ${article.category}, Color ID: $colorId")
            if (colorId != null)
                binding.container.setBackgroundColor(fragment.resources.getColor(colorId))

            binding.container.setOnClickListener {
                // Handle article click
                Log.d("NewsArticlesAdapter", "Article clicked: ${article.title}")
                if(!NewsArticleFragment.isShowing())
                    NewsArticleFragment.show(article.asArticle(),article.title,fragment.childFragmentManager)
            }

        }
    }

    fun CategorizedArticle.asArticle() : Article {
        return Article(
            this.title,
            this.content,
            this.createdAt,
            appGenerated
        )
    }

}



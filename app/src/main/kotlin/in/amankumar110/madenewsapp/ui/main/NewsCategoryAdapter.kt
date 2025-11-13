package `in`.amankumar110.madenewsapp.ui.main

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import `in`.amankumar110.madenewsapp.R
import `in`.amankumar110.madenewsapp.databinding.NewsListItemLayoutBinding
import `in`.amankumar110.madenewsapp.domain.models.news.CategorizedArticle
import `in`.amankumar110.madenewsapp.utils.NewsCategory
import `in`.amankumar110.madenewsapp.utils.SpacingItemDecoration

class NewsCategoryAdapter (
    private val fragment: Fragment
) : RecyclerView.Adapter<NewsCategoryAdapter.NewsCategoryViewHolder>() {

    private val categoryData = mutableListOf<Pair<String, List<CategorizedArticle>>>()

    fun setCategoryArticles(data: Map<String, List<CategorizedArticle>>) {
        categoryData.clear()
        categoryData.addAll(data.entries.map { it.toPair() })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsCategoryViewHolder {
        val binding = NewsListItemLayoutBinding.inflate(
            LayoutInflater.from(fragment.context), parent, false
        )
        return NewsCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsCategoryViewHolder, position: Int) {
        val (categoryName, articles) = categoryData[position]
        holder.bind(categoryName, articles)
    }

    override fun getItemCount(): Int = categoryData.size

    inner class NewsCategoryViewHolder(
        private val binding: NewsListItemLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.rvNewsArticle.setHasFixedSize(true)
            val spacingPx = fragment.resources.getDimensionPixelSize(R.dimen.spacing_m)
            val spacingDp = spacingPx.toDp()
            binding.rvNewsArticle.addItemDecoration(
                SpacingItemDecoration(fragment.requireContext(), spacingDp, true)
            )
        }

        fun bind(categoryName: String, articles: List<CategorizedArticle>) {

            NewsCategory.getByName(categoryName)?.let {
                val updatedCategoryTitle = "${it.emoji} $categoryName"
                binding.tvCategoryTitle.text = updatedCategoryTitle
            } ?: run {
                binding.tvCategoryTitle.text = categoryName
            }

            val adapter = NewsArticlesAdapter(fragment)
            binding.rvNewsArticle.adapter = adapter
            adapter.setArticles(articles)
        }
    }


    fun Int.toDp(): Int {
        return (this / Resources.getSystem().displayMetrics.density).toInt()
    }
}




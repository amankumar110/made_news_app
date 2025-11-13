package `in`.amankumar110.madenewsapp.utils

import `in`.amankumar110.madenewsapp.R

enum class SatireStyle(
    val id: String,
    val displayName: String,
    val colorResId: Int
) {
    NOSTALGIC_UNCLE("nostalgicUncle", "Nostalgic Uncle — ‘Good Old Days’", R.color.app_color_nostalgic_uncle),
    TECH_BRO_VISIONARY("techBroVisionary", "Tech Bro Visionary", R.color.app_color_tech_bro_visionary),
    TRUMP_STYLE("trumpStyle", "Trump-Style Ranter", R.color.app_color_trump_style),
    GEN_Z("genZ", "Gen Z — ‘Vibe Check Bot’", R.color.app_color_gen_z),
    GLOBAL_DIPLOMAT("globalDiplomat", "Global Unity Diplomat — ‘Better Tomorrow’", R.color.app_color_global_diplomat),
    PR_MANAGER("prManager", "PR Manager — ‘Spin Doctor Supreme’", R.color.app_color_pr_manager),
    GOSSIP_AUNT("gossipAunt", "Gossip Aunt — ‘Tea Time Truth Twister’", R.color.app_color_gossip_aunt),
    MONEY_MOGUL("moneyMogul", "Money Mogul — ‘Greedy Wall Street Bro’", R.color.app_color_money_mogul),
    HOLLYWOOD_PRODUCER("hollywoodProducer", "Hollywood Producer — ‘Deals, Drama & Dollar Signs’", R.color.app_color_hollywood_producer);

    companion object {
        fun fromId(id: String): SatireStyle? =
            entries.firstOrNull { it.id == id }
    }
}

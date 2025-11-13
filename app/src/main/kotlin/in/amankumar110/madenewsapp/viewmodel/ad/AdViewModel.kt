package `in`.amankumar110.madenewsapp.viewmodel.ad

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AdViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val adRequest = AdRequest.Builder().build()
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test ID
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false
    private var rewardGiven = false

    // StateFlow for loading state (survives config changes)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // SharedFlow for one-time events
    private val _adEvents = MutableSharedFlow<AdEvent>()
    val adEvents: SharedFlow<AdEvent> = _adEvents

    init {
        loadAd()
    }

    fun loadAd() {

        if (isAdLoading || rewardedAd != null) return
        isAdLoading = true

        RewardedAd.load(
            getApplication(),
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isAdLoading = false
                    Log.d("AdViewModel", "Ad successfully loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isAdLoading = false
                    Log.e("AdViewModel", "Ad silently failed to load: $loadAdError")
                }
            }
        )
    }

    fun showAd(activity: Activity,adSource: AdSource) {

        if (rewardedAd != null) {
            showRewardedAd(activity,adSource)
        } else {
            _isLoading.value = true
            viewModelScope.launch {
                _adEvents.emit(AdEvent.ShowEarnStoryDialog) // If needed
            }

            // One final retry
            RewardedAd.load(
                getApplication(),
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isAdLoading = false
                        _isLoading.value = false
                        showRewardedAd(activity,adSource)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isAdLoading = false
                        _isLoading.value = false
                        Log.e("AdViewModel", "Final ad retry failed: $loadAdError")
                        viewModelScope.launch {
                            _adEvents.emit(AdEvent.Error("Ad could not be available right now.",adSource))
                        }
                    }
                }
            )
        }
    }

    private fun showRewardedAd(activity: Activity,adSource: AdSource) {
        rewardGiven = false // Reset before showing

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                viewModelScope.launch {
                    _adEvents.emit(AdEvent.DismissEarnStoryDialog)
                }
                rewardedAd = null // Clear reference after showing
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("AdViewModel", "Ad failed to show: $adError")
                viewModelScope.launch {
                    _adEvents.emit(AdEvent.Error("Ad failed to display.",adSource))
                }
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d("AdViewModel", "Ad closed. Reward given: $rewardGiven")
                if (!rewardGiven) {
                    Log.d("AdViewModel", "Ad skipped or closed before reward.")
                }
                loadAd() // Preload next ad
            }
        }

        rewardedAd?.show(activity) { rewardItem: RewardItem ->
            rewardGiven = true
            viewModelScope.launch {
                _adEvents.emit(AdEvent.RewardEarned(adSource))
            }
        }
    }

    sealed class AdEvent {
        data class RewardEarned(val adSource: AdSource) : AdEvent()
        object DismissEarnStoryDialog : AdEvent()
        object ShowEarnStoryDialog : AdEvent()
        data class Error(val message: String,val adSource: AdSource) : AdEvent()
    }

    enum class AdSource { MAIN, NEWS_ARTICLE}

}
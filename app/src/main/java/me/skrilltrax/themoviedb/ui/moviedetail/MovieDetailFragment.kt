package me.skrilltrax.themoviedb.ui.moviedetail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import java.util.Stack
import kotlin.collections.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.skrilltrax.themoviedb.R
import me.skrilltrax.themoviedb.adapter.CreditsAdapter
import me.skrilltrax.themoviedb.adapter.GenreAdapter
import me.skrilltrax.themoviedb.adapter.RecommendationAdapter
import me.skrilltrax.themoviedb.adapter.VideoAdapter
import me.skrilltrax.themoviedb.constants.CreditsType
import me.skrilltrax.themoviedb.databinding.FragmentMovieDetailBinding
import me.skrilltrax.themoviedb.interfaces.ListItemClickListener
import me.skrilltrax.themoviedb.interfaces.MovieDetailItemClickListener
import me.skrilltrax.themoviedb.model.credits.CastItem
import me.skrilltrax.themoviedb.model.credits.CrewItem
import me.skrilltrax.themoviedb.model.list.movie.MovieListResultItem
import me.skrilltrax.themoviedb.model.videos.VideoResultsItem
import me.skrilltrax.themoviedb.utils.SystemLayoutUtils.makeFullScreenHideNavigation
import me.skrilltrax.themoviedb.utils.SystemLayoutUtils.setStatusBarTint
import me.skrilltrax.themoviedb.utils.gone
import me.skrilltrax.themoviedb.utils.setHeroImage
import me.skrilltrax.themoviedb.utils.setPosterImage
import me.skrilltrax.themoviedb.utils.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieDetailFragment : Fragment(), MovieDetailItemClickListener, ListItemClickListener {

    private val movieDetailViewModel: MovieDetailViewModel by viewModel()
    private val movieDetailActivity by lazy { requireActivity() as MovieDetailActivity }
    private var movieId: MutableLiveData<String> = MutableLiveData("")
    private lateinit var movieStack: Stack<String>
    private lateinit var binding: FragmentMovieDetailBinding
    private lateinit var scrollChangedListener: ViewTreeObserver.OnScrollChangedListener
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        makeFullScreenHideNavigation()
        arguments?.let { movieId.postValue(it.getString("movie_id", "")) }
        movieStack = Stack()
        movieStack.push(movieId.value)
        callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            movieId.postValue(movieStack.pop())
            isEnabled = movieStack.size > 1
            movieDetailActivity.showLoading()
            binding.root.fullScroll(ScrollView.FOCUS_UP)
        }
        callback.isEnabled = false
        binding = FragmentMovieDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { rootView, insets ->
            rootView.updatePadding(0, 0, 0, insets.systemWindowInsetBottom)
            insets
        }
        movieDetailActivity.showLoading()
        observeScroll(view)
        setupObservers(viewLifecycleOwner)
    }

    private fun observeScroll(view: View) {
        scrollChangedListener = setStatusBarTint(view, binding.movieHeader.root)
        view.viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)
    }

    private fun setupObservers(viewLifecycleOwner: LifecycleOwner) {
        movieId.observe(viewLifecycleOwner, Observer {
            movieDetailViewModel.movieId.postValue(it)
        })

        movieDetailViewModel.movieId.observe(viewLifecycleOwner, Observer {
            movieDetailViewModel.fetchMovieDetails()
            movieDetailViewModel.fetchCastAndCrew()
            movieDetailViewModel.fetchVideos()
            movieDetailViewModel.fetchRecommendations()
        })

        movieDetailViewModel.movieDetail.observe(viewLifecycleOwner, Observer {
            with(binding) {
                if (it.overview.isNullOrEmpty()) {
                    synopsis.gone()
                    titleSynopsis.gone()
                } else {
                    synopsis.visible()
                    titleSynopsis.visible()
                }

                val voteAverage: Float = it.voteAverage?.toFloat() ?: 0.0f
                if (voteAverage == 0.0f) {
                    movieHeader.ratingText.text = "N/A"
                } else {
                    movieHeader.ratingText.text = voteAverage.toString()
                    movieHeader.ratingBar.rating = voteAverage / 2
                }

                synopsis.text = it.overview
                movieHeader.movieTitle.text = it.title
                movieHeader.releaseDate.text =
                    resources.getString(R.string.release_date_s, it.releaseDate)
                it.backdropPath?.let { url -> movieHeader.movieBackground.setHeroImage(url) }
                it.posterPath?.let { url -> movieHeader.moviePoster.setPosterImage(url) }
            }
        })

        movieDetailViewModel.genres.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                binding.genreRecyclerView.gone()
                return@Observer
            }
            binding.genreRecyclerView.visible()

            binding.genreRecyclerView.adapter = GenreAdapter(it)
        })

        movieDetailViewModel.cast.observe(viewLifecycleOwner, Observer {
            val castList: ArrayList<CastItem> = arrayListOf()
            for (castListItem in it) {
                if (castListItem.profilePath != null) {
                    castList.add(castListItem)
                }
            }

            if (castList.isEmpty()) {
                binding.castRecyclerView.gone()
                binding.titleCast.gone()
                return@Observer
            }
            binding.castRecyclerView.visible()
            binding.titleCast.visible()

            binding.castRecyclerView.adapter =
                CreditsAdapter(castList as List<CastItem>, CreditsType.CAST)
        })

        movieDetailViewModel.crew.observe(viewLifecycleOwner, Observer {
            val crewList: ArrayList<CrewItem> = arrayListOf()
            for (crewListItem in it) {
                if (crewListItem.profilePath != null) {
                    crewList.add(crewListItem)
                }
            }

            if (crewList.isEmpty()) {
                binding.crewRecyclerView.gone()
                binding.titleCrew.gone()
                return@Observer
            }
            binding.crewRecyclerView.visible()
            binding.titleCrew.visible()

            binding.crewRecyclerView.adapter =
                CreditsAdapter(crewList as List<CrewItem>, CreditsType.CREW)
        })

        movieDetailViewModel.videos.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                binding.videosRecyclerView.gone()
                binding.titleVideos.gone()
                return@Observer
            }
            binding.videosRecyclerView.visible()
            binding.titleVideos.visible()
            binding.videosRecyclerView.adapter = VideoAdapter(it, this)
        })

        movieDetailViewModel.recommendations.observe(viewLifecycleOwner, Observer { list ->
            if (list.isEmpty()) {
                binding.recommendedRecyclerView.gone()
                binding.titleRecommended.gone()
                return@Observer
            }
            binding.recommendedRecyclerView.visible()
            binding.titleRecommended.visible()

            val urlIdList: ArrayList<Pair<String, String>> = ArrayList()
            list.forEach {
                urlIdList.add(Pair(it.posterPath ?: "", it.id.toString()))
            }
            binding.recommendedRecyclerView.adapter = RecommendationAdapter(urlIdList, this)
        })

        movieDetailViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it == false) lifecycleScope.launch(Dispatchers.IO) {
                delay(300)
                withContext(Dispatchers.Main) {
                    movieDetailActivity.hideLoading()
                }
            }
        })
    }

    override fun onVideoItemClick(videoResultsItem: VideoResultsItem) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${videoResultsItem.key}"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=${videoResultsItem.key}")
        )
        try {
            requireContext().startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            requireContext().startActivity(webIntent)
        }
    }

    override fun onItemClick(resultsItem: MovieListResultItem) {
        this.onItemClick(resultsItem.id.toString())
    }

    override fun onItemClick(id: String) {
        movieDetailActivity.showLoading()
        movieId.postValue(id)
        binding.root.fullScroll(ScrollView.FOCUS_UP)
        movieStack.push(movieId.value)
        callback.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        view?.viewTreeObserver?.addOnScrollChangedListener(scrollChangedListener)
    }

    override fun onPause() {
        view?.viewTreeObserver?.removeOnScrollChangedListener(scrollChangedListener)
        movieDetailActivity.dialog?.let {
            if (it.isShowing) {
                it.hide()
            }
            it.dismiss()
        }
        super.onPause()
    }

    companion object {
        fun newInstance(id: String): MovieDetailFragment {
            return MovieDetailFragment().also {
                val bundle = Bundle()
                bundle.putString("movie_id", id)
                it.arguments = bundle
            }
        }
    }
}

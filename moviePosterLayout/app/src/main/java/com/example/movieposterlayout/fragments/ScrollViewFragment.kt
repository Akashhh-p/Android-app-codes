package com.example.movieposterlayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.movieposterlayout.Movie
import com.example.movieposterlayout.R
import com.google.android.material.card.MaterialCardView

class ScrollViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scroll, container, false)
        val linearLayout = view.findViewById<LinearLayout>(R.id.scrollLinearLayout)
        val movieList = Movie.getMovieList()

        for (movie in movieList) {
            val cardView = LayoutInflater.from(context).inflate(R.layout.item_movie, linearLayout, false) as MaterialCardView
            
            val imageView = cardView.findViewById<ImageView>(R.id.ivPoster)
            val titleView = cardView.findViewById<TextView>(R.id.tvTitle)
            val yearView = cardView.findViewById<TextView>(R.id.tvYear)

            imageView.setImageResource(movie.image)
            titleView.text = movie.name
            yearView.text = "Year: ${movie.year}"

            linearLayout.addView(cardView)
        }

        return view
    }
}

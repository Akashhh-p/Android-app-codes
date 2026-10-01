package com.example.movieposterlayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieposterlayout.Movie
import com.example.movieposterlayout.MovieAdapter
import com.example.movieposterlayout.R

class RecycleViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recycle, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MovieAdapter(Movie.getMovieList())
        
        return view
    }
}

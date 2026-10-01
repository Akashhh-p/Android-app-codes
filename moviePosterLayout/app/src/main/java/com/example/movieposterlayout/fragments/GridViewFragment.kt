package com.example.movieposterlayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.movieposterlayout.Movie
import com.example.movieposterlayout.R

class GridViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grid, container, false)
        val gridView = view.findViewById<GridView>(R.id.gridView)
        val movieList = Movie.getMovieList()

        gridView.adapter = object : BaseAdapter() {
            override fun getCount(): Int = movieList.size
            override fun getItem(position: Int): Any = movieList[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val imageView: ImageView
                if (convertView == null) {
                    imageView = ImageView(context)
                    imageView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        450
                    )
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setPadding(4, 4, 4, 4)
                } else {
                    imageView = convertView as ImageView
                }
                imageView.setImageResource(movieList[position].image)
                return imageView
            }
        }

        return view
    }
}

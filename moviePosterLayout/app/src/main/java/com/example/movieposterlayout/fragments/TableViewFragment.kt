package com.example.movieposterlayout.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.movieposterlayout.Movie
import com.example.movieposterlayout.R

class TableViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_table, container, false)
        val tableLayout = view.findViewById<TableLayout>(R.id.tableLayout)
        val movieList = Movie.getMovieList()

        for (movie in movieList) {
            val tableRow = TableRow(context)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            tableRow.setPadding(8, 8, 8, 8)

            val imageView = ImageView(context)
            imageView.layoutParams = TableRow.LayoutParams(200, 300)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(movie.image)

            val textView = TextView(context)
            textView.text = "${movie.name}\nYear: ${movie.year}"
            textView.setTextColor(resources.getColor(R.color.text_primary, null))
            textView.setPadding(16, 0, 0, 0)
            textView.textSize = 16f

            tableRow.addView(imageView)
            tableRow.addView(textView)
            tableLayout.addView(tableRow)
        }

        return view
    }
}

package com.example.movieposterlayout

data class Movie(
    val name: String,
    val year: Int,
    val image: Int
) {
    companion object {
        fun getMovieList(): List<Movie> {
            return listOf(
                Movie("RRR", 2022, R.drawable.rrr_poster),
                Movie("Baahubali: The Beginning", 2015, R.drawable.baahubali_poster),
                Movie("Baahubali 2: The Conclusion", 2017, R.drawable.baahubali_2_poster),
                Movie("Jersey", 2019, R.drawable.jersey_poster),
                Movie("Rangasthalam", 2018, R.drawable.rangasthalam_poster),
                Movie("Pushpa: The Rise", 2021, R.drawable.pushpa_poster),
                Movie("Pushpa 2: The Rule", 2024, R.drawable.pushpa_2_poster),
                Movie("Arjun Reddy", 2017, R.drawable.arjun_reddy_poster),
                Movie("Sita Ramam", 2022, R.drawable.sita_ramam_poster),
                Movie("Eega", 2012, R.drawable.eega_poster)
            )
        }
    }
}

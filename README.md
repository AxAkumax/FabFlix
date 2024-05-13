
Team: Suika-Lords

Members: Akshita Akumalla, Niharika Kumar

Demo URL: https://drive.google.com/file/d/1azrLnqpxVsGjdvh0TN_oT64ayRIxKsZ_/view?usp=sharing

Generated files:
   STAR_ERRORS.txt: https://drive.google.com/file/d/1_GAad8E95eW0dFkzZCLeoEm1UvEdkhgA/view?usp=sharing
   STARMOVIE_ERRORS.txt: https://drive.google.com/file/d/1RVi3Sj1Z0jpb-eF-9U0sy2_Xi8auIBJk/view?usp=sharing
   MOVIE_ERRORS.txt: https://drive.google.com/file/d/1Oae__nYdT_UUu96aKpxEW1XmEmdFvmrW/view?usp=sharing



Project 3:



* Contributions:
    * Akshita
        * Added reCAPTCHA to fabflix
        * Modified and implemented password encryption
        * Designed and Implemented Dashboard page
        * Created stored procedure for new entries from dashboard
        * Redesigned fabflix home page
    * Niharika
        * Parsed actors.xml, mains.xml, and casts.xml
        * Optimized and inserted new entries into database
        * Added https support to the website
    * We had been using PreparedStatement SQL queries from the beginning, hence none of us worked on that task.
* Filenames with Prepared Statements
    * BrowseServlet
    * ConfirmationServlet
    * GenreServlet
    * EmployeeLoginServlet
    * LoginServlet
    * MetadataServlet
    * MovieParser
    * PaymentServlet
    * SearchServlet
    * ShoppingServlet
    * SingleMovieServlet
    * SingleStarServlet
    * StarInMovieParser
    * StarParser
    * UpdateSecurePassword
* Optimizations
    * Unoptimized
        * 18 mins
    * First optimization (in memory hashset/hashmap of database tables)
        * Stored necessary tables in memory to avoid making singular SQL queries to check if an entry exists in database already
        * 90 secs
    * Second Optimization (batch insert)
        * Executed 2000 entries at once instead one-by-one
        * 60 secs
    * Additional things to speed up the parser
        * Utilized hashset and hashmap whenever possible to speed up checking if an object (star, movie, star in movies, etc) exists already
        * Passed around the stars and movies data from previous SQL query when inserting casts, instead of querying/populating stars and movies again when inserting stars_in_movies
* Inconsistent Data reports:
    * Inconsistencies
        * Each inconsistent entry and the inconsistency reason is listed in the respective mentioned file
        * Found 342 inconsistent stars (all duplicates) 
            * Considered a star to be duplicate if they had the same (name,birthyear)
            * 336 entries were duplicates because the star existed in database already
            * 6 entries were duplicates because of duplicate entries in XML file
            * STAR_ERRORS.txt
        * Found 71 inconsistent movies
            * Considered a movie to be duplicate if they had the same (title,year,director)
            * Reasons ranged from title/director/year not provided, to duplicate entries in the XML file
            * MOVIE_ERRORS.txt
        * Found 16560 inconsistent cast
            * Considered a cast to be duplicate if they had the same movie and actor
            * Reasons varied in this category, but most were due to referencing a star name that did not exist in the database. Second most was due to referencing a film id that did not exist in the movies XML
            * STARMOVIE_ERRORS.txt
    * Consistent entries
        * Inserted 10 new genres.
        * Inserted 9293 new genres_in_movies.
        * Inserted 6521 new actors. New entries are listed in STARS.txt
        * Inserted 12044 new movies. New entries are listed in MOVIES.txt
        * Inserted 32378 new cast. New entries are listed in STARMOVIES.txt

Project 2:



* Contributions:
    * Akshita:
        * Created and linked the Login page
        * Implemented searching page 
        * Implemented substring matching
        * Rewrote Browsing.js so that Searching and Browsing both lead to Movie List page
        * Implemented jump functionality so that movie list page is maintained when returned
        * Implemented pagination (prev/next feature)
    * Niharika
        * Wrote Browsing Servlet
        * Implemented sorting the movie results
        * Created and linked Shopping/Payment/Confirmation pages & servlets
        * Added the 'checkout and 'add to cart' feature on all pages
* Substring Matching Design:
    * I used the LIKE operator to match strings and substrings, so that the query can match the pattern that the users enter in the input box.
    * I converted the attributes to lowercase so that the comparison can be performed case insensitively. This makes it easier on the user, so that they don't have to provide the exact string to be matched, including the case.

Project 1:



* Contributions:
    * Akshita
        * created the single movie and single star page
        * linked single movie and single star pages
        * attached movie links in star page
        * worked on the query to get top 20 movies
        * worked on decorating the website with margins using CSS and bootstrap (and the color schemes)
        * showed stars DOB as N/A instead of null
        * uploaded database files
    * Niharika
        * created the landing page (top 20 movies) functionality
        * attached links for actors links in movie page
        * worked on the query to get top 20 movies
        * worked on the decorating the website using CSS and bootstrap
        * made navigation bar for all pages

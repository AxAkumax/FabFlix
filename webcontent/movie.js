var currentPage = 1;

$(document).ready(function() {
    fetchSearchResults(); // Call fetchSearchResults directly
});

function fetchSearchResults(){
        // Extract search parameters from the URL
        var urlParams = new URLSearchParams(window.location.search);
        var title = urlParams.get('title');
        var year = urlParams.get('year');
        var director = urlParams.get('director');
        var starName = urlParams.get('starName');
        var sortAttribute = urlParams.get('sortAttribute');
        var recordsPerPage = urlParams.get('recordsPerPage');

        var formData = {};
        if(title){ formData["title"]=title;}
        if(year){ formData["year"]=year; }
        if(director){ formData["director"]=director; }
        if(starName){ formData["starName"]=starName; }
        if(sortAttribute){ formData["sortAttribute"]=sortAttribute; }
        if(recordsPerPage){ formData["recordsPerPage"]=recordsPerPage; }
        formData["page"]=currentPage;

        // Make AJAX request to fetch search results with updated page number
        $.ajax({
            url: "api/search", // Update with your API endpoint
            method: "GET",
            dataType: "json",
            data: formData,
            success: function(resultData) {
                // Populate the table with search results
                console.log("sucesss");
                populateTable(resultData);
            },
            error: function(xhr, status, error) {
                console.error("Error occurred while fetching search results:", error);
            }
        });
    }


// Function to populate the table with search results
function populateTable(resultData) {
    console.log(resultData);
    var table = $("#movie_table_body");
    var tableHeadings = $("#movie_table thead");

    // Clear existing table rows
    table.empty();
    var noResultsMessage = $("#noResultsMessage");

    // Check if the movies array is empty
    if (resultData.movies.length === 0) {
        console.log("No movies found.");
        // Hide the table and show the no results message
        table.hide();
        tableHeadings.hide()
        noResultsMessage.show();
        return; // Exit the function early if no movies are found
    }

    noResultsMessage.hide();
    table.show();
    tableHeadings.show()
    $("#prevNextButton").show()
    // Iterate through search results and populate table
    console.log(resultData);

    for (let j = 0; j < resultData.movies.length; j++) {
        // Construct HTML for table row
        let movie = resultData.movies[j];

        var rowHTML = "<tr>";

        let movie_link = '<a href="single-movie.html?id=' + movie.id + '">' + movie.title + '</a>';

        rowHTML += "<td>" + movie_link + "</td>";
        rowHTML += "<td>" + movie.year + "</td>";
        rowHTML += "<td>" + movie.director + "</td>";

        let genre_id_names = movie.genres.split(";");
        let genres = [];

        for (let i = 0; i < genre_id_names.length; i += 2) {
            let genre_id = genre_id_names[i];
            let genre_name = genre_id_names[i + 1];
            genres.push({id: genre_id, name: genre_name});
        }
        //sorted alphabetically
        genres.sort((a, b) => a.name.localeCompare(b.name));

        let genre_entry = "";

        for (let i = 0; i < genres.length; i++) {
            let genre = genres[i];

            let genre_link = '<a href="browse.html#api/genre?genreId=' + genre.id + '">' + genre.name + '</a>';
            genre_entry += genre_link;

            // Add comma and space if it's not the last star
            if (i < genres.length - 1) {
                genre_entry += ", ";
            }
        }

        rowHTML += "<td>" + genre_entry + "</td>";

        let star_id_names = movie.stars.split(";");
        let stars = [];

        // Create star objects and push them into the stars array
        for (let i = 0; i < star_id_names.length; i += 2) {
            let star_id = star_id_names[i];
            let star_name = star_id_names[i + 1];
            stars.push({id: star_id, name: star_name});
        }

        // Sort the stars array by star name in ascending order
        stars.sort((a, b) => a.name.localeCompare(b.name));

        // Initialize the star_entry string
        let star_entry = '';

        // Construct hyperlinks for sorted stars
        for (let i = 0; i < stars.length; i++) {
            let star = stars[i];
            let star_link = '<a href="single-star.html?id=' + star.id + '">' + star.name + '</a>';
            star_entry += star_link;

            // Add comma and space if it's not the last star
            if (i < stars.length - 1) {
                star_entry += ", ";
            }
        }

        // Display hyperlinked star names
        rowHTML += "<td>" + star_entry + "</td>";
        rowHTML += "<td>" + movie.rating + "</td>";
        rowHTML += "</tr>";

        // Append row to table
        $("#movie_table_body").append(rowHTML);
    }
}

function nextPage() {
    currentPage++; // Increment current page number
    fetchSearchResults(); // Submit the form with updated page number
}

// Function to handle "Prev" button click
function prevPage() {
    if (currentPage > 1) {
        currentPage--; // Decrement current page number if not already on the first page
        fetchSearchResults();
    }
}
$("#nextBtn").click(nextPage);
$("#prevBtn").click(prevPage);
var currentPage = 1;

$(document).ready(function() {
    fetchSearchResults(); // Call fetchSearchResults directly
});

function fetchSearchResults(){
        // Extract search parameters from the URL
        var urlParams = new URLSearchParams(window.location.search);
        var genreId = urlParams.get('genreId'); // Check if genreId parameter exists
        var character =  urlParams.get('character');
        var sortAttribute = urlParams.get('sortAttribute');
        var recordsPerPage = urlParams.get('recordsPerPage');
        if (genreId || character) {
            console.log(genreId);
            console.log("before going into fetch movies by genre");
            fetchMoviesByGenre(genreId, character, sortAttribute, recordsPerPage);
            return;
        }
        var title = urlParams.get('title');
        var year = urlParams.get('year');
        var director = urlParams.get('director');
        var starName = urlParams.get('starName');

        var formData = {};
        if(title){ formData["title"]=title;}
        if(year){ formData["year"]=year; }
        if(director){ formData["director"]=director; }
        if(starName){ formData["starName"]=starName; }
        if(sortAttribute){ formData["sortAttribute"]=sortAttribute; }
        if(recordsPerPage){ formData["recordsPerPage"]=recordsPerPage; }
        formData["page"]=currentPage;

        console.log(formData);

        // Make AJAX request to fetch search results with updated page number
        $.ajax({
            url: "api/search", // Update with your API endpoint
            method: "GET",
            dataType: "json",
            data: formData,
            success: function(resultData) {
                // Populate the table with search results
                $("#noResultsMessage").hide();
                console.log(formData);
                console.log("sucesss");

                populateTable(resultData);
            },
            error: function(xhr, status, error) {
                $("#noResultsMessage").show();
                console.error("Error occurred while fetching search results:", error);
            }
        });
    }


// Function to populate the table with search results
function fetchMoviesByGenre(genreId, character, sortAttribute, recordsPerPage) {
    //Make AJAX request to fetch movies by genreId
    formData = {};
    if(genreId){
        formData["genreId"] = genreId;
    }
    if(character){
        formData["character"] = character;
    }
    formData["sortAttribute"] = sortAttribute;
    formData["recordsPerPage"] = recordsPerPage;
    formData["page"]=currentPage;

    console.log("formData: ", formData);
    $.ajax({
        url: "api/browse",
        method: "GET",
        dataType: "json",
        data: formData,
        success: function (resultData) {
            // Populate the table with genre results
            console.log("success");
            populateTable(resultData);
        },
        error: function (xhr, status, error) {
            console.log(genreId);
            console.error("Error occurred while fetching genre movies:", error);
        }
    });
}
function populateTable(resultData) {
    console.log("Result data:", resultData);
    var table = $("#movie_table_body");
    var tableHeadings = $("#movie_table thead");

    // Clear existing table rows
    table.empty();
    var noResultsMessage = $("#noResultsMessage");

    if (!resultData || !resultData.movies || resultData.movies.length === 0) {
        console.log("No movies found.");
        // Hide the table and show the no results message
        noResultsMessage.show();
        table.hide();
        tableHeadings.hide()
        return;

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

        let genreSpan = $("<span>");

        for (let i = 0; i < Math.min(3, genres.length); i++) {
            let genre = genres[i];

            let genreLink = $("<a class='browse-link'>")
                .attr("href", "movie.html?genreId=" + genre.id)
                .text(genre.name);
            // Append dropdown option values to the genre links
            let sortAttribute = $("#sortAttribute").val();
            let moviesPerPage = $("#moviesPerPage").val();
            let urlParams = "&sortAttribute=" + encodeURIComponent(sortAttribute)
                + "&recordsPerPage=" + encodeURIComponent(moviesPerPage);
            genreLink.attr("href", genreLink.attr("href") + urlParams);

            genreSpan.append(genreLink);
            if (i < genres.length - 1) {
                genreSpan.append(", ");
            }
        }
        let genreSpanHTML = genreSpan.prop('outerHTML');
        rowHTML += "<td>" + genreSpanHTML + "</td>";

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

        rowHTML += '<td> <button type="button" class="btn btn-outline-secondary" ' +
            'onclick="addToCart(\'' + movie.id + '\', this)"> Add </button> </td>';

        rowHTML += "</tr>";


        // Append row to table
        $("#movie_table_body").append(rowHTML);
    }
}


function addToCart(movieId, buttonElement) {
    // Create a JSON object containing the movie ID
    let data = {
        "movieId": movieId,
        "action": "increment"
    };

    // Save the original text of the button
    let originalText = "Add";

    // Send an AJAX POST request to your backend API to add the movie to the cart
    $.ajax({
        type: "POST",
        url: "api/cart", // Replace this with the actual endpoint of your backend API
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(response) {
            // Handle the success response from the server
            console.log("Movie successfully added to cart");

            // Change the color of the button when successfully added to cart
            $(buttonElement).removeClass("btn-outline-secondary").addClass("btn-success");
            $(buttonElement).text("Added");

            // Revert the color back to original after 1 second
            setTimeout(function() {
                $(buttonElement).removeClass("btn-success").addClass("btn-outline-secondary");
                $(buttonElement).text(originalText);
            }, 500);

        },
        error: function(xhr, status, error) {
            // Handle errors if any
            console.error("Error adding movie to cart:", error);
        }
    });
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

$(document).ready(function() {
    $("#sortForm").submit(submitSortForm);
});

function submitSortForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Extract selected values from the dropdowns
    var sortAttribute = $('#sortAttribute').val();
    var moviesPerPage = $('#moviesPerPage').val();

    // Construct the new URL with updated parameters
    var currentUrl = window.location.href;
    var updatedUrl = updateQueryStringParameter(currentUrl, 'sortAttribute', sortAttribute);
    updatedUrl = updateQueryStringParameter(updatedUrl, 'recordsPerPage', moviesPerPage);

    // Redirect to the updated URL
    window.location.href = updatedUrl;

    currentPage = 1;
    //fetchSearchResults();
}

// Function to update query string parameters in a URL
function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    var separator = uri.indexOf('?') !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    }
    else {
        return uri + separator + key + "=" + value;
    }
}
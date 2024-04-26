// Function to handle form submission
// Function to handle form submission
function submitSearchForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    var formData = {
        "title": $("#inputTitle").val(),
        "year": $("#inputYear").val(),
        "director": $("#inputDirector").val(),
        "starName": $("#inputStar").val()
    };
    // Remove empty values from form data
    Object.keys(formData).forEach(function(key) {
        if (formData[key] === "") {
            delete formData[key];
        }
    });
    // Convert form data object to query string
    var queryString = $.param(formData);

    // Construct URL with form data
    var url = "api/search";
    if (queryString) {
        url += "?" + queryString;
    }
    console.log("Constructed URL:", url); // Log the constructed URL

    // Perform AJAX request to fetch search results
    $.ajax({
        url: url,
        method: "GET",
        dataType: "json",
        success: function(resultData) {
            // Handle the received data
            handleMovieData(resultData);
        },
        error: function(xhr, status, error) {
            console.error("Error occurred while fetching search results:", error);
        }
    });
}

// Function to handle movie data and update the table
function handleMovieData(resultData) {
    console.log("handleMovieData: populating movie table from resultData");
    console.log(resultData);

    // Clear existing table rows
    $("#movie_table_body").empty();

    // Iterate through search results and populate table
    resultData.movies.forEach(function(movie) {
        // Construct HTML for table row
        var rowHTML = "<tr>";
        rowHTML += "<td>" + movie.title + "</td>";
        rowHTML += "<td>" + movie.year + "</td>";
        rowHTML += "<td>" + movie.director + "</td>";

        let sortedGenres = movie.genres.split(', ').sort().join(', ');
        rowHTML += "<td>" + sortedGenres + "</td>";

        let star_id_names = movie.stars.split(";");
        let stars = [];

        // Create star objects and push them into the stars array
        for (let i = 0; i < star_id_names.length; i += 2) {
            let star_id = star_id_names[i];
            let star_name = star_id_names[i + 1];
            stars.push({ id: star_id, name: star_name });
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
    });
}

// Event listener for form submission
$(document).ready(function() {
    $("#searchForm").submit(submitSearchForm);
});

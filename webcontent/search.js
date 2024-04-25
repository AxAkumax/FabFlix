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
        rowHTML += "<td>" + movie.genres + "</td>";
        // Display only star names
        rowHTML += "<td>" + movie.stars.split(';').join(', ') + "</td>";
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

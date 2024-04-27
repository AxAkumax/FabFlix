// Function to handle form submission
// Function to handle form submission
function submitSearchForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    var formData = {
        "title": $("#inputTitle").val(),
        "year": $("#inputYear").val(),
        "director": $("#inputDirector").val(),
        "starName": $("#inputStar").val(),
        "sortAttribute": $("#sortAttribute").val() // Get the value of sortAttribute
        // "sortOrder": $("#sortOrder").val()
    };
    // Remove empty values from form data
    Object.keys(formData).forEach(function(key) {
        if (formData[key] === "") {
            delete formData[key];
        }
    });

    if (Object.keys(formData).length === 1) {
        // Display a message or perform any other action indicating that the form is empty
        $("#movie_table_body").empty();
        $("#movie_table thead").hide();
        $("#noResultsMessage").hide();
        $("#nParametersMessage").show();

        return; // Exit the function
    }

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

    $("#nParametersMessage").hide();

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


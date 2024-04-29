// Function to handle form submission
// Global variable to store the current page number
var currentPage = 1;

// Function to handle form submission
function submitSearchForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object

    var formData = {
        "title": $("#inputTitle").val(),
        "year": $("#inputYear").val(),
        "director": $("#inputDirector").val(),
        "starName": $("#inputStar").val(),
        "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        page: currentPage, // Use the current page number
        recordsPerPage: $("#moviesPerPage").val()
    };

    // Remove empty values from form data
    Object.keys(formData).forEach(function(key) {
        if (!formData[key] && formData[key] !== 0) { // Also check for 0, as it's a valid value
            delete formData[key];
        }
    });

    if (Object.keys(formData).length === 3) {
        // Display a message or perform any other action indicating that the form is empty
        $("#movie_table_body").empty();
        $("#movie_table thead").hide();
        $("#noResultsMessage").hide();
        $("#prevNextButton").hide();
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

    // Perform AJAX request to fetch search results
    $.ajax({
        url: url,
        method: "GET",
        dataType: "json",
        success: function(resultData) {
            redirectToNewPage(formData);

        },
        error: function(xhr, status, error) {
            console.error("Error occurred while fetching search results:", error);
        }
    });
}
function redirectToNewPage(formData) {
    var url = "movie.html"; // Replace with the actual filename of the new HTML file

    // Convert form data object to query string
    var queryString = $.param(formData);

    // If there are parameters, append them to the URL
    if (queryString) {
        url += "?" + queryString;
    }

    // Redirect to the new page
    window.location.href = url;
}

//
// // Event listener for form submission
 $(document).ready(function() {
     $("#searchForm").submit(submitSearchForm);
 });
//     // Event listener for "Next" button click
//     $("#nextBtn").click(nextPage);
//
//     // Event listener for "Prev" button click
//     $("#prevBtn").click(prevPage);


// // Function to handle "Next" button click
// function nextPage() {
//     currentPage++; // Increment current page number
//     submitSearchForm(new Event('submit')); // Submit the form with updated page number
// }
//
// // Function to handle "Prev" button click
// function prevPage() {
//     if (currentPage > 1) {
//         currentPage--; // Decrement current page number if not already on the first page
//         submitSearchForm(new Event('submit')); // Submit the form with updated page number
//     }
// }
//
// // Function to handle movie data and update the table
// function handleMovieData(resultData) {
//     console.log("handleMovieData: populating movie table from resultData");
//     console.log(resultData);
//
//     $("#nParametersMessage").hide();
//
//     var table = $("#movie_table_body");
//     var tableHeadings = $("#movie_table thead");
//
//     // Clear existing table rows
//     table.empty();
//     var noResultsMessage = $("#noResultsMessage");
//
//     // Check if the movies array is empty
//     if (resultData.movies.length === 0) {
//         console.log("No movies found.");
//         // Hide the table and show the no results message
//         table.hide();
//         tableHeadings.hide()
//         noResultsMessage.show();
//         return; // Exit the function early if no movies are found
//     }
//
//     noResultsMessage.hide();
//     table.show();
//     tableHeadings.show()
//     $("#prevNextButton").show()
//     // Iterate through search results and populate table
//     console.log(resultData);
//
//     var startIndex = (currentPage - 1) * $("#moviesPerPage").val() + 1
//     for (let j = 0; j < resultData.movies.length; j++) {
//         // Construct HTML for table row
//         let movie = resultData.movies[j];
//
//         var rowHTML = "<tr>";
//
//         let movie_link = '<a href="single-movie.html?id=' + movie.id + '">' + movie.title + '</a>';
//
//         rowHTML += "<td>" + movie_link + "</td>";
//         rowHTML += "<td>" + movie.year + "</td>";
//         rowHTML += "<td>" + movie.director + "</td>";
//
//         let genre_id_names = movie.genres.split(";");
//         let genres = [];
//
//         for (let i = 0; i < genre_id_names.length; i += 2) {
//             let genre_id = genre_id_names[i];
//             let genre_name = genre_id_names[i + 1];
//             genres.push({id: genre_id, name: genre_name});
//         }
//         //sorted alphabetically
//         genres.sort((a, b) => a.name.localeCompare(b.name));
//
//         let genre_entry = "";
//
//         for (let i = 0; i < genres.length; i++) {
//             let genre = genres[i];
//
//             let genre_link = '<a href="browse.html#api/genre?genreId=' + genre.id + '">' + genre.name + '</a>';
//             genre_entry += genre_link;
//
//             // Add comma and space if it's not the last star
//             if (i < genres.length - 1) {
//                 genre_entry += ", ";
//             }
//         }
//
//         rowHTML += "<td>" + genre_entry + "</td>";
//
//         let star_id_names = movie.stars.split(";");
//         let stars = [];
//
//         // Create star objects and push them into the stars array
//         for (let i = 0; i < star_id_names.length; i += 2) {
//             let star_id = star_id_names[i];
//             let star_name = star_id_names[i + 1];
//             stars.push({id: star_id, name: star_name});
//         }
//
//         // Sort the stars array by star name in ascending order
//         stars.sort((a, b) => a.name.localeCompare(b.name));
//
//         // Initialize the star_entry string
//         let star_entry = '';
//
//         // Construct hyperlinks for sorted stars
//         for (let i = 0; i < stars.length; i++) {
//             let star = stars[i];
//             let star_link = '<a href="single-star.html?id=' + star.id + '">' + star.name + '</a>';
//             star_entry += star_link;
//
//             // Add comma and space if it's not the last star
//             if (i < stars.length - 1) {
//                 star_entry += ", ";
//             }
//         }
//
//         // Display hyperlinked star names
//         rowHTML += "<td>" + star_entry + "</td>";
//         rowHTML += "<td>" + movie.rating + "</td>";
//         rowHTML += "</tr>";
//
//         // Append row to table
//         $("#movie_table_body").append(rowHTML);
//         startIndex+=1;
//     }
//
// }
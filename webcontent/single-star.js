/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page

    let content = "";
    content+="<h2>" + "<span style='color: #bb86fc;'>" + resultData[0]["star_name"] + "</span></h2>";
    let dob = "";
    if (resultData[0]["star_dob"]==null){
        dob = "N/A";
    }
    else{
        dob = resultData[0]["star_dob"];
    }
    content+= "<p>Date Of Birth: " + dob + "</p>";
    starInfoElement.append(content);

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i <  resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";

        let genre_id_names = resultData[i]["movie_genres"].split(";");

        let genres = [];

        for (let i = 0; i < genre_id_names.length; i += 2) {
            let genre_id = genre_id_names[i];
            let genre_name = genre_id_names[i + 1];
            genres.push({id: genre_id, name: genre_name});
        }
        //sorted alphabetically
        genres.sort((a, b) => a.name.localeCompare(b.name));

        let genreSpan = $("<span>");

        for (let i = 0; i <  genres.length; i++) {
            let genre = genres[i];

            let genreLink = $("<a class='browse-link'>")
                .attr("href", "movie.html?genreId=" + genre.id)
                .text(genre.name);
            // Append dropdown option values to the genre links
            let sortAttribute = "title ASC, average_rating ASC";
            let moviesPerPage = "10";
            let urlParams = "&sortAttribute=" + encodeURIComponent(sortAttribute)
                + "&recordsPerPage=" + encodeURIComponent(moviesPerPage);
            genreLink.attr("href", genreLink.attr("href") + urlParams);

            genreSpan.append(genreLink);
            if (i < genres.length - 1) {
                genreSpan.append(", ");
            }
        }
        let genreSpanHTML = genreSpan.prop('outerHTML');

        rowHTML +="<td>" + genreSpanHTML + "</td>";

        rowHTML += '<td> <button type="button" class="btn btn-outline-secondary" ' +
            'onclick="addToCart(\'' + resultData[i]['movie_id'] + '\')"> Add </button> </td>';

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
function addToCart(movieId) {
    // Create a JSON object containing the movie ID
    let data = {
        "movieId": movieId,
        "action": "increment"
    };

    // Send an AJAX POST request to your backend API to add the movie to the cart
    $.ajax({
        type: "POST",
        url: "api/cart", // Replace this with the actual endpoint of your backend API
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(response) {
            // Handle the success response from the server
            console.log("Movie successfully added to cart");
            // Optionally, can redirect the user to the shopping cart page after adding the movie
        },
        error: function(xhr, status, error) {
            // Handle errors if any
            console.error("Error adding movie to cart:", error);
        }
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

// Function to retrieve the most recent URL from session storage
function getRecentURL() {
    return sessionStorage.getItem('recentURL');
}

console.log(getRecentURL());

document.getElementById('goBack').addEventListener('click', () => {
var recentURL = getRecentURL();
if (recentURL) {
    // Redirect back to the recent URL (movie page)
    window.location.href = recentURL;
} else {
    // Handle case when no recent URL is stored
}
});


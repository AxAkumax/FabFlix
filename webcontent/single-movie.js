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
    console.log(resultData);
    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    let content = "";
    content+="<h2>" + "<span style='color: #bb86fc;'>" + resultData[0]["movie_title"] + "</span></h2>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        "<p>Rating: " + resultData[0]["average_rating"] + "</p>" +
        "<p>Year: " + resultData[0]["movie_year"] + "</p>";

        // "<p>Genres: " + resultData[0]["movie_genres"] + "</p>";
        let genre_id_names = resultData[0]["movie_genres"].split(";");

        let genres = [];

        for (let i = 0; i < genre_id_names.length; i += 2) {
            let genre_id = genre_id_names[i];
            let genre_name = genre_id_names[i + 1];
            genres.push({id: genre_id, name: genre_name});
        }
        //sorted alphabetically
        genres.sort((a, b) => a.name.localeCompare(b.name));


        let genreSpan = $("<span>");
        for (let i = 0; i < genres.length; i++) {
            let genre_id = genres[i].id;
            let genre_name = genres[i].name;

            let genreLink = $("<a class='browse-link'>")
                .attr("href", "movie.html?genreId=" + genre_id)
                .text(genre_name);

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
        content+= "<p>Genres: "+genreSpanHTML+"</p>";

        let star_id_names = resultData[0]["movie_stars"].split(";");
    let stars = [];

    // Create star objects and push them into the stars array
    for (let i = 0; i <  star_id_names.length; i += 3) {
        let star_id = star_id_names[i];
        let star_name = star_id_names[i + 1];
        let total_movies = star_id_names[i + 2];
        stars.push({ id: star_id, name: star_name, total_movies: total_movies });
    }

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
    //Display hyperlinked star names
    content += "<p id = 'stars'> Stars: " + star_entry + "</p>";

    // delete button
    content += '<p> <button type="button" class="btn btn-outline-secondary" ' +
        'onclick="addToCart(\'' + resultData[0]["movie_id"] + '\')"> Add to Cart </button> </p>';


    starInfoElement.append(content);

    /* '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
            + resultData[i]["star_name"] +     // display star_name for the link text
            '</a>'
     */
    console.log("handleResult: populating movie table from resultData");
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
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

window.addEventListener('beforeunload', () => {
    sessionStorage.setItem('previousPage', window.location.href);
});

// Function to navigate back to the previous page
document.getElementById('goBack').addEventListener('click', () => {
    const previousPage = sessionStorage.getItem('previousPage') || '/';
    window.location.href = previousPage;
});
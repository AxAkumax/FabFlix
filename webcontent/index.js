/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populates top 20 movies
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("Populating top 20 movies");
    console.log(resultData);

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        let rowHTML = "";

        rowHTML += "<tr>";

        // Column: row number
        rowHTML +="<th>"+(i + 1).toString()+"</th>"; // Adding row number

        // Column: movie name
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";

        // Column: movie year
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        // Splitting up star names and star ids
        let star_ids = resultData[i]['movie_starIds'].split(';');
        let star_names = resultData[i]['movie_stars'].split(';');


        let star_entries = "";

        // Iterate through stars, no more than 3 actors in the column
        let length  = Math.min(3, star_ids.length);
        for (let j = 0; j < length; j++) {

            star_entries +=
                // Add a link to single-star.html with id passed with GET url parameter
                '<a href="single-star.html?id=' + star_ids[j] + '">'
                + star_names[j] +     // display star_name for the link text
                '</a>';
            if (j< length-1){
                star_entries+=", ";
            }
        }

        // Column: 3 movie stars
        rowHTML += "<th>" + star_entries + "</th>";

        // Column: 3 genres of the movie
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";

        // Column: average rating of the movie
        rowHTML += "<th>" + resultData[i]["average_rating"] + "</th>";

        // Column: add to cart button
        rowHTML += '<th> <button type="button" class="btn btn-outline-secondary" ' +
            'onclick="addToCart(\'' + resultData[i]["movie_id"] + '\')"> Add </button> </th>';

        rowHTML += "</tr>";

        // Append the row created to the table body
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Handles the add movie to cart functionality
 **/
function addToCart(movieId) {

    // Create a JSON object containing the movie ID
    let data = {
        "movieId": movieId,
        "action": "increment"
    };

    // Send an AJAX POST request to apo/cart to add the movie to the cart
    $.ajax({
        method: "POST",                         // sending request as POST
        url: "api/cart",                        // api servlet (ShoppingServlet)
        contentType: "application/json",        // sending content as json
        data: JSON.stringify(data),
        success: function(response) {
            console.log("Movie successfully added to cart from index.js");
            console.log(response);
        },
        error: function(xhr, status, error) {
            console.error("Error adding movie to cart from index.js: ", error);
        }
    });
}


/**
 * Once this index.js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MovieServlet
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

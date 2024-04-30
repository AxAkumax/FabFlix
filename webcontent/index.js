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
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    console.log(resultData);

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");


    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        console.log(resultData[i]);
        // Concatenate the html tags with resultData jsonObject

        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +="<th>"+(i + 1).toString()+"</th>"; // Adding row number
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        let star_ids = resultData[i]['movie_starIds'].split(';');
        let star_names = resultData[i]['movie_stars'].split(';');

        // Iterate through resultData, no more than 10 entries
        let star_entries = "";

        let length  = Math.min(3, star_ids.length);
        for (let j = 0; j < length; j++) {

            // Concatenate the html tags with resultData jsonObject

            star_entries +=
                // Add a link to single-star.html with id passed with GET url parameter
                '<a href="single-star.html?id=' + star_ids[j] + '">'
                + star_names[j] +     // display star_name for the link text
                '</a>';
            if (j< length-1){
                star_entries+=", ";
            }
        }
        rowHTML += "<th>" + star_entries + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        rowHTML += "<th>" + resultData[i]["average_rating"] + "</th>";

        rowHTML += '<th> <button type="button" class="btn btn-outline-secondary" ' +
            'onclick="addToCart(\'' + resultData[i]["movie_id"] + '\')"> Add </button> </th>';

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


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

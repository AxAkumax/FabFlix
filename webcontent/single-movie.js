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
    content+="<h2>Movie Title: " + resultData[0]["movie_title"] + "</h2>" +
    "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
    "<p>Rating: " + resultData[0]["average_rating"] + "</p>" +
    "<p>Year: " + resultData[0]["movie_year"] + "</p>" +
    "<p>Genres: " + resultData[0]["movie_genres"] + "</p>";

    let star_ids = resultData[0]['movie_starIds'].split(';');
    let star_names = resultData[0]['movie_stars'].split(';');
    let star_entries = "";
    let length  = star_ids.length;
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
    content += "<p>Stars: "+star_entries+"</p>";

    starInfoElement.append(content);

    /* '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
            + resultData[i]["star_name"] +     // display star_name for the link text
            '</a>'
     */
    console.log("handleResult: populating movie table from resultData");
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
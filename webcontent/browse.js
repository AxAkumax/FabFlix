
function handleBrowseResult(resultData) {

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse", // Setting request url, which is mapped by BrowseServlet in BrowseServlet.java
    success: (resultData) => handleBrowseResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
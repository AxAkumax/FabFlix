function submitSearchForm() {
    // Retrieve form data
    var title = document.getElementById("inputTitle").value;
    var year = document.getElementById("inputYear").value;
    var director = document.getElementById("inputDirector").value;
    var starName = document.getElementById("inputStar").value;

    // Construct URL with form data
    var url = "api/search?title=" + encodeURIComponent(title) +
        "&year=" + encodeURIComponent(year) +
        "&director=" + encodeURIComponent(director) +
        "&starName=" + encodeURIComponent(starName);

    // Redirect to the URL
    window.location.href = url;
}

function handleMovieData(resultData) {
    console.log("handleMovieData: populating movie table from resultData");
    console.log(resultData);

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Clear existing content
    movieTableBodyElement.empty();

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the HTML tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + resultData[i]["movie_title"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_genres"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_stars"] + "</td>";
        rowHTML += "<td>" + resultData[i]["average_rating"] + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body
        movieTableBodyElement.append(rowHTML);
    }
}

// // Perform initial search when the page loads
// jQuery.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/search", // Setting request url, which is mapped by StarsServlet in Stars.java
//     success: (resultData) => handleMovieData(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
// });
document.getElementById("searchForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent default form submission behavior
    submitSearchForm(); // Call function to handle form submission
});
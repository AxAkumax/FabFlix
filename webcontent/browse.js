let genreId = null;
let titleStart = null;


// Function to create browsing results


function create_browsing_result(url) {
    // Make AJAX request to fetch genre movies
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: function(resultData) {
            // Handle successful response
            let browsingBodyElement = jQuery("#browsing_table_body");

            // Clear previous results
            browsingBodyElement.empty();

            for (let i = 0; i < resultData.length; i++) {
                let rowHTML = ""
                rowHTML += "<tr>";
                rowHTML += "<th>" + (i+1) + "</th>";

                rowHTML +=
                    "<th>" +
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                    + resultData[i]["movie_title"] +     // display movie title for the link text
                    '</a>' +
                    "</th>";

                rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

                let star_ids = resultData[i]['movie_starIds'].split(', ');
                let star_names = resultData[i]['movie_stars'].split(', ');

                let star_entries = "";

                let length  = Math.min(3, star_ids.length);
                for (let j = 0; j < length; j++) {
                    // Concatenate the html tags with resultData jsonObject
                    star_entries +=
                        // Add a link to single-star.html with id passed with GET url parameter
                        '<a href="single-star.html?id=' + star_ids[j] + '">'
                        + star_names[j] +     // display star_name for the link text
                        '</a>';
                    if (j < length - 1){
                        star_entries += ", ";
                    }
                }

                rowHTML += "<th>" + star_entries + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
                rowHTML += "<th>" + resultData[i]["average_rating"] + "</th>";
                rowHTML += "</tr>";
                browsingBodyElement.append(rowHTML)
            }

        },
        error: function(xhr, status, error) {
            // Handle error
            console.error("Error fetching genre movies:", error);
        }
    });
}

    // Call the function to fetch and display browsing results

function handleBrowseClick(event) {
    // Prevent default link behavior
    event.preventDefault();

    console.log(this);

    genreId = jQuery(this).data("genre-id");
    titleStart = jQuery(this).data("alphabet");

    // Manually construct form data object
    if (genreId !== undefined) {
        var formData = {
            "genreId": jQuery(this).data("genre-id"),
            "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        };
    }
    else if (titleStart !== undefined) {
        var formData = {
            "titleStart": jQuery(this).data("alphabet"),
            "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        };
    }

    // Convert form data object to query string
    var queryString = $.param(formData);

    // Construct URL with form data
    var url = "api/browse";
    if (queryString) {
        url += "?" + queryString;
    }

    console.log("Constructed URL:", url); // Log the constructed URL


    create_browsing_result(url);
}

// Function to populate genre names and alphabets for browsing
function handleBrowseResult(resultData) {
    let genresElement = jQuery("#genre_table_body");
    let row = "";

    // Iterate through the resultData array
    for (let i = 0; i < resultData.length; i++) {
        // Add a new row for every fifth genre or at the beginning of the loop
        if (i % 5 === 0) {
            // Close the previous row if it exists
            if (i !== 0) {
                row += "</tr>";
            }
            // Start a new row
            row += "<tr>";
        }

        row += "<td>" +
            '<a href="#" class="genre-link" data-genre-id="' + resultData[i]['genreId'] + '">' +
            resultData[i]["genreName"] +     // display genreName for the link text
            '</a>' +
            "</td>";
    }

    // Close the last row
    row += "</tr>";

    // Append the constructed row to the genresElement
    genresElement.append(row);

    let alphabetElement = jQuery("#alphabet_table_body");
    let row2 = "";
    let resultData2 = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0',
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '*']

    // Iterate through the resultData array
    for (let i = 0; i < resultData2.length; i++) {
        // Add a new row for every eighth alphabet or at the beginning of the loop
        if (i % 8 === 0) {
            // Close the previous row if it exists
            if (i !== 0) {
                row2 += "</tr>";
            }
            // Start a new row
            row2 += "<tr>";
        }

        row2 += "<td>" +

            '<a href="#" class="alphabet-link" data-alphabet="' + resultData2[i] + '">' +
            resultData2[i] +     // display alphabet for the link text
            '<a href="#" id = "alphabet_click" class="alphabet-link" data-alphabet="' + resultData2[i] + '">' +
            resultData2[i] +     // display genreName for the link text
            '</a>' +
            "</td>";
    }

    // Close the last row
    row2 += "</tr>";

    // Append the constructed row to the alphabetElement
    alphabetElement.append(row2);


    // Add click event listener to genre and alphabet links


    // Add click event listener to genre links
    jQuery(".genre-link").click(handleBrowseClick);
    jQuery(".alphabet-link").click(handleBrowseClick);
}

// Makes the HTTP GET request and registers on success callback function handleBrowseResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/browse",
    success: (resultData) => handleBrowseResult(resultData)
});



// Function to handle change in the dropdown menu
function handleDropdownChange() {
    var selectedOption = $(this).val(); // Get the selected option value

    // Manually construct form data object
    if (genreId !== undefined) {
        var formData = {
            "genreId": genreId,
            "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        };
    }


    else if (titleStart !== undefined) {

        var formData = {
            "titleStart": titleStart,
            "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        };
    }

    console.log(genreId);
    console.log(titleStart);

    // Convert form data object to query string
    var queryString = $.param(formData);

    // Construct URL with form data
    var url = "api/browse";
    if (queryString) {
        url += "?" + queryString;
    }

    create_browsing_result(url);

}
$("#sortAttribute").change(handleDropdownChange);


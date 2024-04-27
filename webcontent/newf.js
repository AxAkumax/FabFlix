function handleGenreClick(event) {
    // Prevent default link behavior
    event.preventDefault();

    let genreId = jQuery(this).data("genre-id");
    let titleStart = jQuery(this).data("alphabet");

    // Manually construct form data object
    if (genreId != undefined) {
        var formData = {
            "genreId": jQuery(this).data("genre-id"),
            "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
        };
    }
    else if (titleStart != undefined) {
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
                    // Add a link to single-star.html with id passed with GET url parameter
                    '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                    + resultData[i]["movie_title"] +     // display star_name for the link text
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
                    if (j< length-1){
                        star_entries+=", ";
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
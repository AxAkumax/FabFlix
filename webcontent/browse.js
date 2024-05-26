// Makes the HTTP GET request and registers on success callback function handleGenreResult
function handleResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    console.log(resultData);

    let genreTableBodyElement = jQuery("#genre_table_body");
    genreTableBodyElement.empty();
    let genreSpan = $("<span>");
    for (let i = 0; i < resultData.length; i++) {
        let genre_id = resultData[i].genre_id;
        let genre_name = resultData[i].genre_name;

        let genreLink = $("<a class='browse-link'>")
            .attr("href", "movie.html?genreId=" + genre_id)
            .text(genre_name);

        // Append dropdown option values to the genre links
        let sortAttribute = "title ASC, average_rating ASC";
        let moviesPerPage = "10";
        let page = "1";
        let urlParams = "&sortAttribute=" + encodeURIComponent(sortAttribute) + "&page="+ encodeURIComponent(page)
            + "&recordsPerPage=" + encodeURIComponent(moviesPerPage);
        genreLink.attr("href", genreLink.attr("href") + urlParams);

        genreSpan.append(genreLink);
        if (i < resultData.length - 1) {
            genreSpan.append(", ");
        }
    }
    genreTableBodyElement.append(genreSpan);

    let alphabet_table_body = jQuery("#alphabet_table_body");
    alphabet_table_body.empty();
    let alphaSpan = $("<span>");
    let letters = ["0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","H","I","J","K","L","M","N","O","P",
    "Q","R","S","T","U","V","W","X","Y","Z","*"];
    for(let i=0; i<letters.length; i++){
        // let alphalink = $("<a class='browse-link'>").attr("href", "movie.html?character=" + letters[i]).text(letters[i]);
        // alphaSpan.append(alphalink);

        let alphalink = $("<a class='browse-link'>")
            .attr("href", "movie.html?character=" + letters[i]).text(letters[i]);

        // Append dropdown option values to the genre links
        let sortAttribute = "title ASC, average_rating ASC";
        let moviesPerPage = "10";
        let page = "1";
        let urlParams = "&sortAttribute=" + encodeURIComponent(sortAttribute)+ "&page="+ encodeURIComponent(page)
            + "&recordsPerPage=" + encodeURIComponent(moviesPerPage);
        alphalink.attr("href", alphalink.attr("href") + urlParams);

        alphaSpan.append(alphalink);


        if (i < letters.length - 1) {
            alphaSpan.append(", ");
        }
    }
    alphabet_table_body.append(alphaSpan);

    addDropdownListeners();
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genre", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData)=>{
    handleResult(resultData);
    }// Setting callback function to handle data returned successfully by the StarsServlet
});


document.addEventListener('DOMContentLoaded', function () {
    const searchForm = document.getElementById('search-form');
    const searchInput = document.getElementById('search-input');
    const searchButton = document.getElementById('search-button');

    // Handle form submission
    searchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const query = searchInput.value.trim();
        //full-text search
        handleNormalSearch(query);
    });
});

//autocomplete------
$('#search-input').autocomplete({
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function (suggestion) {
        handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300,
    formatResult: function (suggestion, currentValue) {
        // Highlight matched words in suggestion value
        const currentValueWords = currentValue.split(' ').map($.Autocomplete.utils.escapeRegExChars);
        const regex = new RegExp('(' + currentValueWords.join('|') + ')', 'gi');
        const highlightedValue = suggestion.value.replace(regex, '<span class="highlight">$1</span>');
        return '<div>' + highlightedValue + '</div>';
    }
});

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated");

    if (query.length < 3) {
        doneCallback({ suggestions: [] });
        return;
    }

    // Check if the query result is in the cache
    const cachedData = sessionStorage.getItem(query);
    if (cachedData) {
        console.log("Using cached data for query:", query);
        doneCallback({ suggestions: JSON.parse(cachedData) });
        return;
    }

    console.log("sending AJAX request to backend Java Servlet");
    jQuery.ajax({
        method: "GET",
        url: "movie-suggestion?query=" + escape(query),
        success: function (data) {
            handleLookupAjaxSuccess(data, query, doneCallback);
        },
        error: function (errorData) {
            console.log("lookup ajax error");
            console.log(errorData);
        }
    });
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful");

    try {
        if (Array.isArray(data) && data.length > 0) {
            const suggestions = data.map(item => ({
                value: item.value,
                data: { movieID: item.data.movieID }
            }));

            // Cache the result
            sessionStorage.setItem(query, JSON.stringify(suggestions));
            doneCallback({ suggestions });
        } else {
            doneCallback({ suggestions: [] });
        }
    } catch (error) {
        console.error("Error handling lookup ajax success:", error);
        doneCallback({ suggestions: [] });
    }
}

function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"]);
    // Store the selected movie ID in session storage with the suggestion value as the key
    const selectedMovieData = {
        value: suggestion["value"],
        movieID: suggestion["data"]["movieID"]
    };
    sessionStorage.setItem('selectedMovieData', JSON.stringify(selectedMovieData));
}

// Bind pressing enter key to a handler function
$('#search-input').keypress(function (event) {
    if (event.keyCode == 13) {
        handleNormalSearch($('#search-input').val());
    }
});

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    if (query) {
        const formData = {
            search: query,
            page: '1',
            recordsPerPage: '10',
            sortAttribute: 'title ASC, average_rating ASC'
        };

        const selectedMovieData = JSON.parse(sessionStorage.getItem('selectedMovieData'));
        let url = "";

        if (selectedMovieData && selectedMovieData.value === query) {
            formData.movieID = selectedMovieData.movieID;
        } else {
            // If no selected movie data found, use the cached suggestions
            const cachedSuggestions = JSON.parse(sessionStorage.getItem(query));
            if (cachedSuggestions) {
                const selectedSuggestion = cachedSuggestions.find(suggestion => suggestion.value === query);
                if (selectedSuggestion) {
                    formData.movieID = selectedSuggestion.data.movieID;
                }
            }
        }
        if(formData.movieID){
            url = "single-movie.html?id="+formData.movieID;
        }
        else{
            const queryString = new URLSearchParams(formData).toString();
            url = "movie.html?"+queryString;
        }
        window.location.href = url;

    }
}

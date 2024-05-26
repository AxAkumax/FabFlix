
function showOrderDetails(cart_items) {
    var totalElement = $('#total_price')

    var tableBody = $('#confirmation_movies_table_body');

    // Clear the table body first to avoid duplicating items
    tableBody.empty();

    let grand_total = 0;

    for (let i = 0; i < cart_items.length; i++) {
        let rowHTML = "";

        // id for quantity display
        let quantity_element = cart_items[i]["movieId"] + "_quantity";

        let movie_price = 5;
        let movie_quantity = cart_items[i]["movieQuantity"];
        let total = movie_price * movie_quantity;

        rowHTML += "<tr>";

        // Adding row number
        rowHTML += "<th>" + (i + 1).toString() + "</th>";

        // sale id
        rowHTML += "<th>" + cart_items[i]["saleId"] + "</th>";

        // movie title
        rowHTML += "<th>" + cart_items[i]["movieTitle"] + "</th>";

        // movie quantity + increment/decrement buttons
        rowHTML += "<th>" + movie_quantity + "</th>";

        // movie price
        rowHTML += "<th>" + "$ " + movie_price + "</th>";

        // total price for that item
        rowHTML += "<th>" + "$ " + total + "</th>"

        grand_total += total;

        rowHTML += "</tr>";
        tableBody.append(rowHTML);
    }

    totalElement.text("Total: $ " + grand_total);
}

$.ajax({
    url: "api/confirm",
    method: "POST",
    dataType: "json",
    success: (resultData) => showOrderDetails(resultData)
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
    //console.log("lookup ajax successful");

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
    //console.log("doing normal search with query: " + query);
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
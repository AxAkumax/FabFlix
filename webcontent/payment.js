let total_data = {
    "action": "get_cart_total"
};

function populateTotalField(resultData) {
    let cartTotalElement = $("#cart_total");
    cartTotalElement.text("Cart Total: $ " + resultData["total_amount"]);
}


$.ajax({
    url: "api/payment",
    method: "GET",
    dataType: "json",
    data: total_data,
    success: (resultData) => populateTotalField(resultData)
});


$('#payment_form').submit(function(event) {
    // Prevent the default form submission behavior
    event.preventDefault();

    // Get the values of the input fields
    var firstName = $('#first_name').val();
    var lastName = $('#last_name').val();
    var creditCardNumber = $('#credit_card_number').val();
    var expirationDate = $('#expiration_date').val();
    var expirationMonth = $('#expiration_month').val();
    var expirationYear = $('#expiration_year').val();

    // Log the values for demonstration
    console.log("First Name: " + firstName);
    console.log("Last Name: " + lastName);
    console.log("Credit Card Number: " + creditCardNumber);
    console.log("Expiration Date: " + expirationDate);
    console.log("Expiration Month: " + expirationMonth);
    console.log("Expiration Year: " + expirationYear);

    // perform further actions here, such as sending the data to the server
    let data = {
        "action": "process_payment",
        "firstName": firstName,
        "lastName": lastName,
        "creditCardNumber": creditCardNumber,
        "expirationDate": expirationDate,
        "expirationMonth": expirationMonth,
        "expirationYear": expirationYear
    }

    $.ajax({
        type: "POST",
        url: "api/payment", // Replace this with the actual endpoint of your backend API
        contentType: "application/json",
        data: JSON.stringify(data),
        dataType: "json",
        success: function(response) {
            console.log("successfully sent the credit card info to PaymentServlet");
            handleResult(response)
        },
        error: function(xhr, status, error) {
            // Handle errors if any
            console.error("Error authenticating credit card user:", error);
            // please enter the correct information
            var label = $('#wrong_information_label').val();
            label.text("Unable to authenticate user info. Please enter correct information");
        }
    });
});

function handleResult(response) {
    if (response["result"] == "success") {
        var label = $('#wrong_information_label');
        label.text("");
        window.location.href = "confirmation.html";
    }
    else {
        var label = $('#wrong_information_label');
        label.text("Unable to authenticate user info. Please enter correct information.");
    }
}

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
let slideIndex = 0;
showSlides();

function showSlides() {
    let slides = document.getElementsByClassName("slide");
    for (let i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }
    slideIndex++;
    if (slideIndex > slides.length) {
        slideIndex = 1;
    }
    slides[slideIndex - 1].style.display = "block";
    setTimeout(showSlides, 3000); // Change image every 3 seconds
}

document.addEventListener('DOMContentLoaded', function () {
    const searchForm = document.getElementById('search-form');
    const searchInput = document.getElementById('search-input');
    const searchButton = document.getElementById('search-button');

    // Handle form submission
    searchForm.addEventListener('submit', function (event) {
        event.preventDefault();
        const query = searchInput.value.trim();

        if (query) {
            const formData = {
                search: query,
                page: '1',
                recordsPerPage: '10',
                sortAttribute: 'title ASC, average_rating ASC'
            };

            const queryString = new URLSearchParams(formData).toString();
            const url = `movie.html?${queryString}`;
            console.log(url);
            // Redirect to the new page
            window.location.href = url;
        }
    });
});
//autocomplete------

$('#search-input').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    if(query.length<3){
        return;
    }
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "movie-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful");

    try {
        // Check if data is an array and has length greater than 0
        if (Array.isArray(data) && data.length > 0) {
            const suggestions = data.map(item => ({
                value: item.value, // Assuming 'value' is the property containing movie title
                data: { movieID: item.data.movieID } // Assuming 'data.movieID' is the property containing movie ID
            }));
            doneCallback({ suggestions });
        } else {
            // No suggestions found
            doneCallback({ suggestions: [] });
        }
    } catch (error) {
        // Error occurred
        console.error("Error handling lookup ajax success:", error);
        // Pass an empty suggestions array to the doneCallback
        doneCallback({ suggestions: [] });
    }
}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"])
}

// bind pressing enter key to a handler function
$('#search-input').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#search-input').val())
    }
})

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    if (query) {
        const formData = {
            search: query,
            page: '1',
            recordsPerPage: '10',
            sortAttribute: 'title ASC, average_rating ASC'
        };

        const queryString = new URLSearchParams(formData).toString();
        const url = `movie.html?${queryString}`;
        console.log(url);
        // Redirect to the new page
        window.location.href = url;
    }
}
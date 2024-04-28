$(document).ready(function() {
    // Parse URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const title = urlParams.get('title');
    const year = urlParams.get('year');
    const director = urlParams.get('director');
    const starName = urlParams.get('starName');
    const sortAttribute = urlParams.get('sortAttribute');
    const page = urlParams.get('page');
    const recordsPerPage = urlParams.get('recordsPerPage');

    // Check if any search criteria is present
    if (title || year || director || starName) {
        // Perform AJAX request to fetch search results
        $.ajax({
            url: 'api/movies', // Adjust the URL as per your API endpoint
            type: 'GET',
            dataType: 'json',
            data: {
                title: title,
                year: year,
                director: director,
                starName: starName,
                sortAttribute: sortAttribute,
                page: page,
                recordsPerPage: recordsPerPage
            },
            success: function(response) {
                // Process the response and display search results
                const movies = response.movies;
                if (movies.length > 0) {
                    displaySearchResults(movies);
                } else {
                    // Display message for no results
                    $('#noResultsMessage').show();
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                // Handle errors
                console.error('Error:', textStatus, errorThrown);
            }
        });
    } else {
        // Display message for no parameters given
        $('#nParametersMessage').show();
    }
});

// Function to display search results
function displaySearchResults(movies) {
    const tbody = $('#movie_table_body');
    tbody.empty(); // Clear existing rows
    movies.forEach(function(movie) {
        const row = '<tr>' +
            '<td>' + movie.title + '</td>' +
            '<td>' + movie.year + '</td>' +
            '<td>' + movie.director + '</td>' +
            '<td>' + movie.genres + '</td>' +
            '<td>' + movie.stars + '</td>' +
            '<td>' + movie.rating + '</td>' +
            '</tr>';
        tbody.append(row);
    });
    // Show the search results table
    $('#movie_table').show();
}

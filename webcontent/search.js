$(document).ready(function() {
    // Listen for form submission
    $('#searchForm').submit(function(event) {
        // Prevent default form submission
        event.preventDefault();

        // Retrieve form data
        var formData = {
            title: $('#inputTitle').val(),
            year: $('#inputYear').val(),
            director: $('#inputDirector').val(),
            starName: $('#inputStar').val(),
            sortAttribute: $('#sortAttribute').val(),
            page: 1, // Assuming initial page is 1
            recordsPerPage: $('#moviesPerPage').val()
        };

        // Construct URL with form data as parameters
        var url = 'index.html?' + $.param(formData);

        // Redirect to index.html with form data as URL parameters
        window.location.href = url;
    });
});

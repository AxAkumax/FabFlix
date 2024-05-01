// Function to handle form submission
// Global variable to store the current page number
var currentPage = 1;

// Function to handle form submission
function submitSearchForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    let title =  $("#inputTitle").val();
    let year = $("#inputYear").val();
    let director = $("#inputDirector").val();
    let starName =  $("#inputStar").val();
    let sortAttribute = $("#sortAttribute").val();
    let page = currentPage;
    let recordsPage = $("#moviesPerPage").val();

    var formData={};
    if(title){
        formData["title"] = title;
    }
    if(year){
        formData["year"] = year;
    }
    if(director){
        formData["director"] = director;
    }
    if(starName){
        formData["starName"] = starName;
    }

    formData["sortAttribute"]=sortAttribute;
    formData["page"] = page;
    formData["recordsPerPage"] = recordsPage;

    // var formData = {
    //     "title": $("#inputTitle").val(),
    //     "year": $("#inputYear").val(),
    //     "director": $("#inputDirector").val(),
    //     "starName": $("#inputStar").val(),
    //     "sortAttribute": $("#sortAttribute").val(), // Get the value of sortAttribute
    //     page: currentPage, // Use the current page number
    //     recordsPerPage: $("#moviesPerPage").val()
    // };
    //
    // // Remove empty values from form data
    // Object.keys(formData).forEach(function(key) {
    //     if (!formData[key] && formData[key] !== 0) { // Also check for 0, as it's a valid value
    //         delete formData[key];
    //     }
    // });

    if (formData.length === 3) {
        // Display a message or perform any other action indicating that the form is empty
        $("#nParametersMessage").show();

        return; // Exit the function
    }
    $("#nParametersMessage").hide();
    // Convert form data object to query string
    //var queryString = $.param(formData);
    redirectToNewPage(formData);


    // Construct URL with form data
    // var url = "api/search";
    // if (queryString) {
    //     url += "?" + queryString;
    // }

    // Perform AJAX request to fetch search results
    // $.ajax({
    //     url: "api/search",
    //     method: "GET",
    //     dataType: "json",
    //     data: formData,
    //     success: function(resultData) {
    //
    //         redirectToNewPage(formData);
    //
    //     },
    //     error: function(xhr, status, error) {
    //         console.log(formData);
    //         console.log(url);
    //         console.error("Error occurred while fetching search results:", error);
    //     }
    // });
}
function redirectToNewPage(formData) {
    var url = "movie.html"; // Replace with the actual filename of the new HTML file

    // Convert form data object to query string
    var queryString = $.param(formData);

    // If there are parameters, append them to the URL
    if (queryString) {
        url += "?" + queryString;
    }
    console.log(url);
    // Redirect to the new page
    window.location.href = url;
}

//
// // Event listener for form submission
 $(document).ready(function() {
     $("#searchForm").submit(submitSearchForm);
 });

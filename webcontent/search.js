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


    if (formData.length === 3) {
        // Display a message or perform any other action indicating that the form is empty
        $("#nParametersMessage").show();

        return; // Exit the function
    }
    $("#nParametersMessage").hide();
    // Convert form data object to query string
    //var queryString = $.param(formData);
    redirectToNewPage(formData);

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

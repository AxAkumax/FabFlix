function submitAddMovieForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    let title =  $("#inputTitle").val();
    let year = $("#inputYear").val();
    let director = $("#inputDirector").val();
    let starName =  $("#inputStar").val();
    let birth_year = $("#inputBirthYear").val();
    let genre = $("#inputGenre").val();

    console.log("title",title);
    console.log("year", year);

    var formData= {};
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
    if(genre){
        formData["genre"] = genre;
    }
    if(birth_year){
        formData["birth_year"] = birth_year;
    }

    console.log(formData);
    if (!formData || Object.keys(formData).length <6) {
        // Display a message or perform any other action indicating that the form is empty
        $("#nParametersMessage").show();

        return; // Exit the function
    }
    $("#nParametersMessage").hide();

    $.ajax({
        url: "../api/add-movie", // Update with the correct servlet URL
        method: "GET", // Use POST method for sending form data
        dataType: "json",
        data: formData,
        success: function (responseData) {
            // Check if there is an error message
            if (responseData.error) {
                console.error("Error occurred while adding the movie:", responseData.error);
                // Display error message to the user
                $("#errorMessage").text(responseData.error).show();
            } else {
                // No error, display success message or perform any other action
                console.log("Movie added successfully!");
                $("#successMessage").text("Movie added successfully!").show();
            }
        },
        error: function (xhr, status, error) {
            console.error("Error occurred while adding the movie:", error);
            // Display error message to the user
            // For example: $("#errorMessage").text("An error occurred while adding the movie.").show();
        }
    });
}

// // Event listener for form submission
$(document).ready(function() {
    $("#addMovieForm").submit(submitAddMovieForm);
});

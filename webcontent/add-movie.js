function submitAddMovieForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    let title =  $("#inputTitle").val();
    let year = $("#inputYear").val();
    let director = $("#inputDirector").val();
    let starName =  $("#inputStar").val();
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

    console.log(formData);
    if (!formData || Object.keys(formData).length < 5) {
        // Display a message or perform any other action indicating that the form is empty
        $("#nParametersMessage").show();

        return; // Exit the function
    }
    $("#nParametersMessage").hide();


}

// // Event listener for form submission
$(document).ready(function() {
    $("#addMovieForm").submit(submitAddMovieForm);
});

function submitAddStarForm(event) {
    event.preventDefault(); // Prevent default form submission

    // Manually construct form data object
    let starName =  $("#inputStar").val();
    let birth_year = $("#inputBirthYear").val();

    var formData= {};
    if(starName){
        formData["starName"] = starName;
    }
    if(birth_year){
        formData["birth_year"] = birth_year;
    }

    console.log(formData);
    if (!formData || Object.keys(formData).length < 1) {
        // Display a message or perform any other action indicating that the form is empty
        $("#nParametersMessage").show();

        return; // Exit the function
    }
    $("#nParametersMessage").hide();
    $("#successMessage").hide();
    $("#starId").hide();
    $("#errorMessage").hide();

    $.ajax({
        url: "../api/add-star", // Update with the correct servlet URL
        method: "GET", // Use POST method for sending form data
        dataType: "json",
        data: formData,
        success: function (responseData) {
            // Check if there is an error message
            $("#nParametersMessage").hide();

            if (responseData.error) {
                console.error("Error occurred while adding the star:", responseData.error);
                // Display error message to the user
                $("#errorMessage").text(responseData.error).show();
                $("#errorMessage").show();

            } else {
                // No error, display success message or perform any other action
                console.log("Star added successfully!");
                $("#successMessage").text("Star added successfully!").show();
                $("#starId").text("Star ID: " + responseData.starId).show();

                $("#successMessage").show();
                $("#starId").show();
            }
        },
        error: function (xhr, status, error) {
            console.error("Error occurred while adding the star:", error);
        }
    });
}

// // Event listener for form submission
$(document).ready(function() {
    $("#addStarForm").submit(submitAddStarForm);
});

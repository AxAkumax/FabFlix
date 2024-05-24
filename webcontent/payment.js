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
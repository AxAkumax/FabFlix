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
        }
    });
});

function handleResult(response) {
    if (response["result"] == "success") {
        window.location.href = "confirmation.html";
    }
}
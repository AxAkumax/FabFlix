function fillCart(cart_items) {
    // Get the reference to the table body where you'll append the movie items

    var tableBody = $('#shopping_table_body');

    // Clear the table body first to avoid duplicating items
    tableBody.empty();

    let grand_total = 0;

    for (let i = 0; i < cart_items.length; i++) {
        let rowHTML = "";

        // id for quantity display
        let quantity_element = cart_items[i]["movieId"] + "_quantity";

        let movie_price = 5;
        let movie_quantity = cart_items[i]["movieQuantity"];
        let total = movie_price * movie_quantity;

        rowHTML += "<tr>";

        // Adding row number
        rowHTML +="<th>" + (i + 1).toString() + "</th>";

        // movie title
        rowHTML += "<th>" + cart_items[i]["movieTitle"] + "</th>";

        // movie quantity + increment/decrement buttons
        rowHTML += "<th>";
        rowHTML += '<button id="decrementButton" type="button" class="btn btn-sm btn-outline-secondary rounded-circle mx-1" ' +
                    'onclick="Decrement(\'' + cart_items[i]["movieId"] + '\')"> &#10094; </button>';
        rowHTML += '<span id=' + quantity_element + '>' + "  " + movie_quantity + "  " + '</span>';
        rowHTML += '<button id="incrementButton" type="button" class="btn btn-sm btn-outline-secondary rounded-circle mx-1" ' +
                    'onclick="Increment(\'' + cart_items[i]["movieId"] + '\')"> &#10095; </button>';
        rowHTML += "</th>";

        // movie price
        rowHTML += "<th>" + "$ " + movie_price + "</th>";

        // total price for that item
        rowHTML += "<th>" + "$ " + total + "</th>";

        // delete button
        rowHTML += '<th><button id=type="button" class="btn btn-danger btn-sm" ' +
                    'onclick="Delete(\'' + cart_items[i]["movieId"] + '\')">Delete</button></th>';

        grand_total += total;

        rowHTML += "</tr>";
        tableBody.append(rowHTML);
    }

    let cartTotalElement = $('#cart_total');
    cartTotalElement.text("Cart Total: $ " + grand_total);
}


function Delete(movieId) {
    let data = {
        "movieId": movieId,
        "action": "delete"
    }

    updateMovieCount(data);
}


function Decrement(movieId) {
    let data = {
        "movieId": movieId,
        "action": "decrement"
    }

    updateMovieCount(data);
}


function Increment(movieId) {
    // Create a JSON object containing the movie ID

    let data = {
        "movieId": movieId,
        "action": "increment"
    };

    updateMovieCount(data);
}


function updateMovieCount(data) {
    // get the id of the quantity element

    // Send an AJAX POST request to your backend API to add the movie to the cart
    $.ajax({
        type: "POST",
        url: "api/cart", // Replace this with the actual endpoint of your backend API
        contentType: "application/json",
        data: JSON.stringify(data),
        dataType: "json",
        success: function(response) {
            // Handle the success response from the server
            console.log("Movie successfully " + data['action']);
            fillCart(response);
        },
        error: function(xhr, status, error) {
            // Handle errors if any
            console.error("Error adding movie to cart:", error);
        }
    });
}

// Function to enable/disable the payment button based on the cart total
function togglePaymentButton(cartTotal) {
    const paymentButton = $("#payment_button");
    if (cartTotal > 0) {
        paymentButton.removeAttr("disabled");
    } else {
        paymentButton.attr("disabled", "disabled");
    }
}

// Add an event listener to update the payment button status when the cart total changes
$("#cart_total").on("DOMSubtreeModified", function() {
    // Extract the cart total value
    const cartTotalText = $(this).text();
    const cartTotal = parseInt(cartTotalText.replace("Cart Total: $ ", ""));

    // Toggle the payment button based on the cart total
    togglePaymentButton(cartTotal);
});

// Initially set the button status based on the initial cart total
$(document).ready(function() {
    const cartTotalText = $("#cart_total").text();
    const cartTotal = parseInt(cartTotalText.replace("Cart Total: $ ", ""));

    // Toggle the payment button based on the initial cart total
    togglePaymentButton(cartTotal);
});

// Function to handle payment button click
$("#payment_button").click(function() {
    // Add your payment processing logic here

    const cartTotalText = $("#cart_total").text();
    const cartTotal = parseInt(cartTotalText.replace("Cart Total: $ ", ""));

    let data = {
        "total": cartTotal,
        "action": "total"
    };

    // Send an AJAX POST request to your backend API to add the movie to the cart
    $.ajax({
        type: "POST",
        url: "api/payment", // Replace this with the actual endpoint of your backend API
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(response) {
            console.log("successfully sent the payment PaymentServlet");
            window.location.href = "payment.html";
        },
        error: function(xhr, status, error) {
            // Handle errors if any
            console.error("Error adding movie to cart:", error);
        }
    });
});

$.ajax({
    url: "api/cart",
    method: "GET",
    dataType: "json",
    success: (resultData) => fillCart(resultData)
});

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
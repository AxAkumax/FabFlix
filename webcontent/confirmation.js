
function showOrderDetails(cart_items) {
    var totalElement = $('#total_price')

    var tableBody = $('#confirmation_movies_table_body');

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
        rowHTML += "<th>" + (i + 1).toString() + "</th>";

        // sale id
        rowHTML += "<th>" + cart_items[i]["saleId"] + "</th>";

        // movie title
        rowHTML += "<th>" + cart_items[i]["movieTitle"] + "</th>";

        // movie quantity + increment/decrement buttons
        rowHTML += "<th>" + movie_quantity + "</th>";

        // movie price
        rowHTML += "<th>" + "$ " + movie_price + "</th>";

        // total price for that item
        rowHTML += "<th>" + "$ " + total + "</th>"

        grand_total += total;

        rowHTML += "</tr>";
        tableBody.append(rowHTML);
    }

    totalElement.text("Total: $ " + grand_total);
}

$.ajax({
    url: "api/confirm",
    method: "POST",
    dataType: "json",
    success: (resultData) => showOrderDetails(resultData)
});
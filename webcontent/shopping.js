function fillCart(cart_items) {
    // Get the reference to the table body where you'll append the movie items
    var tableBody = $('#shopping_table_body');

    // Clear the table body first to avoid duplicating items
    tableBody.empty();

    let grant_total = 0;

    for (let i = 0; i < cart_items.length; i++) {
        let rowHTML = "";

        let movie_quantity = 1;
        let movie_price = 5;
        let total = movie_price * movie_quantity;

        rowHTML += "<tr>";

        // Adding row number
        rowHTML +="<th>"+(i + 1).toString()+"</th>";

        rowHTML += "<th>" + cart_items[i]["movie_title"] + "</th>";  // movie title
        rowHTML += "<th>" + movie_quantity + "</th>";               // movie quantity
        rowHTML += "<th>" + movie_price + "</th>";               // movie price
        rowHTML += "<th>" + total + "</th>"

        grant_total += total;

        rowHTML += "</tr>";
        tableBody.append(rowHTML);
    }
}


$.ajax({
    url: "api/cart",
    method: "GET",
    dataType: "json",
    success: (resultData) => fillCart(resultData)
});
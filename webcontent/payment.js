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
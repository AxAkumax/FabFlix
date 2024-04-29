// Makes the HTTP GET request and registers on success callback function handleGenreResult
function handleResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    console.log(resultData);

    let genreTableBodyElement = jQuery("#genre_table_body");
    genreTableBodyElement.empty();
    let genreSpan = $("<span>");
    for (let i = 0; i < resultData.length; i++) {
        let genre_id = resultData[i].genre_id;
        let genre_name = resultData[i].genre_name;

        let genreLink =$("<a class='browse-link'>").attr("href", "movie.html?genreId=" + genre_id).text(genre_name);
        genreSpan.append(genreLink);
        if (i < resultData.length - 1) {
            genreSpan.append(", ");
        }
    }
    genreTableBodyElement.append(genreSpan);

    let alphabet_table_body = jQuery("#alphabet_table_body");
    alphabet_table_body.empty();
    let alphaSpan = $("<span>");
    let letters = ["0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","H","I","J","K","L","M","N","O","P",
    "Q","R","S","T","U","V","W","X","Y","Z","*"];
    for(let i=0; i<letters.length; i++){
        let alphalink = $("<a class='browse-link'>").attr("href", "movie.html?character=" + letters[i]).text(letters[i]);
        alphaSpan.append(alphalink);
        if (i < letters.length - 1) {
            alphaSpan.append(", ");
        }
    }
    alphabet_table_body.append(alphaSpan);
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genre", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
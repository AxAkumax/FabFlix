let genreId = null;
let titleStart = null;
let currentPage = 1;
let totalPages = 1;

function create_browsing_result(url) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: function(resultData) {
            let browsingBodyElement = jQuery("#browsing_table_body");
            browsingBodyElement.empty();

            for (let i = 0; i < resultData.length; i++) {
                let rowHTML = "<tr>";
                rowHTML += "<th>" + (i+1) + "</th>";
                rowHTML += "<th><a href='single-movie.html?id=" + resultData[i]['movie_id'] + "'>" + resultData[i]["movie_title"] + "</a></th>";
                rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

                let star_ids = resultData[i]['movie_starIds'].split(', ');
                let star_names = resultData[i]['movie_stars'].split(', ');

                let star_entries = "";

                let length  = Math.min(3, star_ids.length);
                for (let j = 0; j < length; j++) {
                    star_entries += '<a href="single-star.html?id=' + star_ids[j] + '">' + star_names[j] + '</a>';
                    if (j < length - 1){
                        star_entries += ", ";
                    }
                }

                rowHTML += "<th>" + star_entries + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
                rowHTML += "<th>" + resultData[i]["average_rating"] + "</th>";
                rowHTML += "</tr>";
                browsingBodyElement.append(rowHTML)
            }
            totalPages = Math.ceil(resultData.length / $("#moviesPerPage").val());
            updatePaginationControls(totalPages);
        },
        error: function(xhr, status, error) {
            console.error("Error fetching genre movies:", error);
        }
    });
}

function handleBrowseClick(event) {
    event.preventDefault();
    currentPage = 1;
    genreId = jQuery(this).data("genre-id");
    titleStart = jQuery(this).data("alphabet");

    if (genreId !== undefined) {
        var formData = {
            "genreId": jQuery(this).data("genre-id"),
            "sortAttribute": $("#sortAttribute").val(),
            page: currentPage,
            recordsPerPage: $("#moviesPerPage").val()
        };
    } else if (titleStart !== undefined) {
        var formData = {
            "titleStart": jQuery(this).data("alphabet"),
            "sortAttribute": $("#sortAttribute").val(),
            page: currentPage,
            recordsPerPage: $("#moviesPerPage").val()
        };
    }

    var url = "";
    if ( genreId !== undefined) {
        url = "api/genre?genreId=" + genreId + "&sortAttribute=" + $("#sortAttribute").val();
    } else if (titleStart !== undefined) {
        url = "api/movie?titleStart=" + titleStart + "&sortAttribute=" + $("#sortAttribute").val();
    } else {
        url = "api/browse";
    }

    create_browsing_result(url);
}

function handleBrowseResult(resultData) {
    let genresElement = jQuery("#genre_table_body");
    let row = "";

    for (let i = 0; i < resultData.length; i++) {
        if (i % 5 === 0) {
            if (i !== 0) {
                row += "</tr>";
            }
            row += "<tr>";
        }

        let genreLink = '<a href="#" class="genre-link" data-genre-id="' + resultData[i]['genreId'] + '">' + resultData[i]["genreName"] + '</a>';
        row += "<td>" + genreLink + "</td>";
    }

    row += "</tr>";
    genresElement.append(row);
    jQuery(".genre-link").click(handleBrowseClick);

    let alphabetElement = jQuery("#alphabet_table_body");
    let alphabetArray = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*'];
    let row2 = "";
    for (let i = 0; i < alphabetArray.length; i++) {
        if (i % 8 === 0) {
            if (i !== 0) {
                row2 += "</tr>";
            }
            row2 += "<tr>";
        }

        let alphabetLink = '<a href="#" class="alphabet-link" data-alphabet="' + alphabetArray[i] + '">' + alphabetArray[i].toUpperCase() + '</a>';
        row2 += "<td>" + alphabetLink + "</td>";
    }

    row2 += "</tr>";
    alphabetElement.append(row2);
    jQuery(".alphabet-link").click(handleBrowseClick);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/genre",
    success: (resultData) => handleBrowseResult(resultData)
});

function handleDropdownChange() {
    var selectedOption = $(this).val();
    currentPage = 1;

    if (genreId !== undefined) {
        var formData = {
            "genreId": genreId,
            "sortAttribute": $("#sortAttribute").val(),
            page: currentPage,
            recordsPerPage: $("#moviesPerPage").val()
        };
    } else if (titleStart !== undefined) {
        var formData = {
            "titleStart": titleStart,
            "sortAttribute": $("#sortAttribute").val(),
            page: currentPage,
            recordsPerPage: $("#moviesPerPage").val()
        };
    }

    var url = "";
    if (genreId !== null) {
        url = "api/genre?genreId=" + genreId + "&sortAttribute=" + $("#sortAttribute").val();
    } else if (titleStart !== null) {
        url = "api/movie?titleStart=" + titleStart + "&sortAttribute=" + $("#sortAttribute").val();
    } else {
        url = "api/browse";
    }

    if (currentPage > 1 || $("#moviesPerPage").val() !== "10") {
        url += "&page=" + currentPage + "&recordsPerPage=" + $("#moviesPerPage").val();
    }

    create_browsing_result(url);
}

function updatePaginationControls(totalPages) {
    if (currentPage === 1) {
        $("#prevBtn").prop("disabled", true);
    } else {
        $("#prevBtn").prop("disabled", false);
    }

    if (currentPage === totalPages) {
        $("#nextBtn").prop("disabled", true);
    } else {
        $("#nextBtn").prop("disabled", false);
    }
}

function nextPage() {
    currentPage++;
    handleDropdownChange();
}

function prevPage() {
    if (currentPage > 1) {
        currentPage--;
        handleDropdownChange();
    }
}

$("#sortAttribute").change(handleDropdownChange);
$("#moviesPerPage").change(handleDropdownChange);
$("#prevBtn").click(prevPage);
$("#nextBtn").click(nextPage);

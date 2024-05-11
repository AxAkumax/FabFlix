document.addEventListener("DOMContentLoaded", function() {
    $.ajax({
        url: "api/metadata",
        method: "GET",
        dataType: "json",
        success: function (resultData) {
            console.log("success");
            populateMetadata(resultData);
        },
        error: function (xhr, status, error) {
            console.error("Error occurred while fetching metadata:", error);
        }
    });
});

function populateMetadata(data){
    const tablesDiv = document.getElementById('tables');
    data.tables.forEach(table => {
        const tableName = table.table_name;
        const columns = table.columns;
        const tableElement = document.createElement('table');
        const headerRow = document.createElement('tr');
        const tableNameHeader = document.createElement('th');
        tableNameHeader.textContent = tableName;
        headerRow.appendChild(tableNameHeader);
        tableElement.appendChild(headerRow);
        columns.forEach(column => {
            const columnRow = document.createElement('tr');
            const columnNameCell = document.createElement('td');
            const columnTypeCell = document.createElement('td');
            columnNameCell.textContent = column.column_name;
            columnTypeCell.textContent = column.column_type;
            columnRow.appendChild(columnNameCell);
            columnRow.appendChild(columnTypeCell);
            tableElement.appendChild(columnRow);
        });
        tablesDiv.appendChild(tableElement);
    });
}

document.addEventListener("DOMContentLoaded", function() {
    fetch('/api/metadata')
        .then(response => response.json())
        .then(data => {
            const tablesDiv = document.getElementById('tables');
            data.columns.forEach(table => {
                const tableName = table[0].column_name;
                const columns = table.slice(1);
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
        })
        .catch(error => console.error('Error:', error));
});

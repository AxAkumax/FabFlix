// login.js
document.addEventListener('DOMContentLoaded', function() {
    // Check if 'error' parameter exists in the URL
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');

    if (error) {
        // Display error message
        const errorMessage = document.createElement('p');
        errorMessage.textContent = '*Invalid email or password. Please try again.';
        errorMessage.id = 'error-msg';
        document.body.appendChild(errorMessage);
    }
});

document.addEventListener('submit', async (e) => {
    const form = e.target;
    // Find the password input in the submitted form
    const passwordInput = form.querySelector('input[type="password"]');
    
    if (passwordInput && passwordInput.value) {
        // Send the password to the background worker to check
        chrome.runtime.sendMessage({
            type: 'CHECK_PASSWORD',
            password: passwordInput.value,
            domain: window.location.hostname
        });
    }
});

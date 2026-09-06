// Simple SHA-1 hash function using Web Crypto API
async function sha1(message) {
    const msgBuffer = new TextEncoder().encode(message);
    const hashBuffer = await crypto.subtle.digest('SHA-1', msgBuffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    return hashHex.toUpperCase();
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'CHECK_PASSWORD') {
        checkPasswordBreach(message.password, message.domain);
    }
});

async function checkPasswordBreach(password, domain) {
    try {
        const hash = await sha1(password);
        const prefix = hash.substring(0, 5);
        
        // Ping our local BreachGuard backend
        const response = await fetch(`http://localhost:8080/api/check-password?prefix=${prefix}&hash=${hash}`);
        if (!response.ok) return;
        
        const data = await response.json();
        
        if (data.isBreached) {
            chrome.notifications.create({
                type: 'basic',
                iconUrl: 'icon.png',
                title: 'BreachGuard Alert!',
                message: `WARNING: The password you used on ${domain} has been exposed in ${data.count} data breaches! Change it immediately.`,
                priority: 2
            });
        }
    } catch (e) {
        console.error("BreachGuard Extension Error:", e);
    }
}

// Background script for IRCTC automation
console.log("IRCTC Automation background script loaded");

// Listen for tab updates
browser.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
    if (changeInfo.status === 'complete' && tab.url && tab.url.includes('irctc.co.in')) {
        console.log("IRCTC page loaded, sending automation message");
        
        // Send message to content script to start automation
        browser.tabs.sendMessage(tabId, {
            action: "start_automation"
        }).then(response => {
            console.log("Automation message sent, response:", response);
        }).catch(error => {
            console.error("Error sending automation message:", error);
        });
    }
});

// Handle messages from content scripts
browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
    console.log("Background script received message:", message);
    
    if (message.type === "automation_result") {
        // Forward the result to native app
        browser.runtime.sendNativeMessage("browser", message);
    }
    
    return true;
});

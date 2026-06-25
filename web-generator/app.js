// Default System Prompt used for Prompt Engineering
const DEFAULT_SYSTEM_PROMPT = `You are an expert Android UI/UX designer and senior frontend engineer.
Your task is to generate a fully functional, self-contained, and exceptionally polished mobile web application that looks and behaves exactly like a premium native Android app.

DESIGN & STYLING RULES (ANDROID MATERIAL 3):
1. THEME: Follow modern Google Material You / Material Design 3 guidelines. Use vibrant but harmonious colors (sleek dark mode by default with light purple/indigo/blue accents).
2. LAYOUT: The app should be responsive, fitting perfectly inside a mobile viewport (max-width: 480px) and filling the height. 
3. UI COMPONENTS:
   - Header Bar: A top app bar with navigation icon (e.g. menu or back), title, and action icons (search, settings, profile).
   - Bottom Navigation: A clean bottom navigation bar with 3 to 4 tabs. Use Material 3 style: active tab should have a colored pill container behind its icon, and short descriptive labels underneath.
   - Actionable Elements: Buttons must have subtle hover states, active states (simulating a material ripple effect), and clear icons. Use Floating Action Buttons (FAB) at the bottom-right for primary actions.
   - Feedback: Use Material-style snackbars or modal dialogs for notifications/forms.
4. TYPOGRAPHY & ICONS:
   - Use Google Fonts (prefer 'Outfit' or 'Plus Jakarta Sans', fallback to 'Roboto').
   - Use Google Material Symbols or Material Icons via CDN for modern, authentic Android icons.
     (e.g., <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />)

FUNCTIONAL REQUIREMENTS:
1. SINGLE FILE: All HTML structure, CSS, and JS logic must be bundled inside this single file. No external assets (images/scripts) unless loaded via standard reliable CDNs. Use font-based icons instead of image files.
2. NO PLACEHOLDERS: The app must be fully interactive. Do not leave "todo" comments or mock buttons that do nothing. Every tab, toggle, filter, and button must work.
3. LOCAL STATE MANAGEMENT: Use JavaScript and browser 'localStorage' to save all user settings, data logs, lists, or histories. When the user closes and re-opens the app, their progress must be retained!
4. MOCK DATA: Pre-populate the app with realistic, beautiful mock data so that it immediately looks complete, colorful, and engaging upon first load.
5. TRANSITIONS: Implement smooth animations for transitions between screens, modal overlays, tab switches, list additions, or toggles.

OUTPUT FORMAT:
- Output ONLY the raw HTML code. Do NOT write explanations, warnings, or intro/outro text.
- Return the code wrapped inside standard markdown code blocks (i.e. \`\`\`html [code] \`\`\`), which will be parsed automatically.`;

// State variables
let systemPrompt = localStorage.getItem('aiyo_system_prompt') || DEFAULT_SYSTEM_PROMPT;
let generatedHtml = '';

// DOM Elements
const apiKeyInput = document.getElementById('api-key');
const toggleKeyBtn = document.getElementById('toggle-key-visibility');
const modelSelect = document.getElementById('model-select');
const customModelGroup = document.getElementById('custom-model-group');
const customModelInput = document.getElementById('custom-model-input');
const promptInput = document.getElementById('prompt-input');
const clearPromptBtn = document.getElementById('clear-prompt-btn');
const generateBtn = document.getElementById('generate-btn');
const charCount = document.getElementById('prompt-char-count');
const systemPromptInput = document.getElementById('system-prompt-input');
const resetSystemPromptBtn = document.getElementById('reset-system-prompt-btn');
const logPanel = document.getElementById('log-panel');
const logOutput = document.getElementById('log-output');
const progressPercent = document.getElementById('progress-percent');
const previewPlaceholder = document.getElementById('preview-placeholder');
const phoneWrapper = document.getElementById('phone-wrapper');
const previewIframe = document.getElementById('preview-iframe');
const codeOutput = document.getElementById('code-output');
const codeActionsContainer = document.getElementById('code-actions-container');
const copyCodeBtn = document.getElementById('copy-code-btn');
const downloadCodeBtn = document.getElementById('download-code-btn');
const showHelpBtn = document.getElementById('show-help-btn');
const closeHelpBtn = document.getElementById('close-help-btn');
const helpModal = document.getElementById('help-modal');
const emulatorTime = document.getElementById('emulator-time');

// Load configurations from LocalStorage
function initConfig() {
    // Restore API Key
    const savedKey = localStorage.getItem('aiyo_openrouter_key');
    if (savedKey) {
        apiKeyInput.value = savedKey;
    }

    // Restore Model
    const savedModel = localStorage.getItem('aiyo_openrouter_model');
    if (savedModel) {
        modelSelect.value = savedModel;
        if (savedModel === 'custom') {
            customModelGroup.classList.remove('hidden');
            customModelInput.value = localStorage.getItem('aiyo_openrouter_custom_model') || '';
        }
    }

    // Initial system prompt setup
    systemPromptInput.value = systemPrompt;

    // Set dynamic current time in Android Emulator
    updateEmulatorTime();
    setInterval(updateEmulatorTime, 60000);
}

// Update clock in simulated device
function updateEmulatorTime() {
    const now = new Date();
    let hours = now.getHours();
    let minutes = now.getMinutes();
    hours = hours < 10 ? '0' + hours : hours;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    emulatorTime.textContent = `${hours}:${minutes}`;
}

// Log message to the console panel
function addConsoleLog(message, type = 'info') {
    logPanel.classList.remove('hidden');
    
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const row = document.createElement('div');
    row.className = `console-row ${type}`;
    
    let iconClass = 'fa-solid fa-circle-info';
    if (type === 'success') iconClass = 'fa-solid fa-circle-check';
    if (type === 'loading') iconClass = 'fa-solid fa-spinner';
    if (type === 'error') iconClass = 'fa-solid fa-circle-xmark';
    
    row.innerHTML = `
        <span class="time">[${time}]</span>
        <span class="status-icon"><i class="${iconClass}"></i></span>
        <span class="message">${message}</span>
    `;
    
    logOutput.appendChild(row);
    logOutput.scrollTop = logOutput.scrollHeight;
    return row; // return to modify later if needed (e.g. for loading states)
}

function clearConsoleLog() {
    logOutput.innerHTML = '';
}

// Handle API key visibility
toggleKeyBtn.addEventListener('click', () => {
    const type = apiKeyInput.getAttribute('type') === 'password' ? 'text' : 'password';
    apiKeyInput.setAttribute('type', type);
    const icon = toggleKeyBtn.querySelector('i');
    icon.className = type === 'password' ? 'fa-solid fa-eye' : 'fa-solid fa-eye-slash';
});

// Save keys & options when edited
apiKeyInput.addEventListener('change', () => {
    localStorage.setItem('aiyo_openrouter_key', apiKeyInput.value.trim());
});

modelSelect.addEventListener('change', () => {
    const selected = modelSelect.value;
    localStorage.setItem('aiyo_openrouter_model', selected);
    
    if (selected === 'custom') {
        customModelGroup.classList.remove('hidden');
    } else {
        customModelGroup.classList.add('hidden');
    }
});

customModelInput.addEventListener('change', () => {
    localStorage.setItem('aiyo_openrouter_custom_model', customModelInput.value.trim());
});

// Textarea character count
promptInput.addEventListener('input', () => {
    charCount.textContent = `${promptInput.value.length} chars`;
});

// Help Modal controls
showHelpBtn.addEventListener('click', () => helpModal.classList.remove('hidden'));
closeHelpBtn.addEventListener('click', () => helpModal.classList.add('hidden'));
helpModal.addEventListener('click', (e) => {
    if (e.target === helpModal) helpModal.classList.add('hidden');
});

// Clear prompt action
clearPromptBtn.addEventListener('click', () => {
    promptInput.value = '';
    charCount.textContent = '0 chars';
    document.querySelectorAll('.template-btn').forEach(b => b.classList.remove('active'));
});

// Templates selection
document.querySelectorAll('.template-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.template-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        promptInput.value = btn.dataset.prompt;
        charCount.textContent = `${promptInput.value.length} chars`;
        promptInput.focus();
    });
});

// System Prompt reset
resetSystemPromptBtn.addEventListener('click', () => {
    if (confirm('Are you sure you want to reset the system instructions to the default?')) {
        systemPrompt = DEFAULT_SYSTEM_PROMPT;
        systemPromptInput.value = systemPrompt;
        localStorage.removeItem('aiyo_system_prompt');
    }
});

systemPromptInput.addEventListener('change', () => {
    systemPrompt = systemPromptInput.value;
    localStorage.setItem('aiyo_system_prompt', systemPrompt);
});

// Tabs switching logic
document.querySelectorAll('.tab-btn').forEach(tabBtn => {
    tabBtn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
        
        tabBtn.classList.add('active');
        const paneId = tabBtn.dataset.tab;
        document.getElementById(paneId).classList.add('active');
    });
});

// Helper: Extract code blocks from Markdown responses
function extractHtml(responseStr) {
    // 1. Try finding HTML backtick code blocks
    let htmlMatch = responseStr.match(/```html([\s\S]*?)```/);
    if (htmlMatch && htmlMatch[1]) {
        return htmlMatch[1].trim();
    }
    
    // 2. Try generic backticks if no HTML label was set
    let genericMatch = responseStr.match(/```([\s\S]*?)```/);
    if (genericMatch && genericMatch[1]) {
        let blockContent = genericMatch[1].trim();
        if (blockContent.toLowerCase().includes('<!doctype html') || blockContent.toLowerCase().includes('<html')) {
            return blockContent;
        }
    }
    
    // 3. Fallback: If no code block, check if standard HTML components exist, return entire string
    if (responseStr.toLowerCase().includes('<html') || responseStr.toLowerCase().includes('<!doctype')) {
        return responseStr.trim();
    }
    
    return '';
}

// Generate Action
generateBtn.addEventListener('click', async () => {
    const key = apiKeyInput.value.trim();
    const model = modelSelect.value === 'custom' ? customModelInput.value.trim() : modelSelect.value;
    const prompt = promptInput.value.trim();
    
    if (!key) {
        alert('Please configure your OpenRouter API Key first.');
        apiKeyInput.focus();
        return;
    }
    
    if (!model && modelSelect.value === 'custom') {
        alert('Please specify your Custom Model ID.');
        customModelInput.focus();
        return;
    }
    
    if (!prompt) {
        alert('Please enter an app idea or choose one of the templates.');
        promptInput.focus();
        return;
    }

    // Set Loading State in UI
    generateBtn.disabled = true;
    generateBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Generating...';
    clearConsoleLog();
    progressPercent.textContent = '10%';
    
    addConsoleLog('Starting generation pipeline...', 'info');
    let apiLog = addConsoleLog('Connecting to OpenRouter endpoint...', 'loading');
    
    try {
        progressPercent.textContent = '25%';
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${key}`,
            'HTTP-Referer': 'https://github.com/beradeep/aiyo',
            'X-Title': 'Aiyo App Studio'
        };

        const body = JSON.stringify({
            model: model,
            messages: [
                { role: 'system', content: systemPrompt },
                { role: 'user', content: `Please build this Android Web App concept: ${prompt}` }
            ],
            temperature: 0.25
        });

        progressPercent.textContent = '40%';
        
        const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
            method: 'POST',
            headers: headers,
            body: body
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            const errMsg = errData.error?.message || `HTTP error ${response.status}`;
            throw new Error(errMsg);
        }

        progressPercent.textContent = '75%';
        apiLog.className = 'console-row success';
        apiLog.querySelector('.status-icon').innerHTML = '<i class="fa-solid fa-circle-check"></i>';
        apiLog.querySelector('.message').textContent = 'OpenRouter connection successful. Parsing response...';
        
        const data = await response.json();
        const rawContent = data.choices?.[0]?.message?.content;
        
        if (!rawContent) {
            throw new Error('Received an empty response from OpenRouter model.');
        }
        
        addConsoleLog('Extracting source code from assistant instructions...', 'info');
        progressPercent.textContent = '90%';
        
        const htmlCode = extractHtml(rawContent);
        
        if (!htmlCode) {
            addConsoleLog('Warning: Could not parse clean HTML tags. Displaying raw markdown output.', 'error');
            generatedHtml = rawContent;
        } else {
            generatedHtml = htmlCode;
        }

        // Render in IFrame
        addConsoleLog('Injecting app code into Android device simulator...', 'info');
        
        previewPlaceholder.classList.add('hidden');
        phoneWrapper.classList.remove('hidden');
        
        // Load content to IFrame using srcdoc to support full script & CSS styles securely
        previewIframe.srcdoc = generatedHtml;
        
        // Fill code container
        codeOutput.textContent = generatedHtml;
        codeActionsContainer.classList.remove('hidden');
        
        addConsoleLog('App generation complete! Loaded successfully in Device Preview.', 'success');
        progressPercent.textContent = '100%';
        
        // Auto-switch to Preview tab if not already there
        document.querySelector('.tab-btn[data-tab="preview-tab"]').click();

    } catch (error) {
        console.error(error);
        apiLog.className = 'console-row error';
        apiLog.querySelector('.status-icon').innerHTML = '<i class="fa-solid fa-circle-xmark"></i>';
        apiLog.querySelector('.message').textContent = `Request failed: ${error.message}`;
        progressPercent.textContent = 'Err';
    } finally {
        generateBtn.disabled = false;
        generateBtn.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Generate App';
    }
});

// Copy Code Button
copyCodeBtn.addEventListener('click', () => {
    if (!generatedHtml) return;
    
    navigator.clipboard.writeText(generatedHtml).then(() => {
        const originalText = copyCodeBtn.innerHTML;
        copyCodeBtn.innerHTML = '<i class="fa-solid fa-check"></i> Copied!';
        setTimeout(() => {
            copyCodeBtn.innerHTML = originalText;
        }, 2000);
    }).catch(err => {
        alert('Failed to copy code: ' + err);
    });
});

// Download HTML file utility
downloadCodeBtn.addEventListener('click', () => {
    if (!generatedHtml) return;
    
    const blob = new Blob([generatedHtml], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'android_app.html';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
});

// Initialize on Load
window.addEventListener('DOMContentLoaded', initConfig);

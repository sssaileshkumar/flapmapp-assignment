import { WebSocketServer } from 'ws';
import * as http from 'http';

const PORT = 8080;

// Create a simple HTTP server to serve the HTML page
const server = http.createServer((req, res) => {
    if (req.url === '/') {
        res.writeHead(200, { 'Content-Type': 'text/html' });
        res.end(`
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>FlamApp Video Stream</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #121212;
                        color: #ffffff;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        height: 100vh;
                        margin: 0;
                    }
                    h1 { margin-bottom: 20px; }
                    #video-container {
                        border: 2px solid #333;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.3);
                    }
                    img {
                        display: block;
                        max-width: 100%;
                        height: auto;
                    }
                    .status {
                        margin-top: 10px;
                        color: #888;
                        font-size: 0.9em;
                    }
                </style>
            </head>
            <body>
                <h1>FlamApp Live Stream</h1>
                <div id="video-container">
                    <img id="stream" src="" alt="Waiting for stream..." width="640" height="480" />
                </div>
                <div class="status" id="status">Disconnected</div>

                <script>
                    const img = document.getElementById('stream');
                    const status = document.getElementById('status');
                    const ws = new WebSocket('ws://' + window.location.host);

                    ws.onopen = () => {
                        status.textContent = 'Connected';
                        status.style.color = '#4caf50';
                    };

                    ws.onmessage = (event) => {
                        const blob = event.data;
                        const url = URL.createObjectURL(blob);
                        img.onload = () => {
                            URL.revokeObjectURL(url);
                        };
                        img.src = url;
                    };

                    ws.onclose = () => {
                        status.textContent = 'Disconnected';
                        status.style.color = '#f44336';
                    };
                </script>
            </body>
            </html>
        `);
    } else {
        res.writeHead(404);
        res.end('Not Found');
    }
});

// Create WebSocket server attached to the HTTP server
const wss = new WebSocketServer({ server });

wss.on('connection', (ws) => {
    console.log('Client connected');

    ws.on('message', (message, isBinary) => {
        // Broadcast the frame to all other connected clients (the browser)
        wss.clients.forEach((client) => {
            if (client !== ws && client.readyState === 1) {
                client.send(message, { binary: isBinary });
            }
        });
    });

    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

server.listen(PORT, () => {
    console.log(`Server started on http://localhost:${PORT}`);
});

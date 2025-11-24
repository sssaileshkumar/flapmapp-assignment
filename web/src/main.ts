// Mock data generator for demonstration
class StreamSimulator {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private isRunning: boolean = false;
    private frameCount: number = 0;
    private lastTime: number = 0;
    private fpsElement: HTMLElement | null;
    private resolutionElement: HTMLElement | null;
    private statusElement: HTMLElement | null;

    constructor(canvasId: string) {
        this.canvas = document.getElementById(canvasId) as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.fpsElement = document.getElementById('fps');
        this.resolutionElement = document.getElementById('resolution');
        this.statusElement = document.getElementById('status');
    }

    start() {
        if (this.isRunning) return;
        this.isRunning = true;
        this.lastTime = performance.now();
        this.updateStatus('Connected (Simulated)');
        this.updateResolution(`${this.canvas.width}x${this.canvas.height}`);
        this.loop();
    }

    stop() {
        this.isRunning = false;
        this.updateStatus('Disconnected');
    }

    private loop = () => {
        if (!this.isRunning) return;

        this.renderMockFrame();
        this.updateStats();

        requestAnimationFrame(this.loop);
    };

    private renderMockFrame() {
        const w = this.canvas.width;
        const h = this.canvas.height;
        const time = performance.now() / 1000;

        // Clear background
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, w, h);

        // Draw some moving "edges" to simulate Canny output
        this.ctx.strokeStyle = '#fff';
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();

        // Moving sine wave
        for (let x = 0; x < w; x += 5) {
            const y = h / 2 + Math.sin(x * 0.02 + time * 5) * 100;
            if (x === 0) this.ctx.moveTo(x, y);
            else this.ctx.lineTo(x, y);
        }
        this.ctx.stroke();

        // Rotating rectangle
        this.ctx.save();
        this.ctx.translate(w / 2, h / 2);
        this.ctx.rotate(time);
        this.ctx.strokeRect(-50, -50, 100, 100);
        this.ctx.restore();
    }

    private updateStats() {
        this.frameCount++;
        const now = performance.now();
        if (now - this.lastTime >= 1000) {
            if (this.fpsElement) {
                this.fpsElement.textContent = this.frameCount.toString();
            }
            this.frameCount = 0;
            this.lastTime = now;
        }
    }

    private updateStatus(status: string) {
        if (this.statusElement) this.statusElement.textContent = status;
        if (this.statusElement) this.statusElement.style.color = status.includes('Connected') ? '#0f0' : '#f00';
    }

    private updateResolution(res: string) {
        if (this.resolutionElement) this.resolutionElement.textContent = res;
    }
}

// Initialize
const simulator = new StreamSimulator('frameCanvas');

document.getElementById('simulateBtn')?.addEventListener('click', () => {
    simulator.start();
});

document.getElementById('stopBtn')?.addEventListener('click', () => {
    simulator.stop();
});

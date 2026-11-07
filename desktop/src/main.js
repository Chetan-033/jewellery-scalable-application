const { app, BrowserWindow, Tray, Menu, dialog, nativeImage } = require('electron');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const http = require('http');

let mainWindow = null;
let tray = null;
let apiProcess = null;
const API_PORT = process.env.SERVER_PORT || '18080';
const API_URL = `http://127.0.0.1:${API_PORT}`;

function runtimeRoot() {
  return app.isPackaged ? process.resourcesPath : path.join(__dirname, '..', 'resources');
}

function resolveJavaCommand() {
  if (process.env.JAVA_HOME) {
    const candidate = path.join(process.env.JAVA_HOME, 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
    if (fs.existsSync(candidate)) {
      return candidate;
    }
  }
  return process.platform === 'win32' ? 'java.exe' : 'java';
}

function resolveJarPath() {
  const packaged = path.join(runtimeRoot(), 'backend', 'jewellery-erp.jar');
  if (fs.existsSync(packaged)) {
    return packaged;
  }
  const devJar = path.join(__dirname, '..', '..', 'backend', 'target', 'jewellery-erp-0.1.0-SNAPSHOT.jar');
  if (fs.existsSync(devJar)) {
    return devJar;
  }
  return null;
}

function resolveUiEntry() {
  const packagedIndex = path.join(runtimeRoot(), 'frontend', 'browser', 'index.html');
  if (fs.existsSync(packagedIndex)) {
    return { type: 'file', value: packagedIndex };
  }
  const distIndex = path.join(__dirname, '..', '..', 'frontend', 'dist', 'jewellery-erp-ui', 'browser', 'index.html');
  if (fs.existsSync(distIndex)) {
    return { type: 'file', value: distIndex };
  }
  return { type: 'url', value: 'http://localhost:4200' };
}

function waitForHealth(timeoutMs = 60000) {
  const started = Date.now();
  return new Promise((resolve, reject) => {
    const tick = () => {
      const req = http.get(`${API_URL}/api/v1/health`, (res) => {
        res.resume();
        if (res.statusCode === 200) {
          resolve();
          return;
        }
        retry();
      });
      req.on('error', retry);
      function retry() {
        if (Date.now() - started > timeoutMs) {
          reject(new Error('API health check timed out'));
          return;
        }
        setTimeout(tick, 1000);
      }
    };
    tick();
  });
}

function startApi() {
  const jar = resolveJarPath();
  if (!jar) {
    throw new Error('Backend jar not found. Build backend first (mvnw package) or place jar in desktop/resources/backend/jewellery-erp.jar');
  }
  const java = resolveJavaCommand();
  const args = [
    '-jar',
    jar,
    `--spring.profiles.active=offline`,
    `--server.port=${API_PORT}`
  ];
  apiProcess = spawn(java, args, {
    cwd: path.dirname(jar),
    env: {
      ...process.env,
      SPRING_PROFILES_ACTIVE: 'offline'
    },
    stdio: 'ignore',
    windowsHide: true
  });
  apiProcess.on('exit', (code) => {
    apiProcess = null;
    if (mainWindow) {
      dialog.showErrorBox('Jewellery ERP', `Local API stopped unexpectedly (code ${code}).`);
    }
  });
}

function stopApi() {
  if (apiProcess && !apiProcess.killed) {
    apiProcess.kill();
    apiProcess = null;
  }
}

async function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1360,
    height: 860,
    minWidth: 1100,
    minHeight: 700,
    title: 'Jewellery ERP',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  const ui = resolveUiEntry();
  if (ui.type === 'file') {
    await mainWindow.loadFile(ui.value);
  } else {
    await mainWindow.loadURL(ui.value);
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

function createTray() {
  const image = nativeImage.createEmpty();
  tray = new Tray(image);
  tray.setToolTip('Jewellery ERP Offline');
  tray.setContextMenu(
    Menu.buildFromTemplate([
      {
        label: 'Show',
        click: () => {
          if (mainWindow) {
            mainWindow.show();
          }
        }
      },
      {
        label: 'Quit',
        click: () => app.quit()
      }
    ])
  );
}

app.whenReady().then(async () => {
  try {
    startApi();
    await waitForHealth();
    createTray();
    await createWindow();
  } catch (err) {
    dialog.showErrorBox('Jewellery ERP failed to start', String(err && err.message ? err.message : err));
    stopApi();
    app.quit();
  }
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('before-quit', () => {
  stopApi();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

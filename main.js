/* Leader Engineering Services — application de bureau (Electron).
   L'application web (index.html, 100% statique) est chargée telle quelle :
   localStorage, impression (Ctrl+P -> PDF), photos et logo fonctionnent en local. */
const { app, BrowserWindow, Menu } = require('electron');
const path = require('path');

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 860,
    minWidth: 1024,
    minHeight: 640,
    title: 'Leader Engineering Services',
    icon: path.join(__dirname, 'assets/icon.png'),
    backgroundColor: '#F4F6FB',
    autoHideMenuBar: true,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  Menu.setApplicationMenu(null); // interface propre, sans menu Electron
  win.loadFile('index.html');
}

app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

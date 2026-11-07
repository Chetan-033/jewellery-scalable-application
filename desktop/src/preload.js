const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('jewelleryErpDesktop', {
  edition: 'offline',
  isDesktop: true
});

# 🚗 CaminoSeguro - Guía Rápida Android Auto (DHU)

### 1. Preparación única (Teléfono)
1. Instalar app `mobile`.
2. Conceder **Acceso a Notificaciones**.
3. **Android Auto App** -> Ajustes -> Versión (tocar 10 veces) -> **Modo Desarrollador**.
4. Menú (⋮) -> **Iniciar servidor de la consola central**.

### 2. Comandos de Conexión (Terminal PowerShell)

**Paso A: Puente ADB**
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" forward tcp:5277 tcp:5277
```

**Paso B: Iniciar Emulador (DHU)**
```powershell
cd "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto"
.\desktop-head-unit.exe
```

### 3. Troubleshooting
* **DHU colgado:**
  ```powershell
  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" forward --remove-all
  # Reintentar Paso A
  ```
* **Verificar conexión:**
  ```powershell
  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
  ```

---
**Nota:** Reproduce música en Spotify para ver los banners de letras.

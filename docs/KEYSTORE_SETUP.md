# 🔐 Configuración de Keystore y GitHub Secrets

Esta guía documenta cómo generar el keystore para firmar el AAB de release y cómo configurar los GitHub Secrets necesarios para el pipeline CI/CD.

> ⚠️ **Nunca commitear el keystore (`.jks`) ni `local.properties` al repositorio.**
> Ya están listados en `.gitignore`.

---

## 1. Generar el Keystore (una sola vez)

Ejecutar desde la raíz del proyecto:

```bash
keytool -genkey -v \
  -keystore keystore.jks \
  -alias taskflow-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Te pedirá:
- Contraseña del keystore (`STORE_PASSWORD`)
- Datos de identidad (nombre, organización, país)
- Contraseña de la clave (`KEY_PASSWORD`)

**Guarda el archivo `keystore.jks` en un lugar seguro** (no en el repo). Si lo pierdes, no podrás actualizar la app en Play Store.

---

## 2. Convertir el Keystore a Base64

Para subirlo como GitHub Secret:

```bash
# Linux / macOS
base64 -i keystore.jks > keystore_base64.txt

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) > keystore_base64.txt
```

Copia el contenido completo de `keystore_base64.txt`.

---

## 3. Configurar GitHub Secrets

Ir a: **Repositorio → Settings → Secrets and variables → Actions → New repository secret**

Crear estos 4 secrets:

| Nombre del Secret | Valor |
|---|---|
| `KEYSTORE_BASE64` | Contenido del archivo `keystore_base64.txt` |
| `KEY_ALIAS` | El alias que usaste (ej. `taskflow-key`) |
| `KEY_PASSWORD` | Contraseña de la clave |
| `STORE_PASSWORD` | Contraseña del keystore |

---

## 4. Construir AAB localmente (opcional)

Si quieres generar el AAB en tu máquina:

```bash
# Linux / macOS
export KEYSTORE_PATH=$(pwd)/keystore.jks
export KEY_ALIAS=taskflow-key
export KEY_PASSWORD=tu_password
export STORE_PASSWORD=tu_store_password

./gradlew bundleRelease
```

El AAB queda en `app/build/outputs/bundle/release/app-release.aab`.

---

## 5. Subir a Play Console — Internal Testing

1. Entrar a [Google Play Console](https://play.google.com/console)
2. Seleccionar la app **TaskFlow**
3. Ir a **Versiones → Pruebas → Pruebas internas**
4. Click en **Crear nueva versión**
5. Subir el AAB (`app-release.aab`)
6. En **Probadores**, crear lista y agregar el correo del evaluador
7. **Revisar y publicar**
8. Copiar el enlace de pruebas internas y pegarlo en el `README.md` principal

---

## Troubleshooting

**Error `jarsigner: certificate chain not found for: taskflow-key`**
→ El alias en `KEY_ALIAS` no coincide con el del keystore. Verifica con:
```bash
keytool -list -v -keystore keystore.jks
```

**Error `Keystore was tampered with, or password was incorrect`**
→ `STORE_PASSWORD` está mal. Reintenta.

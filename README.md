# Reportes S&S Group para Samsung S23

Proyecto Android con todos los archivos de la app dentro del APK. No abre el sitio publicado ni necesita internet para cargar reportes, fotos, clientes, obras o generar PDF. Borradores guardados en un archivo privado con escritura atómica. Exportación a Archivos y compartir por las aplicaciones de Android.

## Estado

Proyecto preparado; APK todavía no compilado ni probado en teléfono. El entorno donde se preparó no tiene Android SDK y no permite descargarlo. El flujo de GitHub realiza la compilación y entrega un APK de prueba firmado automáticamente. La compilación y las pruebas reales siguen pendientes.

## Generar el instalador desde GitHub

1. Crear un repositorio nuevo para este proyecto (preferentemente privado). Mantener intacto el repositorio Streamlit anterior.
2. Descomprimir este ZIP. Subir el contenido de `Reportes-SSGroup-Android` a la raíz del repositorio: deben aparecer `app`, `build.gradle`, `settings.gradle` y `.github/workflows/android-apk.yml`. En Mac, Cmd+Shift+. muestra la carpeta `.github`; si la interfaz de GitHub no permite subirla, crear el archivo desde «Add file / Create new file» con ese nombre y copiar su contenido.
3. Abrir la pestaña **Actions**. Elegir **Generar APK Samsung** y **Run workflow**. Confirmar la ejecución.
4. Esperar hasta que termine con una marca verde. En **Artifacts**, descargar **Reportes-SSGroup-Samsung-APK**.
5. Descomprimir ese archivo y pasar `Reportes-SSGroup-Samsung.apk` al Samsung S23.
6. Abrir el APK desde Mis archivos y permitir la instalación desde esa aplicación si Android lo pide.

Este APK es de prueba. Cada ejecución puede usar una firma de prueba distinta. Antes de futuras instalaciones, exportar un respaldo; puede ser necesario desinstalar la versión anterior y después importar el respaldo. Antes de distribuir versiones definitivas hay que configurar una clave de firma permanente.

## Prueba en el teléfono

- Abrir la aplicación por primera vez en modo avión.
- Cargar cliente y obra y comenzar a escribir un hallazgo; cerrar y reabrir para comprobar el borrador.
- Adjuntar varias fotos de Galería y otra con Cámara.
- Agregar un hallazgo y generar PDF, guardarlo desde el selector Android.
- Compartir el PDF cuando vuelva la conexión.
- Exportar una copia de seguridad y comprobar su importación.

Las obras y reportes anteriores no están en el ZIP de GitHub original. Las firmas deben cargarse en Instalación y respaldo. Los 44 clientes y las ocho recomendaciones provienen del código original. La app no sincroniza automáticamente entre equipos; usa respaldos exportables. El PDF conserva los contenidos del original; el diseño del PDF es una adaptación, no una réplica exacta.

## Compilar con Android Studio

Abrir esta carpeta como proyecto. Requiere JDK 17, Android SDK 35 y Gradle 8.9. Desde terminal con Gradle instalado: `gradle :app:assembleDebug`. Resultado: `app/build/outputs/apk/debug/app-debug.apk`.

No se requiere permiso de internet. La cámara se abre mediante la app de cámara del teléfono y permisos temporales para su archivo. Los archivos solo se comparten mediante permisos temporales de Android.

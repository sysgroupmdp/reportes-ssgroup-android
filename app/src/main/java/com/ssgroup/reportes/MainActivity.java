package com.ssgroup.reportes;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AtomicFile;
import android.util.Base64;
import android.webkit.*;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> chooser;
    private Uri cameraUri;
    private byte[] exportBytes;
    private final Object storageLock=new Object();
    private static final int PICK=11,CAMERA=12,EXPORT=13;
    private static final String ORIGIN="https://appassets.androidplatform.net";
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff11823b);getWindow().setNavigationBarColor(0xfff5f6f8);web=new WebView(this);setContentView(web);
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new NativeBridge(),"AndroidApp");
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){return true;}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){Uri u=r.getUrl();if(!ORIGIN.equals(u.getScheme()+"://"+u.getHost())||!u.getPath().startsWith("/assets/")||u.getPath().contains(".."))return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));
                try{String path=u.getPath().substring(8);String mime=path.endsWith(".js")?"application/javascript":path.endsWith(".css")?"text/css":path.endsWith(".png")?"image/png":path.endsWith(".jpg")?"image/jpeg":path.endsWith(".json")||path.endsWith(".webmanifest")?"application/json":"text/html";return new WebResourceResponse(mime,"UTF-8",getAssets().open(path));}catch(IOException e){return new WebResourceResponse("text/plain","UTF-8",404,"Not Found",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));}}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){if(chooser!=null)chooser.onReceiveValue(null);chooser=callback;
                try{if(params.isCaptureEnabled()){File f=exportFile("camera_"+System.currentTimeMillis()+".jpg",new byte[0]);cameraUri=fileUri(f);Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,cameraUri);i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,CAMERA);}else{Intent i=params.createIntent();i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,params.getMode()==FileChooserParams.MODE_OPEN_MULTIPLE);startActivityForResult(i,PICK);}return true;}catch(Exception e){chooser.onReceiveValue(null);chooser=null;notifyUser("No se pudo abrir la cámara o el selector de archivos.");return true;}}
            @Override public boolean onJsConfirm(WebView v,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("Aceptar",(d,w)->result.confirm()).setNegativeButton("Cancelar",(d,w)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
        });
        web.loadUrl(ORIGIN+"/assets/www/index.html");
    }
    private void notifyUser(String message){runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show());}
    private File exportFile(String name,byte[] bytes) throws IOException {String safe=name.replaceAll("[^\\p{L}\\p{N}._ -]","_");File dir=new File(getCacheDir(),"exports");dir.mkdirs();File file=new File(dir,safe);try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);}return file;}
    private Uri fileUri(File file){return new Uri.Builder().scheme("content").authority("com.ssgroup.reportes.files").appendPath(file.getName()).build();}
    private class NativeBridge {
        @JavascriptInterface public String loadState(){synchronized(storageLock){AtomicFile f=new AtomicFile(new File(getFilesDir(),"reports.json"));if(!f.getBaseFile().exists())return "";try{return new String(f.readFully(),StandardCharsets.UTF_8);}catch(IOException e){throw new IllegalStateException("No se pudo leer el guardado",e);}}}
        @JavascriptInterface public String loadCurrent(){synchronized(storageLock){AtomicFile f=new AtomicFile(new File(getFilesDir(),"current.json"));if(!f.getBaseFile().exists())return "";try{return new String(f.readFully(),StandardCharsets.UTF_8);}catch(IOException e){throw new IllegalStateException("No se pudo leer el borrador",e);}}}
        @JavascriptInterface public void saveCurrent(String json){synchronized(storageLock){AtomicFile f=new AtomicFile(new File(getFilesDir(),"current.json"));FileOutputStream out=null;try{out=f.startWrite();out.write(json.getBytes(StandardCharsets.UTF_8));f.finishWrite(out);}catch(Exception e){if(out!=null)f.failWrite(out);throw new IllegalStateException("No se pudo guardar el borrador",e);}}}
        @JavascriptInterface public void saveState(String json){synchronized(storageLock){AtomicFile f=new AtomicFile(new File(getFilesDir(),"reports.json"));FileOutputStream out=null;try{out=f.startWrite();out.write(json.getBytes(StandardCharsets.UTF_8));f.finishWrite(out);new AtomicFile(new File(getFilesDir(),"current.json")).delete();}catch(Exception e){if(out!=null)f.failWrite(out);throw new IllegalStateException("No se pudo guardar",e);}}}
        @JavascriptInterface public void export(String base64,String name,String mime,boolean share){byte[] bytes=Base64.decode(base64,Base64.DEFAULT);try{File f=exportFile(name,bytes);runOnUiThread(()->{try{if(share){Uri u=fileUri(f);Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,u);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Compartir reporte"));}else{if(exportBytes!=null){notifyUser("Terminá primero el guardado anterior.");return;}exportBytes=bytes;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,f.getName());startActivityForResult(i,EXPORT);}}catch(Exception e){exportBytes=null;notifyUser("No se pudo abrir el guardado o compartir.");}});}catch(IOException e){throw new IllegalStateException("No se pudo preparar el archivo",e);}}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==PICK||request==CAMERA){if(chooser!=null){Uri[] files=null;if(result==RESULT_OK){if(request==CAMERA)files=new Uri[]{cameraUri};else if(data!=null&&data.getClipData()!=null){files=new Uri[data.getClipData().getItemCount()];for(int i=0;i<files.length;i++)files[i]=data.getClipData().getItemAt(i).getUri();}else if(data!=null&&data.getData()!=null)files=new Uri[]{data.getData()};}chooser.onReceiveValue(files);chooser=null;cameraUri=null;}}
        if(request==EXPORT){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&exportBytes!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IOException();out.write(exportBytes);notifyUser("Archivo guardado.");}catch(IOException e){notifyUser("No se pudo guardar el archivo.");}}exportBytes=null;}}
    @Override public void onBackPressed(){new AlertDialog.Builder(this).setMessage("¿Cerrar Reportes? El borrador se guarda automáticamente.").setPositiveButton("Cerrar",(d,w)->finish()).setNegativeButton("Seguir trabajando",null).show();}
}

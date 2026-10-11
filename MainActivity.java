package com.emphotopro.mobile;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.*;
import android.util.Base64;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebResourceRequestCompat;
import java.io.OutputStream;

public class MainActivity extends Activity {
 private WebView web;
 private ValueCallback<Uri[]> fileCallback;
 private static final int FILE_REQUEST=42;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  web=new WebView(this); setContentView(web);
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);
  web.getSettings().setAllowContentAccess(true);
  web.addJavascriptInterface(new Object(){
   @JavascriptInterface public void saveJpeg(String base64,String name){
    try {
     byte[] bytes=Base64.decode(base64,Base64.DEFAULT);
     String safe=name.replaceAll("[^a-zA-Z0-9_-]","_");
     ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,safe+".jpg");
     values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
     values.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/EM PHOTO PRO");
     Uri dest=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
     if(dest==null) throw new Exception("No se pudo crear archivo");
     try(OutputStream out=getContentResolver().openOutputStream(dest)){if(out==null)throw new Exception("No se pudo guardar");out.write(bytes);}
     runOnUiThread(()->Toast.makeText(MainActivity.this,"Guardado en Imágenes / EM PHOTO PRO",Toast.LENGTH_LONG).show());
    }catch(Exception ex){runOnUiThread(()->Toast.makeText(MainActivity.this,"Error al guardar: "+ex.getMessage(),Toast.LENGTH_LONG).show());}
   }
  },"EMAndroid");
  final WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClientCompat(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return loader.shouldInterceptRequest(request.getUrl());}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){
    if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=callback;
    try {Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("image/*");startActivityForResult(intent,FILE_REQUEST);return true;}
    catch(Exception ex){fileCallback=null;callback.onReceiveValue(null);return true;}
   }
  });
  web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==FILE_REQUEST&&fileCallback!=null){Uri[] uris=(result==RESULT_OK&&data!=null&&data.getData()!=null)?new Uri[]{data.getData()}:null;fileCallback.onReceiveValue(uris);fileCallback=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}

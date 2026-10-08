package com.ssgroup.reportes;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
public class LocalFilesProvider extends ContentProvider {
    private File file(Uri uri) throws FileNotFoundException {
        String name=uri.getLastPathSegment();
        if(name==null || name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) throw new FileNotFoundException();
        return new File(new File(getContext().getCacheDir(),"exports"),name);
    }
    public boolean onCreate(){return true;}
    public String getType(Uri uri){String n=uri.getLastPathSegment();return n!=null&&n.endsWith(".pdf")?"application/pdf":n!=null&&n.endsWith(".json")?"application/json":"image/jpeg";}
    public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){try{File f=file(uri);String[] cols=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;MatrixCursor c=new MatrixCursor(cols);Object[] values=new Object[cols.length];for(int i=0;i<cols.length;i++)values[i]=OpenableColumns.DISPLAY_NAME.equals(cols[i])?f.getName():OpenableColumns.SIZE.equals(cols[i])?f.length():null;c.addRow(values);return c;}catch(Exception e){return null;}}
    public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException{return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.parseMode(mode));}
    public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
    public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}

package paulscode.android.mupen64plusae.game;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import paulscode.android.mupen64plusae.jni.CoreInterface;
import paulscode.android.mupen64plusae.persistent.GamePrefs;

public class GameAutoSaveManager
{
    public static final String V2 = "v2";

    private final GamePrefs mGamePrefs;
    private final String mAutoSavePath;
    private final int mMaxAutoSave;
    public static final String sFormatString = "yyyy-MM-dd-HH-mm-ss";
    private static final String sMatcherString = "^\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d-\\d\\d-\\d\\d\\..*sav$";
    private static final String sDefaultString = "yyyy-mm-dd-hh-mm-ss.sav";

    public GameAutoSaveManager(GamePrefs gamePrefs, int maxAutoSaves)
    {
        mGamePrefs = gamePrefs;
        mAutoSavePath = mGamePrefs.autoSaveDir + "/";
        mMaxAutoSave = maxAutoSaves;
    }

    public String getLatestAutoSave()
    {
        final List<String> result = new ArrayList<String>();
        final File savePath = new File(mAutoSavePath);

        //Only find files that end with .sav
        final FileFilter fileFilter = new FileFilter(){

            @Override
            public boolean accept(File pathname)
            {
                //It must match this format "yyyy-MM-dd-HH-mm-ss"
                final String fileName = pathname.getName();
                return fileName.matches(sMatcherString);
            }

        };

        final File[] fileList = savePath.listFiles(fileFilter);

        //Add all files found
        if(fileList != null)
        {
            for( final File file : fileList )
            {
                //Do not attempt to load states that are missing a corresponding ".complete" file
                //but only check for .complete if it's a V2 file
                File completeFile = new File(file.getPath() + "." + CoreInterface.COMPLETE_EXTENSION);
                if(!file.getPath().contains(V2) || completeFile.exists())
                {
                    result.add( file.getPath() );
                }
            }
        }

        //Sort by file name
        Collections.sort(result);

        String resultValue = "";
        if(result.size() > 0)
        {
            resultValue = result.get(result.size()-1);
        }
        else
        {
            //Fall back to this if we can't find a valid filename
            resultValue = mAutoSavePath + sDefaultString;
        }

        //Grab the last file
        return resultValue;
    }

    public void clearOldest()
    {
        final List<File> result = new ArrayList<File>();
        final File savePath = new File(mAutoSavePath);

        if(savePath.listFiles() != null && savePath.listFiles().length != 0)
        {
            //Only find files that match the matcher string
            final FileFilter fileFilter = new FileFilter(){

                @Override
                public boolean accept(File pathname)
                {
                    //It must match this format "yyyy-MM-dd-HH-mm-ss"
                    final String fileName = pathname.getName();
                    return fileName.matches(sMatcherString);
                }

            };

            //Add all files found
            for( final File file : savePath.listFiles(fileFilter) )
            {
                result.add( file );
            }

            //Sort by file name
            Collections.sort(result);

            while(result.size() > mMaxAutoSave)
            {
                Log.i("GameAutoSaveManager", "Deleting old autosave file: " + result.get(0).getName());
                File theResult = result.get(0);

                if(!theResult.isDirectory())
                {
                    theResult.delete();

                    //Also remove the corresponding ".complete" file
                    String completeFile = theResult.getAbsolutePath();
                    completeFile = completeFile + "." + CoreInterface.COMPLETE_EXTENSION;
                    new File(completeFile).delete();
                }
                result.remove(0);
            }
        }
    }

    public String getAutoSaveFileName()
    {
        final DateFormat dateFormat = new SimpleDateFormat(sFormatString, java.util.Locale.getDefault());
        final String dateAndTime = dateFormat.format(new Date()).toString();
        final String fileName = dateAndTime + "." + V2 + ".sav";

        return mAutoSavePath + fileName;
    }
}

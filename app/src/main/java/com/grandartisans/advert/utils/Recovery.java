package com.grandartisans.advert.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import android.content.Context;
import android.os.RecoverySystem;
import android.util.Log;


public class Recovery {
    private static final String TAG = "Recovery";
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");

    public static boolean verifyPackage(File packageFile){

        long fileLen = packageFile.length();

        try {
			RandomAccessFile raf = new RandomAccessFile(packageFile, "r");
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);

            if (footer[2] != (byte)0xff || footer[3] != (byte)0xff) {
            	return false;
            }

            int commentSize = (footer[4] & 0xff) | ((footer[5] & 0xff) << 8);
            int signatureStart = (footer[0] & 0xff) | ((footer[1] & 0xff) << 8);
            Log.v(TAG, String.format("comment size %d; signature start %d",
                                     commentSize, signatureStart));

            byte[] eocd = new byte[commentSize + 22];
            raf.seek(fileLen - (commentSize + 22));
            raf.readFully(eocd);

            // Check that we have found the start of the
            // end-of-central-directory record.
            if (eocd[0] != (byte)0x50 || eocd[1] != (byte)0x4b ||
                eocd[2] != (byte)0x05 || eocd[3] != (byte)0x06) {
                return false;
            }

            for (int i = 4; i < eocd.length-3; ++i) {
                if (eocd[i  ] == (byte)0x50 && eocd[i+1] == (byte)0x4b &&
                    eocd[i+2] == (byte)0x05 && eocd[i+3] == (byte)0x06) {
                    return false;
                }
            }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
    }

	public static void reboot(Context context,String path){
    	/*
		try {
			File file = new File(path+"/update.zip");
			RecoverySystem.installPackage(context, file);
		} catch (IOException e) {
				
		}
		*/
	}
    
	/**
     * Reboots the device in order to install the given update
     * package.
     * Requires the {@link android.Manifest.permission#REBOOT} permission.
     *
     * @param context      the Context to use
     * @param path  the update package to install.  Must be on
     * a partition mountable by recovery.  (The set of partitions
     * known to recovery may vary from device to device.  Generally,
     * /cache and /data are safe.)
     *
     * @throws IOException  if writing the recovery command file
     * fails, or if the reboot itself fails.
     */
	public static void bootRecovery(Context context,String path){
	
	}
	


    /**
     * Reboot into the recovery system with the supplied argument.
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    public static void saveCommand(Context context, String arg){
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        LOG_FILE.delete();

        FileWriter command;
		try {
			command = new FileWriter(COMMAND_FILE);
	        try {
	            command.write("--update_package=" +arg);
	            command.write("\n");
	        } finally {
	            command.close();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Having written the command file, go ahead and reboot
        //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //pm.reboot("recovery");
        //SystemProperties.set("ubootenv.var.upgrade_step", Integer.toString(1));

//        throw new IOException("Reboot failed (no permissions?)");
    }

    /**
     * Reboot into the recovery system with the supplied argument.
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    public static void saveCommand(Context context, String[] paths){
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        LOG_FILE.delete();

        FileWriter command;
		try {
			command = new FileWriter(COMMAND_FILE);
	        try {
	        	for(String path : paths){
	        		String arg = "--update_package=" + path; 
		            command.write(arg);
		            command.write("\n");
		            /*  many 2012-06-21 
		            if(isRecoverySpi(context,path)){
		            	arg = "--rb_recovery";
			            command.write(arg);
			            command.write("\n");
		            }
		            */
	        	}
	        	/*String arg = "--wipe_media"; 
	            command.write(arg);
	            command.write("\n");
        		arg = "--wipe_data"; 
	            command.write(arg);
	            command.write("\n");
	        	*/
	        } finally {
	            command.close();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Having written the command file, go ahead and reboot
        //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //pm.reboot("recovery");
        //SystemProperties.set("ubootenv.var.upgrade_step", Integer.toString(1));

//        throw new IOException("Reboot failed (no permissions?)");
    }
    

}
